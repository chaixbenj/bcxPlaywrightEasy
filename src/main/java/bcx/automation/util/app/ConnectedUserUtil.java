package bcx.automation.util.app;

import com.microsoft.playwright.Page;

import java.util.HashMap;

/**
 * Utilitaire pour gérer les utilisateurs connectés sur différents drivers.
 */
public class ConnectedUserUtil {
    private static HashMap<Page, String> connectedUserMap;

    private ConnectedUserUtil() {
    }

    /**
     * Renvoie la map des utilisateurs connectés sur les différents drivers.
     *
     * @return La map des utilisateurs connectés.
     */
    public static HashMap<Page, String> getConnectedUserMap() {
        if (connectedUserMap == null) connectedUserMap = new HashMap<>();
        return connectedUserMap;
    }

    /**
     * Efface la liste des utilisateurs connectés.
     */
    public static void clear() {
        if (connectedUserMap != null) connectedUserMap.clear();
    }

    /**
     * Enregistre le nom de l'utilisateur connecté sur le driver.
     * La page AUTHpageLogin gère la connexion et utilise cette méthode et la méthode Driver.getConnectedUser()
     * pour savoir si une reconnexion est nécessaire en fonction de l'utilisateur déjà connecté.
     *
     * @param userName Le nom de l'utilisateur.
     * @param page     La page associée au driver.
     */
    public static void setConnectedUser(String userName, Page page) {
        if (!getConnectedUserMap().containsKey(page)) {
            getConnectedUserMap().put(page, userName);
        } else {
            getConnectedUserMap().replace(page, userName);
        }
    }

    /**
     * Renvoie le nom de l'utilisateur connecté sur le driver en cours.
     *
     * @param page La page associée au driver.
     * @return Le nom de l'utilisateur connecté, ou null si aucun utilisateur n'est connecté.
     */
    public static String getConnectedUser(Page page) {
        try {
            return getConnectedUserMap().get(page).split(";")[0];
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Renvoie un booléen indiquant si l'utilisateur est celui connecté sur le driver en cours.
     *
     * @param user Le nom de l'utilisateur à vérifier.
     * @param page La page associée au driver.
     * @return True si l'utilisateur est connecté, false sinon.
     */
    public static boolean isConnectedUser(String user, Page page) {
        return getConnectedUser(page) != null && getConnectedUser(page).equals(user);
    }
}
