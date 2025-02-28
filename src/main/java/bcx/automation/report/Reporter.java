package bcx.automation.report;

import bcx.automation.playwright.element.BaseElement;
import bcx.automation.properties.EnvProp;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import bcx.automation.properties.GlobalProp;
import bcx.automation.util.data.DataUtil;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Classe utilitaire pour générer des rapports de test.
 */
@Slf4j
public class Reporter {
    public static final String SKIP_STATUS = "skip";
    public static final String INFO_STATUS = "info";
    public static final String PASS_STATUS = "pass";
    public static final String WARNING_STATUS = "warning";
    public static final String WARNING_STATUS_NO_SCREENSHOT = "warningnoscreenshot";
    public static final String FAIL_STATUS = "fail";
    public static final String FAIL_NEXT_STATUS = "failnext";
    public static final String ERROR_STATUS = "error";
    public static final String ERROR_NEXT_STATUS = "errornext";
    public static final String ERROR_STATUS_NO_SCREENSHOT = "errornoscreenshot";
    public static final String ERROR_NEXT_STATUS_NO_SCREENSHOT = "errornextnoscreenshot";
    public static final String ERROR_NEXT_STATUS_NO_SCREENSHOT_END_SUITE = "errornextnoscreenshotendsuite";

    @Getter
    @Setter
    private Page page;
    private final SoftAssert softAssert = new SoftAssert();
    private int nbTestEnd;
    @Getter
    private int nbTestError;
    private int nbTestErrorStop;
    private String strError;
    private int nbTestWarn;
    boolean testSkipped;
    private String currentSuite;
    private ExtentReports extent;
    private ExtentTest logger;
    private String previousLog;
    private LocalDateTime timePreviousLog;
    private static final HashMap<String, Integer> suiteError = new HashMap<>();
    private static final HashMap<String, Integer> suiteWarning = new HashMap<>();
    private static final ArrayList<String> startedTests = new ArrayList<>();
    private static final ArrayList<String> endedSuites = new ArrayList<>();
    private static final LinkedHashMap<String, String> suiteSummary = new LinkedHashMap<>();

    /**
     * Enregistre l'élément courant.
     *
     * @param element L'élément courant.
     */
    @Setter
    private ElementHandle currentElement;
    @Getter
    private String lastAction;
    @Getter
    private String lastStatus;
    private static final String TODO_ICONE = "<span class=\"test-status label right outline capitalize skip\">todo</span>";
    private static final String PASS_ICONE = "<span class=\"test-status label right outline capitalize pass\">pass</span>";
    private static final String FAIL_ICONE = "<span class=\"test-status label right outline capitalize fail\">fail</span>";
    private static final String WARN_ICONE = "<span class=\"test-status label right outline capitalize warning\">warning</span>";

    private static final String ENV = "Environment";
    private static final String INDEX_TEST_HTML_PATH = GlobalProp.getReportFolder() + "index_tests.html";
    private static final String EXTENT_CONFIG_XML_PATH = "/target/test-classes/extent-config.xml";
    private static final String DATA_IMAGE_FORMAT = "data:image/png;base64,";

    /**
     * Constructeur par défaut.
     */
    public Reporter() {
        new Reporter(null);
    }

    /**
     * Constructeur avec le nom de la suite de tests.
     *
     * @param suiteName Le nom de la suite de tests.
     */
    public Reporter(String suiteName) {
        copyCommonResource("extent-config.xml");
        if (suiteName != null) {
            currentSuite = suiteName;
            extent = new ExtentReports(GlobalProp.getReportFolder() + suiteName + ".html", true);
            suiteError.put(suiteName, 0);
            suiteWarning.put(suiteName, 0);
            suiteSummary.put(suiteName, "");
            extent.addSystemInfo(ENV, EnvProp.getEnvironnement());
            extent.loadConfig(new File(Paths.get("").toAbsolutePath() + EXTENT_CONFIG_XML_PATH));
        }
        nbTestError = 0;
        nbTestErrorStop = 0;
        strError = "";
        nbTestWarn = 0;
        testSkipped = false;
    }

