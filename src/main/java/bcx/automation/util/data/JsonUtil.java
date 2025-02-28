package bcx.automation.util.data;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.json.JSONArray;
import org.json.JSONObject;
import bcx.automation.report.Reporter;

import java.text.Collator;
import java.util.*;

/**
 * Utilitaire de gestion de JSON.
 *
 * @author bcx
 */
public class JsonUtil {

    public static final String NOSCREENSHOT_VALEUR_DE_L_ATTRIBUT = "[noscreenshot]valeur de l'attribut ";

    private JsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Renvoie une HashMap avec clé et valeur du JSON (fichier ou contenu).
     *
     * @param strJson JSON file or content.
     * @return HashMap contenant les clés et valeurs du JSON.
     */
    public static LinkedHashMap<String, LinkedHashMap<String, String>> jsonArrayFileToHash(String strJson) {
        LinkedHashMap<String, LinkedHashMap<String, String>> allKeyAndValues = new LinkedHashMap<>();
        try {
            strJson = DataUtil.getFileContent(strJson);
            try {
                org.apache.wink.json4j.JSONArray jsonarray = new org.apache.wink.json4j.JSONArray(strJson);
                for (int i = 0; i < jsonarray.length(); i++) {
                    LinkedHashMap<String, String> keyAndValues = new LinkedHashMap<>();
                    OrderedJSONObject json = new OrderedJSONObject(jsonarray.getString(i));
                    loadJson(keyAndValues, json, "", i);
                    allKeyAndValues.put(String.valueOf(i + 1), keyAndValues);
                }
            } catch (Exception e2) {
                return null;
            }
            return allKeyAndValues;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Renvoie une HashMap avec clé et valeur du JSON (fichier ou contenu).
     *
     * @param strJson JSON file or content.
     * @return HashMap contenant les clés et valeurs du JSON.
     */
    public static LinkedHashMap<String, String> jsonFileToHash(String strJson) {
        LinkedHashMap<String, String> keyAndValues = new LinkedHashMap<>();
        try {
            strJson = DataUtil.getFileContent(strJson);
            try {
                OrderedJSONObject json = new OrderedJSONObject(strJson);
                loadJson(keyAndValues, json, "", 0);
            } catch (Exception e) {
                try {
                    org.apache.wink.json4j.JSONArray jsonarray = new org.apache.wink.json4j.JSONArray(strJson);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        OrderedJSONObject json = new OrderedJSONObject(jsonarray.getString(i));
                        loadJson(keyAndValues, json, "", i);
                    }
                } catch (Exception e2) {
                    keyAndValues.put("not a json", "not a json");
                }
            }
            return keyAndValues;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convertit une chaîne de caractères en JSONArray.
     *
     * @param report Le rapporteur pour les logs.
     * @param jsString La chaîne de caractères à convertir.
     * @return Le JSONArray correspondant.
     */
    public static JSONArray toJSONArray(Reporter report, String jsString) {
        try {
            return new JSONArray(jsString);
        } catch (Exception e) {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "ERROR JsonUtil toJSONArray " + jsString, e);
            return null;
        }
    }

    /**
     * Convertit une chaîne de caractères en JSONObject.
     *
     * @param report Le rapporteur pour les logs.
     * @param jsString La chaîne de caractères à convertir.
     * @return Le JSONObject correspondant.
     */
    public static JSONObject toJSONObject(Reporter report, String jsString) {
        try {
            return new JSONObject(jsString);
        } catch (Exception e) {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "ERROR JsonUtil.toJSONObject " + jsString, e);
            return null;
        }
    }

    /**
     * Récupère la chaîne de caractères représentant un objet JSON dans un tableau JSON à partir d'un couple clé/valeur.
     *
     * @param report Le rapporteur pour les logs.
     * @param array Le tableau JSON.
     * @param key La clé à rechercher.
     * @param value La valeur à rechercher.
     * @return La chaîne de caractères représentant l'objet JSON.
     */
    public static String getJsonObjectAsStringInArray(Reporter report, String array, String key, String value) {
        return String.valueOf(getJsonObjectInArray(report, toJSONArray(report, array), key, value));
    }

    /**
     * Récupère la chaîne de caractères représentant un objet JSON dans un tableau JSON à partir d'une liste de couples clé/valeur.
     *
     * @param report Le rapporteur pour les logs.
     * @param array Le tableau JSON.
     * @param keyValue La liste de couples clé/valeur.
     * @return La chaîne de caractères représentant l'objet JSON.
     */
    public static String getJsonObjectAsStringInArray(Reporter report, String array, HashMap<String, String> keyValue) {
        return String.valueOf(getJsonObjectInArray(report, toJSONArray(report, array), keyValue));
    }

    /**
     * Récupère un objet JSON dans un tableau JSON à partir d'un couple clé/valeur.
     *
     * @param report Le rapporteur pour les logs.
     * @param array Le tableau JSON.
     * @param key La clé à rechercher.
     * @param value La valeur à rechercher.
     * @return L'objet JSON correspondant.
     */
    public static JSONObject getJsonObjectInArray(Reporter report, JSONArray array, String key, String value) {
        try {
            boolean found = false;
            JSONObject object = null;
            for (int n = 0; n < array.length(); n++) {
                object = array.getJSONObject(n);
                if (String.valueOf(object.get(key)).equals(String.valueOf(value))) {
                    found = true;
                    break;
                }
            }
            if (!found) throw new Exception();
            return object;
        } catch (Exception e) {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "ERROR JsonUtil getJsonObjectInArray " + array + " key:" + key + " value:" + value, e);
            return new JSONObject("{}");
        }
    }

