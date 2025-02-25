package bcx.playwright.util.image;

import com.relevantcodes.extentreports.LogStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import bcx.playwright.properties.GlobalProp;
import bcx.playwright.report.Reporter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
public class ImageUtil {
    private static final String TEST_FILE_REPO = Paths.get("").toAbsolutePath() + "/" + GlobalProp.getTestFileFolder();

    public ImageUtil() {
        throw new IllegalStateException("Utility class");
    }


    private static String encodeFileToBase64Binary(File file){
        String encodedfile = null;
        if (file.exists()) {
            try (FileInputStream fileInputStreamReader = new FileInputStream(file)) {
                byte[] bytes = new byte[(int) file.length()];
                if (fileInputStreamReader.read(bytes) > 0) {
                    encodedfile = new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                log.error("encodeFileToBase64Binary exception", e);
            }
        }
        return encodedfile;
    }

    public static boolean compare(Reporter report, String pdfFilePath, String refPdfFilePath, double pourcentageDiffAdmissible) throws Exception {
        return compare(report, null, pdfFilePath, refPdfFilePath, pourcentageDiffAdmissible);
    }

    public static boolean compare(Reporter report, String jiraKey, String pdfFilePath, String refPdfFilePath, double pourcentageDiffAdmissible) throws Exception {
        boolean hasDiff = true;
        double difference = -1;
        String diffBase64 = null;
        File fileImageCurrent = new File(pdfFilePath);
        File fileImageRef = new File(refPdfFilePath);
        if (jiraKey!=null && !new File(GlobalProp.getReportFolder()+jiraKey).exists())
            new File(GlobalProp.getReportFolder()+jiraKey).mkdir();
        File rempFile = new File(GlobalProp.getReportFolder() + (jiraKey!=null?jiraKey+"/":"") +   fileImageRef.getName());
        File rempFileSansMasque = new File(GlobalProp.getReportFolder() + (jiraKey!=null?jiraKey+"/":"") + "SANS_MASQUE_" + fileImageRef.getName());
        File fileImageDiff = new File(fileImageCurrent.getParent() + "/" + fileImageRef.getName().replace(".", "_DIFF."));
        boolean hasMasque = false;
        double nbpixdiff = 0;

        if (!fileImageCurrent.exists()) throw new FileNotFoundException("Fil 1 not found: " + pdfFilePath);

        File fileImageCurrentMasked = new File(fileImageCurrent.getParent() + "/" + fileImageRef.getName().replace(".", "_WITH_MASKED_ZONE."));
        if (fileImageRef.exists()) {
            if (!ImageUtil.areImageEquals(pdfFilePath, refPdfFilePath)) {
                BufferedImage img1 = ImageIO.read(fileImageCurrent);
                BufferedImage img2 = ImageIO.read(fileImageRef);

                int highlight1 = Color.MAGENTA.getRGB();
                int highlight2 = Color.GREEN.getRGB();
                //int[] zoneAcomparer = getComparisonZone(fileRefPath, fileCurrentPath, img1.getWidth(), img1.getHeight(), img2.getWidth(), img2.getHeight(), fileImageCurrent);
                int w = img1.getWidth() > img2.getWidth() ? img2.getWidth() : img1.getWidth();//zoneAcomparer[4];
                int h = img1.getHeight() > img2.getHeight() ? img2.getHeight() : img1.getHeight();
                int xStartIm1 = 0;// zoneAcomparer[0];
                int yStartIm1 = 0;// zoneAcomparer[1];
                int xStartIm2 = 0;// zoneAcomparer[2];
                int yStartIm2 = 0;// zoneAcomparer[3];

                double minnbpixdiff = w * h;

                int[] p1 = img1.getRGB(xStartIm1, yStartIm1, w, h, null, 0, w);
                int[] p1PourRemplacementSansMasque = img1.getRGB(xStartIm1, yStartIm1, w, h, null, 0, w);
                int[] p1PourRemplacementAvecMasque = img1.getRGB(xStartIm1, yStartIm1, w, h, null, 0, w);
                int[] p2 = img2.getRGB(xStartIm2, yStartIm2, w, h, null, 0, w);
                nbpixdiff = 0;
                for (int i = 0; i < Math.min(p1.length, p2.length); i++) {
                    if (p2[i] != -12629812 && p1[i] != p2[i]) {  //rgb : 63,72,204 masquer les zones à ne pas vérifier dans vos png avec cette couleur
                        Color c1 = new Color(p1[i], true);
                        Color c2 = new Color(p2[i], true);
                        int c1r = c1.getRed();
                        int c1g = c1.getGreen();
                        int c1b = c1.getBlue();
                        int c2r = c2.getRed();
                        int c2g = c2.getGreen();
                        int c2b = c2.getBlue();
                        double r = (c1r + c2r) / 2;
                        double Dr = (c1r - c2r);
                        double Dg = (c1g - c2g);
                        double Db = (c1b - c2b);
                        double DC = Math.sqrt((2 + r / 256) * Dr * Dr + 4 * Dg * Dg + (2 + (255 - r) / 256) * Db * Db);


                        if (DC > 10) {
                            float[] hsv1 = Color.RGBtoHSB(c1r, c1g, c1b, null);
                            float[] hsv2 = Color.RGBtoHSB(c2r, c2g, c2b, null);
                            float ecartTeinte = Math.abs(hsv1[0] - hsv2[0]) * 100;
                            float ecartSaturation = Math.abs(hsv1[1] - hsv2[1]) * 100;
                            float ecartLum = Math.abs(hsv1[2] - hsv2[2]) * 100;
                            if (((ecartTeinte + ecartSaturation) > 0.0 || ecartLum > 50) && !(ecartTeinte < 0.1 && (ecartSaturation + ecartLum) < 14)) {
                                //log.info(ecartTeinte + " " + ecartSaturation + " " + ecartLum);
                                if (p1[i] - p2[i] > 0) {
                                    p1[i] = highlight1;
                                } else {
                                    p1[i] = highlight2;
                                }
                                nbpixdiff += 1;
                                if (nbpixdiff > minnbpixdiff) break;
                            }
                        }
                    }
                    if (p2[i] == -12629812) {
                        hasMasque = true;
                        p1PourRemplacementAvecMasque[i] = -12629812;
                    }
                }
                BufferedImage outcurrent = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                if (nbpixdiff > 0 && hasMasque) {
                    try {
                        outcurrent.setRGB(0, 0, w, h, p1PourRemplacementAvecMasque, 0, w);
                    } catch (Exception eprout) {
                        log.error("ERROR " + eprout.getMessage());
                    }
                    ImageIO.write(outcurrent, "png", fileImageCurrentMasked);
                }
                try {
                    outcurrent.setRGB(0, 0, w, h, p1PourRemplacementSansMasque, 0, w);
                } catch (Exception eprout) {
                    log.error("ERROR " + eprout.getMessage());
                }
                ImageIO.write(outcurrent, "png", fileImageCurrent);
                BufferedImage outdiff = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                try {
                    outdiff.setRGB(0, 0, w, h, p1, 0, w);
                } catch (Exception eprout) {
                    log.error("ERROR " + eprout.getMessage());
                }
                ImageIO.write(outdiff, "png", fileImageDiff);
                difference = nbpixdiff / p1.length * 100;
                log.info("diff image " + nbpixdiff + "==>" + difference + " " + p1.length);
            } else {
                difference = 0;
            }
            diffBase64 = encodeFileToBase64Binary(fileImageDiff);
        } else {
            Files.copy(fileImageCurrent.toPath(), rempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            difference = -100;
        }

        if (difference > 0) {
            if (difference < pourcentageDiffAdmissible || nbpixdiff < 10) {
                report.log(Reporter.PASS_STATUS, "diff écran / reference = " + nbpixdiff + " pixels, " + difference + " %");
                report.logImage(LogStatus.PASS, diffBase64);
                hasDiff = false;
            } else {
                if (Math.abs(difference) < 10) {
                    report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "diff écran / reference = " + nbpixdiff + " pixels, " + difference + " %");
                    report.logImage(LogStatus.WARNING, diffBase64);
                } else {
                    Files.copy(fileImageRef.toPath(), new File(TEST_FILE_REPO + "/REF_" + fileImageRef.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "diff écran / reference = " + nbpixdiff + " pixels, " + difference + " %");
                    report.logImage(LogStatus.WARNING, diffBase64);
                }
                if (hasMasque) {
                    Files.copy(fileImageCurrentMasked.toPath(), rempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(fileImageCurrent.toPath(), rempFileSansMasque.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.copy(fileImageCurrent.toPath(), rempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } else {
            if (difference >= 0) {
                report.log(Reporter.PASS_STATUS, "Image identique à la réf");
                hasDiff = false;
            } else {
                report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "Référence manquante");
                hasDiff = false;
            }
        }
        if (fileImageCurrentMasked.exists()) fileImageCurrentMasked.delete();
        if (fileImageDiff.exists()) fileImageDiff.delete();

        return hasDiff;
    }

    public static boolean areImageEquals(String file1, String file2) {
        boolean areEquals = true;
        try {
            BufferedImage img1 = ImageIO.read(new File(file1));
            BufferedImage img2 = ImageIO.read(new File(file2));
            final int w = img1.getWidth(),
                    h = img1.getHeight();
            final int w2 = img2.getWidth(),
                    h2 = img2.getHeight();
            if (w == w2 && h == h2) {
                int[] p1 = img1.getRGB(0, 0, w, h, null, 0, w);
                int[] p2 = img2.getRGB(0, 0, w2, h2, null, 0, w2);
                for (int i = 0; i < Math.min(p1.length, p2.length); i++) {
                    if (p1[i] != -12629812 && p2[i] != -12629812) {
                        Color c1 = new Color(p1[i], true);
                        Color c2 = new Color(p2[i], true);
                        if ((Math.abs(c1.getRed() - c2.getRed()) >= 10) || (Math.abs(c1.getGreen() - c2.getGreen()) >= 10) || (Math.abs(c1.getBlue() - c2.getBlue()) >= 10)) {
                            areEquals = false;
                            break;
                        }
                    }
                }
            } else {
                areEquals = false;
            }
        } catch (Exception e) {
            areEquals = false;
        }
        log.info(file1 + " EQUALS " + file2 + " : " + areEquals);
        return areEquals;
    }

}