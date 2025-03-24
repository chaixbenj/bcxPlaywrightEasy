package pages.WebAndMobile;

import bcx.automation.appium.page.MobBasePage;
import bcx.automation.appium.element.MobElement;
import bcx.automation.test.TestContext;
import org.openqa.selenium.By;


public class MessagesAndroid extends MobBasePage {
    public MobElement message = new MobElement(this.getTestContext(), "message {0}",  By.xpath("//android.widget.TextView[@resource-id='com.example.mymessageapp:id/textViewMessage' and @text=\"{0}\"]"));
    public MobElement newmessage = new MobElement(this.getTestContext(), "Écris ton message ici",  By.id("com.example.mymessageapp:id/editTextMessage")); //textarea
    public MobElement send = new MobElement(this.getTestContext(), "Envoyer", By.id("com.example.mymessageapp:id/buttonSend")); //button

    public MessagesAndroid(TestContext testContext) {
        super(testContext);
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