package bcx.playwright.util.data;

import lombok.extern.slf4j.Slf4j;

/**
 * Utilitaire de gestion de doubles
 * @author aus
 *
 */
@Slf4j
public class DoubleUtil {

    private DoubleUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convertit une string en double arrondi à 2 décimales
     * @param s
     * @return double
     */
    public static double getDouble(String s){
        return ((double)Math.round(Double.parseDouble((s==null || s.equals("")?"0":s))*100))/100;
    }

    /**
     * si string potentiel numérique renvoi la string format double 2 decimales sinon la valeur en paramètre
     * @param value
     * @return
     */
    public static String asNum(String value) {
        String num = value;
        try {
            value = value
                    .replace("%","")
                    .replace("€","")
                    .replace(" ","")
                    .replace(" ", "")
                    .replace(" ", "")
                    .replace("&nbsp;", "");
            int lastPointPos = value.lastIndexOf('.');
            int lastVirgPos = value.lastIndexOf(',');
            if (lastPointPos>0 && lastVirgPos>0) {
                if (lastPointPos>lastVirgPos) {
                    value = value.replace(",", "");
                } else {
                    value = value.replace(".", "");
                }
            }
            if (value.split("\\.").length>2) value = value.replace(".", "");
            if (value.split(",").length>2) value = value.replace(",", "");
            if (value.contains(".") && (value.split("\\.")[value.split("\\.").length-1]).length()==3) value = value.replace(".", "");
            num = String.valueOf(((double)Math.round(Double.parseDouble(value.replace(",", "."))*100))/100);
        } catch (Exception ex) {
            // ça peu arriver, on s'en moque
        }
        return num;
    }

}
