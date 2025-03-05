package bcx.automation.appium.element;

import bcx.automation.properties.GlobalProp;
import bcx.automation.report.Reporter;
import bcx.automation.test.TestContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.openqa.selenium.support.locators.RelativeLocator.with;

/**
 * classe de base common des éléments du DOM : recherche, gestion de locator paramétrable, affectation de container
 * Ne pas modifier dans le cadre d'une appli : modifier la classe BaseElement qui en hérite
 * @author bcx
 *
 */
@Slf4j
public abstract class BaseElement {
    public static final String ELEMENT_NOT_FOUND_SUITE_TIME_OVER_LIMIT = "Element not found, suite time over limit";
    public static final String NORMALIZE_SPACE_TEXT = "//*[normalize-space(text())=\"";
    @Getter
    private final TestContext testContext;
    @Getter
    private final RemoteWebDriver driver;
    @Getter
    private final Reporter report;
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String intialName;
    @Setter
    @Getter
    private By locator;
    @Setter
    @Getter
    private By initialLocator;
    @Getter
    private Element container;
    @Getter
    private Element labelForElement;
    @Setter
    @Getter
    private String labelForIdSuffix;
    @Getter
    private Element nearElement;
    @Getter
    private Element aboveElement;
    @Getter
    private Element belowElement;
    @Getter
    private Element toRightOfElement;
    @Getter
    private Element toLeftOfElement;


    private static final String BY_XPATH_LOC_START = "By.xpath: ";
    private static final String BY_ID_LOC_START = "By.id: ";
    private static final String BY_NAME_LOC_START = "By.name: ";
    private static final String BY_CSS_LOC_START = "By.cssSelector: ";
    private static final String BY_TAGNAME_LOC_START = "By.tagName: ";
    private static final String BY_CLASSNAME_LOC_START = "By.className: ";
    private static final String BY_LINKTEXT_LOC_START = "By.linkText: ";
    private static final String BY_PARTIALLINKTEXT_LOC_START = "By.partialLinkText: ";

    /**
     * constructeur de l'élément
     * @param elementName    : nom / description de l'élément
     * @param elementLocator : information d'identification de l'élément. Exemple : By.id("idelement"), By.name("elementName"), By.xpath("//a[@class='toto']")
     */
    protected BaseElement(TestContext testContext, String elementName, By elementLocator) {
        this.testContext = testContext;
        this.report = testContext.getReport();
        this.driver = testContext.getAppiumDriver();
        this.name = elementName;
        this.intialName = elementName;
        this.locator = elementLocator;
        this.initialLocator = elementLocator;
        this.container = null;
        this.labelForElement = null;
        this.labelForIdSuffix = null;
        this.nearElement = null;
        this.aboveElement = null;
        this.belowElement = null;
        this.toRightOfElement = null;
        this.toLeftOfElement = null;
    }

    /**
     * constructeur de l'élément
     * @param element    : nouvel élément à partir d'un élément existant
     */
    protected BaseElement(Element element) {
        this.testContext = element.getTestContext();
        this.report = testContext.getReport();
        this.driver = testContext.getAppiumDriver();
        this.name = element.getName();
        this.intialName = element.getIntialName();
        this.locator = element.getLocator();
        this.initialLocator = element.getInitialLocator();
        this.container = element.getContainer();
        this.labelForElement = element.getLabelForElement();
        this.labelForIdSuffix = element.getLabelForIdSuffix();
        this.nearElement = element.getNearElement();
        this.aboveElement = element.getAboveElement();
        this.belowElement = element.getBelowElement();
        this.toRightOfElement = element.getToRightOfElement();
        this.toLeftOfElement = element.getToLeftOfElement();
    }

    /**
     * renvoi le locator de l'élément à utiliser pour la recherche (avec near, above, below, toLeftOf, toRightOf, si défini)
     * @return
     */
    public By getFindLocator() {
        if (nearElement!=null) {
            return with(locator).near(nearElement.findElementDisplayed());
        } else if (aboveElement!=null) {
            return with(locator).above(aboveElement.findElementDisplayed());
        } else if (belowElement!=null) {
            return with(locator).below(belowElement.findElementDisplayed());
        } else if (toLeftOfElement!=null) {
            return with(locator).toLeftOf(toLeftOfElement.findElementDisplayed());
        } else if (toRightOfElement!=null) {
            return with(locator).toRightOf(toRightOfElement.findElementDisplayed());
        } else {
            return locator;
        }
    }
    
