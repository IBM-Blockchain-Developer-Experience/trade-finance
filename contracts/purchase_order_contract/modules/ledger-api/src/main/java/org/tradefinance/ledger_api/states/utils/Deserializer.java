package org.tradefinance.ledger_api.states.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.tradefinance.ledger_api.annotations.DefaultDeserialize;
import org.tradefinance.ledger_api.annotations.Deserialize;
import org.tradefinance.ledger_api.annotations.OptionalParam;
import org.tradefinance.ledger_api.collections.BooleanRulesHandler;
import org.tradefinance.ledger_api.states.Concept;
import org.tradefinance.ledger_api.states.State;

import org.hyperledger.fabric.contract.execution.JSONTransactionSerializer;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Deserializer {

    public static <T> T deserialize(Class<T> clazz, String json, String[] collections) {
        if (collections == null) {
            collections = new String[0];
        }

        @SuppressWarnings("unchecked")
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();

        Constructor<T> matchingConstructor = null;

        for (Constructor<T> constructor : constructors) {
            if (collections.length > 0) {
                final Deserialize annotation = constructor.getAnnotation(Deserialize.class);

                if (annotation != null) {
                    BooleanRulesHandler collectionHandler = new BooleanRulesHandler(annotation.collections(),
                            collections);

                    if (collectionHandler.evaluate()) {
                        if (matchingConstructor == null
                                || constructor.getParameterCount() > matchingConstructor.getParameterCount()) {
                            matchingConstructor = constructor;
                        }
                    }
                }
            } else {
                final DefaultDeserialize annotation = constructor.getAnnotation(DefaultDeserialize.class);

                if (annotation != null) {
                    matchingConstructor = constructor;
                    break;
                }
            }
        }

        if (matchingConstructor == null) {
            throw new RuntimeException("No valid constructor found for collections returned");
        }

        JSONObject jsonObject = new JSONObject(json);

        Parameter[] parameters = matchingConstructor.getParameters();

        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            final String parameterName = parameters[i].getName();
            final Class<?> parameterType = parameters[i].getType();

            OptionalParam annotation = parameters[i].getAnnotation(OptionalParam.class);

            if (!jsonObject.has(parameterName) && annotation == null) {
                throw new JSONException("State missing required constructor argument " + parameterName);
            } else if (!jsonObject.has(parameterName) && annotation != null) {
                args[i] = null;
            } else {
                args[i] = Deserializer.resolveJSON(parameterType, jsonObject.get(parameterName), collections);
            }
        }

        return Deserializer.buildItem(args, matchingConstructor);
    }

    @SuppressWarnings("unchecked")
    private static <T> Object resolveJSON(Class<?> type, Object value, String[] collections) {
        // TODO does matthews code solve this
        if (State.class.isAssignableFrom(type)) {
            return State.deserialize((Class) type, value.toString(), collections);
        } else if (Concept.class.isAssignableFrom(type)) {
            return Concept.deserialize((Class) type, value.toString());
        } else if (type.getName().equals("java.util.Date")) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            try {
                return formatter.parse((String) value);
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
        } else if (Enum.class.isAssignableFrom(type)) {
            Method valueOf;
            try {
                valueOf = type.getMethod("valueOf", String.class);
                return valueOf.invoke(null, value);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (type.isArray() || Number.class.isAssignableFrom(type)) {
            final TypeRegistry tr = new TypeRegistryImpl(); // may need some setting up
            final JSONTransactionSerializer jts = new JSONTransactionSerializer(tr);

            final TypeSchema schema = TypeSchema.typeConvert(type);

            String str;

            if (type.isArray()) {
                final JSONArray jsonArray = (JSONArray) value;
                str = jsonArray.toString();
            } else {
                str = ((Number) value).toString();
            }

            return jts.fromBuffer(str.getBytes(), schema);
        }

        return value;
    }

    public static <T> T buildItem(Object[] args, Constructor<T> constructor) {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e.getMessage());
		}
    }
}
