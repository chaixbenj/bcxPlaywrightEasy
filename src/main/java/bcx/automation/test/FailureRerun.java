package bcx.automation.test;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import bcx.automation.properties.GlobalProp;

public class FailureRerun implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 1;

    /**
     * Classe pour le rejeu des tests en cas d'erreur, ajouter "retryAnalyzer = FailureRerun.class" dans l'annotation Test
     * ex : @Test(retryAnalyzer = FailureRerun.class, priority = 10, groups = {"testGroup1", "testGroup2"})
     * @param result
     * @return
     * */
    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT && GlobalProp.isRetryOnFail() && !GlobalProp.isSuiteOverTimeOut()) {
            retryCount++;
            return true;
        }
        return false;
    }
}