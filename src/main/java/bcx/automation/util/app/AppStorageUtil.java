package bcx.automation.util.app;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe utilitaire pour gérer les cookies et le session storage dans une page Playwright.
 */
public class AppStorageUtil {

    private AppStorageUtil() {
    }

    /**
     * Renvoie la valeur d'un cookie.
     *
     * @param page La page Playwright.
     * @param cookieName Le nom du cookie.
     * @return La valeur du cookie ou null si le cookie n'existe pas.
     */
    public static String getCookie(Page page, String cookieName) {
        List<Cookie> cookies = page.context().cookies();
        for (Cookie cookie : cookies) {
            if (cookie.name.equals(cookieName)) {
                return cookie.value;
            }
        }
        return null;
    }

    /**
     * Met à jour la valeur d'un cookie.
     *
     * @param page La page Playwright.
     * @param cookieName Le nom du cookie.
     * @param newValue La nouvelle valeur du cookie.
     * @return Vrai si le cookie a été mis à jour, faux sinon.
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
     * Récupère une valeur dans le session storage.
     *
     * @param page La page Playwright.
     * @param key La clé de la valeur à récupérer.
     * @return La valeur associée à la clé ou null si la clé n'existe pas.
     */
    public static String getSessionStorageItemValue(Page page, String key) {
        return (String) page.evaluate(String.format(
                "return sessionStorage.getItem('%s');", key));
    }
}
