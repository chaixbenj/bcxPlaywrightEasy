package bcx.automation.report;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import bcx.automation.properties.GlobalProp;
import bcx.automation.util.data.DataUtil;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Classe utilitaire pour générer des rapports de test.
 */
@Slf4j
public class Reporter {
    public static final String SKIP_STATUS = "skip";
    public static final String INFO_STATUS = "info";
    public static final String PASS_STATUS = "pass";
    public static final String WARNING_STATUS = "warning";
    public static final String WARNING_STATUS_NO_SCREENSHOT = "warningnoscreenshot";
    public static final String FAIL_STATUS = "fail";
    public static final String FAIL_NEXT_STATUS = "failnext";
    public static final String FAIL_STATUS_NO_SCREENSHOT = "failnoscreenshot";
    public static final String FAIL_NEXT_STATUS_NO_SCREENSHOT = "failnextnoscreenshot";
    public static final String CONTENT_TYPE = "image/png";

    @Getter
    @Setter
    private Page page;
    @Getter
    @Setter
    private RemoteWebDriver driver;
    private SoftAssert softAssert;
    private List<String> steps;
    @Getter
    private boolean inError;
    boolean testSkipped;
    private String previousLog;
    private LocalDateTime timePreviousLog;

    private ElementHandle currentElement;
    @Getter
    @Setter
    private boolean playwrightCmd;
    @Getter
    private String lastAction;
    @Getter
    private String lastStatus;

    /**
     * Constructeur par défaut.
     */
    public Reporter() {
        testSkipped = false;
    }

    /**
     * Initialise un test.
     */
    public void initTest() {
        softAssert = new SoftAssert();
        inError = false;
        steps = new ArrayList<>();
        previousLog = "";
        playwrightCmd = true;
    }

    /**
     * débute un step
     * @param stepName
     */
    public void startStep(String stepName) {
        String uuid = UUID.randomUUID().toString();
        steps.add(uuid);
        Allure.getLifecycle().startStep(uuid, new StepResult().setName(stepName));
    }

    /**
     * termine le step en cours
     */
    public void stopStep() {
        Allure.getLifecycle().stopStep(steps.getLast());
        steps.removeLast();
    }

    /**
     * vérifie tous les softasserts pour marquer le test comme échoué
     */
    public void softAssertAll() {
        softAssert.assertAll();
    }

    /**
     * definit l'élément playwright courant
     * @param element
     */
    public void setCurrentElement(ElementHandle element) {
        this.currentElement = element;
        this.playwrightCmd = true;
    }

    /**
     * Vérifie l'égalité entre deux entiers et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(int expected, int actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Vérifie l'égalité entre deux entiers et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(String info, int expected, int actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'égalité entre deux booléens et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(boolean expected, boolean actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Vérifie l'égalité entre deux booléens et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(String info, boolean expected, boolean actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'égalité entre deux doubles et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(Double expected, Double actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Vérifie l'égalité entre deux doubles et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(String info, Double expected, Double actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'égalité entre deux strings et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(String expected, String actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Vérifie l'égalité entre deux strings et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEquals(String info, String expected, String actual) {
        String status = DataUtil.equalsIgnoreIgnoredString(this, expected, actual, true);
        log(status, (info != null ? info + " " : "") + "assertEquals", null, expected, status.equals(PASS_STATUS) ? null : actual, null);
    }

    /**
     * Vérifie l'égalité entre deux strings et logue le résultat dans le rapport avec une explication, stoppe le test.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertEqualsStop(String info, String expected, String actual) {
        String status = DataUtil.equalsIgnoreIgnoredString(this, expected, actual, true);
        status = (status.equals(PASS_STATUS) || status.equals(WARNING_STATUS) ? PASS_STATUS : FAIL_STATUS);
        log(status, (info != null ? info + " " : "") + "assertEquals", null, expected, status.equals(PASS_STATUS) ? null : actual, null);
    }

    /**
     * Vérifie que la première chaîne contient la seconde et logue le résultat dans le rapport.
     *
     * @param contenant La chaîne contenant.
     * @param contenu La chaîne contenue.
     */
    public void assertContains(String contenant, String contenu) {
        assertContains(null, contenant, contenu);
    }

    /**
     * Vérifie que la première chaîne contient la seconde et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param contenant La chaîne contenant.
     * @param contenu La chaîne contenue.
     */
    public void assertContains(String info, String contenant, String contenu) {
        String status = contenant.contains(contenu) ? PASS_STATUS : FAIL_NEXT_STATUS;
        log(status, (info != null ? info + " " : "") + "assertContains", null, contenant, status.equals(PASS_STATUS) ? null : contenu, null);
    }

