package pages.AutomationExercise;

import bcx.playwright.page.BasePage;
import bcx.playwright.page.element.BaseElement;
import bcx.playwright.test.TestContext;

public class AutomationExerciseDeleteAccount extends BasePage {
    public final BaseElement accountDeletedMessage = new BaseElement(this.getTestContext(), "account deleted message", this.getPage().locator("//*[.=\"Account Deleted!\"]"));
    public final BaseElement continueButton = new BaseElement(this.getTestContext(), "continueButton", this.getPage().locator("//a[@data-qa=\"continue-button\"]"));

    public AutomationExerciseDeleteAccount(TestContext testContext) {
        super(testContext);
        elements.put("accountDeletedMessage", accountDeletedMessage);
        elements.put("continueButton", continueButton);
    }


}
