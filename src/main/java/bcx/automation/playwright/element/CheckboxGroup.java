package bcx.automation.playwright.element;

import bcx.automation.test.TestContext;
import com.microsoft.playwright.Locator;
import java.util.Map;


/**
 * classe gérant les actions et assertions sur les groupes de checkbox
 * @author bcx
 *
 */
public class CheckboxGroup extends BaseElement {
    private static final String OPTION_PARAM = "{OPTION}";
    public static final String POTENTIAL_TYPE = "div[class='checkbox-group']";
    final BaseElement chkbxElement = new BaseElement(this.getTestContext(), "option '"+OPTION_PARAM+"' dans le checkbox-group '"+ this.getName() +"'", this.getPage().getByText(OPTION_PARAM)).setContainer(this);


    /**
     * constructeur de l'élément
     * @param testContext    : contexte de test
     * @param name : nom de l'élément
     * @param locator : locator de l'élément
     */
    public CheckboxGroup(TestContext testContext, String name, Locator locator) {
        super(testContext, name, locator);
        this.setPotentialType(POTENTIAL_TYPE);
    }

    @Override
    public CheckboxGroup setContainer(BaseElement container) {
        super.setContainer(container);
        return this;
    }

    @Override
    public CheckboxGroup resetContainer() {
        super.resetContainer();
        return this;
    }

    @Override
    public CheckboxGroup injectValues(Map<String, String> params) {
        super.injectValues(params);
        return this;
    }

    @Override
    public CheckboxGroup injectValues(String key, String value) {
        super.injectValues(key, value);
        return this;
    }

    /////////////// ACTION
    /**
     * selection une valeur dans un radio group
     * @param options à sélectionner
     */
    @Override
    public void setValue(String options) {
       for (String option : options.split(",")) {
           chkbxElement.injectValues(OPTION_PARAM, option);
           boolean selected = chkbxElement.getAttribute("class") !=null && chkbxElement.getAttribute("class").contains("selected");
           if (!selected) {
               chkbxElement.click();
           }
       }
    }

    @Override
    public void assertValue(String option) {
        chkbxElement.injectValues(OPTION_PARAM, option).assertAttributeContains("class", "selected");
    }
}

