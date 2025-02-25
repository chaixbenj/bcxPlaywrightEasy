package pages.AutomationExercise;

import bcx.playwright.page.BasePage;
import bcx.playwright.page.element.BaseElement;
import bcx.playwright.page.element.ElementList;
import bcx.playwright.page.element.RadioGroup;
import bcx.playwright.test.TestContext;


public class AutomationExerciseNewUserAccount extends BasePage {
    public final RadioGroup title = new RadioGroup(this.getTestContext(), "title", this.getPage().locator("//DIV[@class=\"clearfix\"]"));
    public final BaseElement name = new BaseElement(this.getTestContext(), "name", this.getPage().locator("//INPUT[@data-qa=\"name\"]"));
    public final BaseElement email = new BaseElement(this.getTestContext(), "email", this.getPage().locator("//INPUT[@data-qa=\"email\"]"));
    public final BaseElement password = new BaseElement(this.getTestContext(), "password", this.getPage().locator("//INPUT[@data-qa=\"password\"]"));
    public final ElementList days = new ElementList(this.getTestContext(), "days", this.getPage().locator("//SELECT[@data-qa=\"days\"]"));
    public final ElementList months = new ElementList(this.getTestContext(), "months", this.getPage().locator("//SELECT[@data-qa=\"months\"]"));
    public final ElementList years = new ElementList(this.getTestContext(), "years", this.getPage().locator("//SELECT[@data-qa=\"years\"]"));
    public final BaseElement firstName = new BaseElement(this.getTestContext(), "first_name", this.getPage().locator("//INPUT[@data-qa=\"first_name\"]"));
    public final BaseElement lastName = new BaseElement(this.getTestContext(), "last_name", this.getPage().locator("//INPUT[@data-qa=\"last_name\"]"));
    public final BaseElement company = new BaseElement(this.getTestContext(), "company", this.getPage().locator("//INPUT[@data-qa=\"company\"]"));
    public final BaseElement address = new BaseElement(this.getTestContext(), "address", this.getPage().locator("//INPUT[@data-qa=\"address\"]"));
    public final BaseElement address2 = new BaseElement(this.getTestContext(), "address2", this.getPage().locator("//INPUT[@data-qa=\"address2\"]"));
    public final ElementList country = new ElementList(this.getTestContext(), "country", this.getPage().locator("//SELECT[@data-qa=\"country\"]"));
    public final BaseElement state = new BaseElement(this.getTestContext(), "state", this.getPage().locator("//INPUT[@data-qa=\"state\"]"));
    public final BaseElement city = new BaseElement(this.getTestContext(), "city", this.getPage().locator("//INPUT[@data-qa=\"city\"]"));
    public final BaseElement zipcode = new BaseElement(this.getTestContext(), "zipcode", this.getPage().locator("//INPUT[@data-qa=\"zipcode\"]"));
    public final BaseElement mobileNumber = new BaseElement(this.getTestContext(), "mobile_number", this.getPage().locator("//INPUT[@data-qa=\"mobile_number\"]"));
    public final BaseElement createAccount = new BaseElement(this.getTestContext(), "create-account", this.getPage().locator("//BUTTON[@data-qa=\"create-account\"]"));

    public final BaseElement accountCreatedMessage = new BaseElement(this.getTestContext(), "account created message", this.getPage().locator("//*[.=\"Account Created!\"]"));
    public final BaseElement continueButtton = new BaseElement(this.getTestContext(), "continueButton", this.getPage().locator("//a[@data-qa=\"continue-button\"]"));

    public AutomationExerciseNewUserAccount(TestContext testContext) {
        super(testContext);
        elements.put("title", title);
        elements.put("name", name);
        elements.put("email", email);
        elements.put("password", password);
        elements.put("days", days);
        elements.put("months", months);
        elements.put("years", years);
        elements.put("firstName", firstName);
        elements.put("lastName", lastName);
        elements.put("company", company);
        elements.put("address", address);
        elements.put("address2", address2);
        elements.put("country", country);
        elements.put("state", state);
        elements.put("city", city);
        elements.put("zipcode", zipcode);
        elements.put("mobileNumber", mobileNumber);
        elements.put("createAccount", createAccount);
        elements.put("accountCreatedMessage", accountCreatedMessage);
        elements.put("continueButtton", continueButtton);
    }

}