    /**
     * Récupère un objet JSON dans un tableau JSON à partir d'une liste de couples clé/valeur.
     *
     * @param report Le rapporteur pour les logs.
     * @param array Le tableau JSON.
     * @param keyValue La liste de couples clé/valeur.
     * @return L'objet JSON correspondant.
     */
    public static JSONObject getJsonObjectInArray(Reporter report, JSONArray array, HashMap<String, String> keyValue) {
        StringBuilder sKeyValues = new StringBuilder();
        try {
            boolean found = true;
            JSONObject object = null;
            for (int n = 0; n < array.length(); n++) {
                object = array.getJSONObject(n);
                found = true;
                for (Map.Entry<String, String> stringStringEntry : keyValue.entrySet()) {
                    sKeyValues.append(stringStringEntry.getKey()).append("=").append(stringStringEntry.getValue()).append(" / ");
                    if (!String.valueOf(object.get(stringStringEntry.getKey())).equals(String.valueOf(stringStringEntry.getValue()))) {
                        found = false;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (!found) throw new Exception();
            return object;
        } catch (Exception e) {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "ERROR JsonUtil getJsonObjectInArray " + array + " values:" + sKeyValues, e);
            return new JSONObject("{}");
        }
    }

    /**
     * Renvoie le nombre d'éléments dans un tableau JSON.
     *
     * @param report Le rapporteur pour les logs.
     * @param array Le tableau JSON.
     * @return Le nombre d'éléments dans le tableau JSON.
     */
    public static int getJsonArraySize(Reporter report, String array) {
        return getJsonArraySize(Objects.requireNonNull(toJSONArray(report, array)));
    }

    /**
     * Renvoie le nombre d'éléments dans un tableau JSON.
     *
     * @param array Le tableau JSON.
     * @return Le nombre d'éléments dans le tableau JSON.
     */
    public static int getJsonArraySize(JSONArray array) {
        return array.length();
    }

    /**
     * Vérifie le nombre d'éléments dans un tableau JSON.
     *
     * @param report Le rapporteur pour les logs.
     * @param array Le tableau JSON.
     * @param size La taille attendue.
     */
    public static void assertJsonArraySize(Reporter report, JSONArray array, int size) {
        report.assertEquals("[noscreenshot]vérification de la taille du jsonArray ", size, getJsonArraySize(array));
    }

    /**
     * Vérifie le nombre d'éléments dans un tableau JSON.
     *
     * @param report Le rapporteur pour les logs.
     * @param array Le tableau JSON.
     * @param size La taille attendue.
     */
    public static void assertJsonArraySize(Reporter report, String array, int size) {
        report.assertEquals("[noscreenshot]vérification de la taille du jsonArray ", size, getJsonArraySize(report, array));
    }

    /**
     * Vérifie la valeur d'un attribut d'une chaîne de caractères représentant un objet JSON.
     *
     * @param report Le rapporteur pour les logs.
     * @param object La chaîne de caractères représentant l'objet JSON.
     * @param key La clé à vérifier.
     * @param value La valeur attendue.
     */
    public static void assertJsonKeyValue(Reporter report, String object, String key, String value) {
        assertJsonKeyValue(report, toJSONObject(report, object), key, value);
    }

    /**
     * Vérifie la valeur d'un attribut d'une chaîne de caractères représentant un objet JSON en ignorant les accents.
     *
     * @param report Le rapporteur pour les logs.
     * @param object La chaîne de caractères représentant l'objet JSON.
     * @param key La clé à vérifier.
     * @param value La valeur attendue.
     */
    public static void assertJsonKeyValueIgnoreAccent(Reporter report, String object, String key, String value) {
        assertJsonKeyValueIgnoreAccent(report, toJSONObject(report, object), key, value);
    }

    /**
     * Vérifie la valeur d'un attribut d'un objet JSON.
     *
     * @param report Le rapporteur pour les logs.
     * @param object L'objet JSON.
     * @param key La clé à vérifier.
     * @param value La valeur attendue.
     */
    public static void assertJsonKeyValue(Reporter report, JSONObject object, String key, String value) {
        String jsKeyValue = getJsonKeyValue(report, object, key);
        report.assertEquals(NOSCREENSHOT_VALEUR_DE_L_ATTRIBUT + key + " du json ", (value == null ? "null" : value), jsKeyValue);
    }

    /**
     * Vérifie la valeur d'un attribut d'un objet JSON en ignorant les accents.
     *
     * @param report Le rapporteur pour les logs.
     * @param object L'objet JSON.
     * @param key La clé à vérifier.
     * @param value La valeur attendue.
     */
    public static void assertJsonKeyValueIgnoreAccent(Reporter report, JSONObject object, String key, String value) {
        String jsKeyValue = getJsonKeyValue(report, object, key);
        final Collator instance = Collator.getInstance();
        instance.setStrength(Collator.NO_DECOMPOSITION);
        report.assertEquals(NOSCREENSHOT_VALEUR_DE_L_ATTRIBUT + key + " du json : " + jsKeyValue + " = " + value, 0, instance.compare(jsKeyValue, value));
    }

    /**
     * Vérifie la valeur d'un attribut d'une chaîne de caractères représentant un objet JSON.
     *
     * @param report Le rapporteur pour les logs.
     * @param object La chaîne de caractères représentant l'objet JSON.
     * @param key La clé à vérifier.
     * @param value La valeur attendue.
     */
    public static void assertJsonKeyValueAsDouble(Reporter report, String object, String key, String value) {
        assertJsonKeyValueAsDouble(report, toJSONObject(report, object), key, value);
    }

    /**
     * Vérifie la valeur d'un attribut d'un objet JSON.
     *
     * @param report Le rapporteur pour les logs.
     * @param object L'objet JSON.
     * @param key La clé à vérifier.
     * @param value La valeur attendue.
     */
    public static void assertJsonKeyValueAsDouble(Reporter report, JSONObject object, String key, String value) {
        String jsKeyValue = getJsonKeyValue(report, object, key);
        report.assertEquals(NOSCREENSHOT_VALEUR_DE_L_ATTRIBUT + key + " du json ", DoubleUtil.getDouble(value == null ? "0" : value), DoubleUtil.getDouble(jsKeyValue));
    }

    /**
     * Récupère la valeur d'un attribut d'un objet JSON.
     *
     * @param report Le rapporteur pour les logs.
     * @param object L'objet JSON.
     * @param key La clé à récupérer.
     * @return La valeur de la clé.
     */
    public static String getJsonKeyValue(Reporter report, JSONObject object, String key) {
        return String.valueOf((object != null && object.has(key) ? object.get(key) : null));
    }

    /**
     * Récupère la valeur d'un attribut d'un objet JSON.
     *
     * @param report Le rapporteur pour les logs.
     * @param object La chaîne de caractères représentant l'objet JSON.
     * @param key La clé à récupérer.
     * @return La valeur de la clé.
     */
    public static String getJsonKeyValue(Reporter report, String object, String key) {
        JSONObject json = toJSONObject(report, object);
        return String.valueOf((json != null && json.has(key) ? json.get(key) : null));
    }

    /**
     * Trie un JSONArray selon une clé.
     *
     * @param jsonArr Le JSONArray à trier.
     * @param key La clé de tri.
     * @param croissant Indique si le tri est croissant.
     * @return Le JSONArray trié.
     */
    public static JSONArray sortJSONArrayByKey(JSONArray jsonArr, String key, boolean croissant) {
        JSONArray sortedJsonArray = new JSONArray();
        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < jsonArr.length(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(key);
                    valB = (String) b.get(key);
                } catch (Exception e) {
                    // Do something
                }

                return croissant ? valA.compareTo(valB) : -valA.compareTo(valB);
            }
        });