    /**
     * Publie le rapport global.
     */
    public void publishGlobalReport() {
        try {
            if (!endedSuites.isEmpty()) {
                extent = new ExtentReports(INDEX_TEST_HTML_PATH, false);
                extent.addSystemInfo(ENV, EnvProp.getEnvironnement());
                extent.loadConfig(new File(Paths.get("").toAbsolutePath() + EXTENT_CONFIG_XML_PATH));
                for (String suiteName : endedSuites) {
                    publishGlobalReportItem(suiteName);
                }
                extent.flush();
            }
        } catch (Exception ex) {
            log.info("publishGlobalReport exception", ex);
        }
        endedSuites.clear();
        log.info(LocalDateTime.now() + " -------------- END publishGlobalReport");
    }

    /**
     * Publie un élément du rapport global.
     *
     * @param suiteName Le nom de la suite de tests.
     */
    private void publishGlobalReportItem(String suiteName) {
        try {
            previousLog = "";
            timePreviousLog = LocalDateTime.now();
            logger = extent.startTest(suiteName);
            int nbError = getSuiteError(suiteName);
            int nbWarn = getSuiteWarning(suiteName);
            log.info(LocalDateTime.now() + " -------------publishGlobalReportItem----------------" + suiteName + " " + nbError + " " + nbWarn);
            String status;
            if (nbError > 0) {
                status = ERROR_NEXT_STATUS_NO_SCREENSHOT_END_SUITE;
            } else {
                status = nbWarn > 0 ? WARNING_STATUS_NO_SCREENSHOT : PASS_STATUS;
            }
            log(status, "<A href=\"" + suiteName + ".html\"><h4>" + suiteName + " " + nbError + " tests fail, " + nbWarn + " tests warning => par ici le détail</h4></a><br><table>" + getSuiteSummary(suiteName) + "</table>", false);
            extent.endTest(logger);
            log.info(LocalDateTime.now() + " -------------END publishGlobalReportItem----------------" + suiteName + " " + nbError + " " + nbWarn);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(LocalDateTime.now() + " -------------publishGlobalReportItem----------------Exception", e);
        }
    }

    /**
     * Publie le rapport.
     */
    public void publish() {
        extent.flush();
    }

    /**
     * Initialise un test.
     *
     * @param test Le nom du test.
     */
    public void initTest(String test) {
        String testUId = getUniqueTestRunId(test);
        if (extent == null) {
            extent = new ExtentReports(GlobalProp.getReportFolder() + currentSuite + ".html", true);
            extent.addSystemInfo(ENV, EnvProp.getEnvironnement());
        }
        logger = extent.startTest(testUId);
        nbTestError = 0;
        nbTestErrorStop = 0;
        nbTestWarn = 0;
        testSkipped = false;
        previousLog = "";
        timePreviousLog = LocalDateTime.now();
        suiteSummary.put(currentSuite, getSuiteSummary(currentSuite) + "<tr><td>" + testUId);
        strError = "";
    }

    /**
     * Termine un test.
     *
     * @param isSuccess Indique si le test est un succès.
     * @return Le nombre d'erreurs.
     */
    public int endTest(boolean isSuccess) {
        return endTest(isSuccess, false);
    }

