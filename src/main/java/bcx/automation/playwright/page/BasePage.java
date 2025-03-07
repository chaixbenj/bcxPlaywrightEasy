package bcx.automation.playwright.page;

import bcx.automation.playwright.element.BaseElement;
import bcx.automation.test.TestContext;
import bcx.automation.util.TimeWait;
import bcx.automation.util.app.ConnectedUserUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import io.qameta.allure.Allure;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import bcx.automation.report.Reporter;
import bcx.automation.util.data.DataSetUtil;

import java.net.URLEncoder;
import java.util.*;

/**
 * Classe de base pour les pages utilisant Playwright.
 */
@Slf4j
public abstract class BasePage {
    public static final String TEST_ID = "test-id";
    @Getter
    private final TestContext testContext;
    @Getter
    private final Page page;
    @Getter
    private final Reporter report;
    private final HashMap<BrowserContext, List<Page>> browserAllTabs = new HashMap<>();
    @Setter
    @Getter
    private String url;
    public LinkedHashMap<String, BaseElement> elements = new LinkedHashMap<>();

    /**
     * Constructeur de la classe BasePage.
     *
     * @param testContext Le contexte de test.
     */
    protected BasePage(TestContext testContext) {
        this.testContext = testContext;
        this.page = testContext.getPage();
        this.report = testContext.getReport();
    }

    /**
     * Constructeur de la classe BasePage avec une URL.
     *
     * @param testContext Le contexte de test.
     * @param url L'URL de la page.
     */
    protected BasePage(TestContext testContext, String url) {
        this.testContext = testContext;
        this.page = testContext.getPage();
        this.report = testContext.getReport();
        this.url = url;
    }

    /**
     * Navigue vers l'URL spécifiée.
     */
    public void navigate() {
        this.page.navigate(url);
        this.report.log(Reporter.INFO_STATUS, "Navigation vers la page " + url);
    }

    /**
     * Navigue vers l'URL spécifiée.
     *
     * @param url L'URL vers laquelle naviguer.
     */
    public void navigate(String url) {
        this.page.navigate(url);
    }

    /**
     * Renvoie l'URL en cours.
     *
     * @return L'URL en cours.
     */
    public String currentUrl() {
        return this.page.url();
    }

    /**
     * Rafraîchit la page.
     */
    public void refresh() {
        this.page.reload();
    }

    /**
     * Retourne à la page précédente.
     */
    public void backPage() {
        this.page.goBack();
    }

    /**
     * Redimensionne le navigateur.
     *
     * @param width La largeur de la fenêtre.
     * @param height La hauteur de la fenêtre.
     */
    public void resize(int width, int height) {
        this.page.setViewportSize(width, height);
    }

    /**
     * Fait défiler la page vers le haut.
     */
    public void scrollTop() {
        this.page.evaluate("window.scrollTo(0 , 0);");
    }

    /**
     * Fait défiler la page vers le bas.
     */
    public void scrollBottom() {
        this.page.evaluate("window.scrollTo(0 , document.body.scrollHeight);");
    }

