package bcx.playwright.page;
import bcx.playwright.page.element.BaseElement;
import bcx.playwright.test.TestContext;
import bcx.playwright.util.app.ConnectedUserUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import bcx.playwright.report.Reporter;
import bcx.playwright.util.data.DataSetUtil;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public abstract class BasePage {
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

    protected BasePage(TestContext testContext) {
        this.testContext = testContext;
        this.page = testContext.getPage();
        this.report = testContext.getReport();
    }

    protected BasePage(TestContext testContext, String url) {
        this.testContext = testContext;
        this.page = testContext.getPage();
        this.report = testContext.getReport();
        this.url = url;
    }

    public void navigate() {
        this.page.navigate(url);
    }

    public void navigate(String url) {
        this.page.navigate(url);
    }


    /**
     * renvoi l'url en cours
     * @return
     */
    public String currentUrl() {
        return this.page.url();
    }

    /**
     * lance un refresh de la page
     */
    public void refresh() {
        this.page.reload();
    }

    /**
     * retourne sur la page précédente
     */
    public void backPage() {
        this.page.goBack();
    }


    /**
     * resize le browser
     * @param width
     * @param height
     */
    public void resize(int width, int height) {
        this.page.setViewportSize(width, height);
    }


    /**
     * scroll en haut de la page
     * il faut savoir qu'il est parfois nécessaire de scroller : imaginons qu'un élément soit sur la page mais caché par un bandeau par exemple, le champs ne sera pas clickable
     * ou modifiable ce qui fera planter le test.
     * Les méthodes BaseElement.findElementEnabled font automatiquement ce scroll via BaseElement.scrollTopToElement() en partant du principe qu'il y a un bandeau de 223px (c'est celui de sistema !!! à modifier en fonction de vos applis)
     */

    public void scrollTop() {
        this.page.evaluate("Tab.scrollTo(0 , 0);");
    }

    /**
     * scroll en bas de la page
     */
    public void scrollBottom() {
        this.page.evaluate("Tab.scrollTo(0 , document.body.scrollHeight);");
    }


    /**
     * méthode d'authentification pour les pages présentant une popup de connexion hors browser
     * @param user
     * @param password
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
            this.report.log(Reporter.ERROR_STATUS_NO_SCREENSHOT, e);
            e.printStackTrace();
        }
        return false;
    }



    public DataSetUtil setValue(String dataFile, String testid) {
        DataSetUtil dataSet = new DataSetUtil(this.report, dataFile, testid);
        setValue(dataSet);
        return dataSet;
    }

    public void setValue(DataSetUtil dataSet) {
        LinkedHashMap<String, String> jddHash = dataSet.getKeyAndValues();
        for (Map.Entry<String, String> jdd : jddHash.entrySet()) {
            String key = String.valueOf(jdd.getKey());
            String value = String.valueOf(jdd.getValue());
            if (!key.equals("test-id") && !value.equals("N/A")) {
                if (elements.containsKey(key)) {
                    elements.get(key).setValue(value);
                } else {
                    log.info("Element " + key + " not found");
                    this.report.log(Reporter.FAIL_STATUS, "Element " + key + " not found");
                }
            }

        }
    }

    public DataSetUtil assertValue(String dataFile, String testid) {
        DataSetUtil dataSet = new DataSetUtil(this.report, dataFile, testid);
        assertValue(dataSet);
        return dataSet;
    }

    public void assertValue(DataSetUtil dataSet) {
        LinkedHashMap<String, String> jddHash = dataSet.getKeyAndValues();
        for (Map.Entry<String, String> jdd : jddHash.entrySet()) {
            String key = String.valueOf(jdd.getKey());
            String value = String.valueOf(jdd.getValue());
            if (!key.equals("test-id") && !value.equals("N/A")) {
                if (elements.containsKey(key)) {
                    elements.get(key).assertValue(value);
                } else {
                    log.info("Element " + key + " not found");
                    this.report.log(Reporter.FAIL_STATUS, "Element " + key + " not found");
                }
            }

        }
    }


    public DataSetUtil assertVisible(String dataFile, String testid) {
        DataSetUtil dataSet = new DataSetUtil(this.report, dataFile, testid);
        assertVisible(dataSet);
        return dataSet;
    }

    public void assertVisible(DataSetUtil dataSet) {
        LinkedHashMap<String, String> jddHash = dataSet.getKeyAndValues();
        for (Map.Entry<String, String> jdd : jddHash.entrySet()) {
            String key = String.valueOf(jdd.getKey());
            String value = String.valueOf(jdd.getValue());
            if (!key.equals("test-id") && !value.equals("N/A")) {
                if (elements.containsKey(key)) {
                    elements.get(key).assertVisible(Boolean.parseBoolean(value));
                } else {
                    log.info("Element " + key + " not found");
                    this.report.log(Reporter.FAIL_STATUS, "Element " + key + " not found");
                }
            }

        }
    }


    public DataSetUtil assertEnabled(String dataFile, String testid) {
        DataSetUtil dataSet = new DataSetUtil(this.report, dataFile, testid);
        assertEnabled(dataSet);
        return dataSet;
    }

    public void assertEnabled(DataSetUtil dataSet) {
        LinkedHashMap<String, String> jddHash = dataSet.getKeyAndValues();
        for (Map.Entry<String, String> jdd : jddHash.entrySet()) {
            String key = String.valueOf(jdd.getKey());
            String value = String.valueOf(jdd.getValue());
            if (!key.equals("test-id") && !value.equals("N/A")) {
                if (elements.containsKey(key)) {
                    elements.get(key).assertEnabled(Boolean.parseBoolean(value));
                } else {
                    log.info("Element " + key + " not found");
                    this.report.log(Reporter.FAIL_STATUS, "Element " + key + " not found");
                }
            }

        }
    }


    public DataSetUtil assertRequired(String dataFile, String testid) {
        DataSetUtil dataSet = new DataSetUtil(this.report, dataFile, testid);
        assertRequired(dataSet);
        return dataSet;
    }

    public void assertRequired(DataSetUtil dataSet) {
        LinkedHashMap<String, String> jddHash = dataSet.getKeyAndValues();
        for (Map.Entry<String, String> jdd : jddHash.entrySet()) {
            String key = String.valueOf(jdd.getKey());
            String value = String.valueOf(jdd.getValue());
            if (!key.equals("test-id") && !value.equals("N/A")) {
                if (elements.containsKey(key)) {
                    elements.get(key).assertRequired(Boolean.parseBoolean(value));
                } else {
                    log.info("Element " + key + " not found");
                    this.report.log(Reporter.FAIL_STATUS, "Element " + key + " not found");
                }
            }

        }
    }



    /**
     * sauvegarde toutes les fenêtres/onglets ouverts dans le driver courant
     * cette méthode doit être appelée avant chaque action déclenchant une ouverture de fenêtre : cela permettra de switcher sur la nouvelle fenêtre par comparaison avec les fénêtres initialement ouvertes
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
     * permet de switcher sur un nouvel onglet qui viens de s'ouvrir
     * à appeler après une action qui a déclenché l'ouverture d'une nouvelle fenêtre si on veut faire des actions ou des assertions dessus
     */
    public Page switchToNewOpenedTab() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BrowserContext browserContext = this.testContext.getBrowserContext();
        List<Page> allTabs = browserAllTabs.get(browserContext);
        LocalDateTime now = LocalDateTime.now();
        while (now.plusSeconds(60).isAfter(LocalDateTime.now())) {
            for(Page actualTab : browserContext.pages()) {
                if (!allTabs.contains(actualTab)) {
                    try {
                        Thread.sleep(1000);
                        return actualTab;
                    } catch (Exception e) {
                        //fenetre refermée, on continue la recherche
                    }
                }
            }
        }
        this.report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "switchToNewOpenedTab, new Tab not found");
        saveAllOpenedTabs();
        return null;
    }
}
