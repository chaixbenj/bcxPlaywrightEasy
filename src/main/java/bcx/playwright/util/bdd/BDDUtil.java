package bcx.playwright.util.bdd;

import bcx.playwright.properties.EnvProp;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import bcx.playwright.report.Reporter;
import bcx.playwright.properties.GlobalProp;
import bcx.playwright.util.TimeWait;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Utilitaire de base de données
 * Permet de se connecter, se déconnecter, d'exécuter des select et d'autres types de requêtes
 * @author vca
 *
 */
@Slf4j
public class BDDUtil {
    private static final HashMap<String, Connection> connexions = new HashMap<>();
    private static final Random rand = new Random();
    private static final String NOPARAM = "noparam";
    public static final String GROUP_BY = "group by";
    public static final String ORDER_BY = "order by";
    public static final String ON = " on ";

    private BDDUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Connexion à la base de données passée en param
     * dans le fichier GlobalVariable.properties, il faut référencer autant de couple de properties urlBddSCHEMA et schemaSCHEMA qu'il y a de shcéma à accéder
     * (par exemple urlBddSYSTOLE/schemaSYSTOLE, urlBddDIAPMGR/schemaDIAPMGR, etc...
     * @param schema
     */
    public static Connection connecterDB(Reporter report, String schema){
        String connexionKey = Thread.currentThread()+schema;
        Connection connexion = connexions.get(connexionKey);
        boolean connected = false;
        try {
            connected = !connexion.isClosed();
        } catch (Exception e) {
            //ignore
        }
        if(!connected){
            String keyBdd = "urlBdd" + schema.toUpperCase();
            String bddUrl = GlobalProp.get(keyBdd);
            try {
                connexion = DriverManager.getConnection(bddUrl);
            } catch (Exception e) {
                if (report!=null)
                    report.log(Reporter.ERROR_STATUS_NO_SCREENSHOT, "BDD CONNEXION FAIL", e);
                else
                    Assert.assertEquals(false, true, "BDD CONNEXION FAIL");
            }
            connexions.remove(connexionKey);
            connexions.put(connexionKey, connexion);
        }
        return connexion;
    }

    /**
     * Deconnexion de la base de données
     */
    public static void deconnecterDB() {
        for (Map.Entry<String, Connection> connexion : connexions.entrySet()) {
            try {
                connexion.getValue().close();
            } catch (Exception ignore) {
                // on passe à la suite
            }
        }
    }

    /**
     * Execute un select en base
     * Le select ne doit remonter qu'une ligne
     * S'il renvoie plusieurs colonnes seule la première est récupérée
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @return la première ligne de la première colonne
     */
    public static String executeSelect(Reporter report, String query) {
        String[] result = executeSelectMultiple(report, query);
        return result!=null?result[0]:null;
    }

    /**
     * Execute un select en base
     * Le select ne doit remonter qu'une ligne
     * S'il renvoie plusieurs colonnes seule la première est récupérée
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param param paramètre à inclure dans la requête
     * @return la première ligne de la première colonne
     */
    public static String executeSelect(Reporter report, String query, String param) {
        String[] result = executeSelectMultiple(report, query, param);
        return result!=null?result[0]:null;
    }

    /**
     * Execute un select en base
     * Le select ne doit remonter qu'une ligne
     * S'il renvoie plusieurs colonnes seule la première est récupérée
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params tableau de paramètres à inclure dans la requête
     * @return la première ligne de la première colonne
     */
    public static String executeSelect(Reporter report, String query, String[] params) {
        String[] result = executeSelectMultiple(report, query, params);
        return result!=null?result[0]:null;
    }

    /**
     * Execute un select en base
     * Le select remonte une ligne random parmis les 100 premières
     * S'il renvoie plusieurs colonnes seule la première est récupérée
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @return la première ligne de la première colonne
     */
    public static String executeSelectRandom(Reporter report, String query) {
        String[] result = executeSelectMultiple(report, query, true, new String[] {NOPARAM});
        return result!=null?result[0]:null;
    }

    /**
     * Execute un select en base
     * Le select remonte une ligne random parmis les 100 premières
     * S'il renvoie plusieurs colonnes seule la première est récupérée
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param param paramètre à inclure dans la requête
     * @return la première ligne de la première colonne
     */
    public static String executeSelectRandom(Reporter report, String query, String param) {
        String[] result = executeSelectMultiple(report, query, true, new String[] {param});
        return result!=null?result[0]:null;
    }

    /**
     * Execute un select en base
     * Le select remonte une ligne random parmis les 100 premières
     * S'il renvoie plusieurs colonnes seule la première est récupérée
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params tableau de paramètres à inclure dans la requête
     * @return la première ligne de la première colonne
     */
    public static String executeSelectRandom(Reporter report, String query, String[] params) {
        String[] result = executeSelectMultiple(report, query, true, params);
        return result!=null?result[0]:null;
    }

    /**
     * Execute un select en base jusqu'à ce que ça ramène la valeur passée en paramètre dans la limite du timeout indiqué
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params tableau de paramètres à inclure dans la requête
     * @param expectedValue valeur que le query doit retourner
     * @param timeout en seconde stop requetage en attente de la valeur attendue
     * @return la première ligne de la première colonne
     */
    public static String executeSelectUntilExpectedValue(Reporter report, String query, String[] params, String expectedValue, int timeout) {
        QueryResultUtil results = executeSelectMultipleMultiRow(report, query, params);
        TimeWait wait = new TimeWait();
        while ((results.size()==0 || !String.valueOf(results.getFirstVal()).equals(String.valueOf(expectedValue))) && wait.notOver(timeout)) {
            results = executeSelectMultipleMultiRow(report, query, params);
        }
        return results.getFirstVal();
    }

    /**
     * Execute un select en base jusqu'à ce que ça ramène une valeur contenant la valeur passée en paramètre dans la limite du timeout indiqué
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params tableau de paramètres à inclure dans la requête
     * @param expectedValue valeur que le query doit retourner
     * @param timeout en seconde stop requetage en attente de la valeur attendue
     * @return la première ligne de la première colonne
     */
    public static String executeSelectUntilContainsExpectedValue(Reporter report, String query, String[] params, String expectedValue, int timeout) {
        QueryResultUtil results = executeSelectMultipleMultiRow(report, query, params);
        TimeWait wait = new TimeWait();
        while ((results.size()==0 || !String.valueOf(results.getFirstVal()).contains(String.valueOf(expectedValue))) && wait.notOver(timeout)) {
            results = executeSelectMultipleMultiRow(report, query, params);
        }
        return results.getFirstVal();
    }

    /**
     * Execute un select en base jusqu'à ce que ça ramène une valeur contenant la valeur passée en paramètre dans la limite du timeout indiqué
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params tableau de paramètres à inclure dans la requête
     * @param expectedValues valeurs que le query doit retourner
     * @param timeout en seconde stop requetage en attente de la valeur attendue
     * @return la première ligne de la première colonne
     */
    public static String executeSelectUntilContainsExpectedValue(Reporter report, String query, String[] params, String[] expectedValues, int timeout) {
        QueryResultUtil results = executeSelectMultipleMultiRow(report, query, params);
        TimeWait wait = new TimeWait();
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(expectedValues));
        while ((results.size()==0 || !arrayList.contains(String.valueOf(results.getFirstVal()))) && wait.notOver(timeout)) {
            results = executeSelectMultipleMultiRow(report, query, params);
        }
        return results.getFirstVal();
    }

