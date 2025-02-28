package bcx.automation.util.bdd;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;

/**
 * Classe permettant de stocker les résultats de requêtes renvoyés par BDDUtil.executeSelectMultipleMultiRow()
 * dans une Stack<HashMap<String, String>> et d'accéder au résultat ligne/ligne, colonne/colonne.
 *
 * @author bcx
 */
@Slf4j
public class QueryResultUtil {
    final Stack<LinkedHashMap<String, String>> sqlResult;

    /**
     * Constructeur prenant en entrée la Stack<HashMap<String, String>> issue du résultat de la requête exécutée par BDDUtil.executeSelectMultipleMultiRow().
     *
     * @param sqlResult La pile contenant les résultats de la requête.
     */
    public QueryResultUtil(Stack<LinkedHashMap<String, String>> sqlResult) {
        this.sqlResult = sqlResult;
    }

    /**
     * Retourne tous les résultats de la requête dans une Stack<HashMap<String, String>>.
     *
     * @return La pile contenant les résultats de la requête.
     */
    public Stack<LinkedHashMap<String, String>> getStack() {
        return sqlResult;
    }

    /**
     * Retourne un tableau de String contenant toutes les valeurs d'une colonne spécifiée.
     *
     * @param colName Le nom de la colonne.
     * @return Un tableau de String contenant toutes les valeurs de la colonne spécifiée.
     */
    public String[] getColumn(String colName) {
        String[] results = null;
        try {
            results = new String[sqlResult.size()];
            int i = 0;
            for (HashMap<String, String> hash : sqlResult) {
                results[i] = hash.get(colName.toUpperCase());
                i++;
            }
        } catch (Exception ex) {
            log.error("getColumn " + colName + " exception", ex);
        }
        return results;
    }

    /**
     * Renvoie la HashMap correspondant au résultat de la ligne spécifiée.
     *
     * @param rowNum Le numéro de la ligne.
     * @return La ligne de résultat de la requête sous forme de HashMap<String, String>.
     */
    public LinkedHashMap<String, String> getRow(int rowNum) {
        return sqlResult.get(rowNum);
    }

    /**
     * Renvoie la valeur du champ d'une ligne spécifiée du résultat de la requête.
     *
     * @param rowNum Le numéro de la ligne.
     * @param colName Le nom du champ.
     * @return La valeur du champ spécifié pour la ligne spécifiée.
     */
    public String get(int rowNum, String colName) {
        try {
            return sqlResult.get(rowNum).get(colName.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Renvoie la valeur du champ de la première ligne du résultat de la requête.
     *
     * @param colName Le nom du champ.
     * @return La valeur du champ spécifié pour la première ligne.
     */
    public String getString(String colName) {
        return getString(0, colName);
    }

    /**
     * Renvoie la valeur du champ d'une ligne spécifiée du résultat de la requête.
     *
     * @param rowNum Le numéro de la ligne.
     * @param colName Le nom du champ.
     * @return La valeur du champ spécifié pour la ligne spécifiée.
     */
    public String getString(int rowNum, String colName) {
        return (get(rowNum, colName) == null ? "" : get(rowNum, colName));
    }

    /**
     * Renvoie la valeur du champ de la première ligne du résultat de la requête.
     *
     * @param colName Le nom du champ.
     * @return La valeur du champ spécifié pour la première ligne.
     */
    public String get(String colName) {
        return get(0, colName);
    }

    /**
     * Renvoie la première valeur de la première ligne du résultat de la requête.
     *
     * @return La première valeur de la première ligne.
     */
    public String getFirstVal() {
        String val = null;
        if (this.size() > 0) {
            LinkedHashMap<String, String> row = getRow(0);
            val = row.entrySet().iterator().next().getValue();
        }
        return val;
    }

    /**
     * Renvoie la valeur du champ d'une ligne spécifiée du résultat de la requête, ou "0" si la valeur est null.
     *
     * @param rowNum Le numéro de la ligne.
     * @param colName Le nom du champ.
     * @return La valeur du champ spécifié pour la ligne spécifiée, ou "0" si la valeur est null.
     */
    public String get0IfNull(int rowNum, String colName) {
        try {
            String value = sqlResult.get(rowNum).get(colName.toUpperCase());
            return (value == null ? "0" : value);
        } catch (Exception e) {
            return "0";
        }
    }

    /**
     * Renvoie le nombre de lignes renvoyées par la requête.
     *
     * @return Le nombre de lignes renvoyées par la requête.
     */
    public int size() {
        return sqlResult.size();
    }
}
