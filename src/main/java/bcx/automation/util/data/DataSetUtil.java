package bcx.automation.util.data;

import com.google.gson.Gson;
import bcx.automation.report.Reporter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utilitaire pour la manipulation de jeux de données via la classe BasePage.
 *
 * @author bcx
 */
@Slf4j
public class DataSetUtil {
    private ArrayList<String> keys;
    private int currentKeyIndex;
    private LinkedHashMap<String, LinkedHashMap<String, String>> allKeyAndValues;
    private LinkedHashMap<String, String> keyAndValues;
    private Reporter report;

    /**
     * Constructeur par défaut.
     */
    public DataSetUtil() {
        keyAndValues = new LinkedHashMap<>();
    }

    /**
     * Constructeur d'un DataSetUtil à partir d'un fichier de données. Charge tout le fichier.
     * Le jeu de données peut être chargé via la méthode next() pour une boucle, ou via les méthodes load(String test-id)/load(String rowNum).
     *
     * @param report   Le rapporteur pour les logs.
     * @param dataFile Le nom du fichier de données.
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
     * Constructeur d'un DataSetUtil à partir d'un fichier de données en identifiant la ligne de données à partir de son champ "test-id".
     *
     * @param report   Le rapporteur pour les logs.
     * @param dataFile Le nom du fichier de données.
     * @param testid   L'identifiant de test.
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
     * Constructeur d'un DataSetUtil à partir de la ligne rowNum d'un fichier de données.
     *
     * @param report   Le rapporteur pour les logs.
     * @param dataFile Le nom du fichier de données.
     * @param rowNum   Le numéro de la ligne.
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
     * Constructeur d'un DataSetUtil à partir d'une LinkedHashMap<String, String>.
     *
     * @param report     Le rapporteur pour les logs.
     * @param keyAndValues La map contenant les paires clé/valeur.
     */
    public DataSetUtil(Reporter report, LinkedHashMap<String, String> keyAndValues) {
        this.report = report;
        this.keyAndValues = keyAndValues;
    }

