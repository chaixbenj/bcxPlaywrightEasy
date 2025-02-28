package bcx.automation.playwright.element;

import bcx.automation.properties.GlobalProp;
import bcx.automation.report.Reporter;
import bcx.automation.test.TestContext;
import bcx.automation.util.ScriptConstants;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import bcx.automation.util.data.DataUtil;
import com.microsoft.playwright.options.LoadState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe de base pour les éléments de la page utilisant Playwright.
 */
@Slf4j
public class BaseElement {
    public static final String ELEMENT_NON_TROUVE = "élément non trouvé";

    @Getter
    private final TestContext testContext;
    @Getter
    private final Page page; // Référence à la page
    @Getter
    private final Reporter report;
    @Getter
    private String name; // Nom de l'élément
    private final String initName; // Nom de l'élément initial
    @Getter
    private Locator locator; // Locator
    private final List<String> locatorType; // Type du locator
    private final List<String> selector; // Selecteur
    private Object container; // Conteneur de l'élément (Page ou Locator)
    @Getter
    private Locator locatorInContainer; // Locator dans le conteneur
    @Setter
    @Getter
    private String potentialType;
    private String potentialElement;
    private final HashMap<String, LocalDateTime> dateStartSearch = new HashMap<>();

    /**
     * Constructeur de la classe BaseElement.
     *
     * @param testContext Le contexte de test.
     * @param name Le nom de l'élément.
     * @param locator Le locator de l'élément.
     */
    public BaseElement(TestContext testContext, String name, Locator locator) {
        this.testContext = testContext;
        this.page = testContext.getPage();
        this.report = testContext.getReport();
        this.name = name;
        this.initName = name;
        this.locator = locator;
        List<String>[] typeAndSelector = extractSelectorsWithTypes(locator.toString());
        this.locatorType = typeAndSelector[0];
        this.selector = typeAndSelector[1];
        this.container = page;
        this.locatorInContainer = locator;
        this.potentialType = "*";
    }

    /**
     * Enregistre l'heure de début d'une action pour pouvoir lui appliquer un timeout.
     *
     * @param from La source de l'action.
     */
    public void startTry(String from) {
        dateStartSearch.remove(from);
        dateStartSearch.put(from, LocalDateTime.now());
    }

