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
public class EntryElement extends BaseElement {
    public static final String POTENTIAL_TYPE = "input,textarea";


    /**
     * constructeur de l'élément
     * @param testContext    : contexte de test
     * @param name : nom de l'élément
     * @param locator : locator de l'élément
     */
    public EntryElement(TestContext testContext, String name, Locator locator) {
        super(testContext, name, locator);
        this.setPotentialType(POTENTIAL_TYPE);
    }

    @Override
    public EntryElement setContainer(BaseElement container) {
        super.setContainer(container);
        return this;
    }

    @Override
    public EntryElement resetContainer() {
        super.resetContainer();
        return this;
    }

    @Override
    public EntryElement injectValues(Map<String, String> params) {
        super.injectValues(params);
        return this;
    }

    @Override
    public EntryElement injectValues(String key, String value) {
        super.injectValues(key, value);
        return this;
    }
}

