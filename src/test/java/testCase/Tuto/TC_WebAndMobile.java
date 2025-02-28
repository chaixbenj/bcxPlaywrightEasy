package testCase.Tuto;


import bcx.automation.appium.AppiumDriver;
import bcx.automation.report.Reporter;
import bcx.automation.test.BaseTest;
import org.testng.annotations.Test;
import pages.WebAndMobile.MessagesAndroid;
import pages.WebAndMobile.MessagesWebFront;


public class TC_WebAndMobile extends BaseTest {


    @Test
    public void testExhangeMessages() {
        try {
            AppiumDriver.setupAndroid(this.testContext, "simu_nxs", "C:/temp/apk/app-debug.apk");

            MessagesWebFront webFront = new MessagesWebFront(this.testContext);
            webFront.navigate();
            MessagesAndroid android = new MessagesAndroid(this.testContext);


            webFront.sendMessage("Y'a quelqu'un sur le site ?");

            android.assertMessage("Y'a quelqu'un sur le site ?");
            android.sendMessage("Moi, je suis sur l'app android");

            webFront.assertMessage("Moi, je suis sur l'app android");
            webFront.sendMessage("T'es un robot ? Qui te commande ?");

            android.assertMessage("T'es un robot ? Qui te commande ?");
            android.sendMessage("APPIUM et SELENIUM. Et toi t'es un robot SELENIUM ?");

            webFront.assertMessage("APPIUM et SELENIUM. Et toi t'es un robot SELENIUM ?");
            webFront.sendMessage("PLAYWRIGHT, papy ! J'ai pas ton time !");

            android.assertMessage("PLAYWRIGHT, papy ! J'ai pas ton time !");
            android.sendMessage("Papy ?! Pierre, c'est toi ? Le jour où tu commandes les apps natives fais signe gamin");

            webFront.assertMessage("Papy ?! Pierre, c'est toi ? Le jour où tu commandes les apps natives fais signe gamin");

        } catch (Exception e) {
            this.getReport().log(Reporter.ERROR_STATUS, e);
        }
    }

}
