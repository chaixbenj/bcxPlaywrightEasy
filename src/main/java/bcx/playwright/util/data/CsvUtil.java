package bcx.playwright.util.data;

import bcx.playwright.properties.GlobalProp;
import bcx.playwright.report.Reporter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class CsvUtil {

    private CsvUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * renvoie une hashmap à partir d'un csv, spérateur ";" : 1ere ligne = entete, 2eme ligne =valeur
     * @param dataSetName nom du fichier dans target/test-classes/
     * @return
     */
    public static LinkedHashMap<String, String> recordToHash(Reporter report, String dataSetName) {
        return recordToHash(report, dataSetName, 1, ";");
    }

    /**
     * renvoie une hashmap à partir d'un csv, spérateur ";" : 1ere ligne = entete, rowNumber ligne =valeur
     * @param dataSetName
     * @param rowNumber
     * @return
     */
    public static LinkedHashMap<String, String> recordToHash(Reporter report, String dataSetName, int rowNumber) {
        return recordToHash(report, dataSetName, rowNumber, ";");
    }

    /**
     * renvoie une hashmap à partir d'un csv, spérateur separator : 1ere ligne = entete, rowNumber ligne =valeur
     * @param dataSetName
     * @param rowNumber
     * @param separator
     * @return
     */
    public static LinkedHashMap<String, String> recordToHash(Reporter report, String dataSetName, int rowNumber, String separator) {
        LinkedHashMap<String, String> recordFields = null;
        LinkedHashMap<String,LinkedHashMap<String, String>> allRecordFields = allRecordsToHash(report, dataSetName, separator);
        if (allRecordFields!=null) {
            recordFields = allRecordFields.get(String.valueOf(rowNumber));
        } else {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "error loading testset " + GlobalProp.getTestFileFolder() + dataSetName + " [" + rowNumber + "] ");
        }
        return recordFields;
    }

    /**
     * renvoie une hashmap à partir d'un csv, spérateur ";" : 1ere ligne = entete, ligne dont la première valeur vaut test-id = valeur
     * @param dataSetName
     * @param testid
     * @return
     */
    public static LinkedHashMap<String, String> recordToHash(Reporter report, String dataSetName, String testid) {
        return recordToHash(report, dataSetName, testid, ";");
    }

    /**
     * renvoie une hashmap à partir d'un csv, spérateur separator : 1ere ligne = entete, ligne dont la première valeur vaut test-id = valeur
     * @param dataSetName
     * @param testid
     * @param separator
     * @return
     */
    public static LinkedHashMap<String, String> recordToHash(Reporter report, String dataSetName, String testid, String separator) {
        LinkedHashMap<String, String> recordFields = null;
        LinkedHashMap<String,LinkedHashMap<String, String>> allRecordFields = allRecordsToHash(report, dataSetName, separator);
        if (allRecordFields!=null) {
            recordFields = allRecordFields.get(testid);
        } else {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "error loading testset " + GlobalProp.getTestFileFolder() + dataSetName + " [" + testid + "] ");
        }
        return recordFields;
    }

    /**
     * renvoie une hashmap de hasmap à partir d'un csv, spérateur separator. 1ere ligne = entete. Clé test-id si extiste dans le csv sinon un compteur
     * @param dataSetName
     * @return
     */
    public static LinkedHashMap<String,LinkedHashMap<String, String>> allRecordsToHash(Reporter report, String dataSetName) {
        return allRecordsToHash(report, dataSetName, ";");
    }

    /**
     * renvoie une hashmap de hasmap à partir d'un csv, spérateur separator. 1ere ligne = entete. Clé test-id si extiste dans le csv sinon un compteur
     * @param dataSetName
     * @param separator
     * @return
     */
    public static LinkedHashMap<String,LinkedHashMap<String, String>> allRecordsToHash(Reporter report, String dataSetName, String separator) {
        LinkedHashMap<String,LinkedHashMap<String, String>> allRecordFields = new LinkedHashMap<>();

        try (InputStream stream = new FileInputStream(GlobalProp.getTestFileFolder() + dataSetName); InputStreamReader reader = new InputStreamReader(stream); BufferedReader buff = new BufferedReader(reader)) {
            ArrayList<String> entetes = new ArrayList<>(Arrays.asList(buff.readLine().split(separator)));
            String ligne = buff.readLine();
            int i = 0;
            while (ligne != null) {
                i++;
                LinkedHashMap<String, String> recordFields = new LinkedHashMap<>();
                ArrayList<String> datas = new ArrayList<>(Arrays.asList(ligne.split(separator)));
                for (String entete : entetes
                ) {
                    String value = DataUtil.variabilise(datas.get(entetes.indexOf(entete)).trim());
                    recordFields.put(entete.trim(), value);
                }
                String key;
                if (entetes.get(0).equals("test-id")) {
                    key = recordFields.get("test-id");
                    recordFields.remove("test-id");
                } else {
                    key = String.valueOf(i);
                }
                allRecordFields.put(key, recordFields);
                ligne = buff.readLine();
            }

        } catch (Exception e) {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "error loading testset " + GlobalProp.getTestFileFolder() + dataSetName, e);
        }
        return allRecordFields;
    }
}
