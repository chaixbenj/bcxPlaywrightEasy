package testCase.Tuto;

import bcx.automation.appium.AppiumDriver;
import bcx.automation.report.Reporter;
import bcx.automation.test.BaseTest;
import org.testng.annotations.Test;
import pages.WebAndMobile.MessagesAndroid;
import pages.WebAndMobile.MessagesWebFront;

public class TC_WebAndMobile extends BaseTest {

    @Test(groups = {"tuto2"})
    public void testExchangeMessages() {
        try {
            //Une page playwright est démarrée dans le @BeforeTest de la classe BaseTest
            //On instancie notre page web messagerie et on navigate vers la page
            MessagesWebFront webFront = new MessagesWebFront(this.testContext);
            webFront.navigate();

            //On démarre un simulateur android et on l'initialise avec l'apk de l'application
            AppiumDriver.setupAndroid(this.testContext, "simu_nxs", "C:/temp/apk/app-debug.apk");

            //On instancie notre page android messagerie
            MessagesAndroid android = new MessagesAndroid(this.testContext);

            //Playwright envoie un message sur le site Web
            webFront.sendMessage("Y'a quelqu'un sur le site ?");

            //Appium+Selenium vérifie la récéption du message sur l'app Android et répond
            android.assertMessage("Y'a quelqu'un sur le site ?");
            android.sendMessage("Moi, je suis sur l'app android");

            //Playwright vérifie la récéption du message sur le site Web et répond
            webFront.assertMessage("Moi, je suis sur l'app android");
            webFront.sendMessage("T'es un robot ? Qui te commande ?");

            //Appium+Selenium vérifie la récéption du message sur l'app Android et répond
            android.assertMessage("T'es un robot ? Qui te commande ?");
            android.sendMessage("APPIUM et SELENIUM. Et toi t'es un robot SELENIUM ?");

            //Playwright vérifie la récéption du message sur le site Web et répond
            webFront.assertMessage("APPIUM et SELENIUM. Et toi t'es un robot SELENIUM ?");
            webFront.sendMessage("PLAYWRIGHT, papy ! J'ai pas ton time !");

            //Appium+Selenium vérifie la récéption du message sur l'app Android et répond
            android.assertMessage("PLAYWRIGHT, papy ! J'ai pas ton time !");
            android.sendMessage("Papy ?! Pierre, c'est toi ? Le jour où tu commandes les apps natives fais signe gamin");

        } catch (Exception e) {
            this.getReport().log(Reporter.FAIL_STATUS, e);
        } finally {
            endTest();
        }
    }

}
