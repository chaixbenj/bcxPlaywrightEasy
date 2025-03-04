package bcx.automation.appium;

import bcx.automation.report.Reporter;
import bcx.automation.test.TestContext;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.URL;
import java.time.Duration;

//npm install -g appium
//appium setup
//appium --relaxed-security
public class AppiumDriver {


    public static final String APPIUM_URL = "http://127.0.0.1:4723/";

    public static void setupAndroid(TestContext testContext, String name, String apkPath) {
        try {
            testContext.getReport().log(Reporter.INFO_STATUS, "Démarrage du driver Android " + name + " sur apk" + apkPath);
            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName("Android");
            options.setAutomationName("UiAutomator2");
            options.setUiautomator2ServerLaunchTimeout(Duration.ofMinutes(2));  // Temps d'attente pour le démarrage du serveur UiAutomator2
            options.setAvd(name);
            options.setAvdLaunchTimeout(Duration.ofMinutes(2));
            options.setAvdReadyTimeout(Duration.ofMinutes(2));
            options.setApp(apkPath);
            options.setNoReset(false);
            options.setFullReset(true);
            options.setCapability("disableWindowAnimation", true);


            URL url = new URL(APPIUM_URL);
            AndroidDriver driver = new AndroidDriver(url, options);
            testContext.setAppiumDriver(driver);
            testContext.getReport().setDriver(driver);
        } catch (Exception e) {
            testContext.getReport().log(Reporter.FAIL_STATUS_NO_SCREENSHOT, "Erreur au démarrage", e);
        }
    }
}