    /**
     * renvoi la string du locator pour les rapports (element + son container)
     * @return
     */
    public String getContainerAndElementStringLocator() {
        String returnLocator = locator.toString();
        if (container!=null) {
            returnLocator = container.getLocator().toString() + " -> " + returnLocator;
        }
        return returnLocator;
    }


    /**
     * Définit dans quel élement on doit chercher l'élément, si non setté on le recherchera dans tout le body
     * par exemple common.page.element.click() cliquera sur le premier élément correspondant dans toute la page quand common.page.element.setContainer(parent).click() cliquera sur le premier élément correspondant dans l'élément parent
     *
     * @param containerElement : élément contenant l'élément recherché
     * @return l'élément recherché dans le container
     */
    public Element setContainer(Element containerElement) {
        container = containerElement;
        String locatorPath = locator.toString();
        if (container != null && locatorPath.contains(BY_XPATH_LOC_START)) {
            locatorPath = locatorPath.substring(locatorPath.indexOf(": ")+2);
            if (locatorPath.startsWith("//")) {
                locator = By.xpath("." + locatorPath);
            }
            if (locatorPath.startsWith("(//")) {
                locator = By.xpath("(." + locatorPath.substring(1));
            }
        }
        return (Element)this;
    }

    /**
     * Remet à null le container de l'élément pour le chercher das tout le body dans tout le body
     *
     * @return l'élément recherché dans le container
     */
    public Element resetContainer() {
        container = null;
        String locatorPath = locator.toString();
        if (locatorPath.contains(BY_XPATH_LOC_START)) {
            locatorPath = locatorPath.substring(locatorPath.indexOf(": ")+2);
            if (locatorPath.startsWith(".//")) {
                locator = By.xpath(locatorPath.substring(1));
            }
            if (locatorPath.startsWith("(.//")) {
                locator = By.xpath("(" + locatorPath.substring(2));
            }
        }
        return (Element)this;
    }

    /**
     * précise un élément (Libelle ou Element ou By) proche de l'élément recherché
     * @param objectStringElementOrBy
     * @return
     */
    public Element near(Object objectStringElementOrBy) {
        switch (objectStringElementOrBy) {
            case String string:
                this.nearElement = new Element(this.testContext, string, By.xpath(NORMALIZE_SPACE_TEXT + string.trim() + "\"]"));
                break;
            case By by:
                this.nearElement = new Element(this.testContext, "", by);
                break;
            default:
                this.nearElement = (Element) objectStringElementOrBy;
        }
        return (Element)this;
    }

    /**
     * précise un élément (Libelle ou Element ou By) sous de l'élément recherché
     * @param objectStringElementOrBy
     * @return
     */
    public Element above(Object objectStringElementOrBy) {
        switch (objectStringElementOrBy) {
            case String string:
                this.aboveElement = new Element(this.testContext, string, By.xpath(NORMALIZE_SPACE_TEXT + string.trim() + "\"]"));
                break;
            case By by:
                this.aboveElement = new Element(this.testContext, "", by);
                break;
            default:
                this.aboveElement = (Element) objectStringElementOrBy;
        }
        return (Element)this;
    }

    /**
     * précise un élément (Libelle ou Element ou By) au dessus de l'élément recherché
     * @param objectStringElementOrBy
     * @return
     */
    public Element below(Object objectStringElementOrBy) {
        switch (objectStringElementOrBy) {
            case String string:
                this.belowElement = new Element(this.testContext, string, By.xpath(NORMALIZE_SPACE_TEXT + string.trim() + "\"]"));
                break;
            case By by:
                this.belowElement = new Element(this.testContext, "", by);
                break;
            default:
                this.belowElement = (Element) objectStringElementOrBy;
        }
        return (Element)this;
    }

