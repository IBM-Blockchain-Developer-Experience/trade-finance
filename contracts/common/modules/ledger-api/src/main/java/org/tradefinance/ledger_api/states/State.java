package org.tradefinance.ledger_api.states;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.tradefinance.ledger_api.annotations.VerifyHash;
import org.tradefinance.ledger_api.states.utils.Deserializer;
import org.tradefinance.ledger_api.states.utils.Serializer;

import org.json.JSONObject;

public abstract class State {

    public static Boolean verifyHash(String id, Map<String, byte[]> transientData) {
        throw new RuntimeException("Not yet implemented");
    }

    public static String makeKey(String[] keyParts) {
        return String.join(":", keyParts);
    }

    public static String[] splitKey(String key) {
        return key.split(":");
    }

    public static <T extends State> Boolean verifyHash(Class<T> clazz, String hash, Object ...args) {
        // anyway to do this without taking clazz?
        @SuppressWarnings("unchecked")
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();

        for (Constructor<T> constructor : constructors) {
            final VerifyHash annotation = constructor.getAnnotation(VerifyHash.class);

            if (annotation != null) {
                Parameter[] parameters = constructor.getParameters();

                if (parameters.length != args.length) {
                    throw new RuntimeException("Invalid args supplied. Expected " + parameters.length + " got " + args.length);
                }

                T obj = Deserializer.buildItem(args, constructor);
                return obj.getHash().equals(hash);
            }
        }
        return false;
    }

    public <T extends State> T toPublicForm() {
        String json = this.serialize();
        return (T) State.deserialize(this.getClass(), json, new String[]{});
    }

    public static <T extends State> T deserialize(Class<T> clazz, String json, String[] collections) {
        return Deserializer.deserialize(clazz, json, collections);
    }

    public static State deserialize(String json) {
        throw new RuntimeException("Not yet implemented");
    };

    private String key;
    @SuppressWarnings("unused")
    private String stateClass;
    private String hash;

    public State(String[] keyParts) {
        this.key = State.makeKey(keyParts);
        this.stateClass = this.getClass().getName();
    }

    protected State(String[] keyParts, String hash) {
        this.key = State.makeKey(keyParts);
        this.stateClass = this.getClass().getName();
        this.hash = hash;
    }

    public String serialize() {
        return this.serialize(null);
    }

    public String serialize(String collection) {
        return this.serialize(collection, false);
    }

    public String serialize(String collection, Boolean force) {
        return Serializer.serialize(this, collection, force);
    }

    public String getKey() {
        return this.key;
    }

    public String[] getSplitKey() {
        return State.splitKey(this.key);
    }

    public String getHash() {
        return this.hash;
    }

    public void updateHash() {
        this.hash = this.generateHash();
    }

    private String generateHash() {
        JSONObject jsonObject = Serializer.jsonify(this, null, true);
        jsonObject.remove("hash");

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // SHOULD NEVER HAPPEN BUT CHEERS UP JAVA
            return "";
        }
        byte[] encodedHash = digest.digest(jsonObject.toString().getBytes());

        StringBuilder sb = new StringBuilder();

        for (byte b : encodedHash) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
