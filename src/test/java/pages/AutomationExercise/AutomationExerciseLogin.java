package pages.AutomationExercise;

import bcx.automation.playwright.page.BasePage;
import bcx.automation.playwright.element.BaseElement;
import bcx.automation.properties.EnvProp;
import bcx.automation.test.TestContext;


public class AutomationExerciseLogin extends BasePage {
    public final BaseElement loginEmail = new BaseElement(this.getTestContext(), "login-email", this.getPage().locator("//INPUT[@data-qa=\"login-email\"]"));
    public final BaseElement loginPassword = new BaseElement(this.getTestContext(), "login-password", this.getPage().locator("//INPUT[@data-qa=\"login-password\"]"));
    public final BaseElement loginButton = new BaseElement(this.getTestContext(), "login-button", this.getPage().locator("//BUTTON[@data-qa=\"login-button\"]"));
    public final BaseElement signupName = new BaseElement(this.getTestContext(), "name", this.getPage().locator("//INPUT[@data-qa=\"signup-name\"]"));
    public final BaseElement signupEmail = new BaseElement(this.getTestContext(), "signup-email", this.getPage().locator("//INPUT[@data-qa=\"signup-email\"]"));
    public final BaseElement signupButton = new BaseElement(this.getTestContext(), "signup-button", this.getPage().locator("//BUTTON[@data-qa=\"signup-button\"]"));

    public AutomationExerciseLogin(TestContext testContext) {
        super(testContext, EnvProp.get("base_url") + "login");
        elements.put("loginEmail", loginEmail);
        elements.put("loginPassword", loginPassword);
        elements.put("loginButton", loginButton);
        elements.put("signupName", signupName);
        elements.put("signupEmail", signupEmail);
        elements.put("signupButton", signupButton);
    }

    public void newUserSignup(String name, String email){
        signupName.setValue(name);
        signupEmail.setValue(email);
        signupButton.click();
    }
    public void login(String email, String password){
        loginEmail.setValue(email);
        loginPassword.setValue(password);
        loginButton.click();
    }

}
