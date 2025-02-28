package pages.Tuto;
import bcx.automation.playwright.page.BasePage;
import bcx.automation.playwright.element.BaseElement;
import bcx.automation.playwright.element.RadioGroup;
import bcx.automation.playwright.element.CheckboxGroup;
import bcx.automation.playwright.element.Dropdown;
import bcx.automation.playwright.element.MultiDropdown;
import bcx.automation.playwright.element.Grid;
import bcx.automation.test.TestContext;


public class MyForm extends BasePage {
    public final BaseElement lastname = new BaseElement(this.getTestContext(), "Nom :", this.getPage().locator("//INPUT[@data-test-id=\"lastname\"]")); //input
    public final BaseElement firstname = new BaseElement(this.getTestContext(), "Prénom :", this.getPage().locator("//INPUT[@data-test-id=\"firstname\"]")); //input
    public final BaseElement email = new BaseElement(this.getTestContext(), "Email :", this.getPage().locator("//INPUT[@data-test-id=\"email\"]")); //input
    public final RadioGroup genre = new RadioGroup(this.getTestContext(), "Genre :", this.getPage().locator("//DIV[@data-test-id=\"genre\"]")); //div
    public final CheckboxGroup langue = new CheckboxGroup(this.getTestContext(), "Langues parlées :", this.getPage().locator("//DIV[@data-test-id=\"langue\"]")); //div
    public final Dropdown country = new Dropdown(this.getTestContext(), "Nationalité :", this.getPage().locator("//DIV[@data-test-id=\"country\"]")); //div
    public final MultiDropdown skills = new MultiDropdown(this.getTestContext(), "Compétences :", this.getPage().locator("//DIV[@data-test-id=\"skills\"]")); //div
    public final BaseElement comment = new BaseElement(this.getTestContext(), "Commentaire :", this.getPage().locator("//TEXTAREA[@data-test-id=\"comment\"]")); //textarea
    public final BaseElement submit = new BaseElement(this.getTestContext(), "Soumettre", this.getPage().locator("//BUTTON[@data-test-id=\"submit\"]")); //button
    public final Grid person = new Grid(this.getTestContext(), "person", this.getPage().locator("//TABLE[@data-test-id=\"person\"]")); //table

    public MyForm(TestContext testContext) {
        super(testContext, "http://localhost:4200/");
        elements.put("Nom :", lastname);
        elements.put("Prénom :", firstname);
        elements.put("Email :", email);
        elements.put("Genre :", genre);
        elements.put("Langues parlées :", langue);
        elements.put("Nationalité :", country);
        elements.put("Compétences :", skills);
        elements.put("Commentaire :", comment);
        elements.put("Soumettre", submit);
        elements.put("person", person);

    }
}


/*jdd csv :
test-id;Nom :;Prénom :;Email :;Genre :;Langues parlées :;Nationalité :;Compétences :;Commentaire :;Soumettre;person;


jdd json :
[
	{
	"test-id":"",
	"Nom :":"input"
	"Prénom :":"input"
	"Email :":"input"
	"Genre :":"div"
	"Langues parlées :":"div"
	"Nationalité :":"div"
	"Compétences :":"div"
	"Commentaire :":"textarea"
	"Soumettre":"button"
	"person":"table"
	}
]*/