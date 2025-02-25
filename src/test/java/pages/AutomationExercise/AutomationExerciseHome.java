package pages.AutomationExercise;

import bcx.playwright.page.BasePage;
import bcx.playwright.page.element.BaseElement;
import bcx.playwright.properties.EnvProp;
import bcx.playwright.test.TestContext;

public class AutomationExerciseHome extends BasePage {
    public final BaseElement autoriser = new BaseElement(this.getTestContext(), "autoriser", this.getPage().locator("//button[@aria-label=\"Autoriser\" or @aria-label=\"Consent\"]"));
    public final BaseElement signupLogin = new BaseElement(this.getTestContext(), "signup / login", this.getPage().locator("//a[@href='/login']"));
    public final BaseElement deleteAccount = new BaseElement(this.getTestContext(), "delete account", this.getPage().locator("//a[@href='/delete_account']"));
    public final BaseElement loggedInAs = new BaseElement(this.getTestContext(), "logged In As {0}", this.getPage().locator("//*[normalize-space()=\"Logged in as {0}\"]"));


    public AutomationExerciseHome(TestContext testContext) {
        super(testContext, EnvProp.get("base_url"));
        elements.put("autoriser", autoriser);
        elements.put("signupLogin", signupLogin);
        elements.put("deleteAccount", deleteAccount);
        elements.put("loggedInAs", loggedInAs);
    }

    public void assertLoggedInAs(String user) {
        loggedInAs.injectValues("{0}", user).assertVisible(true);
    }
}
