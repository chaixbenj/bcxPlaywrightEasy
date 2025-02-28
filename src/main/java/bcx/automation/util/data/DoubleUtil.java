package bcx.automation.util.data;

import lombok.extern.slf4j.Slf4j;

/**
 * Utilitaire de gestion de doubles.
 *
 * @author aus
 */
@Slf4j
public class DoubleUtil {

    private DoubleUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convertit une chaîne de caractères en double arrondi à 2 décimales.
     *
     * @param s La chaîne de caractères à convertir.
     * @return Le double arrondi à 2 décimales.
     */
    public static double getDouble(String s) {
        return ((double) Math.round(Double.parseDouble((s == null || s.equals("") ? "0" : s)) * 100)) / 100;
    }

    /**
     * Si la chaîne de caractères est potentiellement numérique, renvoie la chaîne formatée en double avec 2 décimales.
     * Sinon, renvoie la valeur en paramètre.
     *
     * @param value La chaîne de caractères à vérifier et formater.
     * @return La chaîne formatée en double avec 2 décimales ou la valeur en paramètre.
     */
    public static String asNum(String value) {
        String num = value;
        try {
            value = value
                    .replace("%", "")
                    .replace("€", "")
                    .replace(" ", "")
                    .replace(" ", "")
                    .replace(" ", "")
                    .replace("&nbsp;", "");
            int lastPointPos = value.lastIndexOf('.');
            int lastVirgPos = value.lastIndexOf(',');
            if (lastPointPos > 0 && lastVirgPos > 0) {
                if (lastPointPos > lastVirgPos) {
                    value = value.replace(",", "");
                } else {
                    value = value.replace(".", "");
                }
            }
            if (value.split("\\.").length > 2) value = value.replace(".", "");
            if (value.split(",").length > 2) value = value.replace(",", "");
            if (value.contains(".") && (value.split("\\.")[value.split("\\.").length - 1]).length() == 3) value = value.replace(".", "");
            num = String.valueOf(((double) Math.round(Double.parseDouble(value.replace(",", ".")) * 100)) / 100);
        } catch (Exception ex) {
            // ça peut arriver, on s'en moque
        }
        return num;
    }
}
