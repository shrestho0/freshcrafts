package fresh.crafts.redwiz.utils;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CraftUtils {

    // singleton
    private CraftUtils() {
    }

    public static void throwIfRequiredValuesAreNull(HashMap<String, Object> notNullables) throws Exception {
        for (String key : notNullables.keySet()) {
            if (notNullables.get(key) == null) {
                throw new Exception("Error: " + key + " must not be null");
            }
        }
    }

    public static void throwIfFalseOrNull(Boolean condition, String message) throws Exception {
        if (condition == null || !condition) {
            throw new Exception(message);
        }
    }

    public static void throwIfNull(Object obj, String message) throws Exception {
        if (obj == null) {
            throw new Exception(message);
        }
    }

    public static void jsonLikePrint(Object obj) {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().setPrettyPrinting()
                .create();
        System.err.println("\n" + gson.toJson(obj) + "\n");

    }

    public static String toJson(Object obj) {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
        return gson.toJson(obj);
    }

}