    /**
     * Execute un select en base
     * Le select ne doit remonter qu'une ligne mais peut renvoyer plusieurs colonnes
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @return un tableau contenant toutes les colonnes récupérées pour la première ligne remontée.
     */
    public static String[] executeSelectMultiple(Reporter report, String query) {
        return executeSelectMultiple(report, query, NOPARAM);
    }

    /**
     * Execute un select en base
     * Le select ne doit remonter qu'une ligne mais peut renvoyer plusieurs colonnes
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param param paramètre à inclure dans la requête
     * @return un tableau contenant toutes les colonnes récupérées pour la première ligne remontée.
     */
    public static String[] executeSelectMultiple(Reporter report, String query, String param) {
        return executeSelectMultiple(report, query, new String[] {param});
    }

    /**
     * Execute un select en base
     * Le select ne doit remonter qu'une ligne mais peut renvoyer plusieurs colonnes
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params tableau de paramètres à inclure dans la requête
     * @return un tableau contenant toutes les colonnes récupérées pour la première ligne remontée.
     */
    public static String[] executeSelectMultiple(Reporter report, String query, String[] params) {
        return executeSelectMultiple(report, query, false, params);
    }

    /**
     * Execute un select en base
     * Le select ne doit remonter qu'une ligne mais peut renvoyer plusieurs colonnes
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param random true selectionne une ligne random dans 100 premiers résultats
     * @param params tableau de paramètres à inclure dans la requête
     * @return un tableau contenant toutes les colonnes récupérées pour la première ligne remontée.
     */
    public static String[] executeSelectMultiple(Reporter report, String query, boolean random, String[] params) {
        String[] results = null;
        QueryResultUtil queryResultUtil = executeSelectMultipleMultiRow(report, query, params);
        if (queryResultUtil.size()>0) {
            int recordIndex = 0;
            if (random) {
                
                recordIndex = rand.nextInt(queryResultUtil.size());
            }
            LinkedHashMap<String, String> recordFields = queryResultUtil.getRow(recordIndex);
            results = new String[recordFields.size()];
            int i = 0;
            for (Map.Entry<String, String> recordData : recordFields.entrySet()) {
                results[i] = recordData.getValue();
                i++;
            }
        } else {
            if (report!=null) report.log(Reporter.INFO_STATUS, "la requete ne renvoi rien");
        }
        return results;
    }