    /**
     * Vérifie que la première chaîne ne contient pas la seconde et logue le résultat dans le rapport.
     *
     * @param contenant La chaîne contenant.
     * @param contenu La chaîne contenue.
     */
    public void assertNotContains(String contenant, String contenu) {
        assertNotContains(null, contenant, contenu);
    }

    /**
     * Vérifie que la première chaîne ne contient pas la seconde et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param contenant La chaîne contenant.
     * @param contenu La chaîne contenue.
     */
    public void assertNotContains(String info, String contenant, String contenu) {
        String status = contenant.contains(contenu) ? FAIL_NEXT_STATUS : PASS_STATUS;
        log(status, (info != null ? info + " " : "") + "assertContains", null, contenant, status.equals(PASS_STATUS) ? null : contenu, null);
    }

    /**
     * Vérifie l'inégalité entre deux entiers et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(int expected, int actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * Vérifie l'inégalité entre deux entiers et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(String info, int expected, int actual) {
        assertNotEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'inégalité entre deux booléens et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(boolean expected, boolean actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * Vérifie l'inégalité entre deux booléens et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(String info, boolean expected, boolean actual) {
        assertNotEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'inégalité entre deux doubles et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(Double expected, Double actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * Vérifie l'inégalité entre deux doubles et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(String info, Double expected, Double actual) {
        assertEquals(info, String.valueOf(expected), String.valueOf(actual));
    }

    /**
     * Vérifie l'inégalité entre deux strings et logue le résultat dans le rapport.
     *
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(String expected, String actual) {
        assertNotEquals(null, expected, actual);
    }

    /**
     * Vérifie l'inégalité entre deux strings et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     */
    public void assertNotEquals(String info, String expected, String actual) {
        String status = (expected.equals(actual) ? FAIL_NEXT_STATUS : PASS_STATUS);
        log(status, (info != null ? info + " " : "") + "assertNotEquals", null, expected, status.equals(PASS_STATUS) ? null : actual, null);
    }

    /**
     * Vérifie qu'un entier est supérieur à un autre et logue le résultat dans le rapport.
     *
     * @param num1 Le premier nombre.
     * @param num2 Le second nombre.
     */
    public void assertGreater(int num1, int num2) {
        assertGreater(null, num1, num2);
    }

    /**
     * Vérifie qu'un entier est supérieur à un autre et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param num1 Le premier nombre.
     * @param num2 Le second nombre.
     */
    public void assertGreater(String info, int num1, int num2) {
        assertGreater(info, Double.valueOf(num1), Double.valueOf(num2));
    }

    /**
     * Vérifie qu'un double est supérieur à un autre et logue le résultat dans le rapport.
     *
     * @param num1 Le premier nombre.
     * @param num2 Le second nombre.
     */
    public void assertGreater(Double num1, Double num2) {
        assertGreater(null, num1, num2);
    }

    /**
     * Vérifie qu'un double est supérieur à un autre et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param num1 Le premier nombre.
     * @param num2 Le second nombre.
     */
    public void assertGreater(String info, Double num1, Double num2) {
        String status = (num1 > num2 ? PASS_STATUS : FAIL_NEXT_STATUS);
        log(status, (info != null ? info + " " : "") + "assertGreater", null, String.valueOf(num1), String.valueOf(num2), null);
    }

    /**
     * Vérifie qu'un double est inférieur ou égal à un autre et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param num1 Le premier nombre.
     * @param num2 Le second nombre.
     */
    public void assertSmallerOrEqual(String info, Double num1, Double num2) {
        String status = (num1 <= num2 ? PASS_STATUS : FAIL_NEXT_STATUS);
        log(status, (info != null ? info + " " : "") + "assertSmallerOrEqual", null, String.valueOf(num1), String.valueOf(num2), null);
    }

    /**
     * Vérifie qu'une String n'est pas null et logue le résultat dans le rapport.
     *
     * @param actual La valeur réelle.
     */
    public void assertNotNull(String actual) {
        assertNotNull(null, actual);
    }

    /**
     * Vérifie qu'une String n'est pas null et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param actual La valeur réelle.
     */
    public void assertNotNull(String info, String actual) {
        String status = (actual != null ? PASS_STATUS : FAIL_NEXT_STATUS);
        log(status, (info != null ? info + " " : "") + "assertNotNull", null, null, actual, null);
    }

