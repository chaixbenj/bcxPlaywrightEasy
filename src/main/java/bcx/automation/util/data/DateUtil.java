package bcx.automation.util.data;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contient des méthodes de transformation de date.
 *
 * @author bcx
 */
@Slf4j
public class DateUtil {

    private DateUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Renvoie le jour courant au format indiqué.
     *
     * @param format Le format de la date.
     * @return La date courante au format spécifié.
     */
    public static String today(String format) {
        return (LocalDateTime.now()).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Renvoie le jour courant au format dd/MM/yyyy.
     *
     * @return La date courante au format dd/MM/yyyy.
     */
    public static String today() {
        return today("dd/MM/yyyy");
    }

    /**
     * Renvoie l'année courante.
     *
     * @return L'année courante au format yyyy.
     */
    public static String currentYear() {
        return today("yyyy");
    }

    /**
     * Renvoie le mois courant.
     *
     * @return Le mois courant au format MM.
     */
    public static String currentMonth() {
        return today("MM");
    }

    /**
     * Renvoie le jour courant.
     *
     * @return Le jour courant au format dd.
     */
    public static String currentDay() {
        return today("dd");
    }

    /**
     * Renvoie le jour au format dd correspondant au jour courant +/- n jours.
     *
     * @param dayPlusMinus Le nombre de jours à ajouter ou soustraire (par exemple -5, +1).
     * @return Le jour au format dd.
     */
    public static String day(int dayPlusMinus) {
        return todayPlusDays("dd", dayPlusMinus);
    }

    /**
     * Renvoie le mois au format MM correspondant au mois courant +/- n mois.
     *
     * @param monthPlusMinus Le nombre de mois à ajouter ou soustraire (par exemple -5, +1).
     * @return Le mois au format MM.
     */
    public static String month(int monthPlusMinus) {
        return todayPlusMonths("MM", monthPlusMinus);
    }

    /**
     * Renvoie l'année au format yyyy correspondant à l'année courante +/- n années.
     *
     * @param yearPlusMinus Le nombre d'années à ajouter ou soustraire (par exemple -5, +1).
     * @return L'année au format yyyy.
     */
    public static String year(int yearPlusMinus) {
        return todayPlusYears("yyyy", yearPlusMinus);
    }

    /**
     * Renvoie le jour courant + N jours au format indiqué.
     *
     * @param format Le format de la date.
     * @param nbDays Le nombre de jours à ajouter.
     * @return La date au format spécifié.
     */
    public static String todayPlusDays(String format, int nbDays) {
        return (LocalDateTime.now().plusDays(nbDays)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Renvoie le jour courant + N jours au format dd/MM/yyyy.
     *
     * @param nbDays Le nombre de jours à ajouter.
     * @return La date au format dd/MM/yyyy.
     */
    public static String todayPlusDays(int nbDays) {
        return (LocalDateTime.now().plusDays(nbDays)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Renvoie le jour courant - N jours au format indiqué.
     *
     * @param format Le format de la date.
     * @param nbDays Le nombre de jours à soustraire.
     * @return La date au format spécifié.
     */
    public static String todayMinusDays(String format, int nbDays) {
        return (LocalDateTime.now().minusDays(nbDays)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Renvoie le jour courant - N jours au format dd/MM/yyyy.
     *
     * @param nbDays Le nombre de jours à soustraire.
     * @return La date au format dd/MM/yyyy.
     */
    public static String todayMinusDays(int nbDays) {
        return (LocalDateTime.now().minusDays(nbDays)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Renvoie le jour courant + N mois au format indiqué.
     *
     * @param format Le format de la date.
     * @param nbMonths Le nombre de mois à ajouter.
     * @return La date au format spécifié.
     */
    public static String todayPlusMonths(String format, int nbMonths) {
        return (LocalDateTime.now().plusMonths(nbMonths)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Renvoie le jour courant + N mois au format dd/MM/yyyy.
     *
     * @param nbMonths Le nombre de mois à ajouter.
     * @return La date au format dd/MM/yyyy.
     */
    public static String todayPlusMonths(int nbMonths) {
        return (LocalDateTime.now().plusMonths(nbMonths)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Renvoie le jour courant - N mois au format indiqué.
     *
     * @param format Le format de la date.
     * @param nbMonths Le nombre de mois à soustraire.
     * @return La date au format spécifié.
     */
    public static String todayMinusMonths(String format, int nbMonths) {
        return (LocalDateTime.now().minusMonths(nbMonths)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Renvoie le jour courant - N mois au format dd/MM/yyyy.
     *
     * @param nbMonths Le nombre de mois à soustraire.
     * @return La date au format dd/MM/yyyy.
     */
    public static String todayMinusMonths(int nbMonths) {
        return (LocalDateTime.now().minusMonths(nbMonths)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Renvoie le jour courant + N années au format indiqué.
     *
     * @param format Le format de la date.
     * @param nbYears Le nombre d'années à ajouter.
     * @return La date au format spécifié.
     */
    public static String todayPlusYears(String format, int nbYears) {
        return (LocalDateTime.now().plusYears(nbYears)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Renvoie le jour courant + N années au format dd/MM/yyyy.
     *
     * @param nbYears Le nombre d'années à ajouter.
     * @return La date au format dd/MM/yyyy.
     */
    public static String todayPlusYears(int nbYears) {
        return (LocalDateTime.now().plusYears(nbYears)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Renvoie le jour courant - N années au format indiqué.
     *
     * @param format Le format de la date.
     * @param nbYears Le nombre d'années à soustraire.
     * @return La date au format spécifié.
     */
    public static String todayMinusYears(String format, int nbYears) {
        return (LocalDateTime.now().minusYears(nbYears)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Renvoie le jour courant - N années au format dd/MM/yyyy.
     *
     * @param nbYears Le nombre d'années à soustraire.
     * @return La date au format dd/MM/yyyy.
     */
    public static String todayMinusYears(int nbYears) {
        return (LocalDateTime.now().minusYears(nbYears)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Convertit une chaîne de caractères en objet Date.
     *
     * @param date La date au format chaîne de caractères.
     * @param dateFormat Le format de la date.
     * @return L'objet Date correspondant.
     */
    public static Date toDate(String date, String dateFormat) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            return sdf.parse(date);
        } catch (Exception e) {
            return new Date();
        }
    }

    /**
     * Ajoute un nombre de jours à une date.
     *
     * @param date La date au format chaîne de caractères.
     * @param dateFormat Le format de la date.
     * @param nbDay Le nombre de jours à ajouter.
     * @return La date au format spécifié après ajout des jours.
     */
    public static String plusDay(String date, String dateFormat, int nbDay) {
        return plusSomething(date, dateFormat, nbDay, Calendar.DATE);
    }

    /**
     * Ajoute un nombre de mois à une date.
     *
     * @param date La date au format chaîne de caractères.
     * @param dateFormat Le format de la date.
     * @param nbMonth Le nombre de mois à ajouter.
     * @return La date au format spécifié après ajout des mois.
     */
    public static String plusMonth(String date, String dateFormat, int nbMonth) {
        return plusSomething(date, dateFormat, nbMonth, Calendar.MONTH);
    }

    /**
     * Ajoute un nombre d'années à une date.
     *
     * @param date La date au format chaîne de caractères.
     * @param dateFormat Le format de la date.
     * @param nbYear Le nombre d'années à ajouter.
     * @return La date au format spécifié après ajout des années.
     */
    public static String plusYear(String date, String dateFormat, int nbYear) {
        return plusSomething(date, dateFormat, nbYear, Calendar.YEAR);
    }

    private static String plusSomething(String date, String dateFormat, int nb, int what) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(toDate(date, dateFormat));
            calendar.add(what, nb);
            return new SimpleDateFormat(dateFormat).format(calendar.getTime());
        } catch (Exception e) {
            return "fail parse " + date + " " + dateFormat;
        }
    }

    /**
     * Reformate une date d'un format vers un autre.
     *
     * @param date La date au format initial.
     * @param initFormat Le format initial de la date.
     * @param targetFormat Le format cible de la date.
     * @return La date au format cible.
     */
    public static String reformate(String date, String initFormat, String targetFormat) {
        try {
            Date d = new SimpleDateFormat(initFormat).parse(date);
            return new SimpleDateFormat(targetFormat).format(d);
        } catch (Exception e) {
            return date;
        }
    }

    /**
     * Dans une chaîne de caractères, remplace les dates au format dd/MM/yyyy par "dd/mm/yyyy",
     * et les dates au format "le dd mois yyyy" par "le dd mm yyyy".
     *
     * @param s La chaîne de caractères contenant les dates.
     * @return La chaîne de caractères avec les dates anonymisées.
     */
    public static String anonymiseInString(String s) {
        String regex = "(\\d{2}/\\d{2}/\\d{4})";
        Matcher m = Pattern.compile(regex).matcher(s);
        while (m.find()) {
            String d1 = m.group(1);
            s = s.replace(d1, "dd/mm/yyyy");
        }

        regex = "le (\\d{2}) \\w+ (\\d{4})";
        m = Pattern.compile(regex).matcher(s);
        while (m.find()) {
            String d1 = m.group(0);
            s = s.replace(d1, "le dd mm yyyy");
        }

        regex = "le (\\d{1}) \\w+ (\\d{4})";
        m = Pattern.compile(regex).matcher(s);
        while (m.find()) {
            String d1 = m.group(0);
            s = s.replace(d1, "le dd mm yyyy");
        }
        return s;
    }
}
