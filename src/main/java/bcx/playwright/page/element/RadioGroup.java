package bcx.playwright.page.element;

import bcx.playwright.test.TestContext;
import com.microsoft.playwright.Locator;

import java.util.Map;


/**
 * classe gérant les actions et assertions sur les élément de type liste : UL/LI ou SELECT/OPTION
 * Ne pas modifier dans le cadre d'une appli : modifier la classe ElementList qui en hérite
 * @author bcx
 *
 */
public class RadioGroup extends BaseElement {
    private static final String OPTION_PARAM = "{OPTION}";
    public static final String POTENTIAL_TYPE = "div[class='radio-group']";
    final BaseElement radioElement = new BaseElement(this.getTestContext(), "Option '"+ OPTION_PARAM +"' dans le radio-group '" + this.getName() + "'", this.getPage().locator("//label[normalize-space()=\"" + OPTION_PARAM + "\"]")).setContainer(this);

    /**
     * constructeur de l'élément
     * @param testContext    : page de l'élément
     * @param name : nom de l'élément
     * @param locator : locator de l'élément
     */
    public RadioGroup(TestContext testContext, String name, Locator locator) {
        super(testContext, name, locator);
    }

    @Override
    public RadioGroup setContainer(BaseElement container) {
        super.setContainer(container);
        setPotentialType(POTENTIAL_TYPE);
        return this;
    }

    @Override
    public RadioGroup resetContainer() {
        super.resetContainer();
        return this;
    }

    @Override
    public RadioGroup injectValues(Map<String, String> params) {
        super.injectValues(params);
        return this;
    }

    @Override
    public RadioGroup injectValues(String key, String value) {
        super.injectValues(key, value);
        return this;
    }

    /////////////// ACTION
    /**
     * selection une valeur dans un radio group
     * @param option à sélectionner
     */
    @Override
    public void setValue(String option) {
        radioElement.injectValues(OPTION_PARAM, option).click();
    }

    @Override
    public void assertValue(String option) {
        radioElement.injectValues(OPTION_PARAM, option).assertAttributeContains("class", "selected");
    }
}

