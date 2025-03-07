package bcx.automation.playwright.element;

import bcx.automation.util.TimeWait;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import lombok.extern.slf4j.Slf4j;
import bcx.automation.properties.GlobalProp;

/**
 * classe permettant de gérer les asynchronismes et l'attente de disparition des loaders
 * @author bcx
 *
 */
@Slf4j
public class Loader {
    /**
     * locator du loader s'il y en a un
     */
    private static Locator loader1 = null;

    /**
     * set le locator du loader, sinon null et pas d'attente de loader. A setter dans le listener before test
     * @param loader
     */
    public static void setLoader(Locator loader) {
        loader1 = loader;
    }


    /**
     * Attend que les loaders ne soient plus displayed
     */
    public static boolean waitNotVisible() {
        return waitNotVisible(0);
    }

    /**
     * Attend que les loaders ne soient plus displayed
     * @param timeout timeout en secondes
     */
    public static boolean waitNotVisible(int timeout) {
        boolean loaderAppears = false;
        if (GlobalProp.isUseLoader()) {
            try {
                if (loader1 != null) {
                    TimeWait t = new TimeWait();
                    ElementHandle loader = loader1.first().elementHandle(new Locator.ElementHandleOptions().setTimeout(timeout == 0 ? 100 : timeout * 1000));
                    while (loader != null && loader.isVisible() && t.notOver(GlobalProp.getLoaderTimeOutMinute()*60)) {
                        loader = loader1.first().elementHandle(new Locator.ElementHandleOptions().setTimeout(1000));
                        loaderAppears = true;
                    }
                }
            } catch (Exception ignore) {
                // y'a eu de loader
            }
        }
        return loaderAppears;
    }
}