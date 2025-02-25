package bcx.playwright.test;

import bcx.playwright.properties.EnvProp;
import lombok.extern.slf4j.Slf4j;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import bcx.playwright.properties.GlobalProp;
import bcx.playwright.report.Reporter;
import bcx.playwright.util.bdd.BDDUtil;
import bcx.playwright.report.HtmlReportToPdf;
import java.nio.file.Files;
import java.nio.file.Path;
import com.microsoft.playwright.*;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class BaseTest {
    private static Map<String, TestContext> testContexts = new HashMap<>();
    private static Map<String, Reporter> reports = new HashMap<>();
    private static Map<String, Boolean> videoPaths = new HashMap<>();
    public TestContext testContext;
    
    @BeforeSuite(alwaysRun=true)
    public  void beforeSuiteCommon(ITestContext tc) {
        try {
            log.info("Before suite***************************************" + tc.getName());
            GlobalProp.load();
            EnvProp.loadProperties((System.getProperty("env")));
            log.info("END Before suite***************************************" + tc.getName());
        } catch (Exception e) {
            log.error("beforeSuiteCommon exception", e);
        }
    }

    @BeforeTest(alwaysRun=true)
    public void beforeTestCommon(final ITestContext iTestContext) {
        log.info(Thread.currentThread() + " Before test***************************************" + iTestContext.getName());
        if (testContexts.containsKey(getThreadId())) {
            testContext = testContexts.get(getThreadId());
        } else {
            testContext = new TestContext();
            testContexts.put(getThreadId(), testContext);
        }
        testContext.initReport(iTestContext.getName());
        reports.put(getThreadId(), testContext.getReport());
        log.info(Thread.currentThread() + " END Before test***************************************" + iTestContext.getName());
    }

    @BeforeMethod(alwaysRun=true)
    public void beforeMethodCommon(ITestContext iTestContext, Method method) {
        String testName = (method.getDeclaringClass().getSimpleName() + "." +method.getName()).replace(".run", "");
        log.info(Thread.currentThread() + " Before method***************************************" + testName);

        if (testContexts.containsKey(getThreadId())) {
            testContext = testContexts.get(getThreadId());
        } else {
            testContext = new TestContext();
            testContexts.put(getThreadId(), testContext);
            testContext.setReport(reports.get(getThreadId()));
        }
        if (testContext.getPage()==null) testContext.startNewBrowser();

        Reporter report = testContext.getReport();
        report.setPage(testContext.getPage());
        report.initTest(testName);
        if (GlobalProp.isSuiteOverTimeOut()) report.log(Reporter.SKIP_STATUS, "Timeout : la suite à dépassé le temps maximum prévu");
        log.info(Thread.currentThread() + " END Before method***************************************" + testName);
    }

    @AfterMethod(alwaysRun=true)
    public void afterMethodCommon(ITestResult result, Method method)
    {
        String testName = (method.getDeclaringClass().getSimpleName() + "." +method.getName()).replace(".run", "");
        log.info(Thread.currentThread() + " After method***************************************" + testName);
        Page page = testContext.getPage();
        Reporter report = testContext.getReport();
        Path videoPath = page.video().path();
        if (GlobalProp.getRecordVideo().equals("always") || ((result.getStatus() != ITestResult.SUCCESS || report.getNbTestError() != 0) && GlobalProp.getRecordVideo().equals("onFailure"))) {
            report.log("<video width=\"640\" height=\"360\" controls>\n" +
                    "    <source src=\"" + GlobalProp.getVideoFolder() + videoPath.getFileName() + "\" type=\"video/mp4\">\n" +
                    "    Votre navigateur ne supporte pas la lecture des vidéos.\n" +
                    "</video>");
            videoPaths.put(videoPath.toString(), false);
        } else {
            if (videoPath!=null && !videoPaths.containsKey(videoPath.toString())) {
                videoPaths.put(videoPath.toString(), true);
            }
        }

        if (GlobalProp.isCloseBrowserAfterMethod()) {
            testContext.closeBrowser();
            testContexts.remove(getThreadId());
            if (videoPath!=null) {
                try {
                    Files.deleteIfExists(videoPath);
                    videoPaths.remove(videoPath.toString());
                } catch (Exception ignore) {
                    // ignore
                }
            }
        }

        report.endTest(result.isSuccess());

        log.info(Thread.currentThread() + " END After method***************************************" + testName);
    }

    @AfterTest(alwaysRun=true)
    public void afterTestCommon(ITestContext iTestContext)
    {
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

    @AfterSuite(alwaysRun=true)
    public void afterSuiteCommon(ITestContext iTestContext) {
        log.info(LocalDateTime.now() + " " + Thread.currentThread() + " After suite***************************************" + iTestContext.getName());
        new Reporter().publishGlobalReport();
        testContext.closeBrowser();
        BDDUtil.deconnecterDB();
        log.info(LocalDateTime.now() + " " +Thread.currentThread() + " END After suite***************************************" + iTestContext.getName());
        if (GlobalProp.isPdfReport()) {
            HtmlReportToPdf.generate();
        }
    }

    public Reporter getReport() {
        return testContext.getReport();
    }

    private static String getThreadId() {
        return Thread.currentThread().toString();
    }

}