    /**
     * précise un élément (Libelle ou Element ou By) à gauche de l'élément recherché
     * @param objectStringElementOrBy
     * @return
     */
    public Element toRightOf(Object objectStringElementOrBy) {
        switch (objectStringElementOrBy) {
            case String string:
                this.toRightOfElement = new Element(this.testContext, string, By.xpath(NORMALIZE_SPACE_TEXT + string.trim() + "\"]"));
                break;
            case By by:
                this.toRightOfElement = new Element(this.testContext, "", by);
                break;
            default:
                this.toRightOfElement = (Element) objectStringElementOrBy;
        }
        return (Element)this;
    }

    /**
     * précise un élément (Libelle ou Element ou By) à droite de l'élément recherché
     * @param objectStringElementOrBy
     * @return
     */
    public Element toLeftOf(Object objectStringElementOrBy) {
        switch (objectStringElementOrBy) {
            case String string:
                this.toLeftOfElement = new Element(this.testContext, string, By.xpath(NORMALIZE_SPACE_TEXT + string.trim() + "\"]"));
                break;
            case By by:
                this.toLeftOfElement = new Element(this.testContext, "", by);
                break;
            default:
                this.toLeftOfElement = (Element) objectStringElementOrBy;
        }
        return (Element)this;
    }
    
    /**
     * Si le elementLocator contient plusieurs variables, par exemple By.xpath("//button[@class='MA_CLASSE'][contains(.,'MA_VARIABLE')]"),
     * utiliser cette méthode pour les valoriser avant de faire une action sur l'élément :
     * mon_element.injectValues(["MA_CLASS","success_button", "MA_VARIABLE", "Enregister"] as String[]);
     * mon_element.click();
     *
     * @param params : tableau des variables et de leur valorisation {var1, value1, var2, value2, var3, value3, ...}
     * @return l'common.page.element paramétré
     */
    public Element injectValues(Map<String, String> params) {
        String valuedPath = getInitialLocator().toString();
        String valuedName = getIntialName();
        for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
            valuedPath = valuedPath.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
            valuedName = valuedName.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        setName(valuedName);
        setLocator(getLocatorFromString(valuedPath));
        return (Element)this;
    }

    /**
     * Si le elementLocator contient une variable et une seule, par exemple By.xpath("//button[contains(.,'MA_VARIABLE')]"),
     * utiliser cette méthode pour la valoriser avant de faire une action sur l'élément :
     * mon_element.injectValues("MA_VARIABLE", "Enregister");
     * mon_element.click();
     *
     * @param key   : nom de la variable dans le locator
     * @param value : valeur de remplacement de la key dans le locator (clé de test_env.properties ou savedData.properties ou valeur en dur, la valeur est traduite si correspond à un clé de label_langue.properties)
     * @return l'common.page.element paramétré
     */
    public Element injectValues(String key, String value) {
        return injectValues(Map.of(key, value));
    }

    /**
     * permet de setter un locator à partir d'un string : uniquement utiliser par les méthodes injectValues
     * @param path (au format par exemple : By.tagName: INPUT, By.xpath : //div[contains(., "toto")], By.id: toto, etc...)
     */
    public By getLocatorFromString(String path) {
         if (path.contains(BY_XPATH_LOC_START)) {
            return By.xpath(path.replaceAll(BY_XPATH_LOC_START, ""));
        } else if (path.contains(BY_ID_LOC_START)) {
            return By.id(path.replaceAll(BY_ID_LOC_START, ""));
        } else if (path.contains(BY_NAME_LOC_START)) {
            return By.name(path.replaceAll(BY_NAME_LOC_START, ""));
        } else if (path.contains(BY_CSS_LOC_START)) {
            return By.cssSelector(path.replaceAll(BY_CSS_LOC_START, ""));
        } else if (path.contains(BY_TAGNAME_LOC_START)) {
            return By.tagName(path.replaceAll(BY_TAGNAME_LOC_START, ""));
        } else if (path.contains(BY_CLASSNAME_LOC_START)) {
            return By.className(path.replaceAll(BY_CLASSNAME_LOC_START, ""));
        } else if (path.contains(BY_LINKTEXT_LOC_START)) {
            return By.linkText(path.replaceAll(BY_LINKTEXT_LOC_START, ""));
        } else if (path.contains(BY_PARTIALLINKTEXT_LOC_START)) {
            return By.partialLinkText(path.replaceAll(BY_PARTIALLINKTEXT_LOC_START, ""));
        } else {
            return locator;
        }
    }