        for (int i = 0; i < jsonArr.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }
        return sortedJsonArray;
    }

    private static void loadJson(LinkedHashMap<String, String> keyAndValues, OrderedJSONObject json, String preKeys, int arrayIndex) {
        Iterator order = json.getOrder();
        while (order.hasNext()) {
            String jsonKey = (String) order.next();
            boolean isJSONobject = isJSONObject(json, jsonKey);

            String key = preKeys + (preKeys.equals("") ? "" : ".") + jsonKey + (arrayIndex > 0 ? "[" + arrayIndex + "]" : "");
            String preJsonKey = (preKeys.equals("") ? preKeys : preKeys + ".") + jsonKey;
            String value;
            try {
                value = json.get(jsonKey).toString();
            } catch (Exception ex) {
                value = "null";
            }
            if (isJSONobject) {
                loadJsonObject(keyAndValues, json, jsonKey, preJsonKey, arrayIndex);
            } else {
                try {
                    loadJsonArray(keyAndValues, json, jsonKey, preJsonKey, arrayIndex);
                } catch (Exception e) {
                    keyAndValues.put(key, value);
                }
            }
        }
    }

    private static void loadJsonObject(LinkedHashMap<String, String> keyAndValues, OrderedJSONObject json, String jsonKey, String preKeys, int arrayIndex) {
        try {
            loadJson(keyAndValues, new OrderedJSONObject(json.getString(jsonKey)), preKeys, arrayIndex);
        } catch (Exception e) {
            // Ignorer l'exception
        }
    }

    private static boolean isJSONObject(OrderedJSONObject json, String jsonKey) {
        try {
            json.getJSONObject(jsonKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void loadJsonArray(LinkedHashMap<String, String> keyAndValues, OrderedJSONObject json, String jsonKey, String preJsonKey, int arrayIndex) throws JSONException {
        org.apache.wink.json4j.JSONArray jsonarray = json.getJSONArray(jsonKey);
        for (int i = 0; i < jsonarray.length(); i++) {
            try {
                loadJson(keyAndValues, new OrderedJSONObject(jsonarray.getString(i)), preJsonKey, arrayIndex);
            } catch (Exception e) {
                // Ignorer l'exception
            }
        }
    }
}
