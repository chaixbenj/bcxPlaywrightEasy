package bcx.automation.report;

import bcx.automation.properties.EnvProp;
import bcx.automation.util.data.DateUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import bcx.automation.properties.GlobalProp;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe utilitaire pour générer des rapports de test au format PDF à partir de fichiers HTML.
 */
public class HtmlReportToPdf {
    public static final String HTML = ".html";
    private static Font fontBigBlack = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.BLACK);
    private static Font fontBlack = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static Font fontBlackTimes = FontFactory.getFont(FontFactory.TIMES, 10, BaseColor.BLACK);
    private static Font fontGreen = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.GREEN);
    private static Font fontRed = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.RED);
    private static Font fontOrange = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.ORANGE);
    private static Font fontBlue = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLUE);

    /**
     * Génère un rapport PDF à partir de tous les fichiers HTML présents dans le dossier de rapports.
     */
    public static void generate() {
        ArrayList<String> filenames = new ArrayList<>();
        File[] files = new File(GlobalProp.getReportFolder()).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String filename = file.getName();
                if (filename.endsWith(HTML) && !filename.contains("index_tests")) {
                    filenames.add(filename.replace(HTML, ""));
                }
            }
        }
        generate(filenames.toArray(new String[filenames.size()]));
    }

    /**
     * Génère un rapport PDF à partir d'un fichier HTML spécifique.
     *
     * @param name Le nom du fichier HTML sans l'extension.
     */
    public static void generate(String name) {
        if (new File(GlobalProp.getReportFolder() + name + HTML).exists()) {
            generate(new String[]{name});
        }
    }

    /**
     * Génère un rapport PDF à partir d'une liste de fichiers HTML.
     *
     * @param names Les noms des fichiers HTML sans l'extension.
     */
    public static void generate(String[] names) {
        try {
            HashMap<String, ArrayList<String>> allTestsName = new HashMap<>();
            HashMap<String, ArrayList<String>> allTestsStatus = new HashMap<>();

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(GlobalProp.getReportFolder() + (names.length == 1 ? names[0] : "test_report") + ".pdf"));
            document.open();
            Paragraph saut = new Paragraph("\n");
            Chunk titre = new Chunk("Rapports de test " + EnvProp.getEnvironnement() + " du " + DateUtil.today("dd/MM/yyyy HH:mm:ss"), fontBigBlack);
            document.add(titre);
            document.add(saut);
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(90);
            table.setWidths(new float[]{1, 4});

            generateIndex(names, document, table, allTestsName, allTestsStatus);
            generateDetail(names, document, allTestsName, allTestsStatus);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Génère l'index du rapport PDF.
     *
     * @param names Les noms des fichiers HTML sans l'extension.
     * @param document Le document PDF.
     * @param table La table PDF pour l'index.
     * @param allTestsName La liste des noms de tests.
     * @param allTestsStatus La liste des statuts de tests.
     * @throws Exception En cas d'erreur lors de la génération de l'index.
     */
    private static void generateIndex(String[] names, Document document, PdfPTable table, HashMap<String, ArrayList<String>> allTestsName, HashMap<String, ArrayList<String>> allTestsStatus) throws Exception {
        Paragraph saut = new Paragraph("\n");
        String testName;
        String testStatus;
        for (String name : names) {
            org.jsoup.nodes.Document html = Jsoup.parse(new File(GlobalProp.getReportFolder() + name + HTML), "UTF-8");
            Elements testsList = html.getElementsByClass("test-head");
            ArrayList<String> arrayTestsName = new ArrayList<>();
            ArrayList<String> arrayTestsStatus = new ArrayList<>();
            for (int n = 0; n < testsList.size(); n++) {
                testName = testsList.get(n).child(0).text();
                testStatus = testsList.get(n).child(1).text();

                Chunk etat = new Chunk(testStatus, getStatusFont(testStatus));
                Anchor anchor = new Anchor(testName, fontBlack);
                anchor.setReference("#" + testName.replace(" ", "").trim());
                table.addCell(new Phrase(etat));
                table.addCell(anchor);
                arrayTestsName.add(testName);
                arrayTestsStatus.add(testStatus);
            }
            allTestsName.put(name, arrayTestsName);
            allTestsStatus.put(name, arrayTestsStatus);
        }
        document.add(table);
        document.add(saut);
    }

    /**
     * Génère les détails du rapport PDF.
     *
     * @param names Les noms des fichiers HTML sans l'extension.
     * @param document Le document PDF.
     * @param allTestsName La liste des noms de tests.
     * @param allTestsStatus La liste des statuts de tests.
     * @throws Exception En cas d'erreur lors de la génération des détails.
     */
    private static void generateDetail(String[] names, Document document, HashMap<String, ArrayList<String>> allTestsName, HashMap<String, ArrayList<String>> allTestsStatus) throws Exception {
        Paragraph saut = new Paragraph("\n");
        String stepStatus;
        String stepTime;
        String stepDesc;
        for (String name : names) {
            org.jsoup.nodes.Document html = Jsoup.parse(new File(GlobalProp.getReportFolder() + name + HTML), "UTF-8");
            Elements testsList = html.getElementsByClass("test-head");
            for (int n = 0; n < testsList.size(); n++) {
                Anchor target = new Anchor(allTestsName.get(name).get(n) + "   ", fontBigBlack);
                target.setName(allTestsName.get(name).get(n).replace(" ", "").trim());
                document.add(saut);
                document.add(target);
                Chunk etat = new Chunk(allTestsStatus.get(name).get(n), getStatusFont(allTestsStatus.get(name).get(n)));
                document.add(etat);
                document.add(saut);
                PdfPTable tableDesc = new PdfPTable(3);
                tableDesc.setWidthPercentage(99);
                tableDesc.setWidths(new float[]{1, 1, 10});

                Elements testSteps = testsList.get(n).parent().getElementsByClass("test-steps").get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
                for (int m = 0; m < testSteps.size(); m++) {
                    stepStatus = testSteps.get(m).child(0).attribute("title").getValue();
                    stepTime = testSteps.get(m).child(1).text();
                    stepDesc = testSteps.get(m).child(2).html();
                    Elements images = testSteps.get(m).child(2).getElementsByTag("img");
                    etat = new Chunk(stepStatus + "   ", getStatusFont(stepStatus));
                    Chunk time = new Chunk(stepTime, fontBlackTimes);
                    tableDesc.addCell(new Phrase(etat));
                    tableDesc.addCell(new Phrase(time));
                    if (images != null && !images.isEmpty()) {
                        String base64 = images.get(0).attribute("src").getValue().replace("data:image/png;base64,", "");
                        Image img = Image.getInstance(Base64.decodeBase64(base64));
                        float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                                - document.rightMargin() - 10) / img.getWidth()) * 100;
                        img.scalePercent(scaler);
                        tableDesc.addCell(img);
                    } else {
                        String desc = stepDesc;
                        if (desc.contains("<a target=\"_blank\" href=\"")) {
                            desc = "navigate to url " + desc.split(">")[1].replace("</a", "");
                        }
                        Chunk libelle = new Chunk(desc.replace("<br>", "\n")
                                .replace("\n\n", "\n")
                                .replace("&gt;", ">")
                                .replace("&lt;", "<")
                                .replace("<b>", "")
                                .replace("</b>", "")
                                .replace("<h5>", "")
                                .replace("</h5>", ""), fontBlackTimes);
                        tableDesc.addCell(new Phrase(libelle));
                    }
                }
                document.add(tableDesc);
            }
        }
    }

    /**
     * Renvoie la police correspondant au statut du test.
     *
     * @param status Le statut du test.
     * @return La police correspondant au statut.
     */
    private static Font getStatusFont(String status) {
        switch (status.toLowerCase()) {
            case "pass":
                return fontGreen;
            case "fail":
            case "error":
                return fontRed;
            case "warning":
                return fontOrange;
            case "info":
            case "skip":
                return fontBlue;
            default:
                return fontBlack;
        }
    }
}
