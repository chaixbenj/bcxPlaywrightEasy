package bcx.playwright.util.bdd;


import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;

/**
 * classe permettant de stocker les résultats de requêtes renvoyés BDDUtil.executeSelectMultipleMultiRow() dans une Stack<HashMap<String, String>>
 * et d'accéder au résultat ligne/ligne, colonne/colonne
 * @author bcx
 *
 */
@Slf4j
public class QueryResultUtil {
    final Stack<LinkedHashMap<String, String>> sqlResult;

    /**
     * contstructeur prenant en entrée la Stack<HashMap<String, String>> issu du résultat de la requête exécutée par BDDUtil.executeSelectMultipleMultiRow()
     */
    public QueryResultUtil(Stack<LinkedHashMap<String, String>> sqlResult) {
        this.sqlResult = sqlResult;
    }

    /**
     * retourne tous les résultats de la requête dans une Stack<HashMap<String, String>>
     * @return sqlResult (Stack<LinkedHashMap<String, String>>)
     */
    public Stack<LinkedHashMap<String, String>> getStack() {
        return sqlResult;
    }

    /**
     * passer un nom de champ de la requête en paramètre pour récupérer un tableau de String contenant toutes les valeurs de la colonne
     * @param colName label de la colonne de table
     */
    public String[] getColumn(String colName) {
        String[] results = null;
        try {
            results = new String[sqlResult.size()];
            int i = 0;
            for(HashMap<String,String> hash: sqlResult) {
                results[i] = hash.get(colName.toUpperCase());
                i++;
            }
        } catch (Exception ex) {
            log.error("getColumn " + colName + "exception", ex);
        }
        return results;
    }

    /**
     * renvoi la hashMap correspond au résultat rowNum de la requête
     * @param rowNum numéro de la ligne
     * @return HashMap<String, String> ligne de résultat de la requête
     */
    public LinkedHashMap<String, String> getRow(int rowNum) {
        return sqlResult.get(rowNum);
    }

    /**
     * renvoi la valeur du champ d'une ligne du résultat de la requête
     * @param rowNum numéro de la ligne de résultat (0 à n-1)
     * @param colName nom du champ
     * @return valeur colName de la ligne rowNum
     */
    public String get(int rowNum, String colName) {
        try {
            return sqlResult.get(rowNum).get(colName.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * renvoi la valeur du champ de la première ligne du résultat de la requête
     * @param colName nom du champ
     * @return valeur colName de la ligne 0 (première ligne)
     */
    public String getString(String colName) {
        return getString(0, colName);
    }

    /**
     * renvoi la valeur du champ d'une ligne du résultat de la requête
     * @param rowNum numéro de la ligne de résultat (0 à n-1)
     * @param colName   nom du champ
     * @return valeur colName de la ligne rowNum
     */
    public String getString(int rowNum, String colName) {
        return (get(rowNum, colName)==null?"":get(rowNum, colName));
    }

    /**
     * renvoi la valeur du champ de la première ligne du résultat de la requête
     * @param colName nom du champ
     * @return valeur colName de la ligne 0 (première ligne)
     */
    public String get(String colName) {
        return get(0, colName);
    }

    /**
     * renvoi la premiere valeur de la première ligne du résultat de la requête
     * @return valeur de la premiere valeur de la première ligne
     */
    public String getFirstVal() {
        String val = null;
        if (this.size()>0) {
            LinkedHashMap<String, String> row = getRow(0);
            val = row.entrySet().iterator().next().getValue();
        }
        return val;
    }

    /**
     * renvoi la valeur du champ d'une ligne du résultat de la requête, "0" si null
     * @param rowNum numéro de la ligne de résultat (0 à n-1)
     * @param colName nom du champ
     * @return valeur colName de la ligne rowNum
     */
    public String get0IfNull(int rowNum, String colName) {
        try {
            String value = sqlResult.get(rowNum).get(colName.toUpperCase());
            return (value==null?"0":value);
        } catch (Exception e) {
            return "0";
        }
    }

    /**
     * renvoi le nombre de lignes renvoyées par la requête
     * @return nombre de lignes renvoyées par la requête
     */
    public int size() {
        return sqlResult.size();
    }
}
