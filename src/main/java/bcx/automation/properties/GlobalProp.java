package bcx.automation.properties;

import bcx.automation.report.Reporter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Classe utilitaire pour gérer les propriétés globales de l'application.
 */
@Slf4j
public class GlobalProp {
    public static final String BROWSER = "browser";
    public static final String BROWSER_PRIVATE = "browserPrivate";
    public static final String BROWSER_WIDTH = "browserWidth";
    public static final String BROWSER_HEIGTH = "browserHeigth";
    public static final String HEADLESS = "headless";
    public static final String PROXY_HOST = "proxyHost";
    public static final String PROXY_PORT = "proxyPort";
    public static final String CLOSE_BROWSER_AFTER_METHOD = "closeBrowserAfterMethod";
    public static final String USE_LOADER = "useLoader";
    public static final String PAGE_LOAD_STRATEGY = "pageLoadStrategy";
    public static final String TIME_OUT = "timeOut";
    public static final String PAGE_LOAD_TIME_OUT = "pageLoadTimeOut";
    public static final String LOADER_TIME_OUT_MINUTE = "loaderTimeOutMinute";
    public static final String UNSAFELY_TREAT_INSECURE = "unsafelyTreatInsecure";
    public static final String TEST_FILE_FOLDER = "testFileFolder";
    public static final String VIDEO_FOLDER = "videoFolder";
    public static final String FORCE_STOP_ON_FAIL = "forceStopOnFail";
    public static final String SUITE_MAX_TIME = "suiteMaxTime";
    public static final String RETRY_ON_FAIL = "retryOnFail";
    public static final String FIND_POTENTIAL_ELEMENT_JS = "findPotentialElementJS";
    public static final String ATTRIBUTES_POTENTIAL_ELEMENT = "attributesPotentialElement";
    public static final String TEST_ID_ATTRIBUTE = "testIdAttribute";
    public static final String APPIUM_URL = "appiumUrl";
    public static final String RECORD_VIDEO = "recordVideo";

    @Setter
    @Getter
    private static String browser;
    @Setter
    @Getter
    private static boolean browserPrivate;
    @Setter
    @Getter
    private static int browserWidth;
    @Setter
    @Getter
    private static int browserHeigth;
    @Getter
    private static boolean headless;
    @Getter
    private static String proxyHost;
    @Getter
    private static String proxyPort;
    @Getter
    private static boolean closeBrowserAfterMethod;
    @Getter
    private static boolean useLoader;
    @Getter
    private static String pageLoadStrategy;
    @Getter
    private static int timeOut;
    @Setter
    @Getter
    private static int pageLoadTimeOut;
    @Getter
    private static int loaderTimeOutMinute;
    @Getter
    private static String unsafelyTreatInsecure;
    @Getter
    private static String testFileFolder;
    @Getter
    private static String recordVideo;
    @Getter
    private static String videoFolder;
    @Getter
    private static String secret;
    @Getter
    private static LocalDateTime startDateTimeSuite;
    @Getter
    private static int suiteMaxTime;
    @Getter
    private static boolean forceStopOnFail;
    @Getter
    private static boolean retryOnFail;
    @Getter
    private static boolean findPotentialElementJS;
    @Getter
    private static String testIdAttribute;
    @Getter
    private static String attributesPotentialElement;
    @Getter
    private static String appiumUrl;

