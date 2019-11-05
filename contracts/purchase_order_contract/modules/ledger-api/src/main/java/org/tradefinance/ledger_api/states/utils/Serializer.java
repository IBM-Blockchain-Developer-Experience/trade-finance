package org.tradefinance.ledger_api.states.utils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.tradefinance.ledger_api.annotations.Private;
import org.tradefinance.ledger_api.collections.BooleanRulesHandler;
import org.tradefinance.ledger_api.states.Concept;
import org.tradefinance.ledger_api.states.State;

import org.json.JSONException;
import org.json.JSONObject;

public class Serializer {

    public static <T> String serialize(T obj, String collection, Boolean force) {
        return Serializer.jsonify(obj, collection, force).toString();
    }

    public static <T> JSONObject jsonify(T obj, String collection, Boolean force) {
        JSONObject json = new JSONObject();

        ArrayList<Field> fields = Serializer.getAllFields(obj.getClass());

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals("logger") || field.getName().startsWith("$")) {
                continue;
            }

            if (force || Serializer.shouldAddToJSON(collection, field)) {
                try {
                    Object value = field.get(obj);

                    if (value instanceof State) {
                        State stateValue = (State) value;
                        json.put(field.getName(), new JSONObject(stateValue.serialize(collection, force)));
                    } else if (value instanceof Concept) {
                        Concept conceptValue = (Concept) value;
                        json.put(field.getName(), new JSONObject(conceptValue.serialize()));
                    } else if (field.getType().getName().equals("java.util.Date") && value != null) {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        json.put(field.getName(), formatter.format((Date) value));
                    } else {
                        json.put(field.getName(), value);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }

    @SuppressWarnings("all")
    private static ArrayList<Field> getAllFields(Class clazz) {
        ArrayList<Field> fields = new ArrayList<Field>();

        do {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        } while ((clazz = clazz.getSuperclass()) != null);

        return fields;
    }

    private static boolean shouldAddToJSON(String collection, Field field) {
        if (collection == null) {
            return field.getAnnotation(Private.class) == null;
        }

        final Private annotation = field.getAnnotation(Private.class);

        if (annotation == null) {
            return false;
        }
        BooleanRulesHandler collectionHandler = new BooleanRulesHandler(annotation.collections(), new String[] {collection});

        return collectionHandler.evaluate();
    }
}
