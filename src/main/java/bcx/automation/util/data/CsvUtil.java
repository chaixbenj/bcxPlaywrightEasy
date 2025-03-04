package bcx.automation.util.data;

import bcx.automation.properties.GlobalProp;
import bcx.automation.report.Reporter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Utilitaire pour la manipulation de fichiers CSV.
 */
public class CsvUtil {

    private CsvUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Renvoie une HashMap à partir d'un fichier CSV, avec un séparateur ";".
     * La première ligne est considérée comme l'en-tête et la deuxième ligne comme les valeurs.
     *
     * @param report      Le rapporteur pour les logs.
     * @param dataSetName Le nom du fichier CSV dans le répertoire target/test-classes/.
     * @return Une HashMap contenant les valeurs de la deuxième ligne du fichier CSV.
     */
    public static LinkedHashMap<String, String> recordToHash(Reporter report, String dataSetName) {
        return recordToHash(report, dataSetName, 1, ";");
    }

    /**
     * Renvoie une HashMap à partir d'un fichier CSV, avec un séparateur ";".
     * La première ligne est considérée comme l'en-tête et la ligne spécifiée comme les valeurs.
     *
     * @param report      Le rapporteur pour les logs.
     * @param dataSetName Le nom du fichier CSV dans le répertoire target/test-classes/.
     * @param rowNumber   Le numéro de la ligne à lire.
     * @return Une HashMap contenant les valeurs de la ligne spécifiée du fichier CSV.
     */
    public static LinkedHashMap<String, String> recordToHash(Reporter report, String dataSetName, int rowNumber) {
        return recordToHash(report, dataSetName, rowNumber, ";");
    }

    /**
     * Renvoie une HashMap à partir d'un fichier CSV, avec un séparateur spécifié.
     * La première ligne est considérée comme l'en-tête et la ligne spécifiée comme les valeurs.
     *
     * @param report      Le rapporteur pour les logs.
     * @param dataSetName Le nom du fichier CSV dans le répertoire target/test-classes/.
     * @param rowNumber   Le numéro de la ligne à lire.
     * @param separator   Le séparateur utilisé dans le fichier CSV.
     * @return Une HashMap contenant les valeurs de la ligne spécifiée du fichier CSV.
     */
    public static LinkedHashMap<String, String> recordToHash(Reporter report, String dataSetName, int rowNumber, String separator) {
        LinkedHashMap<String, String> recordFields = null;
        LinkedHashMap<String, LinkedHashMap<String, String>> allRecordFields = allRecordsToHash(report, dataSetName, separator);
        if (allRecordFields != null) {
            recordFields = allRecordFields.get(String.valueOf(rowNumber));
        } else {
            report.log(Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT, "error loading testset " + GlobalProp.getTestFileFolder() + dataSetName + " [" + rowNumber + "] ");
        }
        return recordFields;
    }

    /**
     * Renvoie une HashMap à partir d'un fichier CSV, avec un séparateur ";".
     * La première ligne est considérée comme l'en-tête et la ligne dont la première valeur est égale à test-id est utilisée.
     *
     * @param report      Le rapporteur pour les logs.
     * @param dataSetName Le nom du fichier CSV dans le répertoire target/test-classes/.
     * @param testid      L'identifiant de test à rechercher.
     * @return Une HashMap contenant les valeurs de la ligne correspondant à l'identifiant de test.
     */
    public static LinkedHashMap<String, String> recordToHash(Reporter report, String dataSetName, String testid) {
        return recordToHash(report, dataSetName, testid, ";");
    }

    /**
     * Renvoie une HashMap à partir d'un fichier CSV, avec un séparateur spécifié.
     * La première ligne est considérée comme l'en-tête et la ligne dont la première valeur est égale à test-id est utilisée.
     *
     * @param report      Le rapporteur pour les logs.
     * @param dataSetName Le nom du fichier CSV dans le répertoire target/test-classes/.
     * @param testid      L'identifiant de test à rechercher.
     * @param separator   Le séparateur utilisé dans le fichier CSV.
     * @return Une HashMap contenant les valeurs de la ligne correspondant à l'identifiant de test.
     */
    public static LinkedHashMap<String, String> recordToHash(Reporter report, String dataSetName, String testid, String separator) {
        LinkedHashMap<String, String> recordFields = null;
        LinkedHashMap<String, LinkedHashMap<String, String>> allRecordFields = allRecordsToHash(report, dataSetName, separator);
        if (allRecordFields != null) {
            recordFields = allRecordFields.get(testid);
        } else {
            report.log(Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT, "error loading testset " + GlobalProp.getTestFileFolder() + dataSetName + " [" + testid + "] ");
        }
        return recordFields;
    }

    /**
     * Renvoie une HashMap de HashMap à partir d'un fichier CSV, avec un séparateur ";".
     * La première ligne est considérée comme l'en-tête. La clé est "test-id" si elle existe dans le CSV, sinon un compteur.
     *
     * @param report      Le rapporteur pour les logs.
     * @param dataSetName Le nom du fichier CSV dans le répertoire target/test-classes/.
     * @return Une HashMap de HashMap contenant toutes les lignes du fichier CSV.
     */
    public static LinkedHashMap<String, LinkedHashMap<String, String>> allRecordsToHash(Reporter report, String dataSetName) {
        return allRecordsToHash(report, dataSetName, ";");
    }

    /**
     * Renvoie une HashMap de HashMap à partir d'un fichier CSV, avec un séparateur spécifié.
     * La première ligne est considérée comme l'en-tête. La clé est "test-id" si elle existe dans le CSV, sinon un compteur.
     *
     * @param report      Le rapporteur pour les logs.
     * @param dataSetName Le nom du fichier CSV dans le répertoire target/test-classes/.
     * @param separator   Le séparateur utilisé dans le fichier CSV.
     * @return Une HashMap de HashMap contenant toutes les lignes du fichier CSV.
     */
    public static LinkedHashMap<String, LinkedHashMap<String, String>> allRecordsToHash(Reporter report, String dataSetName, String separator) {
        LinkedHashMap<String, LinkedHashMap<String, String>> allRecordFields = new LinkedHashMap<>();

        try (InputStream stream = new FileInputStream(GlobalProp.getTestFileFolder() + dataSetName);
             InputStreamReader reader = new InputStreamReader(stream);
             BufferedReader buff = new BufferedReader(reader)) {
            ArrayList<String> entetes = new ArrayList<>(Arrays.asList(buff.readLine().split(separator)));
            String ligne = buff.readLine();
            int i = 0;
            while (ligne != null) {
                i++;
                LinkedHashMap<String, String> recordFields = new LinkedHashMap<>();
                ArrayList<String> datas = new ArrayList<>(Arrays.asList(ligne.split(separator)));
                for (String entete : entetes) {
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
            report.log(Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT, "error loading testset " + GlobalProp.getTestFileFolder() + dataSetName, e);
        }
        return allRecordFields;
    }
}
