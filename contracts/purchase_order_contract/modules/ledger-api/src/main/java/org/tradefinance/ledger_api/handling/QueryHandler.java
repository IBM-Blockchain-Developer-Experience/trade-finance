package org.tradefinance.ledger_api.handling;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.tradefinance.ledger_api.annotations.Private;
import org.tradefinance.ledger_api.collections.BooleanRulesHandler;
import org.tradefinance.ledger_api.states.State;

import java.util.logging.Logger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONArray;
import org.json.JSONObject;
public class QueryHandler<T extends State> {
    protected final Logger logger = Logger.getLogger(QueryHandler.class.getName());
    private Map<String, Object> collectionQueries;
    private String[] collections;
    private Context ctx;

    public QueryHandler(JSONObject query, String listName, String[] collections, Context ctx, Class<? extends T> supportedClass) {
        this.collections = collections;
        this.ctx = ctx;
        this.collectionQueries = this.parseQuery(query, listName, collections, supportedClass);
    }

    public QueryResponse execute() {
        ArrayList<Map<String, JSONObject>> queryResults = new ArrayList<Map<String, JSONObject>>();

        ChaincodeStub stub = this.ctx.getStub();

        ArrayList<String> usedCollections = new ArrayList<String>();

        final String worldStateQueryString = collectionQueries.get("worldState").toString();
        final QueryResultsIterator<KeyValue> worldStateValues = this.ctx.getStub().getQueryResult(worldStateQueryString);
        queryResults.add(this.iterateIntoMap(worldStateValues));

        Set<String> foundIds = queryResults.get(0).keySet();

        for (String collection : this.collections) {
            JSONObject collectionJSON = (JSONObject) collectionQueries.get(collection);

            // todo update keys and remove those that are already in the map and don't exist here
            JSONArray idLimiter = new JSONArray(foundIds);

            JSONObject existingId = collectionJSON.getJSONObject("selector").getJSONObject("_id");
            existingId.put("$in", idLimiter);

            collectionQueries.put(collection, collectionJSON);

            final String queryString = collectionJSON.toString();

            QueryResultsIterator<KeyValue> queryResponse = null;

            try {
                queryResponse = stub.getPrivateDataQueryResult(collection, queryString);
            } catch (Exception e) {
                continue;
            }

            Map<String, JSONObject> queryResult = this.iterateIntoMap(queryResponse);
            if(queryResult.size() == 0) {
                continue;
            } else {
                usedCollections.add(collection);
            }

            foundIds = queryResult.keySet();
            queryResults.add(queryResult);
        }

        if (collectionQueries.containsKey("privateCollectionsRule")) {
            BooleanRulesHandler collectionHandler = new BooleanRulesHandler((String) collectionQueries.get("privateCollectionsRule"), usedCollections.toArray(new String[]{}));
            if (!collectionHandler.evaluate()) {
                return new QueryResponse(new String[] {}, new HashMap<String, JSONObject>());
            }
        }

        Set<String> matchingIds = queryResults.get(0).keySet();

        if (queryResults.size() > 1) {
            for (int i = 1; i < queryResults.size(); i++) {
                Set<String> queryIds = queryResults.get(i).keySet();

                matchingIds.retainAll(queryIds);
            }
        }

        Map<String, JSONObject> finalResult = new HashMap<String, JSONObject>();

        for (String id : matchingIds) {
            for (Map<String, JSONObject> queryResult : queryResults) {
                JSONObject json = queryResult.get(id);

                if (finalResult.containsKey(id)) {
                    final JSONObject existing = finalResult.get(id);

                    for (String jsonKey : JSONObject.getNames(existing)) {
                        json.put(jsonKey, existing.get(jsonKey));
                    }
                }

                finalResult.put(id, json);
            }
        }

        return new QueryResponse(usedCollections.toArray(new String[usedCollections.size()]), finalResult);
    }

    private Map<String, JSONObject> iterateIntoMap(QueryResultsIterator<KeyValue> values) {
        Map<String, JSONObject> resultMap = new HashMap<String, JSONObject>();

        for (KeyValue value : values) {

            final String data = value.getStringValue();

            JSONObject json = new JSONObject(data);

            resultMap.put(value.getKey(), json);
        }

        return resultMap;
    }

    private Map<String, Object> parseQuery(JSONObject query, String listName, String[] collections, Class<? extends T> clazz) {
        final JSONObject baseQuery = new JSONObject("{\"selector\": {}}");
        baseQuery.getJSONObject("selector").put("_id", new JSONObject());
        baseQuery.getJSONObject("selector").getJSONObject("_id").put("$regex", ".*" + listName + ".*");

        Map<String, Object> collectionQueries = new HashMap<String, Object>();

        collectionQueries.put("worldState", new JSONObject(baseQuery.toString()));

        for (String collection: collections) {
            collectionQueries.put(collection, new JSONObject(baseQuery.toString()));
        }

        ArrayList<String> collectionRules = new ArrayList<String>();

        if (query.has("selector")) {
            JSONObject selector = query.getJSONObject("selector");

            for (String property : selector.keySet()) {
                try {
                    Field field = this.getDeclaredProperty(clazz, property);

                    final Private annotation = field.getAnnotation(Private.class);

                    if (annotation != null) {
                        BooleanRulesHandler collectionHandler = new BooleanRulesHandler(annotation.collections());
                        String[] entries = collectionHandler.getEntries();

                        String selectorRule = "AnyOf(";

                        if (entries[0].equals("*")) {
                            entries = collections;
                        }

                        for (String collection : entries) {
                            if (!collectionQueries.containsKey(collection)) {
                                // TODO: If no collections from annotation are listed in supplied collections, query cannot succeed. Return blank
                                continue;
                            }

                            JSONObject collectionQuery = (JSONObject) collectionQueries.get(collection);
                            JSONObject collectionSelector = collectionQuery.getJSONObject("selector");
                            collectionSelector.put(property, selector.get(property));
                            selectorRule += "'" + collection + "', ";
                        }

                        selectorRule = selectorRule.substring(0, selectorRule.length() - 2) + ")";

                        collectionRules.add(selectorRule);
                    } else {
                        JSONObject worldStateSelector = ((JSONObject) collectionQueries.get("worldState")).getJSONObject("selector");
                        worldStateSelector.put(property, selector.get(property));
                    }

                } catch (NoSuchFieldException | SecurityException e) {
                    throw new RuntimeException("Property " + property + " does not exist for state type " + clazz.getName());
                }
            }
        }

        String queryPrivateRule = "AllOf(";

        for (String collectionRule : collectionRules) {
            queryPrivateRule += collectionRule + ", ";
        }

        queryPrivateRule = queryPrivateRule.substring(0, queryPrivateRule.length() - 2) + ")";

        if (collectionRules.size() > 0) {
            collectionQueries.put("privateCollectionsRule", queryPrivateRule);
        }

        return collectionQueries;
    }

    private Field getDeclaredProperty(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException exception) {
            // Ignore
            Class<?> superClazz = clazz.getSuperclass();
            if (superClazz != null) {
                return this.getDeclaredProperty(superClazz, name);
            } else {
                throw exception;
            }
        }
    }
}
