package bcx.playwright.util.app;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;
import java.util.List;
import java.util.stream.Collectors;

public class AppStorageUtil {

    private AppStorageUtil() {
    }

    /**
     * renvoie la valeur d'un cookie
     * @param cookieName
     * @return
     */
    public static String getCookie(Page page, String cookieName) {
        List<Cookie> cookies = page.context().cookies();
        for (Cookie cookie : cookies) {
            if (cookie.name.equals(cookieName)) {
                return(cookie.value);
            }
        }
        return null;
    }

    /**
     * update la valeur d'un cookie
     * @param cookieName
     * @param newValue
     */
    public static boolean updateCookie(Page page, String cookieName, String newValue) {
        boolean exists = false;
        List<Cookie> cookies = page.context().cookies();
        for (Cookie cookie : cookies) {
            if (cookie.name.equals(cookieName)) {
                exists = true;
            }
        }

        List<Cookie> updatedCookies = cookies.stream()
                .filter(cookie -> !cookie.name.equals(cookieName)) // Garde tous sauf celui à modifier
                .collect(Collectors.toList());

        if (exists) {
            updatedCookies.add(new Cookie(cookieName, newValue));
            page.context().clearCookies();
            page.context().addCookies(updatedCookies);
            page.reload();
        }
        return exists;
    }

    /**
     * récupère une valeur dans le session storage
     * @param key
     * @return
     */
    public static String getSessionStorageItemValue(Page page, String key) {
        return (String) page.evaluate(String.format(
                "return sessionStorage.getItem('%s');", key));
    }
}