    /**
     * retourne le path du locator de l'élément converti au format xPath;
     * utiliser quand on a besoin de construire d'autre xpath à partir de cet xapth pour par exemple cherche des élément fils précis
     *
     * @return : String xpath
     */
    public String locatorXpath() {
        return convertByLocatorToByXPath().toString().replaceAll(BY_XPATH_LOC_START, "");
    }
    
    /**
     * retourne le locator de l'élément converti au format xPath;
     * utiliser quand on a besoin de construire d'autre xpath à partir de cet xapth pour par exemple cherche des élément fils précis
     *
     * @return : common.page.element By.xpath
     */
    public By convertByLocatorToByXPath() {
        return convertByLocatorToByXPath("", "");
    }

    /**
     * retourne le locator de l'élément converti au format xPath avec une chaine avant ou/et après pour compléter le xpath
     * utiliser quand on a besoin de construire d'autre xpath à partir de cet xapth pour par exemple cherche des élément fils précis
     *
     * @param stringToAddAfterXpath
     * @param stringToAddBeforeXpath
     * @return : common.page.element xpath
     */
    public By convertByLocatorToByXPath(String stringToAddBeforeXpath, String stringToAddAfterXpath) {
        String locatorPath = getLocator().toString();
        String xpath;
        if (locator instanceof By.ById) {
            String id = locatorPath.replaceAll(BY_ID_LOC_START, "");
            xpath = "//*[@id=\"" + id + "\"]";
        } else if (locator instanceof By.ByClassName) {
            String className = locatorPath.replaceAll(BY_CLASSNAME_LOC_START, "");
            xpath = "//*[contains(@class, \"" + className + "\")]";
        } else if (locator instanceof By.ByTagName) {
            String tagName = locatorPath.replaceAll(BY_TAGNAME_LOC_START, "");
            xpath = "//" + tagName;
        } else if (locator instanceof By.ByLinkText) {
            String linkText = locatorPath.replaceAll(BY_LINKTEXT_LOC_START, "");
            xpath = "//a[text()=\"" + linkText + "\"]";
        } else if (locator instanceof By.ByPartialLinkText) {
            String partialLinkText = locatorPath.replaceAll(BY_PARTIALLINKTEXT_LOC_START, "");
            xpath = "//a[contains(text(), \"" + partialLinkText + "\")]";
        } else if (locator instanceof By.ByName) {
            String nameAttr = locatorPath.replaceAll(BY_PARTIALLINKTEXT_LOC_START, "");
            xpath = "//*[@name=\"" + nameAttr + "\"]";
        }else if (locator instanceof By.ByCssSelector) {
            String cssSelector = locatorPath.replaceAll(BY_CSS_LOC_START, "");
            xpath = convertCssToXPath(cssSelector).toString().replaceAll(BY_XPATH_LOC_START, "");
        } else {
            xpath = locatorPath.replaceAll(BY_XPATH_LOC_START, "");
        }
        return By.xpath(stringToAddBeforeXpath + xpath + stringToAddAfterXpath);
    }