    /**
     * Méthode d'authentification pour les pages présentant une popup de connexion hors navigateur.
     *
     * @param user Le nom d'utilisateur.
     * @param password Le mot de passe.
     * @return Vrai si l'authentification a réussi, faux sinon.
     */
    public boolean authenticate(String user, String password) {
        this.page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        String currentUrl = currentUrl();
        try {
            password = URLEncoder.encode(password, "UTF-8");
            user = URLEncoder.encode(user, "UTF-8");
            String https = "https://";
            navigate(String.valueOf(currentUrl).replace(https, https + user + ":" + password + "@"));
            this.report.log(Reporter.INFO_STATUS, "Login with user : " + user);
            ConnectedUserUtil.setConnectedUser(user, this.page);
            return true;
        } catch (Exception e) {
            this.report.log(Reporter.FAIL_STATUS_NO_SCREENSHOT, e);
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Définit les valeurs des éléments à partir d'un fichier de données.
     *
     * @param dataFile Le fichier de données.
     * @param testid L'identifiant du test.
     * @return L'utilitaire de jeu de données.
     */
    public DataSetUtil setValue(String dataFile, String testid) {
        DataSetUtil dataSet = new DataSetUtil(this.report, dataFile, testid);
        setValue(dataSet);
        return dataSet;
    }

    /**
     * Définit les valeurs des éléments à partir d'un utilitaire de jeu de données.
     *
     * @param dataSet L'utilitaire de jeu de données.
     */
    public void setValue(DataSetUtil dataSet) {
        performAction(dataSet, "setValue");
    }

    /**
     * Vérifie les valeurs des éléments à partir d'un fichier de données.
     *
     * @param dataFile Le fichier de données.
     * @param testid L'identifiant du test.
     * @return L'utilitaire de jeu de données.
     */
    public DataSetUtil assertValue(String dataFile, String testid) {
        DataSetUtil dataSet = new DataSetUtil(this.report, dataFile, testid);
        assertValue(dataSet);
        return dataSet;
    }

    /**
     * Vérifie les valeurs des éléments à partir d'un utilitaire de jeu de données.
     *
     * @param dataSet L'utilitaire de jeu de données.
     */
    public void assertValue(DataSetUtil dataSet) {
        performAction(dataSet, "assertValue");
    }

    /**
     * Vérifie la visibilité des éléments à partir d'un fichier de données.
     *
     * @param dataFile Le fichier de données.
     * @param testid L'identifiant du test.
     * @return L'utilitaire de jeu de données.
     */
    public DataSetUtil assertVisible(String dataFile, String testid) {
        DataSetUtil dataSet = new DataSetUtil(this.report, dataFile, testid);
        assertVisible(dataSet);
        return dataSet;
    }

    /**
     * Vérifie la visibilité des éléments à partir d'un utilitaire de jeu de données.
     *
     * @param dataSet L'utilitaire de jeu de données.
     */
    public void assertVisible(DataSetUtil dataSet) {
        performAction(dataSet, "assertVisible");
    }

    /**
     * Vérifie si les éléments sont activés à partir d'un fichier de données.
     *
     * @param dataFile Le fichier de données.
     * @param testid L'identifiant du test.
     * @return L'utilitaire de jeu de données.
     */
    public DataSetUtil assertEnabled(String dataFile, String testid) {
        DataSetUtil dataSet = new DataSetUtil(this.report, dataFile, testid);
        assertEnabled(dataSet);
        return dataSet;
    }

    /**
     * Vérifie si les éléments sont activés à partir d'un utilitaire de jeu de données.
     *
     * @param dataSet L'utilitaire de jeu de données.
     */
    public void assertEnabled(DataSetUtil dataSet) {
        performAction(dataSet, "assertEnabled");
    }

    /**
     * Vérifie si les éléments sont requis à partir d'un fichier de données.
     *
     * @param dataFile Le fichier de données.
     * @param testid L'identifiant du test.
     * @return L'utilitaire de jeu de données.
     */
    public DataSetUtil assertRequired(String dataFile, String testid) {
        DataSetUtil dataSet = new DataSetUtil(this.report, dataFile, testid);
        assertRequired(dataSet);
        return dataSet;
    }

    /**
     * Vérifie si les éléments sont requis à partir d'un utilitaire de jeu de données.
     *
     * @param dataSet L'utilitaire de jeu de données.
     */
    public void assertRequired(DataSetUtil dataSet) {
        performAction(dataSet, "assertRequired");
    }

    // Méthode générique pour effectuer une action sur les éléments
    private void performAction(DataSetUtil dataSet, String action) {
        report.startStep(this.getClass().getSimpleName() + " " + action);
        LinkedHashMap<String, String> jddHash = dataSet.getKeyAndValues();
        for (Map.Entry<String, String> jdd : jddHash.entrySet()) {
            String key = String.valueOf(jdd.getKey());
            String value = String.valueOf(jdd.getValue());
            if (!key.equals(TEST_ID) && !value.equals("N/A")) {
                if (elements.containsKey(key)) {
                    switch (action) {
                        case "setValue":
                            elements.get(key).setValue(value);
                            break;
                        case "assertValue":
                            elements.get(key).assertValue(value);
                            break;
                        case "assertVisible":
                            elements.get(key).assertVisible(Boolean.parseBoolean(value));
                            break;
                        case "assertEnabled":
                            elements.get(key).assertEnabled(Boolean.parseBoolean(value));
                            break;
                        case "assertRequired":
                            elements.get(key).assertRequired(Boolean.parseBoolean(value));
                            break;
                        default:
                            log.info("Action " + action + " not recognized");
                            break;
                    }
                } else {
                    log.info("Element " + key + " not found");
                    this.report.log(Reporter.FAIL_STATUS, "Element " + key + " not found");
                }
            }
        }
        report.stopStep();
    }

    /**
     * Sauvegarde toutes les fenêtres/onglets ouverts dans le contexte de navigateur courant.
     */
    public void saveAllOpenedTabs() {
        BrowserContext browserContext = this.testContext.getBrowserContext();
        List<Page> Tabs = browserContext.pages();
        if (browserAllTabs.containsKey(browserContext)) {
            browserAllTabs.replace(browserContext, Tabs);
        } else {
            browserAllTabs.put(browserContext, Tabs);
        }
    }

    /**
     * Permet de basculer sur un nouvel onglet qui vient de s'ouvrir.
     *
     * @return La nouvelle page ouverte ou null si aucune nouvelle page n'a été trouvée.
     */
    public Page switchToNewOpenedTab() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BrowserContext browserContext = this.testContext.getBrowserContext();
        List<Page> allTabs = browserAllTabs.get(browserContext);
        TimeWait t = new TimeWait();
        while (t.notOver(60)) {
            for (Page actualTab : browserContext.pages()) {
                if (!allTabs.contains(actualTab)) {
                    try {
                        Thread.sleep(1000);
                        return actualTab;
                    } catch (Exception e) {
                        // Fenêtre refermée, on continue la recherche
                    }
                }
            }
        }
        this.report.log(Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT, "switchToNewOpenedTab, new Tab not found");
        saveAllOpenedTabs();
        return null;
    }
}