    /**
     * exécute une requête sql. Renvoi 100 resultats max
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @return un object QueryResultUtil contenant les résultat dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query) {
        return executeSelectMultipleMultiRow(report, query, 100, NOPARAM);
    }

    /**
     * exécute une requête sql
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param nbLine nombre de lignes à ramener dans l'objet QueryResultUtil
     * @return un object QueryResultUtil contenant les résultat dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, int nbLine) {
        return executeSelectMultipleMultiRow(report, query, nbLine, NOPARAM);
    }

    /**
     * exécute une requête sql. Renvoi 100 resultats max
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param param paramètre à inclure dans la requête
     * @return un object QueryResultUtil contenant les résultat dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, String param) {
        return executeSelectMultipleMultiRow(report, query, 100, new String[] {param}, true);
    }

    /**
     * exécute une requête sql
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param nbLine nombre de lignes à ramener dans l'objet QueryResultUtil
     * @param param paramètre à inclure dans la requête
     * @return un object QueryResultUtil contenant les résultat dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, int nbLine, String param) {
        return executeSelectMultipleMultiRow(report, query, nbLine, new String[] {param}, true);
    }

    /**
     * exécute une requête sql. Renvoi 100 resultats max
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params tableau de paramètres à inclure dans la requête
     * @return un object QueryResultUtil contenant les résultat dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, String[] params) {
        return executeSelectMultipleMultiRow(report, query, 100, params, true);
    }

    /**
     * exécute une requête sql
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param nbLine nombre de lignes à ramener dans l'objet QueryResultUtil
     * @param params tableau de paramètres à inclure dans la requête
     * @return un object QueryResultUtil contenant les résultat dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, int nbLine, String[] params) {
        return executeSelectMultipleMultiRow(report, query, nbLine, params, true);
    }

    /**
     * exécute une requête sql
     * @param query la requete sql à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param nbLine nombre de lignes à ramener dans l'objet QueryResultUtil
     * @param params tableau de paramètres à inclure dans la requête
     * @param logReport
     * @return un object QueryResultUtil contenant les résultat dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, int nbLine, String[] params, boolean logReport) {
        ResultSet rs = null;
        PreparedStatement statement = null;
        // si query correspond à un fichier, récupère son contenu
        query = getQuery(query);
        //exécution de la requête
        Stack<LinkedHashMap<String, String>> result = new Stack<>();
        boolean executed=false;
        Exception e = null;
        int columnsNumber;
        int nLine;
        try {
            statement = prepareStatement(report, query, params, logReport);
            rs = statement.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            columnsNumber = rsmd.getColumnCount();
            nLine = 0;
            while (rs.next() && nLine < nbLine) {
                LinkedHashMap<String, String> resultRow = new LinkedHashMap<>();
                nLine++;
                for (int i = 1; i <= columnsNumber; i++) {
                    log.info("## " + rsmd.getColumnName(i) + " = " + rs.getString(i));
                    resultRow.put(rsmd.getColumnName(i).toUpperCase(), rs.getString(i));
                }
                result.push(resultRow);
            }
            executed = true;
        } catch (Exception ex) {
            e=ex;
        } finally {
            if (statement!=null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                    // on passe à la suite
                }
            }
            if (rs!=null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                    // on passe à la suite
                }
            }
            if (!executed && report!=null) {
                report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "BDD executeSelectMultipleMultiRow FAIL", e);
            }
        }
        return new QueryResultUtil(result);
    }

    /**
     * Execute non-query (usually INSERT/UPDATE/DELETE/COUNT/SUM...) on database
     * @param query a SQL statement
     */
    public static void executeInsertUpdateOrDelete(Reporter report, String query) {
        executeInsertUpdateOrDelete(report, query, NOPARAM);
    }

