package bcx.automation.test;

import bcx.automation.playwright.PlaywrightBrowser;
import bcx.automation.report.Reporter;
import com.microsoft.playwright.*;
import io.appium.java_client.AppiumDriver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe de contexte de test pour gérer les instances de navigateur et les pages.
 */
@Slf4j
public class TestContext {

    @Setter
    private  Playwright playwright;
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
        this.playwright = null;
        this.browser = null;
        this.browserContext = null;
        this.page = null;
        this.report = new Reporter();
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
    public void closeBrowsersAndDriver() {
        closeOtherTabs();
        closeOtherContexts();
        if (browser != null) {
            browser.close();
            playwright.close();
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
        return newTestContext(null, null, null, null, null);
    }

    /**
     * Crée un nouveau contexte de test.
     *
     * @param proxy Le proxy à utiliser.
     * @param locale La langue par défaut du navigateur.
     * @param browserType Le type de navigateur.
     * @param credentialUser Le nom d'utilisateur pour les identifiants HTTP.
     * @param credentialPassword Le mot de passe pour les identifiants HTTP.
     * @return Le nouveau contexte de test créé.
     */
    public TestContext newTestContext(String proxy, String locale, String browserType, String credentialUser, String credentialPassword) {
        TestContext newContext = new TestContext();
        newContext.setReport(report);
        otherContexts.add(newContext);
        PlaywrightBrowser.startNewBrowser(newContext, proxy, locale, browserType, credentialUser, credentialPassword);
        return newContext;
    }

}