    /**
     * Indique si le temps imparti pour l'action est écoulé.
     *
     * @param timeout Le timeout en secondes.
     * @param from La source de l'action.
     * @return Vrai si le temps est écoulé, faux sinon.
     */
    public boolean stopTry(int timeout, String from) {
        try {
            if (dateStartSearch.get(from) == null) dateStartSearch.put(from, LocalDateTime.now());
            return (report.isTimePreviousLogOlderThan(500) || dateStartSearch.get(from).plusSeconds(timeout).isBefore(LocalDateTime.now()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Définit le conteneur de l'élément.
     *
     * @param element L'élément conteneur.
     * @return L'instance courante de BaseElement.
     */
    public BaseElement setContainer(BaseElement element) {
        this.container = element.getLocator();
        this.locatorInContainer = getLocatorFromSelector(locatorType, selector, true);
        return this;
    }

    /**
     * Réinitialise le conteneur de l'élément à la page.
     *
     * @return L'instance courante de BaseElement.
     */
    public BaseElement resetContainer() {
        this.container = page;
        this.locatorInContainer = getLocatorFromSelector(locatorType, selector, false);
        return this;
    }

    /**
     * Injecte des valeurs dans les sélecteurs de l'élément.
     *
     * @param key La clé de la valeur à injecter.
     * @param value La valeur à injecter.
     * @return L'instance courante de BaseElement.
     */
    public BaseElement injectValues(String key, String value) {
        return injectValues(Map.of(key, value));
    }

    /**
     * Injecte des valeurs dans les sélecteurs de l'élément.
     *
     * @param values La map des valeurs à injecter.
     * @return L'instance courante de BaseElement.
     */
    public BaseElement injectValues(Map<String, String> values) {
        List<String> selector = new ArrayList<>();
        for (int i = 0; i < this.selector.size(); i++) {
            selector.add(i == this.selector.size() - 1 ? DataUtil.replacePlaceholders(this.selector.get(i), values) : this.selector.get(i));
        }
        this.locator = getLocatorFromSelector(this.locatorType, selector, false);
        this.locatorInContainer = getLocatorFromSelector(this.locatorType, selector, true);
        this.name = DataUtil.replacePlaceholders(this.initName, values);
        return this;
    }

    /**
     * Trouve l'élément dans la page.
     *
     * @param findPotentialElement Indique si l'on doit chercher un élément potentiel.
     * @return L'élément trouvé ou null.
     */
    public Object find(boolean findPotentialElement) {
        Loader.waitNotVisible();
        try {
            getLocatorInContainer().first().waitFor(new Locator.WaitForOptions().setTimeout(GlobalProp.getTimeOut() * 1000));
            return getLocator();
        } catch (Exception error) {
            try {
                Object elementProbable = findPotentialElement(findPotentialElement);
                if (elementProbable != null) {
                    return elementProbable;
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
        return null;
    }

    /**
     * Trouve un élément potentiel dans la page.
     *
     * @param findPotentialElement Indique si l'on doit chercher un élément potentiel.
     * @return L'élément potentiel trouvé ou null.
     */
    public Object findPotentialElement(boolean findPotentialElement) {
        if (container != page) {
            try {
                int nbElem = locator.count();
                if (nbElem == 1) return locator;
            } catch (Exception ignore) {
                // ignore
            }
        }
        this.report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "L'élément '" + getName() + "' avec le locator " + locator + " est introuvable");
        if (GlobalProp.isFindPotentialElementJS() && findPotentialElement) {
            this.report.log(Reporter.INFO_STATUS, "Le test recherche un élément approchant pour continuer");
            try {
                return findPotentialElementWithJS();
            } catch (Exception ignore) {
                // ignore
            }
        }
        return null;
    }

    /**
     * Trouve le premier élément dans la page.
     *
     * @param findPotentialElement Indique si l'on doit chercher un élément potentiel.
     * @return Le premier élément trouvé ou null.
     */
    public Object findFirst(boolean findPotentialElement) {
        Object firstElement = find(findPotentialElement);
        if (firstElement instanceof Locator) {
            return ((Locator) firstElement).first();
        } else {
            return firstElement;
        }
    }

    /**
     * Vérifie si l'élément existe dans la page.
     *
     * @return Vrai si l'élément existe, faux sinon.
     */
    public boolean exists() {
        return find(false) != null;
    }

    /**
     * Vérifie si l'élément est visible.
     *
     * @return Vrai si l'élément est visible, faux sinon.
     */
    public boolean isVisible() {
        Object element = findFirst(false);
        if (element instanceof Locator) {
            return ((Locator) element).isVisible();
        } else if (element instanceof ElementHandle) {
            return ((ElementHandle) element).isVisible();
        }
        return false;
    }

    /**
     * Vérifie si l'élément est activé.
     *
     * @return Vrai si l'élément est activé, faux sinon.
     */
    public boolean isEnabled() {
        Object element = findFirst(false);
        if (element instanceof Locator) {
            return ((Locator) element).isEnabled();
        } else if (element instanceof ElementHandle) {
            return ((ElementHandle) element).isEnabled();
        }
        return false;
    }

    /**
     * Vérifie si l'élément est requis.
     *
     * @return Vrai si l'élément est requis, faux sinon.
     */
    public boolean isRequired() {
        return getAttribute("required") != null;
    }

    /**
     * Récupère le nom de la balise de l'élément.
     *
     * @return Le nom de la balise ou null si l'élément n'est pas trouvé.
     */
    public String getTagName() {
        Object element = findFirst(false);
        if (element instanceof Locator) {
            return ((Locator) element).evaluate("el => el.tagName").toString();
        } else if (element instanceof ElementHandle) {
            return ((ElementHandle) element).evaluate("el => el.tagName").toString();
        }
        return null;
    }

    /**
     * Compte le nombre d'éléments correspondant au locator.
     *
     * @return Le nombre d'éléments trouvés.
     */
    public int count() {
        Object element = find(false);
        if (element instanceof Locator) {
            return ((Locator) element).count();
        } else if (element instanceof ElementHandle) {
            return 1;
        }
        return 0;
    }

    /**
     * Récupère la valeur de l'élément.
     *
     * @return La valeur de l'élément ou "élément non trouvé" si l'élément n'est pas trouvé.
     */
    public String getValue() {
        Object element = findFirst(true);
        if (element instanceof Locator) {
            return ((Locator) element).inputValue();
        } else if (element instanceof ElementHandle) {
            return ((ElementHandle) element).getAttribute("value");
        }
        return ELEMENT_NON_TROUVE;
    }

    /**
     * Récupère la valeur d'un attribut de l'élément.
     *
     * @param attributeName Le nom de l'attribut.
     * @return La valeur de l'attribut ou "élément non trouvé" si l'élément n'est pas trouvé.
     */
    public String getAttribute(String attributeName) {
        Object element = findFirst(true);
        if (element instanceof Locator) {
            return String.valueOf(((Locator) element).getAttribute(attributeName));
        } else if (element instanceof ElementHandle) {
            return String.valueOf(((ElementHandle) element).getAttribute(attributeName));
        }
        return ELEMENT_NON_TROUVE;
    }

    /**
     * Exécute une action sur l'élément.
     *
     * @param actionName Le nom de l'action.
     * @param findPotentialElement Indique si l'on doit chercher un élément potentiel.
     * @param ignoreFailure Indique si l'on doit ignorer les échecs.
     * @param locatorAction L'action à exécuter sur le locator.
     * @param elementHandleAction L'action à exécuter sur l'élément.
     */
    private void performAction(String actionName, boolean findPotentialElement, boolean ignoreFailure, Consumer<Locator> locatorAction, Consumer<ElementHandle> elementHandleAction) {
        log.info(actionName + " " + name);
        String status = Reporter.FAIL_STATUS;
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        Exception exception = null;
        String message = null;
        try {
            Object element = findFirst(findPotentialElement);

            if (element instanceof Locator) {
                locatorAction.accept((Locator) element);
                status = Reporter.PASS_STATUS;
            } else if (element instanceof ElementHandle) {
                elementHandleAction.accept((ElementHandle) element);
                status = Reporter.PASS_STATUS;
            } else {
                message = ELEMENT_NON_TROUVE;
                status = findPotentialElement ? status : Reporter.FAIL_NEXT_STATUS;
            }

        } catch (Exception e) {
            exception = e;
        }
        if (!ignoreFailure || status.equals(Reporter.PASS_STATUS)) {
            this.report.log(status, actionName, name, null, null, message, exception);
        }
    }

    /**
     * Clique sur l'élément.
     */
    public void click() {
        performAction("click", true, false,
                Locator::click,
                ElementHandle::click
        );
    }

    /**
     * Double-clique sur l'élément.
     */
    public void doubleClick() {
        performAction("doubleClick", true, false,
                Locator::dblclick,
                ElementHandle::dblclick
        );
    }

    /**
     * Clique sur l'élément si possible.
     */
    public void clickIfPossible() {
        performAction("clickIfPossible", false, true,
                Locator::click,
                ElementHandle::click
        );
    }

    /**
     * Définit la valeur de l'élément.
     *
     * @param value La valeur à définir.
     */
    public void setValue(String value) {
        performAction("setValue '" + value + "'", true, false,
                locator -> {
                    locator.clear();
                    locator.fill(value);
                },
                elementHandle -> {
                    elementHandle.press("Control+A");
                    elementHandle.press("Backspace");
                    elementHandle.fill(value);
                }
        );
    }

    /**
     * Sélectionne une option dans l'élément.
     *
     * @param value La valeur de l'option à sélectionner.
     */
    public void selectOption(String value) {
        performAction("setValue '" + value + "'", true, false,
                locator -> {
                    locator.selectOption(value);
                },
                elementHandle -> {
                    elementHandle.selectOption(value);
                }
        );
    }

    /**
     * Vérifie que la valeur de l'élément est égale à la valeur attendue.
     *
     * @param value La valeur attendue.
     */
    public void assertValue(String value) {
        log.info("assertValue " + name + " >> " + value);
        String actualValue = getValue();
        String status = value.equals(actualValue) ? Reporter.PASS_STATUS : Reporter.FAIL_NEXT_STATUS;
        this.report.log(status, "assertValue", name, value, actualValue, null);
    }

    /**
     * Vérifie que la valeur de l'élément contient la valeur attendue.
     *
     * @param value La valeur attendue.
     */
    public void assertValueContains(String value) {
        log.info("assertValueContains " + name + " >> " + value);
        String actualValue = getValue();
        String status = actualValue.contains(value) ? Reporter.PASS_STATUS : Reporter.FAIL_NEXT_STATUS;
        this.report.log(status, "assertValueContains", name, value, actualValue, null);
    }

    /**
     * Vérifie que la valeur de l'élément ne contient pas la valeur attendue.
     *
     * @param value La valeur attendue.
     */
    public void assertValueDoesntContain(String value) {
        log.info("assertValueDoesntContains " + name + " >> " + value);
        String actualValue = getValue();
        String status = actualValue.contains(value) ? Reporter.FAIL_NEXT_STATUS : Reporter.PASS_STATUS;
        this.report.log(status, "assertValueContains", name, value, actualValue, null);
    }

    /**
     * Vérifie que la valeur d'un attribut de l'élément est égale à la valeur attendue.
     *
     * @param attributeName Le nom de l'attribut.
     * @param value La valeur attendue.
     */
    public void assertAttribute(String attributeName, String value) {
        log.info("assertAttribute " + name + " >> " + attributeName + " >> " + value);
        String actualValue = getAttribute(attributeName);
        String status = value.equals(actualValue) ? Reporter.PASS_STATUS : Reporter.FAIL_NEXT_STATUS;
        this.report.log(status, "assertAttribute", name, attributeName, value, actualValue, null);
    }

    /**
     * Vérifie que la valeur d'un attribut de l'élément contient la valeur attendue.
     *
     * @param attributeName Le nom de l'attribut.
     * @param value La valeur attendue.
     */
    public void assertAttributeContains(String attributeName, String value) {
        log.info("assertAttributeContains " + name + " >> " + attributeName + " >> " + value);
        String actualValue = getAttribute(attributeName);
        String status = actualValue.contains(value) ? Reporter.PASS_STATUS : Reporter.FAIL_NEXT_STATUS;
        this.report.log(status, "assertAttributeContains", name, attributeName, value, actualValue, null);
    }

    /**
     * Vérifie que l'élément est visible ou non.
     *
     * @param visible Vrai si l'élément doit être visible, faux sinon.
     */
    public void assertVisible(boolean visible) {
        log.info("assertVisible " + name + " >> " + visible);
        String status = visible == isVisible() ? Reporter.PASS_STATUS : Reporter.FAIL_NEXT_STATUS;
        this.report.log(status, "assertVisible", name, String.valueOf(visible), null, null);
    }

    /**
     * Vérifie que l'élément existe ou non.
     *
     * @param exists Vrai si l'élément doit exister, faux sinon.
     */
    public void assertExists(boolean exists) {
        log.info("assertExists " + name + " >> " + exists);
        String status = exists == exists() ? Reporter.PASS_STATUS : Reporter.FAIL_NEXT_STATUS;
        this.report.log(status, "assertExists", name, String.valueOf(exists), null, null);
    }

    /**
     * Vérifie que l'élément est activé ou non.
     *
     * @param enabled Vrai si l'élément doit être activé, faux sinon.
     */
    public void assertEnabled(boolean enabled) {
        log.info("assertEnabled " + name + " >> " + enabled);
        boolean existsAndEnabled = exists() && isEnabled();
        String status = enabled == existsAndEnabled ? Reporter.PASS_STATUS : Reporter.FAIL_NEXT_STATUS;
        this.report.log(status, "assertEnabled", name, String.valueOf(enabled), null, null);
    }

    /**
     * Vérifie que l'élément est requis ou non.
     *
     * @param required Vrai si l'élément doit être requis, faux sinon.
     */
    public void assertRequired(boolean required) {
        log.info("assertRequired " + name + " >> " + required);
        boolean requiredAndExists = exists() && isRequired();
        String status = required == requiredAndExists ? Reporter.PASS_STATUS : Reporter.FAIL_NEXT_STATUS;
        this.report.log(status, "assertRequired", name, String.valueOf(required), null, null);
    }

    /**
     * Recherche un élément potentiel dans la page à partir de son nom.
     *
     * @return L'élément potentiel trouvé ou null.
     */
    public ElementHandle findPotentialElementWithJS() {
        String script = ScriptConstants.FIND_POTENTIAL_ELEMENT_SCRIPT;

        StringBuilder potentialAttributes = new StringBuilder();
        boolean firstPotentialAttribute = true;
        for (String attr : GlobalProp.getAttributesPotentialElement().split(",")) {
            potentialAttributes.append(firstPotentialAttribute ? "" : ", ");
            potentialAttributes.append("input.getAttribute('");
            potentialAttributes.append(attr.trim());
            potentialAttributes.append("')");
            firstPotentialAttribute = false;
        }

        String tagNames = potentialType.replace("'", "\\'");

        script = script.replace("{tagNames}", tagNames).replace("{potentialAttributes}", potentialAttributes);

        ElementHandle element = null;
        try {
            element = (ElementHandle) page.evaluateHandle(script, this.getName().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (element != null) {
            String newPotentialElement = (String) element.evaluate("el => el.outerHTML");
            if (potentialElement == null || !potentialElement.equals(newPotentialElement)) {
                report.setCurrentElement(element);
                report.takeScreenShot("Elément potentiel trouvé pour l'élément \"" + this.getName() + "\" (locator inital " + this.getLocator() + ") : <BR>" + newPotentialElement.replace("<", "&lt;").replace(">", "&gt;"), true);
            }
            potentialElement = newPotentialElement;
        } else {
            this.report.log(Reporter.FAIL_STATUS, "Aucun élément trouvé pour \"" + this.getName() + "\" avec le locator " + this.getLocator());
        }
        return element;
    }

    /**
     * Récupère un locator à partir d'un sélecteur.
     *
     * @param typeLocator Le type du locator.
     * @param selector Le sélecteur.
     * @param inContainer Indique si le locator est dans un conteneur.
     * @return Le locator correspondant.
     */
    private Locator getLocatorFromSelector(String typeLocator, String selector, boolean inContainer) {
        Object context = inContainer ? container : page;

        try {
            Method method = context.getClass().getMethod(typeLocator, String.class);
            return (Locator) method.invoke(context, selector);
        } catch (Exception e) {
            throw new RuntimeException("Invalid locator type: " + typeLocator, e);
        }
    }

    /**
     * Récupère un locator à partir d'une liste de types de locators et de sélecteurs.
     *
     * @param locatorTypes La liste des types de locators.
     * @param selectors La liste des sélecteurs.
     * @param inContainer Indique si le locator est dans un conteneur.
     * @return Le locator correspondant.
     */
    private Locator getLocatorFromSelector(List<String> locatorTypes, List<String> selectors, boolean inContainer) {
        Locator locator = null;
        for (int i = 0; i < locatorTypes.size(); i++) {
            if (i == 0) {
                locator = getLocatorFromSelector(locatorTypes.get(i), selectors.get(i), inContainer);
            } else {
                locator = locator.locator(getCssSelectorFromLocator(locatorTypes.get(i), selectors.get(i)));
            }
        }
        return locator;
    }

    /**
     * Récupère un sélecteur CSS à partir d'un type de locator et d'un sélecteur.
     *
     * @param typeLocator Le type du locator.
     * @param selector Le sélecteur.
     * @return Le sélecteur CSS correspondant.
     */
    private String getCssSelectorFromLocator(String typeLocator, String selector) {
        switch (typeLocator) {
            case "getByRole":
                return "[role='" + selector + "']";
            case "getByText":
                return "[text()='" + selector + "']";
            case "getByLabel":
                return "label[text()='" + selector + "']";
            case "getByPlaceholder":
                return "[placeholder='" + selector + "']";
            case "getByAltText":
                return "[alt='" + selector + "']";
            case "getByTitle":
                return "[title='" + selector + "']";
            case "getByTestId":
                return "[data-test-id='" + selector + "']";
            case "locator":
                return selector;
            default:
                return "";
        }
    }

    /**
     * Extrait les sélecteurs et leurs types à partir d'une chaîne de locator.
     *
     * @param locatorString La chaîne de locator.
     * @return Une liste contenant les types de locators et les sélecteurs.
     */
    public static List<String>[] extractSelectorsWithTypes(String locatorString) {
        List<String> resultType = new ArrayList<>();
        List<String> resultLocator = new ArrayList<>();
        List<String> parts = splitSafely(locatorString);

        Pattern pattern = Pattern.compile(
                "role=(\\w+)|text=\\\"(.*?)\\\"i|label=\\\"(.*?)\\\"i|attr=\\[placeholder=\\\"(.*?)\\\"i\\]|" +
                        "attr=\\[alt=\\\"(.*?)\\\"i\\]|attr=\\[title=\\\"(.*?)\\\"i\\]|attr=\\[data-test-id=\\\"(.*?)\\\"]|Locator=(.+)"
        );

        for (String part : parts) {
            Matcher matcher = pattern.matcher(part.trim());
            if (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    if (matcher.group(i) != null) {
                        String type = getTypeFromGroupIndex(i);
                        resultType.add(type);
                        resultLocator.add(matcher.group(i).replace("Locator@", ""));
                        break;
                    }
                }
            }
        }
        return new List[]{resultType, resultLocator};
    }

    /**
     * Récupère le type de locator à partir de l'index du groupe de correspondance.
     *
     * @param index L'index du groupe de correspondance.
     * @return Le type de locator correspondant.
     */
    private static String getTypeFromGroupIndex(int index) {
        switch (index) {
            case 1:
                return "getByRole";
            case 2:
                return "getByText";
            case 3:
                return "getByLabel";
            case 4:
                return "getByPlaceholder";
            case 5:
                return "getByAltText";
            case 6:
                return "getByTitle";
            case 7:
                return "getByTestId";
            case 8:
            default:
                return "locator";
        }
    }

    /**
     * Divise une chaîne de locator en parties en tenant compte des guillemets, crochets et parenthèses.
     *
     * @param locatorString La chaîne de locator.
     * @return La liste des parties de la chaîne de locator.
     */
    private static List<String> splitSafely(String locatorString) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;
        int bracketCount = 0;
        int parenthesisCount = 0;

        for (int i = 0; i < locatorString.length(); i++) {
            char c = locatorString.charAt(i);

            if (c == '"') {
                insideQuotes = !insideQuotes;
            } else if (c == '[') {
                bracketCount++;
            } else if (c == ']') {
                bracketCount--;
            } else if (c == '(') {
                parenthesisCount++;
            } else if (c == ')') {
                parenthesisCount--;
            }

            if (!insideQuotes && bracketCount == 0 && parenthesisCount == 0 && locatorString.startsWith(" >> ", i)) {
                result.add(current.toString().trim());
                current.setLength(0);
                i += 2;
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            if (current.toString().contains("internal:")) {
                result.add(current.toString().trim());
            } else {
                result.add("Locator=" + current.toString().trim());
            }
        }

        return result;
    }
}
