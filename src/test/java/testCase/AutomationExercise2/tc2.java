package testCase.AutomationExercise2;


import bcx.automation.properties.GlobalProp;
import bcx.automation.report.Reporter;
import bcx.automation.test.BaseTest;
import bcx.automation.util.data.DataUtil;
import org.testng.annotations.Test;
import pages.AutomationExercise.AutomationExerciseDeleteAccount;
import pages.AutomationExercise.AutomationExerciseHome;
import pages.AutomationExercise.AutomationExerciseLogin;
import pages.AutomationExercise.AutomationExerciseNewUserAccount;


public class tc2 extends BaseTest {
    String name = "b" + DataUtil.randomAlphaString().toLowerCase();
    String email = name + "@" + name + ".fr";

    @Test(groups = {"test"})
    public void createAccount() {
        try {
            this.getReport().title("Navigation vers la page d'accueil");
            AutomationExerciseHome automationExerciseHome = new AutomationExerciseHome(this.testContext);
            automationExerciseHome.navigate();
            automationExerciseHome.autoriser.clickIfPossible();

            this.getReport().title("Création d'un compte");
            automationExerciseHome.signupLogin.click();
            AutomationExerciseLogin automationExerciseLogin = new AutomationExerciseLogin(this.testContext);
            automationExerciseLogin.newUserSignup(name, email);

            this.getReport().title("Saisie des données du compte");
            AutomationExerciseNewUserAccount automationExerciseNewUserAccount = new AutomationExerciseNewUserAccount(this.testContext);
            automationExerciseNewUserAccount.setValue("new-account.csv", "tc2");
            automationExerciseNewUserAccount.createAccount.click();

            this.getReport().title("Vérification de la création du compte");
            automationExerciseNewUserAccount.accountCreatedMessage.assertVisible(true);
            automationExerciseNewUserAccount.continueButtton.click();
            automationExerciseHome.loggedInAs.injectValues("{0}", name).assertVisible(true);

        } catch (Exception e) {
            this.getReport().log(Reporter.FAIL_STATUS, e);
        } finally {
            this.getReport().softAssertAll();
        }
    }

    @Test(groups = {"test"})
    public void deleteAccount() {
        try {
            AutomationExerciseHome automationExerciseHome = new AutomationExerciseHome(this.testContext);
            if (GlobalProp.isCloseBrowserAfterMethod()) {
                this.getReport().title("Navigation vers la page d'accueil");
                automationExerciseHome.navigate();
                automationExerciseHome.autoriser.clickIfPossible();

                this.getReport().title("Connexion d'un compte");
                automationExerciseHome.signupLogin.click();
                AutomationExerciseLogin automationExerciseLogin = new AutomationExerciseLogin(this.testContext);
                automationExerciseLogin.login(email, "Password01!");
            }
            this.getReport().title("Suppression du compte");
            automationExerciseHome.deleteAccount.click();
            AutomationExerciseDeleteAccount automationExerciseDeleteAccount = new AutomationExerciseDeleteAccount(this.testContext);
            automationExerciseDeleteAccount.accountDeletedMessage.assertVisible(true);
            automationExerciseDeleteAccount.continueButton.click();

        } catch (Exception e) {
            this.getReport().log(Reporter.FAIL_STATUS, e);
        } finally {
            endTest();
        }

    }
}
