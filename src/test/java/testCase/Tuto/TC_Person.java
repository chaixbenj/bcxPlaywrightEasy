package testCase.Tuto;


import bcx.automation.test.BaseTest;
import bcx.automation.report.Reporter;
import org.testng.annotations.Test;
import pages.Tuto.MyForm;


public class TC_Person extends BaseTest {


    @Test
    public void createPerson() {
        try {
            MyForm myForm = new MyForm(this.testContext);
            myForm.navigate();
            myForm.setValue("person.csv", "tc1");
            myForm.submit.click();
            myForm.person.assertOneRowContains(new String[]{"Martin","Patrice","patrice.martin@gmail.com","Homme","Français, Anglais","France","Design, Marketing","C'est OK"});


            myForm = new MyForm(this.testContext.newTestContext());
            myForm.navigate();
            myForm.setValue("person.csv", "tc1");
            myForm.submit.click();
            myForm.person.assertOneRowContains(new String[]{"Martin","Patrice","patrice.martin@gmail.com","Homme","Français, Anglais","France","Design, Marketing","C'est OK"});


        } catch (Exception e) {
            this.getReport().log(Reporter.ERROR_STATUS, e);
        }
    }

}