    /**
     * Vérifie qu'une date à un format donné est égale à la date courante dans un intervalle de + ou - N minutes et logue le résultat dans le rapport.
     *
     * @param date La date à vérifier.
     * @param format Le format de la date.
     * @param minuteEcartAcceptable L'écart acceptable en minutes.
     */
    public void assertDateEqualsLocalDateTime(String date, String format, int minuteEcartAcceptable) {
        assertDateEqualsLocalDateTime(null, date, format, minuteEcartAcceptable);
    }

    /**
     * Vérifie qu'une date à un format donné est égale à la date courante dans un intervalle de + ou - N minutes et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param date La date à vérifier.
     * @param format Le format de la date.
     * @param minuteEcartAcceptable L'écart acceptable en minutes.
     */
    public void assertDateEqualsLocalDateTime(String info, String date, String format, int minuteEcartAcceptable) {
        LocalDateTime dateFormatee;
        String status = PASS_STATUS;
        String message = null;
        LocalDateTime now = LocalDateTime.now();
        try {
            dateFormatee = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
            if (dateFormatee.plusMinutes(minuteEcartAcceptable).isBefore(now) || dateFormatee.minusMinutes(minuteEcartAcceptable).isAfter(now)) {
                status = FAIL_NEXT_STATUS;
            }
        } catch (Exception e) {
            status = FAIL_NEXT_STATUS;
            message = e.getMessage();
        }
        log(status, (info != null ? info + " " : "") + "assertDateEqualsLocalDateTime", null, now + " +/- " + minuteEcartAcceptable + " minutes", String.valueOf(date), message);
    }

    /**
     * Vérifie qu'une date à un format donné est antérieure à la date courante - N minutes et logue le résultat dans le rapport avec une explication.
     *
     * @param info L'explication.
     * @param date La date à vérifier.
     * @param format Le format de la date.
     * @param minuteARetirer Le nombre de minutes à retirer.
     */
    public void assertDateLowerThanLocalDateTime(String info, String date, String format, int minuteARetirer) {
        LocalDateTime dateFormatee;
        String status = PASS_STATUS;
        String message = null;
        LocalDateTime now = LocalDateTime.now();
        try {
            dateFormatee = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
            if (now.minusMinutes(minuteARetirer).isBefore(dateFormatee)) {
                status = FAIL_NEXT_STATUS;
            }
        } catch (Exception e) {
            status = FAIL_NEXT_STATUS;
            message = e.getMessage();
        }
        log(status, (info != null ? info + " " : "") + "assertDateLowerThanLocalDateTime", null, now + " - " + minuteARetirer + " minutes", String.valueOf(date), message);
    }

    /**
     * Logue un message de type "info" dans le rapport.
     *
     * @param messageLog Le message à loguer.
     */
    public void title(String messageLog) {
        log(Reporter.INFO_STATUS, messageLog.toUpperCase());
    }

