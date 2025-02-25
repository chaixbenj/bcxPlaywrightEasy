package bcx.playwright.util.data;

import bcx.playwright.test.TestContext;
import com.google.gson.Gson;
import bcx.playwright.report.Reporter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


/**
 * le dataSetUtil est utilisé pour jouer des jeux de données via la classe Formulaire
 * @author bcx
 *
 */
@Slf4j
public class DataSetUtil {
    private ArrayList<String> keys;
    private int currentKeyIndex;
    private LinkedHashMap<String,LinkedHashMap<String, String>> allKeyAndValues;
    private LinkedHashMap<String, String> keyAndValues;
    private Reporter report;

    public DataSetUtil() {
        keyAndValues = new LinkedHashMap<>();
    }

    /**
     * constructeur d'un dataSetUtil à partir d'un data file. Charge tout le fichier.
     * chargement du jeu via méthode next() pour boucle, ou via méthode load(String test-id)/load(String rowNum)
     * @param dataFile
     */
    public DataSetUtil(Reporter report, String dataFile) {
        this.report = report;
        allKeyAndValues = new LinkedHashMap<>();
        keyAndValues = new LinkedHashMap<>();
        keys = new ArrayList<>();
        currentKeyIndex = -1;
        loadDataSetFile(dataFile);
        keyAndValues = allKeyAndValues.get(keys.get(0));
    }

    /**
     * constructeur d'un dataSetUtil à partir d'un data file en identifiant la ligne de donnée à partir de son champ "test-id" = test-id.
     * @param dataFile
     * @param testid
     */
    public DataSetUtil(Reporter report, String dataFile, String testid) {
        this.report = report;
        allKeyAndValues = new LinkedHashMap<>();
        keyAndValues = new LinkedHashMap<>();
        keys = new ArrayList<>();
        currentKeyIndex = -1;
        loadDataSetFile(dataFile, testid);
    }

    /**
     * constructeur d'un dataSetUtil à partir de la ligne rowNum d'un data file
     * @param dataFile
     * @param rowNum
     */
    public DataSetUtil(Reporter report, String dataFile, int rowNum) {
        this.report = report;
        allKeyAndValues = new LinkedHashMap<>();
        keyAndValues = new LinkedHashMap<>();
        keys = new ArrayList<>();
        currentKeyIndex = -1;
        loadDataSetFile(dataFile, rowNum);
    }



    /**
     * contructeur d'un dataSetUtil à partir d'une LinkedHashMap<String, String>
     * @param keyAndValues
     */
    public DataSetUtil(Reporter report, LinkedHashMap<String, String> keyAndValues) {
        this.report = report;
        this.keyAndValues = keyAndValues;
    }

    /**
     * charge la ligne suivante d'un DataSetUtil complet (construit par DataSetUtil(String dataFile))
     * @return
     */
    public boolean next() {
        currentKeyIndex++;
        if (keys!=null && keys.size()>currentKeyIndex && allKeyAndValues.size()>currentKeyIndex) {
            keyAndValues = allKeyAndValues.get(keys.get(currentKeyIndex));
            return true;
        } else {
            return false;
        }
    }

    /**
     * renvoie le TestId courant
     * @return
     */
    public String getTestId() {
        return keys.get(currentKeyIndex);
    }
    /**
     * ajoute des couples key/value dans un dataSetUtil
     * @param key
     * @param value
     */
    public void add(String key, String value) {
        if (keyAndValues.containsKey(key)) {
            keyAndValues.replace(key, value);
        } else {
            keyAndValues.put(key, value);
        }
    }

    /**
     * ajoute des couples key/value dans un dataSetUtil à partir d'un jeu de donnée d'un data file
     * @param dataFile
     * @param testid
     */
    public void addFromDataFile(String dataFile, String testid) {
        LinkedHashMap<String, String> fileValues;
        if (dataFile.endsWith(".json")) {
            fileValues = JsonUtil.jsonFileToHash(dataFile);
        } else {
            fileValues = CsvUtil.recordToHash(report, dataFile  + (dataFile.endsWith(".csv")?"":".csv"), testid);
        }
        for (Map.Entry<String, String> stringStringEntry : Objects.requireNonNull(fileValues).entrySet()) {
            String key = String.valueOf(stringStringEntry.getKey());
            String value = String.valueOf(stringStringEntry.getValue());
            if (!value.equals("N/A")) {
                add(key, value);
            }
        }
    }



    /**
     * efface les données d'un dataSetUtil
     */
    public void clear() {
        keyAndValues.clear();
    }