    /**
     * Charge les propriétés globales à partir du fichier de configuration.
     */
    public static void load() {
        secret = System.getProperty("secret") != null ? System.getProperty("secret").substring(0, 16) : null;
        Path path = Paths.get("");

        startDateTimeSuite = LocalDateTime.now();

        final java.util.Properties prop = new java.util.Properties();
        try (InputStream input = new FileInputStream("target/test-classes/test.properties")) {
            prop.load(input);
            browser = System.getProperty(BROWSER) == null ? prop.getProperty(BROWSER) : System.getProperty(BROWSER);
            browserPrivate = prop.getProperty(BROWSER_PRIVATE) != null && Boolean.parseBoolean(prop.getProperty(BROWSER_PRIVATE));
            browserWidth = Integer.parseInt(prop.getProperty(BROWSER_WIDTH));
            browserHeigth = Integer.parseInt(prop.getProperty(BROWSER_HEIGTH));
            headless = Boolean.parseBoolean(prop.getProperty(HEADLESS));

            proxyHost = prop.getProperty(PROXY_HOST);
            proxyPort = prop.getProperty(PROXY_PORT);

            closeBrowserAfterMethod = Boolean.parseBoolean(prop.getProperty(CLOSE_BROWSER_AFTER_METHOD));
            useLoader = Boolean.parseBoolean(prop.getProperty(USE_LOADER));

            pageLoadStrategy = prop.getProperty(PAGE_LOAD_STRATEGY);
            timeOut = Integer.parseInt(prop.getProperty(TIME_OUT));
            pageLoadTimeOut = Integer.parseInt(prop.getProperty(PAGE_LOAD_TIME_OUT));
            loaderTimeOutMinute = Integer.parseInt(prop.getProperty(LOADER_TIME_OUT_MINUTE));

            unsafelyTreatInsecure = prop.getProperty(UNSAFELY_TREAT_INSECURE);

            testFileFolder = (path.toAbsolutePath() + File.separator + prop.getProperty(TEST_FILE_FOLDER)).replace("/", File.separator);
            recordVideo = prop.getProperty(RECORD_VIDEO);
            videoFolder = (path.toAbsolutePath() + File.separator + prop.getProperty(VIDEO_FOLDER)).replace("/", File.separator);

            suiteMaxTime = Integer.parseInt(prop.getProperty(SUITE_MAX_TIME));
            forceStopOnFail = Boolean.parseBoolean(prop.getProperty(FORCE_STOP_ON_FAIL));
            retryOnFail = Boolean.parseBoolean(prop.getProperty(RETRY_ON_FAIL));

            testIdAttribute = prop.getProperty(TEST_ID_ATTRIBUTE);
            findPotentialElementJS = Boolean.parseBoolean(prop.getProperty(FIND_POTENTIAL_ELEMENT_JS));
            attributesPotentialElement = prop.getProperty(ATTRIBUTES_POTENTIAL_ELEMENT);

            appiumUrl = prop.getProperty(APPIUM_URL);

            if (!new File(testFileFolder).exists()) {
                new File(testFileFolder).mkdir();
            }
        } catch (final IOException ex) {
            log.error("Exception lors du chargement des variables globales", ex);
        }
    }

    /**
     * Renvoie le timeout d'assertion en fonction du statut du rapport.
     *
     * @param report Le rapporteur.
     * @return Le timeout d'assertion.
     */
    public static int getAssertTimeOut(Reporter report) {
        if (report != null && !String.valueOf(report.getLastStatus()).equals(Reporter.PASS_STATUS)) {
            return 2;
        } else {
            return timeOut;
        }
    }

    /**
     * Renvoie la valeur d'une propriété spécifique à partir du fichier de configuration.
     *
     * @param key La clé de la propriété à récupérer.
     * @return La valeur de la propriété ou null si non trouvée.
     */
    public static String get(String key) {
        final java.util.Properties prop = new java.util.Properties();
        String value = null;
        try (InputStream input = new FileInputStream("target/test-classes/test.properties")) {
            prop.load(input);
            value = prop.getProperty(key);
        } catch (Exception e) {
            log.error("Exception lors du chargement de la valeur de la variable globale " + key, e);
        }
        return value;
    }

    /**
     * Vérifie si le temps maximum de la suite de tests est dépassé.
     *
     * @return Vrai si le temps maximum est dépassé, faux sinon.
     */
    public static boolean isSuiteOverTimeOut() {
        return startDateTimeSuite.plusMinutes(suiteMaxTime).isBefore(LocalDateTime.now());
    }

    /**
     * Définit la clé secrète.
     *
     * @param v La clé secrète.
     * @throws Exception Si la clé n'a pas la taille requise.
     */
    public static void setSecret(String v) throws Exception {
        if (v.length() >= 16) {
            secret = v.substring(0, 16);
        } else {
            throw new Exception("La taille de la clé doit être supérieure ou égale à 16");
        }
    }
}
