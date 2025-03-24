package bcx.automation.appium.element;

import bcx.automation.properties.GlobalProp;
import bcx.automation.report.Reporter;
import bcx.automation.test.TestContext;
import bcx.automation.util.TimeWait;
import bcx.automation.util.data.DataUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.time.LocalDateTime;
import java.util.HashMap;
/**
 * classe contenant toute les actions et assertions que l'on peut faire sur un élément du DOM
 * Ne pas modifier dans le cadre d'une appli : modifier la classe Element qui en hérite
 * @author bcx
 *
 */
@Slf4j
public class MobElement extends MobBaseElement {
    private final HashMap<String, LocalDateTime> dateStartSearch = new HashMap<>();

    private static final String MSG_NOT_FOUND = "not found";

    /**
     * constructor
     * @param elementName description, for report
     * @param elementLocator locator
     */
    public MobElement(TestContext testContext, String elementName, By elementLocator) {
        super(testContext, elementName, elementLocator);
    }

    /**
     * constructor
     * @param element element à copier
     */
    public MobElement(MobElement element) {
        super(element);
    }

    /**
     * retourne le nombre de WebElement selenium correspondant à la description de l'élément dans un délai de : timeout secondes;
     *
     * @param timeout : timeout en seconde
     * @return le nombre de selenium WebElements correspondant au locator de l'élément
     */
    public int getElementsCount(int timeout) {
        try {
            findAllElements(timeout);
            return findAllElements(0).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * indique si l'élément est visible a l'écran dans un délai de : timeout secondes;
     *
     * @param timeout : timeout en seconde
     * @return true si displayed, false sinon
     */
    public boolean isVisible(int timeout) {
        try {
            return findElementDisplayed(timeout).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * indique si l'élément est visible a l'écran dans un délai de : 1 seconde;
     *
     * @return true si displayed, false sinon
     */
    public boolean isVisible() {
        return isVisible(GlobalProp.getAssertTimeOut(this.getTestContext().getReport()));
    }


    /**
     * renvoi la valeur d'élément dans un délai de : TestProperties.timeOut secondes;
     *
     * @return la valeur si trouvée, MSG_NOT_FOUND si pas trouvé. Le résultat est tracé dans le rapport.
     */
    public String getValue() {
        return getValue(GlobalProp.getTimeOut());
    }

    /**
     * renvoi la valeur d'élément dans un délai de : timeout secondes;
     *
     * @param timeout : timeout en seconde
     * @return la valeur si trouvée, MSG_NOT_FOUND si pas trouvé. Le résultat est tracé dans le rapport.
     */
    public String getValue(int timeout) {
        log.info("getValue " + this.getName());
        try {
            return DataUtil.normalizeSpace(findElement(timeout).getText());
        } catch (Exception e) {
            return MSG_NOT_FOUND;
        }
    }

     /////////////// ACTION
    /**
     * saisi une valeur dans l'élément des qu'il est enabled dans un délai de : TestProperties.timeOut secondes;
     * Le résultat est tracé dans le rapport.
     * @param value : valeur a saisir (clé de test_env.properties ou savedData.properties ou valeur en dur, la valeur est traduite si correspond à un clé de label_langue.properties)
     */
    public void setValue(String value) {
        if (value == null) return;
        log.info("setValue " + this.getName() + " >> " + value);
        String action = "setValue >> " + value;
        WebElement element = findElementEnabled();
        try {
            element.clear();
            element.sendKeys(value);
            this.getReport().log(Reporter.PASS_STATUS, action, getName(), null, null, null, null);
        } catch (Exception e) {
            this.getReport().log(Reporter.FAIL_STATUS, action, getName(), null, null, null, e);
        }
    }


    /**
     * click sur l'élément des qu'il est enabled dans un délai de : TestProperties.timeOut secondes;
     */
    public void click() {
        String action = "click";
        log.info(action + " " + this.getName());

        String result = Reporter.FAIL_STATUS;
        Exception exception = null;
        int attempts = 0;

        WebElement element = findElementEnabled();
        TimeWait t = new TimeWait();

        while (!result.equals(Reporter.PASS_STATUS) && t.notOver(GlobalProp.getTimeOut())) {
            try {
                if (attempts > 0) {
                    Thread.sleep(500);
                }
                element.click();
                result = Reporter.PASS_STATUS; // Action réussie
                exception = null;
            } catch (Exception e) {
                exception = e;
                attempts++;
            }
        }

        this.getReport().log(result, action, getName(), null, null, null, exception);
    }


    /**
     * Vérifie que l'élément est visible ou non.
     *
     * @param visible Vrai si l'élément doit être visible, faux sinon.
     * @return
     */
    public void assertVisible(boolean visible) {
        String action = "assertVisible >> " + visible;
        log.info(action + " " + getName());
        String status = visible == isVisible() ? Reporter.PASS_STATUS : Reporter.FAIL_NEXT_STATUS;
        this.getReport().log(status, action, getName(), null, null, null, null);
    }



}