    /**
     * Termine un test avec gestion des erreurs.
     *
     * @param isSuccess Indique si le test est un succès.
     * @param catchFail Indique si les erreurs doivent être capturées.
     * @return Le nombre d'erreurs.
     */
    public int endTest(boolean isSuccess, boolean catchFail) {
        nbTestEnd += 1;
        if (nbTestError == 0) nbTestError = isSuccess || testSkipped ? 0 : 1;
        LogStatus status = LogStatus.PASS;
        String iconeStatus = PASS_ICONE;
        if (testSkipped) {
            status = LogStatus.SKIP;
            iconeStatus = TODO_ICONE;
        } else if (nbTestError > 0) {
            status = LogStatus.FAIL;
            iconeStatus = FAIL_ICONE;
        } else if (nbTestWarn > 0) {
            status = LogStatus.WARNING;
            iconeStatus = WARN_ICONE;
        }
        logger.log(status, "[TEST END] <b>" + nbTestError + " erreurs " + nbTestWarn + " warnings \n" + strError + "</b>");

        suiteSummary.put(currentSuite, getSuiteSummary(currentSuite) + iconeStatus + "</td></tr>");

        if (!endedSuites.contains(currentSuite)) {
            endedSuites.add(currentSuite);
        }
        suiteError.put(currentSuite, getSuiteError(currentSuite) + (nbTestError != 0 ? 1 : 0));
        suiteWarning.put(currentSuite, getSuiteWarning(currentSuite) + (nbTestWarn != 0 ? 1 : 0));
        extent.endTest(logger);
        publish();
        if (!catchFail && nbTestError > 0 && nbTestErrorStop == 0) Assert.assertEquals(nbTestError, 0);
        return nbTestError;
    }

    /**
     * Compte le nombre de tests terminés.
     *
     * @return Le nombre de tests terminés.
     */
    public int countEndedTest() {
        return nbTestEnd;
    }

    /**
     * Vérifie l'égalité entre deux entiers et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(int expected, int actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Vérifie l'égalité entre deux entiers et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(String info, int expected, int actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'égalité entre deux booléens et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(boolean expected, boolean actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Vérifie l'égalité entre deux booléens et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(String info, boolean expected, boolean actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'égalité entre deux doubles et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(Double expected, Double actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Vérifie l'égalité entre deux doubles et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(String info, Double expected, Double actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'égalité entre deux strings et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(String expected, String actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Vérifie l'égalité entre deux strings et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(String info, String expected, String actual) {
        String status = DataUtil.equalsIgnoreIgnoredString(this, expected, actual, true);
        log(status, (info != null ? info + " " : "") + "assertEquals", null, expected, status.equals(PASS_STATUS) ? null : actual, null);
    }

    /**
     * Vérifie l'égalité entre deux strings et logue le résultat dans le rapport avec une explication, stoppe le test.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEqualsStop(String info, String expected, String actual) {
        String status = DataUtil.equalsIgnoreIgnoredString(this, expected, actual, true);
        status = (status.equals(PASS_STATUS) || status.equals(WARNING_STATUS) ? PASS_STATUS : FAIL_STATUS);
        log(status, (info != null ? info + " " : "") + "assertEquals", null, expected, status.equals(PASS_STATUS) ? null : actual, null);
    }

    /**
     * Vérifie que la première chaîne contient la seconde et logue le résultat dans le rapport.
     *
     * @param contenant La chaîne contenant.
     * @param contenu La chaîne contenue.
     */
    public void assertContains(String contenant, String contenu) {
        assertContains(null, contenant, contenu);
    }

    /**
     * Vérifie que la première chaîne contient la seconde et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param contenant La chaîne contenant.
     * @param contenu La chaîne contenue.
     */
    public void assertContains(String info, String contenant, String contenu) {
        String status = contenant.contains(contenu) ? PASS_STATUS : FAIL_NEXT_STATUS;
        log(status, (info != null ? info + " " : "") + "assertContains", null, contenant, status.equals(PASS_STATUS) ? null : contenu, null);
    }

    /**
     * Vérifie que la première chaîne ne contient pas la seconde et logue le résultat dans le rapport.
     *
     * @param contenant La chaîne contenant.
     * @param contenu La chaîne contenue.
     */
    public void assertNotContains(String contenant, String contenu) {
        assertNotContains(null, contenant, contenu);
    }

