package bcx.automation.playwright;

import bcx.automation.properties.GlobalProp;
import bcx.automation.test.TestContext;
import bcx.automation.util.TimeWait;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PlaywrightBrowser {

    /**
     * Démarre un nouveau navigateur avec les paramètres par défaut.
     * @param testContext Le contexte de test.
     */
    public static void startNewBrowser(TestContext testContext) {
        startNewBrowser(testContext,null, null, null, null, null);
    }

    /**
     * Démarre un nouveau navigateur avec des paramètres spécifiques.
     *
     * @param testContext Le contexte de test.
     * @param proxy Le proxy à utiliser.
     * @param locale La langue par défaut du navigateur.
     * @param browserType Le type de navigateur.
     * @param credentialUser Le nom d'utilisateur pour les identifiants HTTP.
     * @param credentialPassword Le mot de passe pour les identifiants HTTP.
     */
    public static void startNewBrowser(TestContext testContext, String proxy, String locale, String browserType, String credentialUser, String credentialPassword) {
        Playwright playwright = Playwright.create();
        boolean browserOk = false;
        TimeWait t = new TimeWait();
        while (!browserOk && t.notOver(5)) {
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
                Browser browser;
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

                BrowserContext browserContext = browser.newContext(credentialUser == null ? contextOptions : contextOptions.setHttpCredentials(credentialUser, credentialPassword));
                browserContext.clearCookies();
                browserContext.setDefaultTimeout(Duration.ofSeconds(GlobalProp.getPageLoadTimeOut()).toMillis());
                browserContext.setDefaultNavigationTimeout(Duration.ofSeconds(GlobalProp.getPageLoadTimeOut()).toMillis());

                Page page = browserContext.newPage();

                testContext.setBrowser(browser);
                testContext.setBrowserContext(browserContext);
                testContext.setPage(page);
                testContext.getReport().setPage(page);

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
    private static List<String> getStrings(String proxy) {
        List<String> args = new ArrayList<>();
        args.add("--disable-search-engine-choice-screen");
        args.add("--no-sandbox");
        args.add("--disable-dev-shm-usage");
        args.add("--disable-gpu");
        args.add("--unsafely-treat-insecure-origin-as-secure=" + GlobalProp.getUnsafelyTreatInsecure());
        if (proxy != null) args.add("--proxy-server=" + proxy);
        return args;
    }


}
