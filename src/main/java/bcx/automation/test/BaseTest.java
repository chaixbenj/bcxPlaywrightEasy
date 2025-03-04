package bcx.automation.test;

import bcx.automation.properties.EnvProp;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import bcx.automation.properties.GlobalProp;
import bcx.automation.report.Reporter;
import bcx.automation.util.bdd.BDDUtil;
import com.microsoft.playwright.*;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe de base pour les tests utilisant Playwright et TestNG.
 */
@Slf4j
public class BaseTest {
    private static Map<String, TestContext> testContexts = new HashMap<>();
    private static Map<String, Reporter> reports = new HashMap<>();
    private static Map<String, Boolean> videoPaths = new HashMap<>();
    public TestContext testContext;

    /**
     * Méthode exécutée avant la suite de tests.
     * Charge les propriétés globales et d'environnement.
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeSuiteCommon() {
        try {
            log.info("Before suite***************************************" );
            GlobalProp.load();
            EnvProp.loadProperties(System.getProperty("env"));

            File file = new File("target/allure-results/environment.properties");
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write("OS=" + System.getProperty("os.name") + "\n");
            writer.write("Browser=" + GlobalProp.getBrowser() + "\n");
            writer.write("Environment=" + System.getProperty("env") + "\n");
            writer.close();

            log.info("END Before suite***************************************" );
        } catch (Exception e) {
            log.error("beforeSuiteCommon exception", e);
        }
    }

    /**
     * Méthode exécutée avant chaque test.
     * Initialise le contexte de test et le rapport.
     *
     * @param iTestContext Le contexte du test.
     */
    @BeforeTest(alwaysRun = true)
    public void beforeTestCommon(final ITestContext iTestContext) {
        log.info(Thread.currentThread() + " Before test***************************************" + iTestContext.getName());
        if (testContexts.containsKey(getThreadId())) {
            testContext = testContexts.get(getThreadId());
        } else {
            testContext = new TestContext();
            testContexts.put(getThreadId(), testContext);
        }
        testContext.initReport();
        reports.put(getThreadId(), testContext.getReport());
        log.info(Thread.currentThread() + " END Before test***************************************" + iTestContext.getName());
    }

    /**
     * Méthode exécutée avant chaque méthode de test.
     * Initialise la page et le rapport pour la méthode de test.
     *
     * @param iTestContext Le contexte du test.
     * @param method La méthode de test.
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeMethodCommon(ITestContext iTestContext, Method method) {
        String testName = (method.getDeclaringClass().getSimpleName() + "." + method.getName()).replace(".run", "");
        log.info(Thread.currentThread() + " Before method***************************************" + testName);

        if (testContexts.containsKey(getThreadId())) {
            testContext = testContexts.get(getThreadId());
        } else {
            testContext = new TestContext();
            testContexts.put(getThreadId(), testContext);
            testContext.setReport(reports.get(getThreadId()));
        }
        if (testContext.getPage() == null) testContext.startNewBrowser();

        Reporter report = testContext.getReport();
        report.setPage(testContext.getPage());
        report.initTest();
        if (GlobalProp.isSuiteOverTimeOut()) report.log(Reporter.SKIP_STATUS, "Timeout : la suite a dépassé le temps maximum prévu");
        log.info(Thread.currentThread() + " END Before method***************************************" + testName);
    }

    /**
     * Méthode exécutée après chaque méthode de test.
     * Gère la fermeture du navigateur et la suppression des vidéos enregistrées.
     *
     * @param result Le résultat du test.
     * @param method La méthode de test.
     */
    @AfterMethod(alwaysRun = true)
    public void afterMethodCommon(ITestResult result, Method method) {
        String testName = (method.getDeclaringClass().getSimpleName() + "." + method.getName()).replace(".run", "");
        log.info(Thread.currentThread() + " After method***************************************" + testName);
        if (GlobalProp.isCloseBrowserAfterMethod()) testContext.closeBrowser();
        log.info(Thread.currentThread() + " END After method***************************************" + testName);
    }

    /**
     * Méthode exécutée après chaque test.
     * Ferme le navigateur et supprime les vidéos enregistrées.
     *
     * @param iTestContext Le contexte du test.
     */
    @AfterTest(alwaysRun = true)
    public void afterTestCommon(ITestContext iTestContext) {
        log.info(Thread.currentThread() + " After test***************************************" + iTestContext.getName());
        testContext.closeBrowser();
        testContexts.remove(getThreadId());
        for (Map.Entry<String, Boolean> videoPath : videoPaths.entrySet()) {
            try {
                if (Boolean.TRUE.equals(videoPath.getValue())) {
                    Files.deleteIfExists(Paths.get(videoPath.getKey()));
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
        log.info(Thread.currentThread() + " END After test***************************************" + iTestContext.getName());
    }

    /**
     * Méthode exécutée après la suite de tests.
     * Ferme tous les browser/driver, ferme les connexions à la base de données.
     *
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuiteCommon() {
        log.info(LocalDateTime.now() + " " + Thread.currentThread() + " After suite***************************************");
        testContext.closeBrowser();
        BDDUtil.deconnecterDB();
        log.info(LocalDateTime.now() + " " + Thread.currentThread() + " END After suite***************************************");
    }

    /**
     * Renvoie le rapporteur associé au contexte de test.
     *
     * @return Le rapporteur.
     */
    public Reporter getReport() {
        return testContext.getReport();
    }

    /**
     * à appeler à la fin de chaque test : publie les vidéo et vérifie les softAssert
     */
    public void endTest() {
        Page page = testContext.getPage();
        Reporter report = testContext.getReport();
        Path videoPath = page.video().path();
        if (GlobalProp.getRecordVideo().equals("always") || (report.isInError() && GlobalProp.getRecordVideo().equals("onFailure"))) {
            report.attachVideoToAllure(videoPath.toString());
            videoPaths.put(videoPath.toString(), false);
        } else {
            if (videoPath != null && !videoPaths.containsKey(videoPath.toString())) {
                videoPaths.put(videoPath.toString(), true);
            }
        }
        if (GlobalProp.isCloseBrowserAfterMethod()) {
            testContext.closeBrowser();
            testContexts.remove(getThreadId());
            if (videoPath != null) {
                try {
                    Files.deleteIfExists(videoPath);
                    videoPaths.remove(videoPath.toString());
                } catch (Exception ignore) {
                    // ignore
                }
            }
        }
        report.softAssertAll();
    }

    /**
     * Renvoie l'identifiant du thread courant.
     *
     * @return L'identifiant du thread courant.
     */
    private static String getThreadId() {
        return Thread.currentThread().toString();
    }

}