    /**
     * remplace la map du jdd par une autre
     * @param newMap
     */
    public void replaceMap(LinkedHashMap<String, String> newMap) {
        this.keyAndValues.clear();
        for (Map.Entry<String, String> stringStringEntry : newMap.entrySet()) {
            add(String.valueOf(stringStringEntry.getKey()), String.valueOf(stringStringEntry.getValue()));
        }
    }

    /**
     * renvoi la LinkedHashMap de tous les key/value du dataSetUtil pour être exploité par la classe Formulaire
     * @return
     */
    public LinkedHashMap<String, String> getKeyAndValues() {
        return keyAndValues;
    }

    /**
     * renvoi la valeur d'une clé du DataSetUtil
     * @param key
     * @return
     */
    public String get(String key) {
        return keyAndValues.get(key);
    }

    /**
     * renvoi la valeur d'une clé du DataSetUtil pour le jeu test-id (si chargement complet construit par DataSetUtil(String dataFile))
     * @param testid
     * @param key
     * @return
     */
    public String get(String testid, String key) {
        load(testid);
        return get(key);
    }

    /**
     * renvoi la valeur d'une clé du DataSetUtil pour le jeu n° rownum (si chargement complet construit par DataSetUtil(String dataFile))
     * @param rowNum
     * @param key
     * @return
     */
    public String get(int rowNum, String key) {
        load(rowNum);
        return get(key);
    }

    /**
     * renvoi une string avec les clé et les valeurs du jdd
     */
    public String toString() {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> stringStringEntry : keyAndValues.entrySet()) {
            content.append(stringStringEntry.getKey()).append(" = ").append(stringStringEntry.getValue()).append("; ");
        }
        return content.toString();
    }

    /**
     * retourne une string format json du jeu de donnée
     * @return
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(keyAndValues);
    }

    /**
     * charge le jeu de données test-id du DataSetUtil (si chargement complet)
     * @param testid
     */
    public DataSetUtil load(String testid) {
        if (allKeyAndValues!=null && allKeyAndValues.containsKey(testid)) {
            keyAndValues = allKeyAndValues.get(testid);
        }
        return this;
    }

    /**
     * charge le jeu de données rowNum du DataSetUtil (si chargement complet)
     * @param rowNum
     */
    public void load(int rowNum) {
        if (allKeyAndValues!=null && allKeyAndValues.containsKey(String.valueOf(rowNum))) {
            keyAndValues = allKeyAndValues.get(String.valueOf(rowNum));
        }
    }

    private void loadDataSetFile(String dataFile, int rowNum) {
        log.info(Reporter.INFO_STATUS, "jdd : " + dataFile + ", n°ligne : " + rowNum);
        if (allKeyAndValues!=null && allKeyAndValues.containsKey(String.valueOf(rowNum))) {
            keyAndValues = allKeyAndValues.get(String.valueOf(rowNum));
        } else {
            if (dataFile.endsWith(".json")) {
                JSONObject json = new JSONArray(dataFile).getJSONObject(rowNum-1);
                keyAndValues = JsonUtil.jsonFileToHash(json.toString());
            } else {
                keyAndValues = CsvUtil.recordToHash(report, dataFile + (dataFile.endsWith(".csv")?"":".csv"), rowNum);
            }
        }
    }

    private void loadDataSetFile(String dataFile, String testid) {
        log.info(Reporter.INFO_STATUS, "jdd : " + dataFile + ", testId : " + testid);
        if (allKeyAndValues!=null && allKeyAndValues.containsKey(testid)) {
            keyAndValues = allKeyAndValues.get(testid);
        } else {
            if (dataFile.endsWith(".json")) {
                JSONObject json = JsonUtil.getJsonObjectInArray(report, new JSONArray(dataFile), "test-id", testid);
                keyAndValues = JsonUtil.jsonFileToHash(json.toString());
            } else {
                keyAndValues = CsvUtil.recordToHash(report, dataFile  + (dataFile.endsWith(".csv")?"":".csv"), testid);
            }
        }
    }

    private void loadDataSetFile(String dataFile) {
        log.info(Reporter.INFO_STATUS, "jdd : " + dataFile);
        if (dataFile.endsWith(".json")) {
            allKeyAndValues = JsonUtil.jsonArrayFileToHash(dataFile);
        } else {
            allKeyAndValues = CsvUtil.allRecordsToHash(report, dataFile  + (dataFile.endsWith(".csv")?"":".csv"));
        }

        for (Map.Entry<String, LinkedHashMap<String, String>> stringMapEntry : Objects.requireNonNull(allKeyAndValues).entrySet()) {
            keys.add(String.valueOf(stringMapEntry.getKey()));
        }
    }

}