    /**
     * Charge la ligne suivante d'un DataSetUtil complet (construit par DataSetUtil(String dataFile)).
     *
     * @return True si la ligne suivante a été chargée avec succès, sinon False.
     */
    public boolean next() {
        currentKeyIndex++;
        if (keys != null && keys.size() > currentKeyIndex && allKeyAndValues.size() > currentKeyIndex) {
            keyAndValues = allKeyAndValues.get(keys.get(currentKeyIndex));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Renvoie l'identifiant de test courant.
     *
     * @return L'identifiant de test courant.
     */
    public String getTestId() {
        return keys.get(currentKeyIndex);
    }

    /**
     * Ajoute des couples clé/valeur dans un DataSetUtil.
     *
     * @param key   La clé.
     * @param value La valeur.
     */
    public void add(String key, String value) {
        if (keyAndValues.containsKey(key)) {
            keyAndValues.replace(key, value);
        } else {
            keyAndValues.put(key, value);
        }
    }

    /**
     * Ajoute des couples clé/valeur dans un DataSetUtil à partir d'un jeu de données d'un fichier de données.
     *
     * @param dataFile Le nom du fichier de données.
     * @param testid   L'identifiant de test.
     */
    public void addFromDataFile(String dataFile, String testid) {
        LinkedHashMap<String, String> fileValues;
        if (dataFile.endsWith(".json")) {
            fileValues = JsonUtil.jsonFileToHash(dataFile);
        } else {
            fileValues = CsvUtil.recordToHash(report, dataFile + (dataFile.endsWith(".csv") ? "" : ".csv"), testid);
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
     * Efface les données d'un DataSetUtil.
     */
    public void clear() {
        keyAndValues.clear();
    }

    /**
     * Remplace la map du jeu de données par une autre.
     *
     * @param newMap La nouvelle map contenant les paires clé/valeur.
     */
    public void replaceMap(LinkedHashMap<String, String> newMap) {
        this.keyAndValues.clear();
        for (Map.Entry<String, String> stringStringEntry : newMap.entrySet()) {
            add(String.valueOf(stringStringEntry.getKey()), String.valueOf(stringStringEntry.getValue()));
        }
    }

    /**
     * Renvoie la LinkedHashMap de tous les couples clé/valeur du DataSetUtil pour être exploitée par la classe Formulaire.
     *
     * @return La map contenant les paires clé/valeur.
     */
    public LinkedHashMap<String, String> getKeyAndValues() {
        return keyAndValues;
    }

    /**
     * Renvoie la valeur d'une clé du DataSetUtil.
     *
     * @param key La clé.
     * @return La valeur associée à la clé.
     */
    public String get(String key) {
        return keyAndValues.get(key);
    }

    /**
     * Renvoie la valeur d'une clé du DataSetUtil pour le jeu de test identifié par test-id (si chargement complet construit par DataSetUtil(String dataFile)).
     *
     * @param testid L'identifiant de test.
     * @param key    La clé.
     * @return La valeur associée à la clé pour le jeu de test identifié par test-id.
     */
    public String get(String testid, String key) {
        load(testid);
        return get(key);
    }

    /**
     * Renvoie la valeur d'une clé du DataSetUtil pour le jeu de test identifié par le numéro de ligne (si chargement complet construit par DataSetUtil(String dataFile)).
     *
     * @param rowNum Le numéro de la ligne.
     * @param key    La clé.
     * @return La valeur associée à la clé pour le jeu de test identifié par le numéro de ligne.
     */
    public String get(int rowNum, String key) {
        load(rowNum);
        return get(key);
    }

    /**
     * Renvoie une chaîne de caractères avec les clés et les valeurs du jeu de données.
     *
     * @return Une chaîne de caractères représentant les paires clé/valeur.
     */
    public String toString() {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> stringStringEntry : keyAndValues.entrySet()) {
            content.append(stringStringEntry.getKey()).append(" = ").append(stringStringEntry.getValue()).append("; ");
        }
        return content.toString();
    }

    /**
     * Renvoie une chaîne de caractères au format JSON du jeu de données.
     *
     * @return Une chaîne de caractères au format JSON représentant le jeu de données.
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(keyAndValues);
    }

    /**
     * Charge le jeu de données identifié par test-id du DataSetUtil (si chargement complet).
     *
     * @param testid L'identifiant de test.
     * @return L'instance courante de DataSetUtil.
     */
    public DataSetUtil load(String testid) {
        if (allKeyAndValues != null && allKeyAndValues.containsKey(testid)) {
            keyAndValues = allKeyAndValues.get(testid);
        }
        return this;
    }

    /**
     * Charge le jeu de données identifié par le numéro de ligne du DataSetUtil (si chargement complet).
     *
     * @param rowNum Le numéro de la ligne.
     */
    public void load(int rowNum) {
        if (allKeyAndValues != null && allKeyAndValues.containsKey(String.valueOf(rowNum))) {
            keyAndValues = allKeyAndValues.get(String.valueOf(rowNum));
        }
    }

    private void loadDataSetFile(String dataFile, int rowNum) {
        log.info(Reporter.INFO_STATUS, "jdd : " + dataFile + ", n°ligne : " + rowNum);
        if (allKeyAndValues != null && allKeyAndValues.containsKey(String.valueOf(rowNum))) {
            keyAndValues = allKeyAndValues.get(String.valueOf(rowNum));
        } else {
            if (dataFile.endsWith(".json")) {
                JSONObject json = new JSONArray(dataFile).getJSONObject(rowNum - 1);
                keyAndValues = JsonUtil.jsonFileToHash(json.toString());
            } else {
                keyAndValues = CsvUtil.recordToHash(report, dataFile + (dataFile.endsWith(".csv") ? "" : ".csv"), rowNum);
            }
        }
    }

    private void loadDataSetFile(String dataFile, String testid) {
        log.info(Reporter.INFO_STATUS, "jdd : " + dataFile + ", testId : " + testid);
        if (allKeyAndValues != null && allKeyAndValues.containsKey(testid)) {
            keyAndValues = allKeyAndValues.get(testid);
        } else {
            if (dataFile.endsWith(".json")) {
                JSONObject json = JsonUtil.getJsonObjectInArray(report, new JSONArray(dataFile), "test-id", testid);
                keyAndValues = JsonUtil.jsonFileToHash(json.toString());
            } else {
                keyAndValues = CsvUtil.recordToHash(report, dataFile + (dataFile.endsWith(".csv") ? "" : ".csv"), testid);
            }
        }
    }

    private void loadDataSetFile(String dataFile) {
        log.info(Reporter.INFO_STATUS, "jdd : " + dataFile);
        if (dataFile.endsWith(".json")) {
            allKeyAndValues = JsonUtil.jsonArrayFileToHash(dataFile);
        } else {
            allKeyAndValues = CsvUtil.allRecordsToHash(report, dataFile + (dataFile.endsWith(".csv") ? "" : ".csv"));
        }

        for (Map.Entry<String, LinkedHashMap<String, String>> stringMapEntry : Objects.requireNonNull(allKeyAndValues).entrySet()) {
            keys.add(String.valueOf(stringMapEntry.getKey()));
        }
    }
}
