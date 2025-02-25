package bcx.playwright.test;

import bcx.playwright.properties.GlobalProp;
import bcx.playwright.report.Reporter;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestContext {

    @Getter
    @Setter
    private Browser browser;
    @Getter
    @Setter
    private BrowserContext browserContext;
    @Getter
    private Page page;
    @Getter
    @Setter
    private Reporter report;
    @Getter
    @Setter
    List<Page> otherTabs;
    @Getter
    @Setter
    List<TestContext> otherContexts;


    public TestContext() {
        this.browser = null;
        this.browserContext = null;
        this.page = null;
        this.report = null;
        this.otherTabs = new ArrayList<>();
        otherContexts = new ArrayList<>();
    }

    public void setPage(Page page) {
        this.page = page;
        this.report.setPage(page);
    }

    public void closeBrowser() {
        closeOtherTabs();
        closeOtherContexts();
        if (browser != null) {
            browser.close();
        }
        browser = null;
        browserContext = null;
        page = null;
    }


    public void closeOtherTabs() {
        for (Page page : otherTabs) {
            page.close();
        }
        otherTabs.clear();
    }

    public void closeOtherContexts() {
        for (TestContext context : otherContexts) {
            context.getBrowser().close();
        }
        otherContexts.clear();
    }

    public void addOtherTab(Page page) {
        otherTabs.add(page);
    }


    public Page newPage() {
        Page newPage = browserContext.newPage();
        otherTabs.add(newPage);
        return newPage;
    }

    public TestContext newTestContext() {
        TestContext newContext = new TestContext();
        newContext.setReport(report);
        newContext.startNewBrowser();
        otherContexts.add(newContext);
        return newContext;
    }


    /**
     * initialise le rapport détail du package
     * @param reportname
     */
    public void initReport(String reportname) {
        report = new Reporter(reportname);
    }

    /**
     * démarre un nouveau browser et l'enregistre sous un om String pour pouvoir faire des switch
     *
     */
    public void startNewBrowser() {
        startNewBrowser(null, null, null, null, null);
    }


    /**
     * démarre un nouveau browser et l'enregistre sous un om String pour pouvoir faire des switch
     *
     * @param proxy
     * @param locale langue par défaut du navigateur
     */
    public void startNewBrowser(String proxy, String locale, String browserType, String credentialUser, String credentialPassword) {
        Playwright playwright = Playwright.create();
        boolean browserOk = false;
        LocalDateTime now = LocalDateTime.now();
        while (!browserOk && now.plusSeconds(5).isAfter(LocalDateTime.now())) {
            try {
                browserType = browserType==null? GlobalProp.getBrowser():browserType;
                locale = locale==null?"fr-FR":locale;
                System.setProperty("LANG", locale);
                System.setProperty("LC_TIME", locale);

                List<String> args = getStrings(proxy);
                if (GlobalProp.getTestIdAttribute()!=null) {
                    playwright.selectors().setTestIdAttribute(GlobalProp.getTestIdAttribute());
                }
                BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                        .setHeadless(GlobalProp.isHeadless())
                        .setArgs(args);
                if (proxy != null) {
                    launchOptions.setProxy(new Proxy(proxy));
                }

                switch (String.valueOf(browserType)) {
                    case "FF":
                        browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setArgs(args).setHeadless(GlobalProp.isHeadless()));
                        break;
                    default: //chrome
                        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setArgs(args).setHeadless(GlobalProp.isHeadless()));
                        break;
                }

                Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                        .setAcceptDownloads(true) // Autoriser les téléchargements
                        .setViewportSize(GlobalProp.getBrowserWidth(), GlobalProp.getBrowserHeigth())
                        .setIgnoreHTTPSErrors(true)
                        .setLocale(locale); // Ignorer les erreurs de certificat

                if (!GlobalProp.getRecordVideo().equals("never")) {
                    contextOptions.setRecordVideoDir(Paths.get(GlobalProp.getVideoFolder()));
                }
                if (GlobalProp.isBrowserPrivate()) {
                    contextOptions.setStorageStatePath(Paths.get("")); // Mode incognito
                }

                browserContext = browser.newContext(credentialUser==null?contextOptions:contextOptions.setHttpCredentials(credentialUser, credentialPassword));
                browserContext.clearCookies();
                browserContext.setDefaultTimeout(Duration.ofSeconds(GlobalProp.getPageLoadTimeOut()).toMillis());
                browserContext.setDefaultNavigationTimeout(Duration.ofSeconds(GlobalProp.getPageLoadTimeOut()).toMillis());

                page = browserContext.newPage();

                browserOk = true;
            } catch (Exception e) {
                log.error("fail to start browser, retry", e);
            }
        }
    }

    private List<String> getStrings(String proxy) {
        List<String> args = new ArrayList<>();
        args.add("--disable-search-engine-choice-screen");
        args.add("--no-sandbox");
        args.add("--disable-dev-shm-usage");
        args.add("--disable-gpu");
        args.add("--unsafely-treat-insecure-origin-as-secure=" + GlobalProp.getUnsafelyTreatInsecure());
        if (proxy != null) args.add("--proxy-server=" + proxy);
        return args;
    }

    private void resetProxy() {
        if (GlobalProp.getProxyHost()!=null) {
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
        }
    }

    private void setProxy() {
        if (GlobalProp.getProxyHost()!=null) {
            System.setProperty("https.proxyHost", GlobalProp.getProxyHost());
            System.setProperty("https.proxyPort", GlobalProp.getProxyPort());
        }
    }
}