    /**
     * Logue un message de type "info" dans le rapport.
     *
     * @param messageLog Le message à loguer.
     */
    public void log(String messageLog) {
        log(Reporter.INFO_STATUS, messageLog);
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param action L'action.
     * @param elementName L'élément.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     * @param message Le message.
     */
    public void log(String status, String action, String elementName, String expected, String actual, String message) {
        log(status, concatMessage(action, elementName, expected, actual, message));
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param action L'action.
     * @param elementName L'élément.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     * @param message Le message.
     * @param e L'exception.
     */
    public void log(String status, String action, String elementName, String expected, String actual, String message, Exception e) {
        log(status, concatMessage(action, elementName, expected, actual, message), e);
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param messageLog Le message à loguer.
     * @param e L'exception.
     */
    public void log(String status, String messageLog, Exception e) {
        log(status, messageLog + "\n" + getExceptionStack(e));
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param e L'exception.
     */
    public void log(String status, Exception e) {
        log(status, getExceptionStack(e));
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param messageLog Le message à loguer.
     */
    public void log(String status, String messageLog) {
        log(status, messageLog, true);
    }

    /**
     * Logue une info de type "status" dans le rapport.
     *
     * @param status Le statut.
     * @param messageLog Le message à loguer.
     * @param takeScreenShot Indique si une capture d'écran doit être prise.
     */
    public void log(String status, String messageLog, boolean takeScreenShot) {
        String threadTimeLog = "[" + Thread.currentThread() + " - " + (LocalDateTime.now()).format(DateTimeFormatter.ofPattern("hh:mm:ss")) + "] ";
        messageLog = (messageLog == null ? "" : messageLog);
        if (status.contains("noscreenshot") || messageLog.contains("[noscreenshot]")) {
            takeScreenShot = false;
            status = status.replace("noscreenshot", "");
            messageLog = messageLog.replace("[noscreenshot]", "");
        }

        messageLog = getPageMethod() + messageLog;
        String currentLog = status + messageLog + takeScreenShot;
        lastAction = messageLog;
        lastStatus = status;
        if (previousLog != null && !previousLog.equals(currentLog)) {
            previousLog = currentLog;
            timePreviousLog = LocalDateTime.now();
            if (GlobalProp.isForceStopOnFail() && status.equals(FAIL_NEXT_STATUS)) {
                status = FAIL_STATUS;
            }

            String slf4jMessage = threadTimeLog + messageLog;


            switch (status) {
                case SKIP_STATUS:
                    log.info(slf4jMessage);
                    propagateAllureStep(messageLog, status, false);
                    testSkipped = true;
                    Assert.assertEquals("skip : " + messageLog, PASS_STATUS);
                    break;
                case WARNING_STATUS:
                    log.warn(slf4jMessage);
                    propagateAllureStep(messageLog, status, takeScreenShot);
                    break;
                case FAIL_STATUS:
                    log.error(slf4jMessage);
                    inError = true;
                    propagateAllureStep(messageLog, FAIL_STATUS, takeScreenShot);
                    Assert.assertEquals("fail : " + messageLog, PASS_STATUS);
                    break;
                case FAIL_NEXT_STATUS:
                    log.error(slf4jMessage);
                    inError = true;
                    propagateAllureStep(messageLog, FAIL_STATUS, takeScreenShot);
                    softAssert.assertEquals("fail : " + messageLog, PASS_STATUS);
                    break;
                default:
                    propagateAllureStep(messageLog, PASS_STATUS, false);
                    log.info(slf4jMessage);
                    break;
            }
        }
    }

    /**
     * Propage l'état de l'étape Allure du step fils au parent.
     * @param messageLog
     * @param status
     */
    private void propagateAllureStep(String messageLog, String status, boolean takeScreenShot) {
        if (takeScreenShot) startStep(messageLog);
        Allure.step(messageLog);
        Status allureStatus = Status.PASSED;
        switch (status) {
            case SKIP_STATUS -> allureStatus = Status.SKIPPED;
            case FAIL_STATUS -> allureStatus = Status.FAILED;
            case WARNING_STATUS -> allureStatus = Status.BROKEN;
        }
        if (takeScreenShot) {
            if (isPlaywrightCmd()) {
                takeScreenShot(messageLog, status, true);
            } else {
                takeScreenShotAppium(messageLog);
            }
        }
        boolean lastStep = true;
        for (String uuid : steps) {
            Status finalStatus = allureStatus;
            boolean finalLastStep = lastStep;
            Allure.getLifecycle().updateStep(uuid, stepResult -> {
                if (finalLastStep
                        || (finalStatus == Status.FAILED)
                        || (finalStatus == Status.BROKEN && stepResult.getStatus() != Status.FAILED)
                        || (finalStatus == Status.SKIPPED && stepResult.getStatus() != Status.FAILED && stepResult.getStatus() != Status.BROKEN)
                        || (finalStatus == Status.PASSED && stepResult.getStatus() != Status.FAILED && stepResult.getStatus() != Status.BROKEN && stepResult.getStatus() != Status.SKIPPED)) {
                    stepResult.setStatus(finalStatus);
                }
            });
            lastStep = false;
        }
        if (takeScreenShot) stopStep();
    }


    /**
     * Renvoie la page et la méthode à partir desquelles le log a été déclenché.
     *
     * @return La page et la méthode.
     */
    private String getPageMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = null;
        for (StackTraceElement stack : stackTraceElements) {
            if (stack.getClassName().contains(".pages.") || stack.getClassName().startsWith("pages.")) {
                stackTraceElement = stack;
                break;
            }
        }
        return stackTraceElement != null ? "[" + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "] " : "";
    }

    /**
     * Ajoute une capture d'écran.
     *
     * @param info L'information.
     */
    public void takeScreenShot(String info) {
        takeScreenShot(info, INFO_STATUS, false);
    }

    /**
     * Ajoute une capture d'écran.
     *
     * @param titre titre photo.
     * @param status statut du log.
     * @param highlightLastField Indique si le dernier champ doit être mis en surbrillance.
     */
    public void takeScreenShot(String titre, String status, boolean highlightLastField) {
        try {
            centerCurrentElement();
            if (highlightLastField) highlight(status.equals(FAIL_STATUS) ? "red" : "green");
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            Allure.addAttachment(titre, CONTENT_TYPE,
                    new ByteArrayInputStream(screenshot), ".png");
        } catch (Exception e) {
            // Ignorer
        }
        removeHighlight();
    }

    /**
     * Ajoute une capture d'écran selenium.
     *
     * @param titre titre photo.
     */
    public void takeScreenShotAppium(String titre) {
        try {
            String screenshot = driver.getScreenshotAs(OutputType.BASE64);
            byte[] decodedScreenshot = Base64.getDecoder().decode(screenshot);
            Allure.addAttachment(titre, CONTENT_TYPE, new ByteArrayInputStream(decodedScreenshot), ".png");
        } catch (Exception e) {
            // Ignorer
        }
    }

    /**
     * Logue une image dans le rapport.
     *
     * @param titre Le titre.
     * @param imageBase64 L'image en base64.
     */
    public void logImage(String titre, String imageBase64) {
        Allure.addAttachment(titre, CONTENT_TYPE,
                new java.io.ByteArrayInputStream(Base64.getDecoder().decode(imageBase64)), ".png");
    }

    public void attachVideoToAllure(String videoPath) {
        try {
            File videoFile = new File(videoPath);
            Allure.addAttachment("Vidéo Playwright", "video/webm", new FileInputStream(videoFile), "webm");
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    /**
     * Centre l'élément courant au milieu de la page.
     */
    private void centerCurrentElement() {
        if (currentElement != null) {
            try {
                (currentElement).evaluate("el => el.scrollIntoView({block: 'center'})");
            } catch (Exception ex) {
                // Ignorer
            }
        }
    }

    /**
     * Supprime la mise en surbrillance de l'élément en erreur.
     */
    private void removeHighlight() {
        try {
            this.page.evaluate("document.getElementById('reportseleniumhighlightdiv').replaceWith(...document.getElementById('reportseleniumhighlightdiv').childNodes)");
        } catch (Exception ignore) {
            // Ignorer
        }
    }

    /**
     * Met en surbrillance l'élément en erreur.
     *
     * @param color La couleur de la mise en surbrillance.
     */
    private void highlight(String color) {
        try {
            if (currentElement != null) {
                currentElement.evaluate("(el, color) => { " +
                        "var parent = el.parentNode;" +
                        "var reportseleniumhighlightdiv = document.createElement('div');" +
                        "reportseleniumhighlightdiv.setAttribute('id', 'reportseleniumhighlightdiv');" +
                        "reportseleniumhighlightdiv.style.border = '3px solid ' + color;" +
                        "parent.replaceChild(reportseleniumhighlightdiv, el);" +
                        "reportseleniumhighlightdiv.appendChild(el);" +
                        "}", color);
            }
        } catch (Exception ignore) {
            // Ignorer
        }
    }

    /**
     * Renvoie la stack d'une exception en string.
     *
     * @param e L'exception.
     * @return La stack de l'exception.
     */
    private String getExceptionStack(Exception e) {
        if (e == null) {
            return "";
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString().replace("\n", "  \n");
        }
    }

    /**
     * Concatène le message.
     *
     * @param action L'action.
     * @param elementName L'élément.
     * @param expected La valeur attendue.
     * @param actual La valeur réelle.
     * @param message Le message.
     * @return Le message concaténé.
     */
    public String concatMessage(String action, String elementName, String expected, String actual, String message) {
        String messageLog = action;
        messageLog += (elementName != null ? " sur '" + elementName + "'" : "");
        messageLog += (expected != null ? ", attendu: >" + expected + "< " : "");
        messageLog += (actual != null ? ", constaté: >" + actual + "< " : " ");
        messageLog += (message != null ? message : "");
        return messageLog;
    }

    /**
     * Indique si le précédent log date de plus de x secondes.
     *
     * @param second Le nombre de secondes.
     * @return Vrai si le précédent log date de plus de x secondes, faux sinon.
     */
    public boolean isTimePreviousLogOlderThan(int second) {
        return LocalDateTime.now().isAfter(timePreviousLog.plusSeconds(second));
    }

}
