package bcx.playwright.util.data;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * contient des méthodes de transformation de date
 * @author bcx
 *
 */
@Slf4j
public class DateUtil {

    private DateUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * renvoi le jour courant au format indiqué
     * @param format
     * @return
     */
    public static String today(String format) {
        return (LocalDateTime.now()).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * renvoi le jour courant au format dd/MM/yyyy
     * @return
     */
    public static String today() {
        return today("dd/MM/yyyy");
    }

    /**
     * renvoi l'année courante
     * @return
     */
    public static String currentYear() {
        return today("yyyy");
    }

    /**
     * renvoi le moi courant
     * @return
     */
    public static String currentMonth() {
        return today("MM");
    }

    /**
     * renvoi le jour courant
     * @return
     */
    public static String currentDay() {
        return today("dd");
    }

    /**
     * renvoi le jour format dd correspandant au jour courant +/-n jour.
     * @param dayPlusMinus par exemple -5, +1
     * @return
     */
    public static String day(int dayPlusMinus) {
        return todayPlusDays("dd", dayPlusMinus);
    }


    /**
     * renvoi le mois format MM correspandant au mois courant +/-n mois.
     * @param monthPlusMinus par exemple -5, +1
     * @return
     */
    public static String month(int monthPlusMinus) {
        return todayPlusMonths("MM", monthPlusMinus);
    }


    /**
     * renvoi l'année format yyyy correspandant à l'année courante +/n jour.
     * @param yearPlusMinus par exemple -5, +1
     * @return
     */
    public static String year(int yearPlusMinus) {
        return todayPlusYears("yyyy", yearPlusMinus);
    }

    /**
     * renvoi le jour courant +N jours au format indiqué
     * @param format
     * @param nbDays
     * @return
     */
    public static String todayPlusDays(String format, int nbDays) {
        return (LocalDateTime.now().plusDays(nbDays)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * renvoi le jour courant +N jours au format dd/MM/yyyy
     * @param nbDays
     * @return
     */
    public static String todayPlusDays(int nbDays) {
        return (LocalDateTime.now().plusDays(nbDays)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * renvoi le jour courant -N jours au format indiqué
     * @param format
     * @param nbDays
     * @return
     */
    public static String todayMinusDays(String format, int nbDays) {
        return (LocalDateTime.now().minusDays(nbDays)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * renvoi le jour courant -N jours au format dd/MM/yyyy
     * @param nbDays
     * @return
     */
    public static String todayMinusDays(int nbDays) {
        return (LocalDateTime.now().minusDays(nbDays)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * renvoi le jour courant +N mois au format indiqué
     * @param format
     * @param nbMonths
     * @return
     */
    public static String todayPlusMonths(String format, int nbMonths) {
        return (LocalDateTime.now().plusMonths(nbMonths)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * renvoi le jour courant +N mois au format dd/MM/yyyy
     * @param nbMonths
     * @return
     */
    public static String todayPlusMonths(int nbMonths) {
        return (LocalDateTime.now().plusMonths(nbMonths)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * renvoi le jour courant -N mois au format indiqué
     * @param format
     * @param nbMonths
     * @return
     */
    public static String todayMinusMonths(String format, int nbMonths) {
        return (LocalDateTime.now().minusMonths(nbMonths)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * renvoi le jour courant -N mois au format dd/MM/yyyy
     * @param nbMonths
     * @return
     */
    public static String todayMinusMonths(int nbMonths) {
        return (LocalDateTime.now().minusMonths(nbMonths)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * renvoi le jour courant +N années au format indiqué
     * @param format
     * @param nbYears
     * @return
     */
    public static String todayPlusYears(String format, int nbYears) {
        return (LocalDateTime.now().plusYears(nbYears)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * renvoi le jour courant +N années au format dd/MM/yyyy
     * @param nbYears
     * @return
     */
    public static String todayPlusYears(int nbYears) {
        return (LocalDateTime.now().plusYears(nbYears)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * renvoi le jour courant -N années au format indiqué
     * @param format
     * @param nbYears
     * @return
     */
    public static String todayMinusYears(String format, int nbYears) {
        return (LocalDateTime.now().minusYears(nbYears)).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * renvoi le jour courant -N années au format dd/MM/yyyy
     * @param nbYears
     * @return
     */
    public static String todayMinusYears(int nbYears) {
        return (LocalDateTime.now().minusYears(nbYears)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * ajoute nbDay jours à une date
     * @param date string date, p.e. "01/01/2000"
     * @param dateFormat string format de la date "dd/MM/yyyy"
     * @return date + nbDay même format
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
     * ajoute nbDay jours à une date
     * @param date string date, p.e. "01/01/2000"
     * @param dateFormat string format de la date "dd/MM/yyyy"
     * @param nbDay nb jour à ajouter
     * @return date + nbDay même format
     */
    public static String plusDay(String date, String dateFormat, int nbDay) {
        return plusSomething(date, dateFormat, nbDay, Calendar.DATE);
    }

    /**
     * ajoute nbMonth mois à une date
     * @param date string date, p.e. "01/01/2000"
     * @param dateFormat string format de la date "dd/MM/yyyy"
     * @param nbMonth nb mois à ajouter
     * @return date + nbMonth même format
     */
    public static String plusMonth(String date, String dateFormat, int nbMonth) {
        return plusSomething(date, dateFormat, nbMonth, Calendar.MONTH);
    }

    /**
     * ajoute nbYear années à une date
     * @param date string date, p.e. "01/01/2000"
     * @param dateFormat string format de la date "dd/MM/yyyy"
     * @param nbYear nb années à ajouter
     * @return date + nbYear même format
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
     * reformate une date d'un format vers un autre
     * @param date
     * @param initFormat
     * @param targetFormat
     * @return
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
     * dans une sting, remplace les dates au format dd/mm/yyyy par "dd/mm/yyyy", et les dates au format "le dd mois yyyy" par "le dd mm yyyy"
     * @param s
     * @return
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