    /**
     * Vérifie que la première chaîne ne contient pas la seconde et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param contenant La chaîne contenant.
     * @param contenu La chaîne contenue.
     */
    public void assertNotContains(String info, String contenant, String contenu) {
        String status = contenant.contains(contenu) ? FAIL_NEXT_STATUS : PASS_STATUS;
        log(status, (info != null ? info + " " : "") + "assertContains", null, contenant, status.equals(PASS_STATUS) ? null : contenu, null);
    }

    /**
     * Vérifie l'inégalité entre deux entiers et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(int expected, int actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * Vérifie l'inégalité entre deux entiers et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(String info, int expected, int actual) {
        assertNotEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'inégalité entre deux booléens et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(boolean expected, boolean actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * Vérifie l'inégalité entre deux booléens et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(String info, boolean expected, boolean actual) {
        assertNotEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'inégalité entre deux doubles et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(Double expected, Double actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * Vérifie l'inégalité entre deux doubles et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(String info, Double expected, Double actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'inégalité entre deux strings et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(String expected, String actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * Vérifie l'inégalité entre deux strings et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(String info, String expected, String actual) {
        String status = (expected.equals(actual) ? FAIL_NEXT_STATUS : PASS_STATUS);
        log(status, (info != null ? info + " " : "") + "assertNotEquals", null, expected, status.equals(PASS_STATUS) ? null : actual, null);
    }

    /**
     * Vérifie qu'un entier est supérieur à un autre et logue le résultat dans le rapport.
     *
     * @param num1 Le premier nombre.
     * @param num2 Le second nombre.
     */
    public void assertGreater(int num1, int num2) {
        assertGreater(null, num1, num2);
    }

    /**
     * Vérifie qu'un entier est supérieur à un autre et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param num1 Le premier nombre.
     * @param num2 Le second nombre.
     */
    public void assertGreater(String info, int num1, int num2) {
        assertGreater(info, Double.valueOf(num1), Double.valueOf(num2));
    }

    /**
     * Vérifie qu'un double est supérieur à un autre et logue le résultat dans le rapport.
     *
     * @param num1 Le premier nombre.
     * @param num2 Le second nombre.
     */
    public void assertGreater(Double num1, Double num2) {
        assertGreater(null, num1, num2);
    }

    /**
     * Vérifie qu'un double est supérieur à un autre et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param num1 Le premier nombre.
     * @param num2 Le second nombre.
     */
    public void assertGreater(String info, Double num1, Double num2) {
        String status = (num1 > num2 ? PASS_STATUS : FAIL_NEXT_STATUS);
        log(status, (info != null ? info + " " : "") + "assertGreater", null, String.valueOf(num1), String.valueOf(num2), null);
    }

    /**
     * Vérifie qu'un double est inférieur ou égal à un autre et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param num1 Le premier nombre.
     * @param num2 Le second nombre.
     */
    public void assertSmallerOrEqual(String info, Double num1, Double num2) {
        String status = (num1 <= num2 ? PASS_STATUS : FAIL_NEXT_STATUS);
        log(status, (info != null ? info + " " : "") + "assertSmallerOrEqual", null, String.valueOf(num1), String.valueOf(num2), null);
    }

    /**
     * Vérifie qu'une String n'est pas null et logue le résultat dans le rapport.
     *
     * @param actual La valeur réelle.
     */
    public void assertNotNull(String actual) {
        assertNotNull(null, actual);
    }

    /**
     * Vérifie qu'une String n'est pas null et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param actual La valeur réelle.
     */
    public void assertNotNull(String info, String actual) {
        String status = (actual != null ? PASS_STATUS : FAIL_NEXT_STATUS);
        log(status, (info != null ? info + " " : "") + "assertNotNull", null, null, actual, null);
    }

    /**
     * Vérifie qu'une date à un format donné est égale à la date courante dans un intervalle de + ou - N minutes et logue le résultat dans le rapport.
     *
     * @param date La date à vérifier.
     * @param format Le format de la date.
     * @param minuteEcartAcceptable L'écart acceptable en minutes.
     */
    public void assertDateEqualsLocalDateTime(String date, String format, int minuteEcartAcceptable) {
        assertDateEqualsLocalDateTime(null, date, format, minuteEcartAcceptable);
    }