    /**
     * Execute non-query (usually INSERT/UPDATE/DELETE/COUNT/SUM...) on database
     * @param query a SQL statement
     * @param param SQL param
     */
    public static void executeInsertUpdateOrDelete(Reporter report, String query, String param) {
        executeInsertUpdateOrDelete(report, query, new String[] {param}, true);
    }

    /**
     * Execute non-query (usually INSERT/UPDATE/DELETE/COUNT/SUM...) on database
     * @param query a SQL statement
     * @param params SQL params
     */
    public static void executeInsertUpdateOrDelete(Reporter report, String query, String[] params) {
        executeInsertUpdateOrDelete(report, query, params, true);
    }

    /**
     * Execute non-query (usually INSERT/UPDATE/DELETE/COUNT/SUM...) on database
     * @param query a SQL statement
     * @param params SQL params
     * @param logReport
     */
    public static void executeInsertUpdateOrDelete(Reporter report, String query, String[] params, boolean logReport) {
        // si query correspond à un fichier, récupère son contenu
        query = getQuery(query);
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(report, query, params, logReport);
            if (statement!=null) {
                statement.executeQuery();
            } else {
                if (report!=null) report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "BDD executeInsertUpdateOrDelete FAIL, statement null");
            }
        } catch(Exception e) {
            if (report!=null) report.log(Reporter.INFO_STATUS, "BDD executeInsertUpdateOrDelete", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                    // on passe à la suite
                }
            }
        }
    }

    /**
     * Prépare un statement à partir d'une requête et d'éventuels paramètres
     * @param query le sql de la requête
     * @param params les paramètres de la requete
     * @return un statement construit à partir de la requete avec ses paramètres settés
     */
    private static PreparedStatement prepareStatement(Reporter report, String query, String[] params, boolean logReport){
        PreparedStatement statement = null;
        String schema = identifyScheme(query);
        Connection connexion = connecterDB(report, schema);
        query  = query.replaceAll(" " + schema + ".", " "+ GlobalProp.get("schema" + schema.toUpperCase()) +".");
        try {
            statement = connexion.prepareStatement(query);
            query = replaceQueryParameters(query, params, statement);

        } catch (Exception e) {
            if (report!=null && logReport) report.log("Error BDDUtil.prepareStatement ", e);
            return null;
        }
        if (report!=null && logReport) report.log(Reporter.PASS_STATUS, "requête : " + query.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>").replace("\r", "<br>"));
        return statement;
    }


    private static String identifyScheme(String query) {
        String[] schemes = EnvProp.getAllBddSchemas().split(",");
        String schema = EnvProp.getDefaultSchema();
        for (String sc: schemes
        ) {
            if (query.contains(" " +sc+".")) {
                schema = sc;
                break;
            }
            if (query.contains(sc+".")) {
                schema = sc;
            }
        }
        return schema;
    }

    private static String replaceQueryParameters(String query, String[] params, PreparedStatement statement) {
        if (params != null && !params[0].equals(NOPARAM)) {
            for (int i = 0; i < params.length; i++) {
                try {
                    query = query.replaceFirst("\\?", "'" + params[i] + "'");
                    statement.setString(i + 1, params[i]);
                } catch (SQLException ignore) {
                    // on passe à la suite
                }
            }
        }
        return query;
    }

    /**
     * @param result une colonne retournée par une requête
     * @return le result converti en double
     */
    public static double extractDouble(String result){
        if (result == null) {
            return 0.00;
        }
        return ((double)Math.round(Double.parseDouble(result)*100))/100;
    }

    /**
     * @param result une colonne retournée par une requête
     * @return la colonne convertie en String (gestion du cas ou elle est nulle)
     */
    public static String extractString(String result){
        if (result == null) {
            return "";
        }
        return result;
    }

    /**
     * renvoi le query d'un fichier .sql si le fichier existe
     * sinon renvoi la chaine en input supposé être une requête sql
     * @return le contenu du fichier si query est un fichier sinon la chaîne query
     */
    public static String getQuery(String query) {
        StringBuilder queryString = new StringBuilder();
        try (InputStream stream = new FileInputStream("target/test-classes/" + query); InputStreamReader reader = new InputStreamReader(stream); BufferedReader buff = new BufferedReader(reader)) {
            String line;
            while ((line = buff.readLine()) != null) {
                queryString.append(line).append("\n");
            }
        } catch (Exception e) {
            queryString = new StringBuilder(query);
        }
        return queryString.toString();
    }

    /**
     * renvoi le query de fichiers sql contenant le query et les conditions
     * @return la concatenation des 2 fichiers
     */
    public static String concatQueryFile(String query, String condition) {
        query = getQuery(query);
        condition = getQuery(condition);
        query = query.replace("GROUP BY", GROUP_BY);
        query = query.replace("ORDER BY", ORDER_BY);
        query = query.replace(" ON ", ON);
        int iLastOrder = query.lastIndexOf(ORDER_BY);
        int iLastGroupBy = query.lastIndexOf(GROUP_BY);
        int iLastOn = query.lastIndexOf(ON);

        if (iLastGroupBy>iLastOn) {
            return addNewConditionBefore(query, GROUP_BY, condition);
        }
        if (iLastOrder>iLastOn) {
            return addNewConditionBefore(query, ORDER_BY, condition);
        }
        return query + " " + condition;
    }

    private static String addNewConditionBefore(String initString, String beforeWhat, String replacement) {
        if (beforeWhat==null) {
            return initString + " " + replacement;
        } else {
            StringBuilder builder = new StringBuilder();
            int start = initString.lastIndexOf(beforeWhat);
            builder.append(initString.substring(0, start));
            builder.append(" " + replacement + " " + beforeWhat);
            builder.append(initString.substring(start + beforeWhat.length()));
            return builder.toString();
        }
    }
}