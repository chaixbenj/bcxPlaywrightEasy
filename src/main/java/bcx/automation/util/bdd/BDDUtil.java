package bcx.automation.util.bdd;

import bcx.automation.properties.EnvProp;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import bcx.automation.report.Reporter;
import bcx.automation.properties.GlobalProp;
import bcx.automation.util.TimeWait;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Utilitaire de base de données.
 * Permet de se connecter, se déconnecter, d'exécuter des select et d'autres types de requêtes.
 *
 * @author vca
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
     * Connexion à la base de données passée en paramètre.
     * Dans le fichier GlobalVariable.properties, il faut référencer autant de couples de properties urlBddSCHEMA et schemaSCHEMA qu'il y a de schémas à accéder.
     *
     * @param schema Le schéma de la base de données.
     * @return La connexion à la base de données.
     */
    public static Connection connecterDB(Reporter report, String schema) {
        String connexionKey = Thread.currentThread() + schema;
        Connection connexion = connexions.get(connexionKey);
        boolean connected = false;
        try {
            connected = !connexion.isClosed();
        } catch (Exception e) {
            // ignore
        }
        if (!connected) {
            String keyBdd = "urlBdd" + schema.toUpperCase();
            String bddUrl = GlobalProp.get(keyBdd);
            try {
                connexion = DriverManager.getConnection(bddUrl);
            } catch (Exception e) {
                if (report != null)
                    report.log(Reporter.FAIL_STATUS_NO_SCREENSHOT, "BDD CONNEXION FAIL", e);
                else
                    Assert.assertEquals(false, true, "BDD CONNEXION FAIL");
            }
            connexions.remove(connexionKey);
            connexions.put(connexionKey, connexion);
        }
        return connexion;
    }

    /**
     * Déconnexion de la base de données.
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
     * Exécute un select en base.
     * Le select ne doit remonter qu'une ligne.
     * S'il renvoie plusieurs colonnes, seule la première est récupérée.
     *
     * @param query La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @return La première ligne de la première colonne.
     */
    public static String executeSelect(Reporter report, String query) {
        String[] result = executeSelectMultiple(report, query);
        return result != null ? result[0] : null;
    }

    /**
     * Exécute un select en base.
     * Le select ne doit remonter qu'une ligne.
     * S'il renvoie plusieurs colonnes, seule la première est récupérée.
     *
     * @param query  La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param param  Paramètre à inclure dans la requête.
     * @return La première ligne de la première colonne.
     */
    public static String executeSelect(Reporter report, String query, String param) {
        String[] result = executeSelectMultiple(report, query, param);
        return result != null ? result[0] : null;
    }

    /**
     * Exécute un select en base.
     * Le select ne doit remonter qu'une ligne.
     * S'il renvoie plusieurs colonnes, seule la première est récupérée.
     *
     * @param query  La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params Tableau de paramètres à inclure dans la requête.
     * @return La première ligne de la première colonne.
     */
    public static String executeSelect(Reporter report, String query, String[] params) {
        String[] result = executeSelectMultiple(report, query, params);
        return result != null ? result[0] : null;
    }

    /**
     * Exécute un select en base.
     * Le select remonte une ligne aléatoire parmi les 100 premières.
     * S'il renvoie plusieurs colonnes, seule la première est récupérée.
     *
     * @param query La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @return La première ligne de la première colonne.
     */
    public static String executeSelectRandom(Reporter report, String query) {
        String[] result = executeSelectMultiple(report, query, true, new String[]{NOPARAM});
        return result != null ? result[0] : null;
    }

    /**
     * Exécute un select en base.
     * Le select remonte une ligne aléatoire parmi les 100 premières.
     * S'il renvoie plusieurs colonnes, seule la première est récupérée.
     *
     * @param query La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param param Paramètre à inclure dans la requête.
     * @return La première ligne de la première colonne.
     */
    public static String executeSelectRandom(Reporter report, String query, String param) {
        String[] result = executeSelectMultiple(report, query, true, new String[]{param});
        return result != null ? result[0] : null;
    }

    /**
     * Exécute un select en base.
     * Le select remonte une ligne aléatoire parmi les 100 premières.
     * S'il renvoie plusieurs colonnes, seule la première est récupérée.
     *
     * @param query  La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params Tableau de paramètres à inclure dans la requête.
     * @return La première ligne de la première colonne.
     */
    public static String executeSelectRandom(Reporter report, String query, String[] params) {
        String[] result = executeSelectMultiple(report, query, true, params);
        return result != null ? result[0] : null;
    }

    /**
     * Exécute un select en base jusqu'à ce que ça ramène la valeur passée en paramètre dans la limite du timeout indiqué.
     *
     * @param query         La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params        Tableau de paramètres à inclure dans la requête.
     * @param expectedValue Valeur que la requête doit retourner.
     * @param timeout       Timeout en secondes pour stopper le requêtage en attente de la valeur attendue.
     * @return La première ligne de la première colonne.
     */
    public static String executeSelectUntilExpectedValue(Reporter report, String query, String[] params, String expectedValue, int timeout) {
        QueryResultUtil results = executeSelectMultipleMultiRow(report, query, params);
        TimeWait wait = new TimeWait();
        while ((results.size() == 0 || !String.valueOf(results.getFirstVal()).equals(String.valueOf(expectedValue))) && wait.notOver(timeout)) {
            results = executeSelectMultipleMultiRow(report, query, params);
        }
        return results.getFirstVal();
    }

    /**
     * Exécute un select en base jusqu'à ce que ça ramène une valeur contenant la valeur passée en paramètre dans la limite du timeout indiqué.
     *
     * @param query         La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params        Tableau de paramètres à inclure dans la requête.
     * @param expectedValue Valeur que la requête doit retourner.
     * @param timeout       Timeout en secondes pour stopper le requêtage en attente de la valeur attendue.
     * @return La première ligne de la première colonne.
     */
    public static String executeSelectUntilContainsExpectedValue(Reporter report, String query, String[] params, String expectedValue, int timeout) {
        QueryResultUtil results = executeSelectMultipleMultiRow(report, query, params);
        TimeWait wait = new TimeWait();
        while ((results.size() == 0 || !String.valueOf(results.getFirstVal()).contains(String.valueOf(expectedValue))) && wait.notOver(timeout)) {
            results = executeSelectMultipleMultiRow(report, query, params);
        }
        return results.getFirstVal();
    }

    /**
     * Exécute un select en base jusqu'à ce que ça ramène une valeur contenant la valeur passée en paramètre dans la limite du timeout indiqué.
     *
     * @param query         La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params        Tableau de paramètres à inclure dans la requête.
     * @param expectedValues Valeurs que la requête doit retourner.
     * @param timeout       Timeout en secondes pour stopper le requêtage en attente de la valeur attendue.
     * @return La première ligne de la première colonne.
     */
    public static String executeSelectUntilContainsExpectedValue(Reporter report, String query, String[] params, String[] expectedValues, int timeout) {
        QueryResultUtil results = executeSelectMultipleMultiRow(report, query, params);
        TimeWait wait = new TimeWait();
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(expectedValues));
        while ((results.size() == 0 || !arrayList.contains(String.valueOf(results.getFirstVal()))) && wait.notOver(timeout)) {
            results = executeSelectMultipleMultiRow(report, query, params);
        }
        return results.getFirstVal();
    }

    /**
     * Exécute un select en base.
     * Le select ne doit remonter qu'une ligne mais peut renvoyer plusieurs colonnes.
     *
     * @param query La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @return Un tableau contenant toutes les colonnes récupérées pour la première ligne remontée.
     */
    public static String[] executeSelectMultiple(Reporter report, String query) {
        return executeSelectMultiple(report, query, NOPARAM);
    }

    /**
     * Exécute un select en base.
     * Le select ne doit remonter qu'une ligne mais peut renvoyer plusieurs colonnes.
     *
     * @param query  La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param param  Paramètre à inclure dans la requête.
     * @return Un tableau contenant toutes les colonnes récupérées pour la première ligne remontée.
     */
    public static String[] executeSelectMultiple(Reporter report, String query, String param) {
        return executeSelectMultiple(report, query, new String[]{param});
    }

    /**
     * Exécute un select en base.
     * Le select ne doit remonter qu'une ligne mais peut renvoyer plusieurs colonnes.
     *
     * @param query  La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params Tableau de paramètres à inclure dans la requête.
     * @return Un tableau contenant toutes les colonnes récupérées pour la première ligne remontée.
     */
    public static String[] executeSelectMultiple(Reporter report, String query, String[] params) {
        return executeSelectMultiple(report, query, false, params);
    }

    /**
     * Exécute un select en base.
     * Le select ne doit remonter qu'une ligne mais peut renvoyer plusieurs colonnes.
     *
     * @param query   La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param random  True pour sélectionner une ligne aléatoire dans les 100 premiers résultats.
     * @param params  Tableau de paramètres à inclure dans la requête.
     * @return Un tableau contenant toutes les colonnes récupérées pour la première ligne remontée.
     */
    public static String[] executeSelectMultiple(Reporter report, String query, boolean random, String[] params) {
        String[] results = null;
        QueryResultUtil queryResultUtil = executeSelectMultipleMultiRow(report, query, params);
        if (queryResultUtil.size() > 0) {
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
            if (report != null) report.log(Reporter.INFO_STATUS, "la requete ne renvoi rien");
        }
        return results;
    }

    /**
     * Exécute une requête SQL. Renvoie 100 résultats max.
     *
     * @param query La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @return Un objet QueryResultUtil contenant les résultats dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer.
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query) {
        return executeSelectMultipleMultiRow(report, query, 100, NOPARAM);
    }

    /**
     * Exécute une requête SQL.
     *
     * @param query   La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param nbLine  Nombre de lignes à ramener dans l'objet QueryResultUtil.
     * @return Un objet QueryResultUtil contenant les résultats dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer.
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, int nbLine) {
        return executeSelectMultipleMultiRow(report, query, nbLine, NOPARAM);
    }

    /**
     * Exécute une requête SQL. Renvoie 100 résultats max.
     *
     * @param query  La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param param  Paramètre à inclure dans la requête.
     * @return Un objet QueryResultUtil contenant les résultats dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer.
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, String param) {
        return executeSelectMultipleMultiRow(report, query, 100, new String[]{param}, true);
    }

    /**
     * Exécute une requête SQL.
     *
     * @param query   La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param nbLine  Nombre de lignes à ramener dans l'objet QueryResultUtil.
     * @param param   Paramètre à inclure dans la requête.
     * @return Un objet QueryResultUtil contenant les résultats dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer.
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, int nbLine, String param) {
        return executeSelectMultipleMultiRow(report, query, nbLine, new String[]{param}, true);
    }

    /**
     * Exécute une requête SQL. Renvoie 100 résultats max.
     *
     * @param query  La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param params Tableau de paramètres à inclure dans la requête.
     * @return Un objet QueryResultUtil contenant les résultats dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer.
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, String[] params) {
        return executeSelectMultipleMultiRow(report, query, 100, params, true);
    }

    /**
     * Exécute une requête SQL.
     *
     * @param query     La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param nbLine    Nombre de lignes à ramener dans l'objet QueryResultUtil.
     * @param params    Tableau de paramètres à inclure dans la requête.
     * @return Un objet QueryResultUtil contenant les résultats dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer.
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, int nbLine, String[] params) {
        return executeSelectMultipleMultiRow(report, query, nbLine, params, true);
    }

    /**
     * Exécute une requête SQL.
     *
     * @param query     La requête SQL à exécuter. Ses paramètres doivent être remplacés par des ?
     * @param nbLine    Nombre de lignes à ramener dans l'objet QueryResultUtil.
     * @param params    Tableau de paramètres à inclure dans la requête.
     * @param logReport Indique si les logs doivent être rapportés.
     * @return Un objet QueryResultUtil contenant les résultats dans une Stack de HashMap(String,String) (nom colonne, valeur) et permettant de les gérer.
     */
    public static QueryResultUtil executeSelectMultipleMultiRow(Reporter report, String query, int nbLine, String[] params, boolean logReport) {
        ResultSet rs = null;
        PreparedStatement statement = null;
        // si query correspond à un fichier, récupère son contenu
        query = getQuery(query);
        // exécution de la requête
        Stack<LinkedHashMap<String, String>> result = new Stack<>();
        boolean executed = false;
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
            e = ex;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                    // on passe à la suite
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                    // on passe à la suite
                }
            }
            if (!executed && report != null) {
                report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "BDD executeSelectMultipleMultiRow FAIL", e);
            }
        }
        return new QueryResultUtil(result);
    }

    /**
     * Exécute une requête non-query (usuellement INSERT/UPDATE/DELETE/COUNT/SUM...) sur la base de données.
     *
     * @param query Une instruction SQL.
     */
    public static void executeInsertUpdateOrDelete(Reporter report, String query) {
        executeInsertUpdateOrDelete(report, query, NOPARAM);
    }

    /**
     * Exécute une requête non-query (usuellement INSERT/UPDATE/DELETE/COUNT/SUM...) sur la base de données.
     *
     * @param query Une instruction SQL.
     * @param param Paramètre SQL.
     */
    public static void executeInsertUpdateOrDelete(Reporter report, String query, String param) {
        executeInsertUpdateOrDelete(report, query, new String[]{param}, true);
    }

    /**
     * Exécute une requête non-query (usuellement INSERT/UPDATE/DELETE/COUNT/SUM...) sur la base de données.
     *
     * @param query  Une instruction SQL.
     * @param params Paramètres SQL.
     */
    public static void executeInsertUpdateOrDelete(Reporter report, String query, String[] params) {
        executeInsertUpdateOrDelete(report, query, params, true);
    }

    /**
     * Exécute une requête non-query (usuellement INSERT/UPDATE/DELETE/COUNT/SUM...) sur la base de données.
     *
     * @param query     Une instruction SQL.
     * @param params    Paramètres SQL.
     * @param logReport Indique si les logs doivent être rapportés.
     */
    public static void executeInsertUpdateOrDelete(Reporter report, String query, String[] params, boolean logReport) {
        // si query correspond à un fichier, récupère son contenu
        query = getQuery(query);
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(report, query, params, logReport);
            if (statement != null) {
                statement.executeQuery();
            } else {
                if (report != null) report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "BDD executeInsertUpdateOrDelete FAIL, statement null");
            }
        } catch (Exception e) {
            if (report != null) report.log(Reporter.INFO_STATUS, "BDD executeInsertUpdateOrDelete", e);
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
     * Prépare un statement à partir d'une requête et d'éventuels paramètres.
     *
     * @param query     Le SQL de la requête.
     * @param params    Les paramètres de la requête.
     * @param logReport Indique si les logs doivent être rapportés.
     * @return Un statement construit à partir de la requête avec ses paramètres settés.
     */
    private static PreparedStatement prepareStatement(Reporter report, String query, String[] params, boolean logReport) {
        PreparedStatement statement = null;
        String schema = identifyScheme(query);
        Connection connexion = connecterDB(report, schema);
        query = query.replaceAll(" " + schema + ".", " " + GlobalProp.get("schema" + schema.toUpperCase()) + ".");
        try {
            statement = connexion.prepareStatement(query);
            query = replaceQueryParameters(query, params, statement);

        } catch (Exception e) {
            if (report != null && logReport) report.log("Error BDDUtil.prepareStatement ", e);
            return null;
        }
        if (report != null && logReport) report.log(Reporter.PASS_STATUS, "requête : " + query.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "  \n").replace("\r", "  \r"));
        return statement;
    }

    private static String identifyScheme(String query) {
        String[] schemes = EnvProp.getAllBddSchemas().split(",");
        String schema = EnvProp.getDefaultSchema();
        for (String sc : schemes) {
            if (query.contains(" " + sc + ".")) {
                schema = sc;
                break;
            }
            if (query.contains(sc + ".")) {
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
     * Convertit une colonne retournée par une requête en double.
     *
     * @param result La colonne retournée par une requête.
     * @return Le résultat converti en double.
     */
    public static double extractDouble(String result) {
        if (result == null) {
            return 0.00;
        }
        return ((double) Math.round(Double.parseDouble(result) * 100)) / 100;
    }

    /**
     * Convertit une colonne retournée par une requête en String.
     *
     * @param result La colonne retournée par une requête.
     * @return La colonne convertie en String (gestion du cas où elle est nulle).
     */
    public static String extractString(String result) {
        if (result == null) {
            return "";
        }
        return result;
    }

    /**
     * Renvoie la requête d'un fichier .sql si le fichier existe.
     * Sinon, renvoie la chaîne en input supposée être une requête SQL.
     *
     * @param query La requête SQL ou le nom du fichier.
     * @return Le contenu du fichier si query est un fichier, sinon la chaîne query.
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
     * Renvoie la requête de fichiers SQL contenant la requête et les conditions.
     *
     * @param query     La requête SQL.
     * @param condition Les conditions à ajouter à la requête.
     * @return La concaténation des deux fichiers.
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

        if (iLastGroupBy > iLastOn) {
            return addNewConditionBefore(query, GROUP_BY, condition);
        }
        if (iLastOrder > iLastOn) {
            return addNewConditionBefore(query, ORDER_BY, condition);
        }
        return query + " " + condition;
    }

    private static String addNewConditionBefore(String initString, String beforeWhat, String replacement) {
        if (beforeWhat == null) {
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