    /**
     * Vérifie qu'une date à un format donné est égale à la date courante dans un intervalle de + ou - N minutes et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param date La date à vérifier.
     * @param format Le format de la date.
     * @param minuteEcartAcceptable L'écart acceptable en minutes.
     */
    public void assertDateEqualsLocalDateTime(String info, String date, String format, int minuteEcartAcceptable) {
        LocalDateTime dateFormatee;
        String status = PASS_STATUS;
        String message = null;
        LocalDateTime now = LocalDateTime.now();
        try {
            dateFormatee = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
            if (dateFormatee.plusMinutes(minuteEcartAcceptable).isBefore(now) || dateFormatee.minusMinutes(minuteEcartAcceptable).isAfter(now)) {
                status = FAIL_NEXT_STATUS;
            }
        } catch (Exception e) {
            status = ERROR_NEXT_STATUS;
            message = e.getMessage();
        }
        log(status, (info != null ? info + " " : "") + "assertDateEqualsLocalDateTime", null, now + " +/- " + minuteEcartAcceptable + " minutes", String.valueOf(date), message);
    }

    /**
     * Vérifie qu'une date à un format donné est antérieure à la date courante - N minutes et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param date La date à vérifier.
     * @param format Le format de la date.
     * @param minuteARetirer Le nombre de minutes à retirer.
     */
    public void assertDateLowerThanLocalDateTime(String info, String date, String format, int minuteARetirer) {
        LocalDateTime dateFormatee;
        String status = PASS_STATUS;
        String message = null;
        LocalDateTime now = LocalDateTime.now();
        try {
            dateFormatee = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
            if (now.minusMinutes(minuteARetirer).isBefore(dateFormatee)) {
                status = FAIL_NEXT_STATUS;
            }
        } catch (Exception e) {
            status = ERROR_NEXT_STATUS;
            message = e.getMessage();
        }
        log(status, (info != null ? info + " " : "") + "assertDateLowerThanLocalDateTime", null, now + " - " + minuteARetirer + " minutes", String.valueOf(date), message);
    }

    /**
     * Logue un message de type "info" dans le rapport.
     *
     * @param messageLog Le message à loguer.
     */
    public void title(String messageLog) {
        log(Reporter.INFO_STATUS, "<h5>" + messageLog + "</h5>");
    }

