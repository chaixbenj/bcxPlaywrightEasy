package bcx.playwright.page.element;

import bcx.playwright.test.TestContext;
import com.microsoft.playwright.Locator;
import bcx.playwright.properties.GlobalProp;
import bcx.playwright.report.Reporter;
import bcx.playwright.util.data.DataUtil;

import java.util.Arrays;
import java.util.Map;


/**
 * classe gérant les actions et assertions sur les élément de type liste : UL/LI ou SELECT/OPTION
 * Ne pas modifier dans le cadre d'une appli : modifier la classe ElementList qui en hérite
 * @author bcx
 *
 */
public class ElementList extends BaseElement {
    private static final String OPTION_PARAM = "{OPTION}";
    public static final String SELECTED = "selected";
    public static final String POTENTIAL_TYPE = "select,ul";
    final BaseElement liElement = new BaseElement(this.getTestContext(), " Option '"+ OPTION_PARAM +"'", this.getPage().locator("//li[@*=\""+ OPTION_PARAM + "\" or contains(.,\""+ OPTION_PARAM +"\")]")).setContainer(this);
    final BaseElement optionElement = new BaseElement(this.getTestContext(), " Option '"+ OPTION_PARAM +"'", this.getPage().locator("//option[contains(.,\""+ OPTION_PARAM +"\") or contains(@value,\""+ OPTION_PARAM +"\")]")).setContainer(this);


    /**
     * constructeur de l'élément
     * @param testContext    : contexte de test
     * @param name : nom de l'élément
     * @param locator : locator de l'élément
     */
    public ElementList(TestContext testContext, String name, Locator locator) {
        super(testContext, name, locator);
        setPotentialType(POTENTIAL_TYPE);
    }


    @Override
    public ElementList setContainer(BaseElement container) {
        super.setContainer(container);
        return this;
    }

    @Override
    public ElementList resetContainer() {
        super.resetContainer();
        return this;
    }

    @Override
    public ElementList injectValues(Map<String, String> params) {
        super.injectValues(params);
        return this;
    }

    @Override
    public ElementList injectValues(String key, String value) {
        super.injectValues(key, value);
        return this;
    }


    /////////////// ACTION
    /**
     * selection une valeur dans liste de type UL/LI ou SELECT/OPTION
     * @param option à sélectionner
     */
    @Override
    public void setValue(String option) {
        String action = "selectInList";
        if (option!=null) {
            try {
                if (this.getTagName().equalsIgnoreCase("SELECT")) {
                    this.selectOption(option);
                } else {
                    BaseElement optionEl = getOptionElement(option);
                    boolean isSelected = this.getValue().contains(DataUtil.normalizeSpace(option)) || optionEl.getAttribute(SELECTED).equals("true");
                    startTry(action);
                    while (!isSelected && !stopTry(GlobalProp.getTimeOut(), action)) {

                        this.click();
                        optionEl.click();
                    }
                    isSelected = this.getValue().contains(DataUtil.normalizeSpace(option)) || optionEl.getAttribute(SELECTED).equals("true");

                    String result = (isSelected ? Reporter.PASS_STATUS : Reporter.FAIL_STATUS);
                    if (!result.equals(Reporter.PASS_STATUS)) this.getTestContext().getReport().log(result, action + " >> " + option , this, null , null, null, null);
                }

            } catch (Exception e) {
                this.getTestContext().getReport().log(Reporter.ERROR_STATUS, action + " >> " + option , this, null , null, null, e);
            }

        }
    }

    /**
     * vérifie que des options sont proposés dans la liste ou pas;
     *
     * @param options : liste des options
     * @param inList : boolean true = proposé
     */
    public void assertOptionsInList(String[] options, boolean inList) {
        String action = "assertOptionsInList";
        if (options==null) return;
        for(String valueToAssert: options) {
            if (valueToAssert!=null) {
                BaseElement option = getOptionElement(valueToAssert);
                option.assertExists(inList);
            } else {
                this.getTestContext().getReport().log((inList?Reporter.FAIL_NEXT_STATUS:Reporter.PASS_STATUS), action + " " + inList + " >> " + Arrays.toString(options), this, null , null, null);
            }
        }

    }

    /**
     * verifie la taille de la liste
     * @param size
     */
    public void assertListSize(int size) {
        BaseElement options = getOptionElement("");
        getReport().assertEquals("Vérification nombre d'option dans la liste", size, options.count());
    }

    /**
     * Gère la création d'un élément d'option basé sur la balise HTML.
     */
    private BaseElement getOptionElement(String valueToAssert) {
        return this.getTagName().equalsIgnoreCase("SELECT")
                ? optionElement.injectValues(OPTION_PARAM, valueToAssert)
                : liElement.injectValues(OPTION_PARAM, valueToAssert);
    }
}

