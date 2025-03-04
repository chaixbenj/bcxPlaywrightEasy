package bcx.automation.util.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import bcx.automation.properties.EnvProp;
import bcx.automation.util.TimeWait;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import bcx.automation.properties.GlobalProp;
import bcx.automation.report.Reporter;

/**
 * Contient des méthodes de valorisation et de transformation de données.
 *
 * @author bcx
 */
@Slf4j
public class DataUtil {
    private static final String TEST_FILE_PATH = "target/test-classes/test_files/";
    private static final String IGNORE_STRING = "[IGNORE_STRING]";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private DataUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Remplace les placeholders dans une chaîne de caractères par les valeurs spécifiées dans une map.
     *
     * @param s           La chaîne de caractères contenant les placeholders.
     * @param replacements La map contenant les paires clé/valeur pour les remplacements.
     * @return La chaîne de caractères avec les placeholders remplacés.
     */
    public static String replacePlaceholders(String s, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            s = s.replace(entry.getKey(), entry.getValue());
        }
        return s;
    }

    /**
     * Lit un fichier, remplace les paramètres par les valeurs spécifiées et crée un nouveau fichier.
     * Renvoie le nom du fichier créé (fichier dans resources/test_files).
     *
     * @param file   Le fichier d'entrée.
     * @param params  Les chaînes de caractères à remplacer dans le fichier.
     * @param values  Les chaînes de remplacement.
     * @return Le nom du fichier créé.
     */
    public static String[] createValorisedFile(String file, String[] params, String[] values) {
        String timestamp = String.valueOf(new Date().getTime());
        File initFile = new File(Paths.get("").toAbsolutePath() + File.separator + TEST_FILE_PATH + file);
        String fileContent = getFileContent(Paths.get("").toAbsolutePath() + File.separator + TEST_FILE_PATH + file);
        int i = 0;
        for (String param : params) {
            fileContent = fileContent.replace(param, values[i]);
            i++;
        }
        String newFileName = timestamp + initFile.getName();
        String newFilePath = Paths.get("").toAbsolutePath() + File.separator + TEST_FILE_PATH + newFileName;
        File valorisedFile = new File(newFilePath);
        try {
            Files.createFile(valorisedFile.toPath());
            try (FileWriter writer = new FileWriter(newFilePath)) {
                writer.write(fileContent);
            }
        } catch (Exception e) {
            // Ignorer l'exception
        }
        return new String[]{newFilePath, newFileName};
    }

    /**
     * Renvoie une chaîne de caractères aléatoire unique.
     *
     * @return Une chaîne de caractères aléatoire unique.
     */
    public static String randomAlphaString() {
        String timestamp = String.valueOf(new Date().getTime());
        String replacementChar = "abeciropus";
        for (int i = 0; i < 10; i++) {
            timestamp = timestamp.replace(String.valueOf(i), replacementChar.substring(i, i + 1));
        }
        return timestamp;
    }

    /**
     * Renvoie la valeur de la date au format jj/mm/aaaa à partir d'une chaîne de type sysdate+2, sysdate, sysdate-3 (+/- N jours).
     *
     * @param sDate La chaîne de caractères représentant la date.
     * @return La date au format jj/mm/aaaa.
     */
    public static String transformDate(String sDate) {
        if (sDate.startsWith("sysdate")) {
            LocalDateTime today = LocalDateTime.now();
            if (sDate.contains("+")) {
                today = today.plusDays(Integer.parseInt(sDate.replace("sysdate+", "")));
            } else {
                if (sDate.contains("-")) {
                    today = today.minusDays(Integer.parseInt(sDate.replace("sysdate-", "")));
                }
            }
            return today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            return sDate;
        }
    }

    /**
     * Supprime un fichier.
     *
     * @param file Le chemin du fichier à supprimer.
     */
    public static void deleteFile(String file) {
        try {
            Files.delete(new File(file).toPath());
        } catch (Exception ex) {
            // Ignorer l'exception
        }
    }

    /**
     * Vérifie l'existence d'un fichier.
     *
     * @param report Le rapporteur pour les logs.
     * @param file   Le chemin du fichier à vérifier.
     */
    public static void assertFileExists(Reporter report, String file) {
        File f = new File(file);
        TimeWait wait = new TimeWait();
        try {
            while (!f.exists() && wait.notOver(30)) {
                // Boucler jusqu'à ce que le fichier existe ou que le délai soit écoulé
            }
        } catch (Exception ignore) {
            // Ignorer l'exception
        }
        report.assertEquals("Le fichier " + file + " existe ", true, f.exists());
    }

    /**
     * Renvoie le contenu d'un fichier ou la chaîne en paramètre si le fichier n'existe pas.
     *
     * @param filePath Le chemin du fichier.
     * @return Le contenu du fichier ou la chaîne en paramètre si le fichier n'existe pas.
     */
    public static String getFileContent(String filePath) {
        StringBuilder fileContent = new StringBuilder();
        int i = 0;
        while (i < 3) {
            i++;
            String fPath = i == 1 ? filePath : (i == 2 ? GlobalProp.getTestFileFolder() + filePath : GlobalProp.getTestFileFolder() + EnvProp.getEnvironnement().toUpperCase() + filePath);
            try (BufferedReader br = new BufferedReader(new FileReader(fPath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
                return fileContent.toString();
            } catch (Exception ignore) {
                // Ignorer l'exception
            }
        }
        return filePath;
    }

    /**
     * Remplace des variables dans une chaîne de caractères, notamment dans un jeu de données CSV/JSON.
     * Remplace les valeurs suivantes dans une chaîne :
     * - {RANDOMSTRING} par une chaîne aléatoire en majuscules.
     * - {randomstring} par une chaîne aléatoire en minuscules.
     * - {Randomstring} par une chaîne aléatoire en minuscules avec le premier caractère en majuscule.
     * - {DAY+2} par le jour courant +2 jours au format dd.
     * - {MONTH+2} par le mois courant +2 mois au format MM.
     * - {YEAR+2} par l'année courante +2 ans au format yyyy.
     * - {CURRENTDATE_dd/MM/yyyy_+2} par la date courante +2 jours au format dd/MM/yyyy.
     *
     * @param value La chaîne de caractères à transformer.
     * @return La chaîne de caractères transformée.
     */
    public static String variabilise(String value) {
        String v = String.valueOf(value);
        boolean variabilised = false;
        if (v != null && v.contains("{")) {
            v = v.replace("{", "|").replace("}", "|");
            String[] tabV = StringUtils.split(v, "|");
            int i = 0;
            for (String tabVi : tabV) {
                if (tabVi.equals("RANDOMSTRING")) {
                    tabV[i] = randomAlphaString().toUpperCase();
                    variabilised = true;
                } else if (tabVi.equals("randomstring")) {
                    tabV[i] = randomAlphaString();
                    variabilised = true;
                } else if (tabVi.equals("Randomstring")) {
                    tabV[i] = StringUtils.capitalize(randomAlphaString());
                    variabilised = true;
                } else if (tabVi.startsWith("DAY")) {
                    tabV[i] = DateUtil.day(tabVi.equals("DAY") ? 0 : Integer.parseInt(tabVi.replace("DAY", "")));
                    variabilised = true;
                } else if (tabVi.startsWith("MONTH")) {
                    tabV[i] = DateUtil.month(tabVi.equals("MONTH") ? 0 : Integer.parseInt(tabVi.replace("MONTH", "")));
                    variabilised = true;
                } else if (tabVi.startsWith("YEAR")) {
                    tabV[i] = DateUtil.year(tabVi.equals("YEAR") ? 0 : Integer.parseInt(tabVi.replace("YEAR", "")));
                    variabilised = true;
                } else if (tabVi.startsWith("CURRENTDATE")) {
                    String[] currentDateParams = tabVi.split("_");
                    tabV[i] = DateUtil.todayPlusDays(currentDateParams[1], currentDateParams.length == 2 ? 0 : Integer.parseInt(currentDateParams[2]));
                    variabilised = true;
                }
                i++;
            }
            v = StringUtils.join(tabV);
        }
        return variabilised ? v : value;
    }

    /**
     * Trace les mots en écarts dans deux textes supposés égaux.
     *
     * @param report   Le rapporteur pour les logs.
     * @param refText  Le texte de référence.
     * @param text     Le texte à comparer.
     * @param logStatus Le statut de log.
     * @return Le statut de la comparaison.
     */
    public static String diffText(Reporter report, String refText, String text, String logStatus) {
        refText = String.valueOf(refText);
        text = String.valueOf(text);
        String[] s1 = refText.replace(",", " , ").split(" ");
        String[] s2 = text.replace(",", " , ").split(" ");
        for (int i = 0; i < Math.min(s1.length, s2.length); i++) {
            if (s1[i].equals(IGNORE_STRING) || s2[i].equals(IGNORE_STRING)) {
                s1[i] = "";
                s2[i] = "";
            }
        }
        if (StringUtils.join(s1, " ").equals(StringUtils.join(s2, " "))) {
            return Reporter.PASS_STATUS;
        }
        String initRefText = refText;
        String initText = text;
        refText = refText.replace("\n", " ").replace("  \n", " ").replace("  ", " ");
        text = text.replace("\n", " ").replace("  \n", " ").replace("  ", " ");
        refText = StringUtils.normalizeSpace(refText.trim());
        text = StringUtils.normalizeSpace(text.trim());
        s1 = refText.replace(",", " , ").split(" ");
        s2 = text.replace(",", " , ").split(" ");
        for (int i = 0; i < Math.min(s1.length, s2.length); i++) {
            if (s1[i].equals(IGNORE_STRING) || s2[i].equals(IGNORE_STRING)) {
                s1[i] = "";
                s2[i] = "";
            }
        }
        refText = StringUtils.join(s1, " ");
        text = StringUtils.join(s2, " ");

        if (refText.equalsIgnoreCase(text)) {
            report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "Différences mineures de saut de ligne ou de casse. Attendu : >" + initRefText + "< Constaté: >" + initText + "<");
            return Reporter.WARNING_STATUS_NO_SCREENSHOT;
        } else {
            int is1 = 0;
            int lastIs1 = 0;
            while (is1 < s1.length) {
                if (s1[is1].length() > 0) {
                    int is2 = 0;
                    while (is2 < s2.length && !s1[is1].equals(s2[is2])) {
                        is2++;
                    }
                    if (is2 < s2.length) {
                        int iis1 = is1;
                        int iis2 = is2;
                        String repStr = "";
                        while (iis1 < s1.length && iis2 < s2.length && s1[iis1].equals(s2[iis2])) {
                            repStr += s1[iis1] + " ";
                            iis1++;
                            iis2++;
                        }
                        refText = "__" + refText.replace(" ", "__") + "__";
                        text = "__" + text.replace(" ", "__") + "__";
                        refText = refText.replace("__" + repStr.trim().replace(" ", "__") + "__", "__").trim();
                        text = text.replace("__" + repStr.trim().replace(" ", "__") + "__", "__").trim();
                        refText = refText.replace("__" + repStr.trim().replace(" ", "__") + ".", "__").trim();
                        text = text.replace("__" + repStr.trim().replace(" ", "__") + ".", "__").trim();
                        refText = refText.replace("__" + repStr.trim().replace(" ", "__") + ",", "__").trim();
                        text = text.replace("__" + repStr.trim().replace(" ", "__") + ",", "__").trim();
                        refText = StringUtils.normalizeSpace(refText.replace("__", " ").trim());
                        text = StringUtils.normalizeSpace(text.replace("__", " ").trim());
                        s1 = refText.split(" ");
                        s2 = text.split(" ");
                        is1 = lastIs1;
                    } else {
                        is1++;
                        lastIs1 = is1;
                    }
                } else {
                    is1++;
                }
            }
            refText = StringUtils.normalizeSpace(refText.replace("__", " ").trim());
            text = StringUtils.normalizeSpace(text.replace("__", " ").trim());
            initRefText = "__" + initRefText.replace(" ", "__") + "__";
            initText = "__" + initText.replace(" ", "__") + "__";
            for (String diffWord : refText.split(" ")) {
                String diffWordM = "__" + diffWord + "__";
                initRefText = initRefText.replace(diffWordM, "<b style='background-color:red'>" + diffWordM + "</b>");
                diffWordM = "__" + diffWord + ".";
                initRefText = initRefText.replace(diffWordM, "<b style='background-color:red'>" + diffWordM + "</b>");
                diffWordM = "__" + diffWord + ",";
                initRefText = initRefText.replace(diffWordM, "<b style='background-color:red'>" + diffWordM + "</b>");
            }
            for (String diffWord : text.split(" ")) {
                String diffWordM = "__" + diffWord + "__";
                initText = initText.replace(diffWordM, "<b style='background-color:red'>" + diffWordM + "</b>");
                diffWordM = "__" + diffWord + ".";
                initRefText = initRefText.replace(diffWordM, "<b style='background-color:red'>" + diffWordM + "</b>");
                diffWordM = "__" + diffWord + ",";
                initRefText = initRefText.replace(diffWordM, "<b style='background-color:red'>" + diffWordM + "</b>");
            }
            initRefText = initRefText.replace("__", " ").trim();
            initText = initText.replace("__", " ").trim();

            if (refText.trim().equals("") && text.trim().equals("")) {
                return Reporter.PASS_STATUS;
            } else {
                if (logStatus != null) {
                    report.log(logStatus, "Attendu :   \n" + initRefText + "  \nconstaté :   \n" + initText);
                }
                report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "retrait des textes communs ref : " + refText);
                report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "retrait des textes communs current : " + text);
                return Reporter.FAIL_NEXT_STATUS;
            }
        }
    }

    /**
     * Compare deux chaînes de caractères. Si l'une des chaînes contient un mot [IGNORE_STRING] en position n,
     * remplace le mot à cette position par vide dans les deux chaînes avant de faire la comparaison.
     *
     * @param report     Le rapporteur pour les logs.
     * @param s1         La première chaîne de caractères.
     * @param s2         La deuxième chaîne de caractères.
     * @param diffText   Indique si la différence de texte doit être tracée.
     * @return Le statut de la comparaison : pass (égalité), warning (égalité en ignorant la casse), fail_next.
     */
    public static String equalsIgnoreIgnoredString(Reporter report, String s1, String s2, boolean diffText) {
        String[] ts1 = String.valueOf(s1).replace("\n", " ").replace("  ", " ").split(" ");
        String[] ts2 = String.valueOf(s2).replace("\n", " ").replace("  ", " ").split(" ");
        for (int i = 0; i < Math.min(ts1.length, ts2.length); i++) {
            if (ts1[i].equals(IGNORE_STRING) || ts2[i].equals(IGNORE_STRING)) {
                ts1[i] = "";
                ts2[i] = "";
            }
        }
        s1 = String.join(" ", ts1);
        s2 = String.join(" ", ts2);
        if (s1.equals(s2)) {
            return Reporter.PASS_STATUS;
        } else if (s1.equalsIgnoreCase(s2)) {
            return Reporter.WARNING_STATUS;
        } else {
            if (diffText)
                return diffText(report, s1, s2, null);
            else
                return Reporter.FAIL_NEXT_STATUS;
        }
    }

    /**
     * Normalise les espaces dans une chaîne de caractères.
     *
     * @param value La chaîne de caractères à normaliser.
     * @return La chaîne de caractères avec les espaces normalisés.
     */
    public static String normalizeSpace(String value) {
        return org.apache.commons.lang3.StringUtils.normalizeSpace(
                String.join(" ",
                        (value == null ? "" : value).replace(" ", " ")
                                .replace(" ", " ")
                                .replace("’", "'")
                                .replaceAll("\\s", " ")
                                .replaceAll("\\s{2,}", " ")
                                .replaceAll("\n", LINE_SEPARATOR)
                                .replaceAll("\r\n", LINE_SEPARATOR).trim().split("\\s* \\s*")));
    }
}