    public By convertCssToXPath(String cssLocator) {
        if (cssLocator.startsWith("link=")) {
            // Si le sélecteur CSS commence par "link=", cela signifie qu'il s'agit d'un sélecteur linkText
            String linkText = cssLocator.substring(5);
            return By.xpath("//a[text()='" + linkText + "']");
        } else if (cssLocator.startsWith("partialLink=")) {
            // Si le sélecteur CSS commence par "partialLink=", cela signifie qu'il s'agit d'un sélecteur partialLinkText
            String partialLinkText = cssLocator.substring(12);
            return By.xpath("//a[contains(text(), '" + partialLinkText + "')]");
        } else {
            // Autres sélecteurs CSS
            StringBuilder xpathBuilder = new StringBuilder();
            String[] parts = cssLocator.split("\\s+");

            for (String part : parts) {
                if (part.startsWith("#")) { // Sélecteur d'ID
                    xpathBuilder.append("//*[@id='" + part.substring(1) + "']");
                } else if (part.startsWith(".")) { // Sélecteur de classe
                    xpathBuilder.append("//*[contains(@class, '" + part.substring(1) + "')]");
                } else if (part.contains("[")) { // Sélecteur d'attribut
                    String attributeName = part.substring(1, part.indexOf('='));
                    String attributeValue = part.substring(part.indexOf('=') + 1, part.indexOf(']'));
                    xpathBuilder.append("//*[" + attributeName + "='" + attributeValue + "']");
                } else if (part.startsWith(":")) { // Pseudo-sélecteurs CSS (non pris en charge dans la conversion)
                    throw new IllegalArgumentException("Pseudo-selectors like '" + part + "' are not supported in CSS to XPath conversion.");
                } else { // Sélecteur de balise
                    xpathBuilder.append("//" + part);
                }

                xpathBuilder.append("/");
            }

            // Supprimer le dernier "/" ajouté
            String xpath = xpathBuilder.toString();
            xpath = xpath.substring(0, xpath.length() - 1);

            return By.xpath(xpath);
        }
    }

