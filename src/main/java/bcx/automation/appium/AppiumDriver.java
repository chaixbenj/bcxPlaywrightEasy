package bcx.automation.appium;

import bcx.automation.report.Reporter;
import bcx.automation.test.TestContext;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

//npm install -g appium
//appium setup
//appium --relaxed-security
public class AppiumDriver {


    public static final String APPIUM_URL = "http://127.0.0.1:4723/";

    /**
     * Démarre le driver Appium sur Android.
     *
     * @param testContext le contexte de test en cours.
     * @param name le nom de l'AVD Android.
     * @param apkPath le chemin vers le fichier APK à tester.
     */
    public static void setupAndroid(TestContext testContext, String name, String apkPath) {
        if (name == null || name.isEmpty() || apkPath == null || apkPath.isEmpty()) {
            testContext.getReport().log(Reporter.FAIL_STATUS, "Nom ou chemin APK est invalide");
            return;
        }

        try {
            testContext.getReport().log(Reporter.INFO_STATUS, "Démarrage du driver Android " + name + " avec APK à " + apkPath);

            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName("Android");
            options.setAutomationName("UiAutomator2");
            options.setUiautomator2ServerLaunchTimeout(Duration.ofMinutes(2)); // Temps d'attente pour démarrer le serveur UiAutomator2.
            options.setAvd(name);
            options.setAvdLaunchTimeout(Duration.ofMinutes(2));
            options.setAvdReadyTimeout(Duration.ofMinutes(2));
            options.setApp(apkPath);
            options.setNoReset(false);
            options.setFullReset(true);
            options.setCapability("disableWindowAnimation", true);

            URL url = new URL(APPIUM_URL); // URL d'Appium
            AndroidDriver driver = new AndroidDriver(url, options);
            testContext.setAppiumDriver(driver);
            testContext.getReport().setDriver(driver);
            testContext.getReport().log(Reporter.INFO_STATUS, "Driver démarré avec succès.");
        } catch (MalformedURLException e) {
            testContext.getReport().log(Reporter.FAIL_STATUS_NO_SCREENSHOT, "URL d'Appium invalide", e);
        } catch (Exception e) {
            testContext.getReport().log(Reporter.FAIL_STATUS_NO_SCREENSHOT, "Erreur lors du démarrage du driver", e);
        }
    }

    /**
     * Démarre le driver Appium sur Android.
     *
     * @param testContext le contexte de test en cours.
     * @param name le nom de l'AVD Android.
     * @param appPath le chemin vers le fichier app ios à tester.
     */
    public static void setupIOS(TestContext testContext, String name, String appPath) {
        if (name == null || name.isEmpty() || appPath == null || appPath.isEmpty()) {
            testContext.getReport().log(Reporter.FAIL_STATUS, "Nom ou chemin de l'application est invalide");
            return;
        }

        try {
            testContext.getReport().log(Reporter.INFO_STATUS, "Démarrage du driver iOS " + name + " avec app à " + appPath);

            XCUITestOptions options = new XCUITestOptions();
            options.setPlatformName("iOS");
            options.setAutomationName("XCUITest");
            options.setApp(appPath);
            options.setDeviceName(name);
            options.setUdid("your-device-udid"); // Optionnel: Définit l'UDID si vous testez sur un appareil physique
            options.setNoReset(false);
            options.setFullReset(true);
            options.setCapability("disableWindowAnimation", true);

            URL url = new URL(APPIUM_URL); // URL d'Appium
            IOSDriver driver = new IOSDriver(url, options);
            testContext.setAppiumDriver(driver);
            testContext.getReport().setDriver(driver);
            testContext.getReport().log(Reporter.INFO_STATUS, "Driver iOS démarré avec succès.");
        } catch (MalformedURLException e) {
            testContext.getReport().log(Reporter.FAIL_STATUS_NO_SCREENSHOT, "URL d'Appium invalide", e);
        } catch (Exception e) {
            testContext.getReport().log(Reporter.FAIL_STATUS_NO_SCREENSHOT, "Erreur lors du démarrage du driver iOS", e);
        }
    }
}