    /**
     * Logue un message de type "info" dans le rapport.
     *
     * @param messageLog Le message à loguer.
     */
    public void log(String messageLog) {
        log(Reporter.INFO_STATUS, messageLog);
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param action L'action.
     * @param elementName L'élément.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     * @param message Le message.
     */
    public void log(String status, String action, String elementName, String expected, String actual, String message) {
        log(status, concatMessage(action, elementName, expected, actual, message));
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param action L'action.
     * @param elementName L'élément.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     * @param message Le message.
     * @param e L'exception.
     */
    public void log(String status, String action, String elementName, String expected, String actual, String message, Exception e) {
        log(status, concatMessage(action, elementName, expected, actual, message), e);
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param messageLog Le message à loguer.
     * @param e L'exception.
     */
    public void log(String status, String messageLog, Exception e) {
        String stackTrace = getExceptionStack(e);
        if (e != null && stackTrace.contains("Error communicating with the remote browser. It may have died.")) {
            status = WARNING_STATUS_NO_SCREENSHOT;
        }
        log(status, messageLog + "\n" + getExceptionStack(e));
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param e L'exception.
     */
    public void log(String status, Exception e) {
        log(status, getExceptionStack(e));
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param messageLog Le message à loguer.
     */
    public void log(String status, String messageLog) {
        log(status, messageLog, true);
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param messageLog Le message à loguer.
     * @param takeScreenShot Indique si une capture d'écran doit être prise.
     */
    public void log(String status, String messageLog, boolean takeScreenShot) {
        String threadTimeLog = "[" + Thread.currentThread() + " - " + (LocalDateTime.now()).format(DateTimeFormatter.ofPattern("hh:mm:ss")) + "] ";
        messageLog = (messageLog == null ? "" : messageLog);
        if (status.contains("noscreenshot") || messageLog.equals("erreur ged....") || messageLog.contains("[noscreenshot]")) {
            takeScreenShot = false;
            status = status.replace("noscreenshot", "");
            messageLog = messageLog.replace("[noscreenshot]", "");
        }

        boolean isEndSuiteLog = status.contains("endsuite");
        status = status.replace("endsuite", "");

        messageLog = getPageMethod() + messageLog;
        String currentLog = status + messageLog + takeScreenShot;
        lastAction = messageLog;
        lastStatus = status;
        if (previousLog != null && !previousLog.equals(currentLog)) {
            previousLog = currentLog;
            timePreviousLog = LocalDateTime.now();
            if (GlobalProp.isForceStopOnFail() && !isEndSuiteLog) {
                if (status.equals(FAIL_NEXT_STATUS)) {
                    status = FAIL_STATUS;
                } else if (status.equals(ERROR_NEXT_STATUS)) {
                    status = ERROR_STATUS;
                }
            }
            switch (status) {
                case SKIP_STATUS:
                    log(LogStatus.SKIP, true, false, false, false, messageLog, "");
                    log.info(threadTimeLog + messageLog);
                    testSkipped = true;
                    Assert.assertEquals("skip : " + messageLog, PASS_STATUS);
                    break;
                case PASS_STATUS:
                    log(LogStatus.PASS, false, false, false, false, messageLog, "");
                    log.info(threadTimeLog + messageLog);
                    break;
                case WARNING_STATUS:
                    log(LogStatus.WARNING, true, false, false, takeScreenShot, messageLog, "");
                    log.warn(threadTimeLog + messageLog);
                    break;
                case FAIL_STATUS:
                    log(LogStatus.FAIL, false, true, true, takeScreenShot, messageLog, messageLog + "<br>");
                    log.error(threadTimeLog + messageLog);
                    Assert.assertEquals("fail : " + messageLog, PASS_STATUS);
                    break;
                case ERROR_STATUS:
                    log(LogStatus.ERROR, false, true, true, takeScreenShot, messageLog, messageLog + "<br>");
                    log.error(threadTimeLog + messageLog);
                    Assert.assertEquals("error : " + messageLog, PASS_STATUS);
                    break;
                case FAIL_NEXT_STATUS:
                    log(LogStatus.FAIL, false, true, false, takeScreenShot, messageLog, messageLog + "<br>");
                    log.error(threadTimeLog + messageLog);
                    softAssert.assertEquals("fail : " + messageLog, PASS_STATUS);
                    break;
                case ERROR_NEXT_STATUS:
                    log(LogStatus.ERROR, false, true, false, takeScreenShot, messageLog, messageLog + "<br>");
                    log.error(threadTimeLog + messageLog);
                    softAssert.assertEquals("error : " + messageLog, PASS_STATUS);
                    break;
                default:
                    log(LogStatus.INFO, false, false, false, false, messageLog, "");
                    log.info(threadTimeLog + messageLog);
                    break;
            }
        }
    }

    private void log(LogStatus status, boolean warn, boolean error, boolean errorStop, boolean takeScreenShot, String message, String errorString) {
        nbTestWarn += (warn?1:0);
        nbTestError += (error?1:0);
        nbTestErrorStop += (errorStop?1:0);
        strError += errorString + "\n";
        logger.log(status, message);
        takeSnapshot(takeScreenShot ? status : null);
    }

    /**
     * Logue une image dans le rapport.
     *
     * @param status Le statut.
     * @param imageBase64 L'image en base64.
     */
    public void logImage(LogStatus status, String imageBase64) {
        logger.log(status, logger.addBase64ScreenShot(DATA_IMAGE_FORMAT + imageBase64));
    }

    /**
     * Renvoie la page et la méthode à partir desquelles le log a été déclenché.
     *
     * @return La page et la méthode.
     */
    private String getPageMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = null;
        for (StackTraceElement stack : stackTraceElements) {
            if (stack.getClassName().contains(".pages.") || stack.getClassName().startsWith("pages.")) {
                stackTraceElement = stack;
                break;
            }
        }
        return stackTraceElement != null ? "<b>[" + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "]</b> " : "";
    }

    /**
     * Ajoute une capture d'écran.
     *
     * @param info L'information.
     */
    public void takeScreenShot(String info) {
        takeScreenShot(info, false);
    }

    /**
     * Ajoute une capture d'écran.
     *
     * @param info L'information.
     * @param highlightLastField Indique si le dernier champ doit être mis en surbrillance.
     */
    public void takeScreenShot(String info, boolean highlightLastField) {
        log(Reporter.INFO_STATUS, info);
        takeSnapshot(LogStatus.INFO, highlightLastField);
    }

    /**
     * Ajoute une capture d'écran.
     *
     * @param logStatus Le statut du log.
     */
    private void takeSnapshot(LogStatus logStatus) {
        takeSnapshot(logStatus, false);
    }

    /**
     * Ajoute une capture d'écran.
     *
     * @param logStatus Le statut du log.
     * @param highlightLastField Indique si le dernier champ doit être mis en surbrillance.
     */
    private void takeSnapshot(LogStatus logStatus, boolean highlightLastField) {
        if (logStatus != null) {
            try {
                boolean error = (logStatus.equals(LogStatus.ERROR) || logStatus.equals(LogStatus.FAIL));
                centerCurrentElement();

                // Ajout de la photo au rapport HTML
                if (error || highlightLastField) highlight(error ? "red" : "green");
                byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                String png = Base64.getEncoder().encodeToString(screenshot);

                logger.log(logStatus, logger.addBase64ScreenShot(DATA_IMAGE_FORMAT + png));
                // Post de la photo dans la Jira
                if (error) {
                    String screenshotname = (new Date().getTime()) + ".png";
                    File screenshotFile = new File(Paths.get("").toAbsolutePath() + File.separator + "target/" + screenshotname);
                    page.screenshot(new Page.ScreenshotOptions().setPath(screenshotFile.toPath()));
                }
            } catch (Exception e) {
                // Ignorer
            }
            removeHighlight();
        }
    }

    /**
     * Centre l'élément courant au milieu de la page.
     */
    private void centerCurrentElement() {
        if (currentElement != null) {
            try {
                (currentElement).evaluate("el => el.scrollIntoView({block: 'center'})");
            } catch (Exception ex) {
                // Ignorer
            }
        }
    }

    /**
     * Supprime la mise en surbrillance de l'élément en erreur.
     */
    private void removeHighlight() {
        try {
            this.page.evaluate("document.getElementById('reportseleniumhighlightdiv').replaceWith(...document.getElementById('reportseleniumhighlightdiv').childNodes)");
        } catch (Exception ignore) {
            // Ignorer
        }
    }

    /**
     * Met en surbrillance l'élément en erreur.
     *
     * @param color La couleur de la mise en surbrillance.
     */
    private void highlight(String color) {
        try {
            if (currentElement != null) {
                currentElement.evaluate("(el, color) => { " +
                        "var parent = el.parentNode;" +
                        "var reportseleniumhighlightdiv = document.createElement('div');" +
                        "reportseleniumhighlightdiv.setAttribute('id', 'reportseleniumhighlightdiv');" +
                        "reportseleniumhighlightdiv.style.border = '3px solid ' + color;" +
                        "parent.replaceChild(reportseleniumhighlightdiv, el);" +
                        "reportseleniumhighlightdiv.appendChild(el);" +
                        "}", color);
            }
        } catch (Exception ignore) {
            // Ignorer
        }
    }

    /**
     * Renvoie la stack d'une exception en string.
     *
     * @param e L'exception.
     * @return La stack de l'exception.
     */
    private String getExceptionStack(Exception e) {
        if (e == null) {
            return "";
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString().replace("\n", "<br>");
        }
    }

    /**
     * Concatène le message.
     *
     * @param action L'action.
     * @param elementName L'élément.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     * @param message Le message.
     * @return Le message concaténé.
     */
    public String concatMessage(String action, String elementName, String expected, String actual, String message) {
        String messageLog = action;
        messageLog += (elementName != null ? " sur '" + elementName + "'" : "");
        messageLog += (expected != null ? ", attendu: >" + expected + "< " : "");
        messageLog += (actual != null ? ", constaté: >" + actual + "< " : " ");
        messageLog += (message != null ? message : "");
        return messageLog;
    }

    /**
     * Indique si le précédent log date de plus de x secondes.
     *
     * @param second Le nombre de secondes.
     * @return Vrai si le précédent log date de plus de x secondes, faux sinon.
     */
    public boolean isTimePreviousLogOlderThan(int second) {
        return LocalDateTime.now().isAfter(timePreviousLog.plusSeconds(second));
    }

    /**
     * Copie les ressources de la common dans les projets si inexistantes.
     *
     * @param file Le fichier à copier.
     */
    private void copyCommonResource(String file) {
        File config = new File("target/test-classes/" + file);
        FileOutputStream fos = null;
        if (!config.exists()) {
            try {
                InputStream is = getClass().getResourceAsStream("/" + file);
                fos = new FileOutputStream(config);
                int readBytes;
                byte[] buffer = new byte[4096];
                while ((readBytes = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, readBytes);
                }
            } catch (IOException e) {
                log.error("copyCommonResource", e);
            } finally {
                try {
                    if (fos != null) fos.close();
                } catch (IOException e) {
                    // Ignorer
                }
            }
        }
    }

    /**
     * Renvoie un nom de test unique avec un numéro associé au nom du test initial pour ne pas écraser les rapports si le même test est exécuté plusieurs fois.
     *
     * @param test Le nom du test.
     * @return Le nom de test unique.
     */
    private static String getUniqueTestRunId(String test) {
        if (startedTests.contains(test)) {
            int i = 0;
            while (startedTests.contains(test + " #" + i)) {
                i++;
            }
            test = test + " #" + i;
        }
        startedTests.add(test);
        return test;
    }

    /**
     * Renvoie le nombre d'erreurs pour une suite de tests.
     *
     * @param suiteName Le nom de la suite de tests.
     * @return Le nombre d'erreurs.
     */
    private int getSuiteError(String suiteName) {
        if (suiteName != null && suiteError.containsKey(suiteName)) {
            return suiteError.get(suiteName);
        } else {
            suiteError.put(String.valueOf(suiteName), 0);
            return 0;
        }
    }

    /**
     * Renvoie le nombre d'avertissements pour une suite de tests.
     *
     * @param suiteName Le nom de la suite de tests.
     * @return Le nombre d'avertissements.
     */
    private int getSuiteWarning(String suiteName) {
        if (suiteName != null && suiteWarning.containsKey(suiteName)) {
            return suiteWarning.get(suiteName);
        } else {
            suiteWarning.put(String.valueOf(suiteName), 0);
            return 0;
        }
    }

    /**
     * Renvoie le résumé d'une suite de tests.
     *
     * @param suiteName Le nom de la suite de tests.
     * @return Le résumé de la suite de tests.
     */
    private String getSuiteSummary(String suiteName) {
        if (suiteName != null && suiteSummary.containsKey(suiteName)) {
            return suiteSummary.get(suiteName);
        } else {
            suiteSummary.put(String.valueOf(suiteName), "");
            return "";
        }
    }
}