    /**
     * renvoie le webElement container, en prenant en compte les containers des containers
     * @param timeout
     * @return
     */
    public WebElement findWebElementContainer(int timeout) {
        try {
            boolean hasContainer = container != null;
            if (hasContainer) {
                List<By> bys = new ArrayList<>();
                Element myContainer = container;
                while (hasContainer) {
                    bys.add(myContainer.getFindLocator());
                    myContainer = myContainer.getContainer();
                    hasContainer = myContainer != null;
                }
                WebElement weContainer = (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                        ExpectedConditions.presenceOfElementLocated(bys.get(bys.size() - 1))
                );
                for (int i = bys.size() - 2; i >= 0; i--) {
                    weContainer = (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                            ExpectedConditions.presenceOfNestedElementLocatedBy(weContainer, bys.get(i))
                    );
                }
                return weContainer;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * recherche un élément dans la page (caché, visible ou non) à partir de son locator dans un délai de TestProperties.timeOut
     * @return webElement selenium
     */
    public WebElement findElement() {
        return findElement(GlobalProp.getTimeOut());
    }

    /**
     * recherche un élément dans la page (caché, visible ou non) à partir de son locator dans un délai de timeout
     * @param timeout
     * @return
     */
    public WebElement findElement(int timeout) {
        if (GlobalProp.isSuiteOverTimeOut()) {
            this.report.log(Reporter.FAIL_STATUS_NO_SCREENSHOT, ELEMENT_NOT_FOUND_SUITE_TIME_OVER_LIMIT);
            return null;
        }
        WebElement monElement = null;
        try {
            if (container == null) {
                monElement = (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                        ExpectedConditions.presenceOfElementLocated(getFindLocator())
                );
            } else {
                monElement = (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                        ExpectedConditions.presenceOfNestedElementLocatedBy(findWebElementContainer(timeout), getFindLocator())
                );
            }
        } catch (Exception ignore) {
            // ignore
        }
        report.setPlaywrightCmd(false);
        report.setDriver(driver);
        return monElement;
    }

    /**
     * recherche tous les éléments dans la page (caché, visible ou non) à partir d'un locator dans un délai de TestProperties.timeOut
     * @return list de webElement selenium
     */
    public List<WebElement> findAllElements() {
        return findAllElements(GlobalProp.getTimeOut());
    }

    /**
     * recherche tous les éléments dans la page (caché, visible ou non) à partir d'un locator dans un délai de timeout
     * @return list de webElement selenium
     */
    public List<WebElement> findAllElements(int timeout) {
        timeout = timeout / 2;
        if (GlobalProp.isSuiteOverTimeOut()) {
            this.report.log(Reporter.FAIL_STATUS_NO_SCREENSHOT, ELEMENT_NOT_FOUND_SUITE_TIME_OVER_LIMIT);
            return new ArrayList<>();
        }

        List<WebElement> monElement = null;
        int nTry = 1;
        while ((monElement == null || monElement.isEmpty()) && nTry<=2) {
            if (nTry==2) break;
            try {
                if (container == null) {
                    monElement = (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                            ExpectedConditions.presenceOfAllElementsLocatedBy(getFindLocator())
                    );
                } else {
                    monElement = (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                            presenceOfNestedElementsLocatedBy(findWebElementContainer(timeout), getFindLocator())
                    );
                }
            } catch (Exception ignore) {
                // ignore
            }
            nTry++;
        }
        return monElement;
    }

    /**
     * recherche un élément clickable dans la page à partir de son locator dans un délai de TestProperties.timeOut
     * un scroll vers l'élément est fait une fois trouvé au cas où il soit caché derrière un autre élément type bandeau
     * @return webElement selenium
     */
    public WebElement findElementEnabled() {
        return findElementEnabled(GlobalProp.getTimeOut());
    }

    /**
     * recherche un élément clickable dans la page à partir de son locator dans un délai de timeout
     * un scroll vers l'élément est fait une fois trouvé au cas où il soit caché derrière un autre élément type bandeau
     * @return webElement selenium
     */
    public WebElement findElementEnabled(int timeout) {
        if (GlobalProp.isSuiteOverTimeOut()) {
            this.report.log(Reporter.FAIL_STATUS_NO_SCREENSHOT, ELEMENT_NOT_FOUND_SUITE_TIME_OVER_LIMIT);
            return null;
        }
        WebElement monElement = findElement(timeout);
        if (monElement != null) {
            try {
                if (container == null) {
                    monElement = (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                            ExpectedConditions.elementToBeClickable(getFindLocator())
                    );
                } else {
                    monElement = (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                            ExpectedConditions.elementToBeClickable(
                                    (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                                            ExpectedConditions.presenceOfNestedElementLocatedBy(findWebElementContainer(timeout), getFindLocator())
                                    ))
                    );
                }
            } catch (Exception e) {
                monElement = findElement(0);
            }
        }
        report.setPlaywrightCmd(false);
        report.setDriver(driver);
        return monElement;
    }

    /**
     * recherche un élément visible dans la page à partir de son locator dans un délai de TestProperties.timeOut
     * @return webElement selenium
     */
    public WebElement findElementDisplayed() {
        return findElementDisplayed(GlobalProp.getTimeOut());
    }

    /**
     * recherche un élément visible dans la page à partir de son locator dans un délai de timeout
     * @return webElement selenium
     */
    public WebElement findElementDisplayed(int timeout) {
        if (GlobalProp.isSuiteOverTimeOut()) {
            this.report.log(Reporter.FAIL_STATUS_NO_SCREENSHOT, ELEMENT_NOT_FOUND_SUITE_TIME_OVER_LIMIT);
            return null;
        }
        WebElement monElement = null;
        try {
            if (container == null) {
                monElement = (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                        ExpectedConditions.visibilityOfElementLocated(getFindLocator())
                );
            } else {
                monElement = (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                        ExpectedConditions.visibilityOf(
                                (new WebDriverWait(this.driver, Duration.ofSeconds(timeout))).until(
                                        ExpectedConditions.presenceOfNestedElementLocatedBy(findWebElementContainer(timeout), getFindLocator())
                                ))
                );
            }
        } catch (Exception ignore) {
            monElement = findElement(0);
        }
        report.setPlaywrightCmd(false);
        report.setDriver(driver);
        return monElement;
    }

    /**
     * recherche un élément visible dans la page à partir de son locator sans attendre
     * @return webElement selenium
     */
    WebElement findElementNow() {
        return findElementDisplayed(0);
    }


    private static ExpectedCondition<List<WebElement>> presenceOfNestedElementsLocatedBy(final WebElement parent, final By childLocator) {
        return new ExpectedCondition<List<WebElement>>() {
            public List<WebElement> apply(WebDriver driver) {
                List<WebElement> allChildren = parent.findElements(childLocator);
                return allChildren.isEmpty() ? null : allChildren;
            }

            public String toString() {
                return String.format("visibility of element located by %s -> %s", parent, childLocator);
            }
        };
    }

}
