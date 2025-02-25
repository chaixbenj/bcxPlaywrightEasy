package bcx.playwright.util.app;

import com.microsoft.playwright.Page;

import java.util.HashMap;

public class ConnectedUserUtil {
    private static HashMap<Page, String> connectedUserMap;

    private ConnectedUserUtil() {
    }

    /**
     * renvoie la map des users connectés sur les différents drivers
     * @return
     */
    public static HashMap<Page,String> getConnectedUserMap() {
        if (connectedUserMap ==null) connectedUserMap = new HashMap<>();
        return connectedUserMap;
    }

    /**
     * efface la liste des users connectés
     * @return
     */
    public static void clear() {
        if (connectedUserMap !=null) connectedUserMap.clear();
    }


    /**
     * enregistre le nom du user connecté sur le driver
     * la page AUTHpageLogin gère la connexion et utilise cette méthode e la méthode Driver.getConnectedUser() pour savoir si une reconnexion est nécessaire en fonction du user déjà connecté
     *
     * @param userName
     */
    public static void setConnectedUser(String userName, Page page) {
        if (!getConnectedUserMap().containsKey(page)) {
            getConnectedUserMap().put(page, userName);
        } else {
            getConnectedUserMap().replace(page, userName);
        }
    }

    /**
     * renvoi le nom du user connecté sur le driver en cours
     *
     * @return user connecté
     */
    public static String getConnectedUser(Page page) {
        try {
            return getConnectedUserMap().get(page).split(";")[0];
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * renvoi un booléen indiquant si le user est celui connecté sur le driver en cours
     *
     * @param user
     * @return user connecté ou pas
     */
    public static boolean isConnectedUser(String user, Page page) {
        return getConnectedUser(page)!=null && getConnectedUser(page).equals(user);
    }

}
