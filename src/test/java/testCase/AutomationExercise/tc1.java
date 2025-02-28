package testCase.AutomationExercise;


import bcx.automation.test.BaseTest;
import bcx.automation.report.Reporter;
import bcx.automation.util.data.DataUtil;
import org.testng.annotations.Test;
import pages.AutomationExercise.AutomationExerciseDeleteAccount;
import pages.AutomationExercise.AutomationExerciseHome;
import pages.AutomationExercise.AutomationExerciseLogin;
import pages.AutomationExercise.AutomationExerciseNewUserAccount;


public class tc1 extends BaseTest {
    @Test
    public void run() {
        try {
            String name = DataUtil.randomAlphaString().toLowerCase();
            String email = name + "@" + name + ".fr";
            String password = name + "A1!";


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
            automationExerciseNewUserAccount.title.setValue("Mr.");
            automationExerciseNewUserAccount.name.assertValue(name);
            automationExerciseNewUserAccount.email.assertValue(email);
            automationExerciseNewUserAccount.password.setValue(password);
            automationExerciseNewUserAccount.days.setValue("11");
            automationExerciseNewUserAccount.months.setValue("February");
            automationExerciseNewUserAccount.years.setValue("1984");

            automationExerciseNewUserAccount.firstName.setValue("Paul");
            automationExerciseNewUserAccount.lastName.setValue(name.toUpperCase());
            automationExerciseNewUserAccount.company.setValue("Automation company");
            automationExerciseNewUserAccount.address.setValue("13 A steet");
            automationExerciseNewUserAccount.address2.setValue("Building B");
            automationExerciseNewUserAccount.country.setValue("United States");
            automationExerciseNewUserAccount.state.setValue("New Jersey");
            automationExerciseNewUserAccount.city.setValue("New York");
            automationExerciseNewUserAccount.zipcode.setValue("07008");
            automationExerciseNewUserAccount.mobileNumber.setValue("07008050505");

            automationExerciseNewUserAccount.createAccount.click();

            this.getReport().title("Vérification de la création du compte");
            automationExerciseNewUserAccount.accountCreatedMessage.assertVisible(true);
            automationExerciseNewUserAccount.continueButtton.click();
            automationExerciseHome.loggedInAs.injectValues("{0}", name).assertVisible(true);

            this.getReport().title("Suppression du compte");
            automationExerciseHome.deleteAccount.click();
            AutomationExerciseDeleteAccount automationExerciseDeleteAccount = new AutomationExerciseDeleteAccount(this.testContext);
            automationExerciseDeleteAccount.accountDeletedMessage.assertVisible(true);
            automationExerciseDeleteAccount.continueButton.click();

        } catch (Exception e) {
            this.getReport().log(Reporter.ERROR_STATUS, e);
        }

    }
}
