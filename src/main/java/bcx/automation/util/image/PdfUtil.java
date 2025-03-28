package bcx.automation.util.image;

import bcx.automation.util.TimeWait;
import bcx.automation.util.data.DataUtil;
import bcx.automation.util.data.DateUtil;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import bcx.automation.properties.GlobalProp;
import bcx.automation.report.Reporter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utilitaire pour la manipulation de fichiers PDF.
 */
public class PdfUtil {

    /**
     * Renvoie le nombre de pages d'un fichier PDF.
     *
     * @param pdfFilePath Le chemin du fichier PDF.
     * @return Le nombre de pages du fichier PDF.
     */
    public static int getNbPages(String pdfFilePath) {
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        PDDocument pd;
        boolean ok = false;
        int nbpage = 0;
        TimeWait wait = new TimeWait();
        while (!ok && wait.notOver(30)) {
            try {
                pd = Loader.loadPDF(new File(pdfFilePath));
                ok = true;
                nbpage = pd.getNumberOfPages();
            } catch (Exception ignore) {
                // On recommence
            }
        }
        return nbpage;
    }

    /**
     * Convertit chaque page d'un fichier PDF en image PNG.
     *
     * @param pdfFilePath Le chemin du fichier PDF.
     * @return Le nombre de pages converties en images PNG.
     */
    public static int toPng(String pdfFilePath) {
        try {
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            PDDocument pd = Loader.loadPDF(new File(pdfFilePath));
            PDFRenderer pr = new PDFRenderer(pd);
            for (int i = 0; i < pd.getNumberOfPages(); i++) {
                BufferedImage bi = pr.renderImageWithDPI(i, 720);
                ImageIO.write(bi, "PNG", new File(pdfFilePath.replace(".pdf", "_" + i + ".png")));
            }
            return pd.getNumberOfPages();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Convertit le contenu d'un fichier PDF en chaîne de caractères.
     *
     * @param pdfFilePath Le chemin du fichier PDF.
     * @return Le contenu du fichier PDF sous forme de chaîne de caractères.
     */
    public static String toString(String pdfFilePath) {
        String s = "";
        for (int i = 1; i <= getNbPages(pdfFilePath); i++) {
            s += toString(pdfFilePath, i) + "\n";
        }
        return s;
    }

    /**
     * Convertit le contenu d'une page spécifique d'un fichier PDF en chaîne de caractères.
     *
     * @param pdfFilePath Le chemin du fichier PDF.
     * @param pageNum Le numéro de la page à convertir.
     * @return Le contenu de la page spécifiée sous forme de chaîne de caractères.
     */
    public static String toString(String pdfFilePath, int pageNum) {
        try {
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            PDDocument pd = Loader.loadPDF(new File(pdfFilePath));
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(pageNum);
            pdfStripper.setEndPage(pageNum);
            return pdfStripper.getText(pd);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Compare un fichier PDF téléchargé avec un fichier PDF de référence.
     *
     * @param report Le rapporteur pour les logs.
     * @param downloadedPdfFilePath Le chemin du fichier PDF téléchargé.
     * @param refPdfFilePath Le chemin du fichier PDF de référence.
     */
    public static void compareToPdf(Reporter report, String downloadedPdfFilePath, String refPdfFilePath) {
        compareToPdf(report, downloadedPdfFilePath, refPdfFilePath, 0);
    }

    /**
     * Compare un fichier PDF téléchargé avec un fichier PDF de référence en tenant compte d'un pourcentage de différence admissible.
     *
     * @param report Le rapporteur pour les logs.
     * @param downloadedPdfFilePath Le chemin du fichier PDF téléchargé.
     * @param refPdfFilePath Le chemin du fichier PDF de référence.
     * @param pourcentageDifAdmissible Le pourcentage de différence admissible.
     */
    public static void compareToPdf(Reporter report, String downloadedPdfFilePath, String refPdfFilePath, double pourcentageDifAdmissible) {
        refPdfFilePath = GlobalProp.getTestFileFolder() + refPdfFilePath;
        TimeWait wait = new TimeWait();
        while (wait.notOver(30) && (!new File(downloadedPdfFilePath).exists() || !new File(refPdfFilePath).exists())) {
            // On attend que les fichiers soient là
        }
        if (!new File(downloadedPdfFilePath).exists()) {
            report.log(Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT, "Fichier téléchargé " + downloadedPdfFilePath + " non trouvé");
        } else if (!new File(refPdfFilePath).exists()) {
            report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "Fichier référence " + refPdfFilePath + " non trouvé");
        } else {
            wait.reinit();
            try {
                int nbPageCurrent = getNbPages(downloadedPdfFilePath);
                int nbPageRef = getNbPages(refPdfFilePath);
                int nbPageCommon = nbPageCurrent > nbPageRef ? nbPageRef : nbPageCurrent;
                while (nbPageRef != nbPageCurrent && wait.notOver(60)) {
                    try {
                        Thread.sleep(1000);
                        nbPageCurrent = getNbPages(downloadedPdfFilePath);
                        nbPageRef = getNbPages(refPdfFilePath);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (DataUtil.getFileContent(downloadedPdfFilePath).equals(DataUtil.getFileContent(refPdfFilePath))) {
                    report.log(Reporter.PASS_STATUS, "Le document " + downloadedPdfFilePath + " est identique à la référence");
                    for (int i = 1; i <= nbPageCurrent; i++) {
                        report.log(Reporter.PASS_STATUS, "Contenu page " + i + " OK  \n" + toString(downloadedPdfFilePath, i));
                    }
                } else {
                    for (int i = 1; i <= nbPageCommon; i++) {
                        String pageCurrent = toString(downloadedPdfFilePath, i);
                        String pageRef = toString(refPdfFilePath, i);
                        if (pageCurrent.equals(pageRef)) {
                            report.log(Reporter.PASS_STATUS, "Contenu page " + i + " OK  \n" + pageCurrent);
                        } else {
                            if (DateUtil.anonymiseInString(pageCurrent).equals(DateUtil.anonymiseInString(pageRef))) {
                                DataUtil.diffText(report, pageRef, pageCurrent, Reporter.WARNING_STATUS_NO_SCREENSHOT);
                            } else {
                                DataUtil.diffText(report, pageRef, pageCurrent, Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT);
                            }
                        }
                    }
                    toPng(downloadedPdfFilePath);
                    toPng(refPdfFilePath);
                    if (nbPageRef != nbPageCurrent) {
                        report.log(Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT, "Le document " + downloadedPdfFilePath + " n'a pas le nombre de pages attendu " + nbPageCurrent + " vs " + nbPageRef);
                    }
                    for (int i = 0; i < nbPageCommon; i++) {
                        try {
                            ImageUtil.compare(report, downloadedPdfFilePath.replace(".pdf", "_" + i + ".png"), refPdfFilePath.replace(".pdf", "_" + i + ".png"), pourcentageDifAdmissible);
                        } catch (Exception e) {
                            report.log(Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT, "Erreur comparaison PDF " + downloadedPdfFilePath, e);
                        }
                    }
                }
                new File(downloadedPdfFilePath).delete();
            } catch (Exception e) {
                report.log(Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT, "Un des fichiers n'est pas un PDF", e);
            }
        }
    }
}
