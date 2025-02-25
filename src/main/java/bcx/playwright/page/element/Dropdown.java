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
public class Dropdown extends BaseElement {
    private static final String OPTION_PARAM = "{OPTION}";
    public static final String POTENTIAL_TYPE = "div[class='dropdown']";
    public static final String ITEM_POTENTIAL_TYPE = "label > input";
    final BaseElement item = new BaseElement(this.getTestContext(), "item '"+OPTION_PARAM+"' dans la dropdown '" + this.getName(), this.getPage().locator("//div[@class='dropdown-options']/div[text()='"+OPTION_PARAM+"']")).setContainer(this);
    final BaseElement selectedItem = new BaseElement(this.getTestContext(), "item selectionné dans la dropdown '" + this.getName(), this.getPage().locator("//div[contains(@class,'selected')]")).setContainer(this);


    /**
     * constructeur de l'élément
     * @param testContext    : contexte de test
     * @param name : nom de l'élément
     * @param locator : locator de l'élément
     */
    public Dropdown(TestContext testContext, String name, Locator locator) {
        super(testContext, name, locator);
        this.setPotentialType(POTENTIAL_TYPE);
        item.setPotentialType(ITEM_POTENTIAL_TYPE);
    }

    @Override
    public Dropdown setContainer(BaseElement container) {
        super.setContainer(container);
        return this;
    }

    @Override
    public Dropdown resetContainer() {
        super.resetContainer();
        return this;
    }

    @Override
    public Dropdown injectValues(Map<String, String> params) {
        super.injectValues(params);
        return this;
    }

    @Override
    public Dropdown injectValues(String key, String value) {
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
           this.click();
           item.injectValues(OPTION_PARAM, option).click();
    }

    @Override
    public void assertValue(String option) {
        selectedItem.assertValue(option);
    }
}

