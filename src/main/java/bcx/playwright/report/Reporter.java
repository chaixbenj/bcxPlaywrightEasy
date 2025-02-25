package bcx.playwright.report;

import bcx.playwright.page.element.BaseElement;
import bcx.playwright.properties.EnvProp;
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
import bcx.playwright.properties.GlobalProp;
import bcx.playwright.util.data.DataUtil;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
     * -- SETTER --
     *  enregistre l'élément courant
     *
     * @param element
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


    public Reporter() {
        new Reporter(null);
    }

    public Reporter(String suiteName) {
        copyCommonResource("extent-config.xml");
        if (suiteName!=null) {
            currentSuite = suiteName;
            extent = new ExtentReports (GlobalProp.getReportFolder() + suiteName + ".html", true);
            suiteError.put(suiteName, 0);
            suiteWarning.put(suiteName, 0);
            suiteSummary.put(suiteName, "");
            extent.addSystemInfo(ENV, EnvProp.getEnvironnement());
            //loading the external xml file (i.e., extent-config.xml) which was placed under the base directory
            //You could find the xml file below. Create xml file in your project and copy past the code mentioned below
            extent.loadConfig(new File(Paths.get("").toAbsolutePath() + EXTENT_CONFIG_XML_PATH));
        }
        nbTestError = 0;
        nbTestErrorStop = 0;
        strError = "";
        nbTestWarn = 0;
        testSkipped = false;
    }


    public void publishGlobalReport() {
        try {
            if (!endedSuites.isEmpty()) {
                extent = new ExtentReports( INDEX_TEST_HTML_PATH, false);
                extent.addSystemInfo(ENV, EnvProp.getEnvironnement());
                extent.loadConfig(new File(Paths.get("").toAbsolutePath() + EXTENT_CONFIG_XML_PATH));
                for (String suiteName :
                        endedSuites) {
                    publishGlobalReportItem(suiteName);
                }
                extent.flush();
            }
        } catch (Exception ex) {log.info("publishGlobalReport exception", ex);}
        endedSuites.clear();
        log.info(LocalDateTime.now() + " -------------- END publishGlobalReport");
    }

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
            log.info(LocalDateTime.now() + " -------------publishGlobalReportItem----------------Exception", e) ;
        }
    }

    public void publish() {
        extent.flush();
    }

    public void initTest(String test) {
        String testUId = getUniqueTestRunId(test);
        if (extent==null) {
            extent = new ExtentReports (GlobalProp.getReportFolder() + currentSuite + ".html", true);
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

    public int endTest(boolean isSuccess) {
        return endTest(isSuccess, false);
    }

    public int endTest(boolean isSuccess, boolean catchFail) {
        nbTestEnd += 1;
        if (nbTestError==0) nbTestError=isSuccess||testSkipped?0:1;
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
        logger.log(status,
                "[TEST END] <b>" + nbTestError + " erreurs " + nbTestWarn + " warnings \n" + strError + "</b>");

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

    public int countEndedTest() {
        return nbTestEnd;
    }

    /**
     * vérifie l'égalité entre 2 entiers et logue le résultat dans le rapport
     * @param expected
     * @param actual
     */
    public void assertEquals(int expected, int actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * vérifie l'égalité entre 2 entiers et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param expected
     * @param actual
     */
    public void assertEquals(String info, int expected, int actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * vérifie l'égalité entre 2 booléens et logue le résultat dans le rapport
     * @param expected
     * @param actual
     */
    public void assertEquals(boolean expected, boolean actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * vérifie l'égalité entre 2 booléens et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param expected
     * @param actual
     */
    public void assertEquals(String info, boolean expected, boolean actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * vérifie l'égalité entre 2 doubles et logue le résultat dans le rapport
     * @param expected
     * @param actual
     */
    public void assertEquals(Double expected, Double actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * vérifie l'égalité entre 2 doubles et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param expected
     * @param actual
     */
    public void assertEquals(String info, Double expected, Double actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * vérifie l'égalité entre 2 strings et logue le résultat dans le rapport
     * @param expected
     * @param actual
     */
    public void assertEquals(String expected, String actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * vérifie l'égalité entre 2 strings et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param expected
     * @param actual
     */
    public void assertEquals(String info, String expected, String actual) {
        String status = DataUtil.equalsIgnoreIgnoredString(this, expected, actual, true);
        log(status, (info!=null?info + " ":"") + "assertEquals", null, expected, status.equals(PASS_STATUS)?null:actual, null);
    }

    /**
     * vérifie l'égalité entre 2 strings et logue le résultat dans le rapport avec en plus une petite "info" d'explication, stop le test
     * @param info
     * @param expected
     * @param actual
     */
    public void assertEqualsStop(String info, String expected, String actual) {
        String status = DataUtil.equalsIgnoreIgnoredString(this, expected, actual, true);
        status = (status.equals(PASS_STATUS) ||
                status.equals(WARNING_STATUS)? PASS_STATUS : FAIL_STATUS);
        log(status, (info!=null?info + " ":"") + "assertEquals", null, expected, status.equals(PASS_STATUS)?null:actual, null);
    }

    /**
     * vérifie que la première chaine en paramètre contient la seconde et logue le résultat dans le rapport
     * @param contenant
     * @param contenu
     */
    public void assertContains(String contenant, String contenu) {
        assertContains(null, contenant, contenu);
    }

    /**
     * vérifie que la première chaine en paramètre contient la seconde et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param contenant
     * @param contenu
     */
    public void assertContains(String info, String contenant, String contenu) {
        String status = contenant.contains(contenu)?PASS_STATUS:FAIL_NEXT_STATUS;
        log(status, (info!=null?info + " ":"") + "assertContains", null, contenant, status.equals(PASS_STATUS)?null:contenu, null);
    }

    /**
     * vérifie que la première chaine en paramètre ne contient pas la seconde et logue le résultat dans le rapport
     * @param contenant
     * @param contenu
     */
    public void assertNotContains(String contenant, String contenu) {
        assertNotContains(null, contenant, contenu);
    }

    /**
     * vérifie que la première chaine en paramètre ne contient pas la seconde et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param contenant
     * @param contenu
     */
    public void assertNotContains(String info, String contenant, String contenu) {
        String status = contenant.contains(contenu)?FAIL_NEXT_STATUS:PASS_STATUS;
        log(status, (info!=null?info + " ":"") + "assertContains", null, contenant, status.equals(PASS_STATUS)?null:contenu, null);
    }
    /**
     * vérifie l'innégalité entre 2 entiers et logue le résultat dans le rapport
     * @param expected
     * @param actual
     */
    public void assertNotEquals(int expected, int actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * vérifie l'innégalité entre 2 entiers et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param expected
     * @param actual
     */
    public void assertNotEquals(String info, int expected, int actual) {
        assertNotEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * vérifie l'inégalité entre 2 booléens et logue le résultat dans le rapport
     * @param expected
     * @param actual
     */
    public void assertNotEquals(boolean expected, boolean actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * vérifie l'innégalité entre 2 booléens et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param expected
     * @param actual
     */
    public void assertNotEquals(String info, boolean expected, boolean actual) {
        assertNotEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * vérifie l'innégalité entre 2 doubles et logue le résultat dans le rapport
     * @param expected
     * @param actual
     */
    public void assertNotEquals(Double expected, Double actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * vérifie l'innégalité entre 2 doubles et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param expected
     * @param actual
     */
    public void assertNotEquals(String info, Double expected, Double actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * vérifie l'innégalité entre 2 strings et logue le résultat dans le rapport
     * @param expected
     * @param actual
     */
    public void assertNotEquals(String expected, String actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * vérifie l'innégalité entre 2 strings et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param expected
     * @param actual
     */
    public void assertNotEquals(String info, String expected, String actual) {
        String status = (expected.equals(actual) ? FAIL_NEXT_STATUS : PASS_STATUS);
        log(status, (info!=null?info + " ":"") + "assertNotEquals", null, expected, status.equals(PASS_STATUS)?null:actual, null);
    }

    /**
     * vérifie q'un int est supérieurs à un autre et logue le résultat dans le rapport
     * @param num1
     * @param num2
     */
    public void assertGreater(int num1, int num2) {
        assertGreater(null, num1, num2);
    }

    /**
     * vérifie q'un int est supérieurs à un autre et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param num1
     * @param num2
     */
    public void assertGreater(String info, int num1, int num2) {
        assertGreater(info, Double.valueOf(num1), Double.valueOf(num2));
    }

    /**
     * vérifie q'un double est supérieurs à un autre et logue le résultat dans le rapport
     * @param num1
     * @param num2
     */
    public void assertGreater(Double num1, Double num2) {
        assertGreater(null, num1, num2);
    }

    /**
     * vérifie q'un double est supérieurs à un autre et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param num1
     * @param num2
     */
    public void assertGreater(String info, Double num1, Double num2) {
        String status = (num1>num2? PASS_STATUS : FAIL_NEXT_STATUS);
        log(status, (info!=null?info + " ":"") + "assertGreater", null, String.valueOf(num1), String.valueOf(num2), null);
    }

    /**
     * vérifie q'un double est inférieur ou égal à un autre et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param num1
     * @param num2
     */
    public void assertSmallerOrEqual(String info, Double num1, Double num2) {
        String status = (num1<=num2? PASS_STATUS : FAIL_NEXT_STATUS);
        log(status, (info!=null?info + " ":"") + "assertSmallerOrEqual", null, String.valueOf(num1), String.valueOf(num2), null);
    }


    /**
     * vérifie q'une String n'est pas null et logue le résultat dans le rapport
     * @param actual
     */
    public void assertNotNull(String actual) {
        assertNotNull(null, actual);
    }

    /**
     * vérifie q'une String n'est pas null et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     * @param info
     * @param actual
     */
    public void assertNotNull(String info, String actual) {
        String status = (actual!=null? PASS_STATUS : FAIL_NEXT_STATUS);
        log(status, (info!=null?info + " ":"") + "assertNotNull", null, null, actual, null);
    }

    /**
     * vérifie qu'une date à u format donnée est égale à la date courant dans un intervalle de + ou - N minutes et logue le résultat dans le rapport
     *
     * @param date
     * @param format
     * @param minuteEcartAcceptable
     */
    public void assertDateEqualsLocalDateTime(String date, String format, int minuteEcartAcceptable) {
        assertDateEqualsLocalDateTime(null, date, format, minuteEcartAcceptable);
    }

    /**
     * vérifie qu'une date à u format donnée est égale à la date courant dans un intervalle de + ou - N minutes et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     *
     * @param date
     * @param format
     * @param minuteEcartAcceptable
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
        log(status, (info!=null?info + " ":"") + "assertDateEqualsLocalDateTime", null, now + " +/- " + minuteEcartAcceptable + " minutes" , String.valueOf(date), message);
    }

    /**
     * vérifie qu'une date à un format donnée est antérieure à la date courante - N minutes et logue le résultat dans le rapport avec en plus une petite "info" d'explication
     *
     * @param date
     * @param format
     * @param minuteARetirer
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
        log(status, (info!=null?info + " ":"") + "assertDateLowerThanLocalDateTime", null, now + " - " + minuteARetirer + " minutes" , String.valueOf(date), message);
    }

    /**
     * logue un messag de type "info" dans le rapport
     * @param messageLog
     */
    public void title(String messageLog) {
        log(Reporter.INFO_STATUS, "<h5>"+messageLog+"</h5>");
    }



    /**
     * logue un messag de type "info" dans le rapport
     * @param messageLog
     */
    public void log(String messageLog) {
        log(Reporter.INFO_STATUS, messageLog);
    }

    /**
     * logue une info de type "status" dans le rapport. Les status failStatus et errorStatus mette fin au test en cours contrairement au failNextStatus et errorNextStatus. Un screnshot est pris.
     * cette méthode est appelé par défaut dans toutes les méthodes d'action et d'assertion de la classe BaseElement.
     * @param status
     * @param action
     * @param element
     * @param expected
     * @param actual
     * @param message
     */
    public void log(String status, String action, BaseElement element, String expected, String actual, String message) {
        log(status, concatMessage(action, element, expected, actual, message));
    }

    /**
     * logue une info de type "status" dans le rapport. Les status failStatus et errorStatus mette fin au test en cours contrairement au failNextStatus et errorNextStatus. Un screnshot est pris.
     * cette méthode est appelé par défaut dans toutes les méthodes d'action et d'assertion de la classe BaseElement.
     * @param status
     * @param action
     * @param element
     * @param expected
     * @param actual
     * @param e
     */
    public void log(String status, String action, BaseElement element, String expected, String actual, String message, Exception e) {
        log(status, concatMessage(action, element, expected, actual, message), e);
    }
    /**
     * logue une info de type "status" dans le rapport. Les status failStatus et errorStatus mette fin au test en cours contrairement au failNextStatus et errorNextStatus. Un screnshot est pris.
     * cette méthode est appelé par défaut dans toutes les méthodes d'action et d'assertion de la classe BaseElement.
     * @param status
     * @param messageLog
     * @param e
     */
    public void log(String status, String messageLog, Exception e) {
        String stackTrace = getExceptionStack(e);
        if (e != null && stackTrace.contains("Error communicating with the remote browser. It may have died.")) {
            status = WARNING_STATUS_NO_SCREENSHOT;
        }
        log(status, messageLog + "\n" + getExceptionStack(e));
    }



    /**
     * logue une info de type "status" dans le rapport. Les status failStatus et errorStatus mette fin au test en cours contrairement au failNextStatus et errorNextStatus. Un screnshot est pris.
     * cette méthode est appelé par défaut dans toutes les méthodes d'action et d'assertion de la classe BaseElement.
     * @param status
     */
    public void log(String status, Exception e) {
        log(status, getExceptionStack(e));
    }

    /**
     * logue une info de type "status" dans le rapport. Les status failStatus et errorStatus mette fin au test en cours contrairement au failNextStatus et errorNextStatus. Un screnshot est pris.
     * cette méthode est appelé par défaut dans toutes les méthodes d'action et d'assertion de la classe BaseElement.
     * @param status
     * @param messageLog
     */
    public void log(String status, String messageLog) {
        log(status, messageLog, true);
    }

    /**
     * logue une info de type "status" dans le rapport. Les status failStatus et errorStatus mette fin au test en cours contrairement au failNextStatus et errorNextStatus. Un screnshot est pris.
     * cette méthode est appelé par défaut dans toutes les méthodes d'action et d'assertion de la classe BaseElement.
     * @param status
     * @param messageLog
     */
    public void log(String status, String messageLog, boolean takeScreenShot) {
        String threadTimeLog = "[" + Thread.currentThread() + " - " + (LocalDateTime.now()).format(DateTimeFormatter.ofPattern("hh:mm:ss")) + "] ";
        messageLog = (messageLog==null?"":messageLog);
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
        if (previousLog !=null && !previousLog.equals(currentLog)) {
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

    public void logImage(LogStatus status, String imageBase64) {
        logger.log(status, logger.addBase64ScreenShot(DATA_IMAGE_FORMAT + imageBase64));
    }

    /**
     * renvoi la page + la méthode à partir desquelles le log à été déclenché
     * @return
     */
    private String getPageMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = null;
        for (StackTraceElement stack:stackTraceElements
        ) {
            if (stack.getClassName().contains(".pages.") || stack.getClassName().startsWith("pages.")) {
                stackTraceElement=stack;
                break;
            }
        }
        return stackTraceElement!=null?"<b>[" + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "]</b> ":"";
    }

    /**
     * ajoute un screenshot
     * @param info
     */
    public void takeScreenShot(String info) {
        takeScreenShot(info, false);
    }

    /**
     * ajoute un screenshot
     * @param info
     * @param highligthLastField 
     */
    public void takeScreenShot(String info, boolean highligthLastField) {
        log(Reporter.INFO_STATUS, info);
        takeSnapshot(LogStatus.INFO, highligthLastField);
    }

    /**
     * ajoute un screenshot
     * @param logStatus
     */
    private void takeSnapshot(LogStatus logStatus) {
        takeSnapshot(logStatus, false);
    }

    /**
     * ajoute un screenshot
     * @param logStatus
     * @param highligthLastField
     */
    private void takeSnapshot(LogStatus logStatus, boolean highligthLastField) {
        if (logStatus != null) {
            try {
                boolean error = (logStatus.equals(LogStatus.ERROR) || logStatus.equals(LogStatus.FAIL));
                centerCurrentElement();

                //ajout de la photo au rapport html
                if (error || highligthLastField) highlight(error?"red":"green");
                byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                String png = Base64.getEncoder().encodeToString(screenshot);

                logger.log(logStatus, logger.addBase64ScreenShot(DATA_IMAGE_FORMAT + png));
                //post de la photo dans la jira
                if (error) {
                    String screenshotname = (new Date().getTime()) + ".png";
                    File screenshotFile = new File(Paths.get("").toAbsolutePath() + File.separator + "target/" + screenshotname);
                    page.screenshot(new Page.ScreenshotOptions().setPath(screenshotFile.toPath()));
                }
            } catch (Exception e) {
                // on ignore
            }
            removeHighlight();
        }
    }

    /**
     * positionne l'élément au milieu de la page
     */
    private void centerCurrentElement() {
        if (currentElement!=null) {
            try {
                ((ElementHandle)currentElement).evaluate("el => el.scrollIntoView({block: 'center'})");
            } catch (Exception ex) {
                // on s'en moque on scroll pas
            }
        }
    }

    /**
     * supprime l'highlight l'élément en erreur
     */
    private void removeHighlight() {
        try {
            this.page.evaluate("document.getElementById('reportseleniumhighlightdiv').replaceWith(...document.getElementById('reportseleniumhighlightdiv').childNodes)");
        } catch (Exception ignore) {
            // ignore
        }
    }

    /**
     * highlight l'élément en erreur
     * @return
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
            // ignore
        }
    }

    /**
     * renvoie la stack d'une exception en string
     * @param e
     * @return
     */
    private String getExceptionStack(Exception e) {
        if (e==null) {
            return "";
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString().replace("\n", "<br>");
        }
    }
    /**
     * concatene le message
     * @param action
     * @param element
     * @param expected
     * @param actual
     * @return
     */
    public String concatMessage(String action, BaseElement element, String expected, String actual, String message) {
        String messageLog = action;
        String elementDescription = null;
        if (element!=null) {
            elementDescription = element.getName();
        }
        messageLog += (elementDescription != null ? " sur '" + elementDescription + "'" : "");
        messageLog += (expected != null ? ", attendu: >" + expected + "< " : "");
        messageLog += (actual != null ? ", constaté: >" + actual + "< " : " ");
        messageLog += (message!=null?message:"");
        return messageLog;
    }

    /**
     * indique si la précédente log date de plus de x secondes
     * @param second
     * @return
     */
    public boolean isTimePreviousLogOlderThan(int second) {
        return LocalDateTime.now().isAfter(timePreviousLog.plusSeconds(second));
    }


    /**
     * copie les ressources de la common dans les projets si inexistant
     * @param file
     */
    private void copyCommonResource(String file) {
        File config = new File("target/test-classes/"+file);
        FileOutputStream fos = null;
        if (!config.exists()) {
            try {
                InputStream is = getClass().getResourceAsStream("/"+file);
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
                    if (fos!=null)fos.close();
                } catch (IOException e) {
                    // on passe
                }
            }
        }
    }

    /**
     * renvoi un nom de test unique avec un numéro associé au nom du test initial pour pas écraser les rapports si y'a plusieurs fois le même test
     * @param test
     * @return
     */
    private static String getUniqueTestRunId(String test) {
        if (startedTests.contains(test)) {
            int i = 0;
            while (startedTests.contains(test + " #" + i)) {
                i++;
            }
            test = test  + " #" + i;
        }
        startedTests.add(test);
        return test;
    }

    private int getSuiteError(String suiteName) {
        if (suiteName!=null && suiteError.containsKey(suiteName)) {
            return suiteError.get(suiteName);
        } else {
            suiteError.put(String.valueOf(suiteName), 0);
            return 0;
        }
    }

    private int getSuiteWarning(String suiteName) {
        if (suiteName!=null && suiteWarning.containsKey(suiteName)) {
            return suiteWarning.get(suiteName);
        } else {
            suiteWarning.put(String.valueOf(suiteName), 0);
            return 0;
        }
    }

    private String getSuiteSummary(String suiteName) {
        if (suiteName!=null && suiteSummary.containsKey(suiteName)) {
            return suiteSummary.get(suiteName);
        } else {
            suiteSummary.put(String.valueOf(suiteName), "");
            return "";
        }
    }


}
