package bcx.automation.test;

import bcx.automation.properties.GlobalProp;
import bcx.automation.report.Reporter;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;
import io.appium.java_client.AppiumDriver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de contexte de test pour gérer les instances de navigateur et les pages.
 */
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
    private List<Page> otherTabs;
    @Getter
    @Setter
    private List<TestContext> otherContexts;
    @Getter
    @Setter
    private AppiumDriver appiumDriver;

    /**
     * Constructeur par défaut de la classe TestContext.
     */
    public TestContext() {
        this.browser = null;
        this.browserContext = null;
        this.page = null;
        this.report = null;
        this.otherTabs = new ArrayList<>();
        this.otherContexts = new ArrayList<>();
        this.appiumDriver = null;
    }

    /**
     * Définit la page actuelle.
     *
     * @param page La page à définir.
     */
    public void setPage(Page page) {
        this.page = page;
        this.report.setPage(page);
    }

    /**
     * Ferme le navigateur et tous les onglets associés.
     */
    public void closeBrowser() {
        closeOtherTabs();
        closeOtherContexts();
        if (browser != null) {
            browser.close();
        }
        browser = null;
        browserContext = null;
        page = null;
        if (appiumDriver != null) {
            appiumDriver.quit();
            appiumDriver = null;
        }
    }

    /**
     * Ferme tous les onglets autres que l'onglet principal.
     */
    public void closeOtherTabs() {
        for (Page page : otherTabs) {
            page.close();
        }
        otherTabs.clear();
    }

    /**
     * Ferme tous les contextes de test autres que le contexte principal.
     */
    public void closeOtherContexts() {
        for (TestContext context : otherContexts) {
            context.getBrowser().close();
        }
        otherContexts.clear();
    }

    /**
     * Ajoute un nouvel onglet à la liste des onglets.
     *
     * @param page L'onglet à ajouter.
     */
    public void addOtherTab(Page page) {
        otherTabs.add(page);
    }

    /**
     * Crée une nouvelle page dans le contexte de navigateur actuel.
     *
     * @return La nouvelle page créée.
     */
    public Page newPage() {
        Page newPage = browserContext.newPage();
        otherTabs.add(newPage);
        return newPage;
    }

    /**
     * Crée un nouveau contexte de test.
     *
     * @return Le nouveau contexte de test créé.
     */
    public TestContext newTestContext() {
        TestContext newContext = new TestContext();
        newContext.setReport(report);
        newContext.startNewBrowser();
        otherContexts.add(newContext);
        return newContext;
    }

    /**
     * Initialise le rapport de test.
     */
    public void initReport() {
        report = new Reporter();
    }

    /**
     * Démarre un nouveau navigateur avec les paramètres par défaut.
     */
    public void startNewBrowser() {
        startNewBrowser(null, null, null, null, null);
    }

    /**
     * Démarre un nouveau navigateur avec des paramètres spécifiques.
     *
     * @param proxy Le proxy à utiliser.
     * @param locale La langue par défaut du navigateur.
     * @param browserType Le type de navigateur.
     * @param credentialUser Le nom d'utilisateur pour les identifiants HTTP.
     * @param credentialPassword Le mot de passe pour les identifiants HTTP.
     */
    public void startNewBrowser(String proxy, String locale, String browserType, String credentialUser, String credentialPassword) {
        Playwright playwright = Playwright.create();
        boolean browserOk = false;
        LocalDateTime now = LocalDateTime.now();
        while (!browserOk && now.plusSeconds(5).isAfter(LocalDateTime.now())) {
            try {
                browserType = browserType == null ? GlobalProp.getBrowser() : browserType;
                locale = locale == null ? "fr-FR" : locale;
                System.setProperty("LANG", locale);
                System.setProperty("LC_TIME", locale);

                List<String> args = getStrings(proxy);
                if (GlobalProp.getTestIdAttribute() != null) {
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

                browserContext = browser.newContext(credentialUser == null ? contextOptions : contextOptions.setHttpCredentials(credentialUser, credentialPassword));
                browserContext.clearCookies();
                browserContext.setDefaultTimeout(Duration.ofSeconds(GlobalProp.getPageLoadTimeOut()).toMillis());
                browserContext.setDefaultNavigationTimeout(Duration.ofSeconds(GlobalProp.getPageLoadTimeOut()).toMillis());

                page = browserContext.newPage();

                browserOk = true;
            } catch (Exception e) {
                log.error("Échec du démarrage du navigateur, nouvelle tentative", e);
            }
        }
    }

    /**
     * Récupère les arguments de lancement du navigateur.
     *
     * @param proxy Le proxy à utiliser.
     * @return La liste des arguments de lancement.
     */
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

    /**
     * Réinitialise les paramètres de proxy.
     */
    private void resetProxy() {
        if (GlobalProp.getProxyHost() != null) {
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
        }
    }

    /**
     * Définit les paramètres de proxy.
     */
    private void setProxy() {
        if (GlobalProp.getProxyHost() != null) {
            System.setProperty("https.proxyHost", GlobalProp.getProxyHost());
            System.setProperty("https.proxyPort", GlobalProp.getProxyPort());
        }
    }
}
