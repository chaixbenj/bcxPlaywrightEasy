package bcx.playwright.test;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import bcx.playwright.properties.GlobalProp;

public class FailureRerun implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 1;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT && GlobalProp.isRetryOnFail() && !GlobalProp.isSuiteOverTimeOut()) {
            retryCount++;
            return true;
        }
        return false;
    }
}