package bcx.playwright.util.data;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.json.JSONArray;
import org.json.JSONObject;
import bcx.playwright.report.Reporter;

import java.text.Collator;
import java.util.*;

public class JsonUtil {


    public static final String NOSCREENSHOT_VALEUR_DE_L_ATTRIBUT = "[noscreenshot]valeur de l'attribut ";

    private JsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * renvoi une hasmap avec clé et valeur du json (fichier ou contenu)
     * @param strJson json file or content
     * @return valeur de la clé
     */
    public static LinkedHashMap<String,LinkedHashMap<String, String>> jsonArrayFileToHash(String strJson) {
        LinkedHashMap<String,LinkedHashMap<String, String>> allKeyAndValues = new LinkedHashMap<>();
        try {
            strJson = DataUtil.getFileContent(strJson);
            try {
                org.apache.wink.json4j.JSONArray jsonarray = new org.apache.wink.json4j.JSONArray(strJson);
                for (int i = 0; i < jsonarray.length(); i++) {
                    LinkedHashMap<String, String> keyAndValues = new LinkedHashMap<>();
                    OrderedJSONObject json = new OrderedJSONObject(jsonarray.getString(i));
                    loadJson(keyAndValues, json, "", i);
                    allKeyAndValues.put(String.valueOf(i+1), keyAndValues);
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
     * renvoi une hasmap avec clé et valeur du json (fichier ou contenu)
     * @param strJson json file or content
     * @return valeur de la clé
     */
    public static LinkedHashMap<String, String> jsonFileToHash(String strJson) {
        LinkedHashMap<String, String> keyAndValues = new LinkedHashMap<>();
        try {
            strJson = DataUtil.getFileContent(strJson);
            try {
                OrderedJSONObject json = new OrderedJSONObject(strJson);
                loadJson(keyAndValues, json, "",  0);
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
     * convert string to json array
     * @param jsString
     * @return
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
     * convert string to json object
     * @param jsString
     * @return
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
     * recupère la string représentant un objet json dans un tableau de json à partir d'un couple clé/valeur
     * @param array
     * @param key
     * @param value
     * @return
     */
    public static String getJsonObjectAsStringInArray(Reporter report, String array, String key, String value) {
        return String.valueOf(getJsonObjectInArray(report, toJSONArray(report, array), key, value));
    }
    /**
     * recupère la string représentant un objet json dans un tableau de json à partir d'une liste de couples clé/valeur
     * @param array
     * @param keyValue
     * @return
     */
    public static String getJsonObjectAsStringInArray(Reporter report, String array, HashMap<String, String> keyValue) {
        return String.valueOf(getJsonObjectInArray(report, toJSONArray(report, array), keyValue));
    }

    /**
     * recupère un objet json dans un tableau de json à partir d'un couple clé/valeur
     * @param array
     * @param key
     * @param value
     * @return
     */
    public static JSONObject getJsonObjectInArray(Reporter report, JSONArray array, String key, String value) {
        try {
            boolean found = false;
            JSONObject object=null;
            for(int n = 0; n < array.length(); n++)	{
                object = array.getJSONObject(n);
                if (String.valueOf(object.get(key)).equals(String.valueOf(value))) {
                    found = true;
                    break;
                }
            }
            if (!found) throw new Exception();
            return object;
        } catch (Exception e) {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "ERROR JsonUtil getJsonObjetInArray " + array + " key:" + key + " value:" + value, e);
            return new JSONObject("{}");
        }
    }

    /**
     * recupère un objet json dans un tableau de json à partir d'un tableau couple clé/valeur
     * @param array
     * @param keyValue
     * @return
     */
    public static JSONObject getJsonObjectInArray(Reporter report, JSONArray array, HashMap<String, String> keyValue) {
        StringBuilder sKeyValues= new StringBuilder();
        try {
            boolean found = true;
            JSONObject object=null;
            for(int n = 0; n < array.length(); n++)	{
                object = array.getJSONObject(n);
                found = true;
                for (Map.Entry<String, String> stringStringEntry : keyValue.entrySet()) {
                    sKeyValues.append(stringStringEntry.getKey()).append("=").append(stringStringEntry.getValue()).append(" / ");
                    if (!String.valueOf(object.get(stringStringEntry.getKey())).equals(String.valueOf(stringStringEntry.getValue()))) {
                        found = false;
                    }
                }
                if (found)  {
                    break;
                }
            }
            if (!found) throw new Exception();
            return object;
        } catch (Exception e) {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "ERROR JsonUtil getJsonObjetInArray " + array + " values:" + sKeyValues, e);
            return new JSONObject("{}");
        }
    }

    /**
     * renvoi le nombre d'élément dans un array json
     * @param array
     * @return
     */
    public static int getJsonArraySize(Reporter report, String array) {
        return getJsonArraySize(Objects.requireNonNull(toJSONArray(report, array)));
    }

    /**
     * renvoi le nombre d'élément dans un array json
     * @param array
     * @return
     */
    public static int getJsonArraySize(JSONArray array) {
        return array.length();
    }

    /**
     * vérifie le nombre d'élément dans un array json
     * @param array
     * @return
     */
    public static void assertJsonArraySize(Reporter report, JSONArray array, int size) {
        report.assertEquals("[noscreenshot]vérificaton de la taille du jsonArray ", size, getJsonArraySize(array));
    }

    /**
     * vérifie le nombre d'élément dans un array json
     * @param array
     * @return
     */
    public static void assertJsonArraySize(Reporter report, String array, int size) {
        report.assertEquals("[noscreenshot]vérificaton de la taille du jsonArray ", size, getJsonArraySize(report, array));
    }

    /**
     * vérification de la valeur d'un attribut d'une string représentant un objet json
     * @param object
     * @param key
     * @param value
     */
    public static void assertJsonKeyValue(Reporter report, String object, String key, String value) {
        assertJsonKeyValue(report, toJSONObject(report, object), key, value);
    }

    /**
     * vérification de la valeur d'un attribut d'une string représentant un objet json en ignorant les accents
     * @param object
     * @param key
     * @param value
     */
    public static void assertJsonKeyValueIgnoreAccent(Reporter report, String object, String key, String value) {
        assertJsonKeyValueIgnoreAccent(report, toJSONObject(report, object), key, value);
    }

    /**
     * vérification de la valeur d'un attribut d'un objet json
     * @param object
     * @param key
     * @param value
     */
    public static void assertJsonKeyValue(Reporter report, JSONObject object, String key, String value) {
        String jsKeyValue = getJsonKeyValue(report, object, key);
        report.assertEquals(NOSCREENSHOT_VALEUR_DE_L_ATTRIBUT + key + " du json ", (value==null?"null":value), jsKeyValue);
    }

    /**
     * vérification de la valeur d'un attribut d'un objet json en ignorant les accents
     * @param object
     * @param key
     * @param value
     */
    public static void assertJsonKeyValueIgnoreAccent(Reporter report, JSONObject object, String key, String value) {
        String jsKeyValue = getJsonKeyValue(report, object, key);
        final Collator instance = Collator.getInstance();
        instance.setStrength(Collator.NO_DECOMPOSITION);
        report.assertEquals(NOSCREENSHOT_VALEUR_DE_L_ATTRIBUT + key + " du json : " + jsKeyValue + " = " + value, 0, instance.compare(jsKeyValue, value));
    }

    /**
     * vérification de la valeur d'un attribut d'une string représentant un objet json
     * @param object
     * @param key
     * @param value
     */
    public static void assertJsonKeyValueAsDouble(Reporter report, String object, String key, String value) {
        assertJsonKeyValueAsDouble(report, toJSONObject(report, object), key, value);
    }

    /**
     * vérification de la valeur d'un attribut d'un objet json
     * @param object
     * @param key
     * @param value
     */
    public static void assertJsonKeyValueAsDouble(Reporter report, JSONObject object, String key, String value) {
        String jsKeyValue = getJsonKeyValue(report, object, key);
        report.assertEquals(NOSCREENSHOT_VALEUR_DE_L_ATTRIBUT + key + " du json ", DoubleUtil.getDouble(value==null?"0":value), DoubleUtil.getDouble(jsKeyValue));
    }

    /**
     * récupère la valeur d'un attribut d'un objet json
     * @param object
     * @param key
     * @return
     */
    public static String getJsonKeyValue(Reporter report, JSONObject object, String key) {
        return String.valueOf((object!=null && object.has(key)?object.get(key):null));
    }

    /**
     * récupère la valeur d'un attribut d'un objet json
     * @param object
     * @param key
     * @return
     */
    public static String getJsonKeyValue(Reporter report, String object, String key) {
        JSONObject json = toJSONObject(report, object);
        return String.valueOf((json!=null && json.has(key)?json.get(key):null));
    }

    /**
     * tri JSONArray selon une clé
     * @param jsonArr
     * @param key
     * @param croissant
     * @return
     */
    public static JSONArray sortJSONArrayByKey(JSONArray jsonArr, String key, boolean croissant) {
        JSONArray sortedJsonArray = new JSONArray();
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArr.length(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }
        Collections.sort( jsonValues, new Comparator<JSONObject>() {
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(key);
                    valB = (String) b.get(key);
                }
                catch (Exception e) {
                    //do something
                }

                return croissant?valA.compareTo(valB):-valA.compareTo(valB);
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

            String key = preKeys + (preKeys.equals("")?"":".") + jsonKey + (arrayIndex>0?"[" + arrayIndex + "]":"");
            String preJsonKey = (preKeys.equals("") ? preKeys : preKeys + ".") + jsonKey;
            String value;
            try {
                value = json.get(jsonKey).toString();
            } catch (Exception ex) {
                value = "null";
            }
            if (isJSONobject) {
                loadJsonObject(keyAndValues, json, jsonKey, preJsonKey, arrayIndex);
            }
            else {
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
            //
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
                //
            }
        }
    }
}