package org.tradefinance.ledger_api.handling;

import java.util.Map;

import org.json.JSONObject;

public class QueryResponse {

    private String[] usedCollections;
    private Map<String, JSONObject> queryResult;

    public QueryResponse(String[] usedCollections, Map<String, JSONObject> queryResult) {
        this.usedCollections = usedCollections;
        this.queryResult = queryResult;
    }

    public String[] getUsedCollections() {
        return this.usedCollections;
    }

    public Map<String, JSONObject> getQueryResult() {
        return this.queryResult;
    }
}
