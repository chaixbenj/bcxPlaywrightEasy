package pages.WebAndMobile;

import bcx.automation.playwright.page.BasePage;
import bcx.automation.test.TestContext;
import bcx.automation.playwright.element.BaseElement;


public class MessagesWebFront extends BasePage {
    public BaseElement message = new BaseElement(this.getTestContext(), "message \"{0}\"",  this.getPage().locator("//li[@data-test-id='message' and contains(., \"{0}\")]")); //li
    public BaseElement newmessage = new BaseElement(this.getTestContext(), "Écris ton message ici",  this.getPage().getByTestId("newMessage")); //textarea
    public BaseElement send = new BaseElement(this.getTestContext(), "Envoyer",  this.getPage().getByTestId("send")); //button

    public MessagesWebFront(TestContext testContext) {
        super(testContext, "http://localhost:4200/");
        elements.put("message", this.message);
        elements.put("Écris ton message ici", this.newmessage);
        elements.put("Envoyer", this.send);

    }

    public void sendMessage(String message) {
        this.newmessage.setValue(message);
        this.send.click();
    }

    public void assertMessage(String message) {
        this.message.injectValues("{0}", message).assertVisible(true);
    }
}


/*jdd csv :
test-id;message;reponse;Écris ton message ici;Envoyer;


jdd json :
[
	{
	"test-id":"",
	"message":"li"
	"reponse":"li"
	"Écris ton message ici":"textarea"
	"Envoyer":"button"
	}
]*/