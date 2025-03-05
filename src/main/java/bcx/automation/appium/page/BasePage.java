package bcx.automation.appium.page;

import bcx.automation.appium.element.Element;
import bcx.automation.report.Reporter;
import bcx.automation.test.TestContext;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import lombok.Getter;
import org.openqa.selenium.interactions.Actions;

import java.util.LinkedHashMap;

public abstract class BasePage {
    @Getter
    private final TestContext testContext;
    @Getter
    private final Reporter report;
    @Getter
    private AndroidDriver androidDriver = null;
    private IOSDriver iosDriver = null;
    private boolean isAndroid;

    public LinkedHashMap<String, Element> elements = new LinkedHashMap<>();
    /**
     * Constructeur de la classe BasePage.
     *
     * @param testContext Le contexte de test.
     */
    protected BasePage(TestContext testContext) {
        this.testContext = testContext;
        this.report = testContext.getReport();
        AppiumDriver appiumDriver = testContext.getAppiumDriver();
        if (appiumDriver instanceof AndroidDriver) {
            this.androidDriver = (AndroidDriver) appiumDriver;
            isAndroid = true;
        } else if (appiumDriver instanceof IOSDriver) {
            this.iosDriver = (IOSDriver) appiumDriver;
            isAndroid = false;
        } else {
            this.report.log(Reporter.FAIL_STATUS_NO_SCREENSHOT, "Erreur au démarrage", new Exception("AppiumDriver non défini"));
        }
    }

    /**
     * Vérifie si le clavier est affiché.
     * @return true si le clavier est visible, false sinon.
     */
    public boolean isKeyboardShown() {
        return isAndroid ? androidDriver.isKeyboardShown() : iosDriver.isKeyboardShown();
    }

    /**
     * Masque le clavier si affiché.
     */
    public void hideKeyboard() {
        if (isKeyboardShown()) {
            if (isAndroid) {
                androidDriver.hideKeyboard();
            } else {
                iosDriver.hideKeyboard();
            }
        }
    }

    /**
     * Affiche le clavier en cliquant sur un champ texte.
     * @param element L'élément du champ de saisie.
     */
    public void showKeyboard(Element element) {
        element.click();
    }

    /**
     * Tape un texte caractère par caractère en simulant un vrai usage du clavier.
     * Compatible iOS et Android.
     * @param element Champ de texte cible.
     * @param text Texte à saisir.
     */
    public void typeUsingKeyboard(Element element, String text) {
        showKeyboard(element);

        Actions actions = new Actions(isAndroid ? androidDriver : iosDriver);
        for (char c : text.toCharArray()) {
            String key = String.valueOf(c);
            actions.sendKeys(key).perform();
            try {
                Thread.sleep(200); // Petit délai pour simuler la frappe humaine
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
