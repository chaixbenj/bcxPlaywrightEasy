package bcx.automation.util.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Objects;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;

import lombok.extern.slf4j.Slf4j;
import bcx.automation.report.Reporter;

/**
 * Utilitaire de lancement de batch.
 *
 * @author bcx
 */
@Slf4j
public class BatchUtil extends ShellUtil {
    private BatchUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Lance un batch.
     *
     * @param report       Le rapporteur pour les logs.
     * @param serverBatch  L'adresse du serveur où lancer le batch.
     * @param userBatch    Le nom d'utilisateur pour la connexion SSH.
     * @param pwdUserBatch Le mot de passe pour la connexion SSH.
     * @param batchPath    Le chemin du batch à lancer.
     * @param batchCommand La commande shell du batch à lancer (ex : ./script.sh).
     * @param timeOut      Le temps maximum en secondes que le batch peut prendre.
     * @param params       Les paramètres à passer au batch.
     * @return             Le log du batch.
     */
    public static String launchBatch(Reporter report, String serverBatch, String userBatch, String pwdUserBatch, String batchPath, String batchCommand, Long timeOut, String[] params) {
        String logBatch = "";
        if (String.valueOf(System.getProperty("batchManuel")).equals("true")) {
            batchCommand = batchCommand.replace("./", batchPath + "/").replace("//", "/");
            StringBuilder chaineParams = new StringBuilder();
            if (params != null) {
                for (String param : params) {
                    chaineParams.append(" ").append(param);
                }
            }
            log.info(batchCommand + chaineParams);
            return "Batch terminé";
        } else {
            connectSession(report, serverBatch, userBatch, pwdUserBatch);
            try {
                launchBatchWithParams(batchPath, batchCommand, params);
                logBatch = waitForBatchEnd(report, timeOut);
            } catch (Exception e) {
                if (report != null)
                    report.log(Reporter.FAIL_STATUS_NO_SCREENSHOT, "call batch " + serverBatch + " " + batchPath + " " + batchCommand, e);
            }
            Objects.requireNonNull(shellStream).close();
            Objects.requireNonNull(session).disconnect();
            Objects.requireNonNull(channel).disconnect();
            if (report != null)
                report.log(Reporter.PASS_STATUS, "call batch " + serverBatch + " " + batchPath + " " + batchCommand);
        }
        return logBatch;
    }

    private static void launchBatchWithParams(String batchPath, String batchCommand, String[] params) throws JSchException, IOException {
        channel = (ChannelShell) session.openChannel("shell");
        // Mise de la sortie du channel en console
        channel.setOutputStream(System.out);

        shellStream = new PrintStream(channel.getOutputStream());
        channel.connect();
        batchCommand = batchCommand.replace("./", batchPath + "/").replace("//", "/");
        // Concaténation des paramètres si présents
        StringBuilder chaineParams = new StringBuilder();
        if (params != null) {
            for (String param : params) {
                chaineParams.append(" ").append(param);
            }
        }
        // Lancement du batch
        shellStream.println(batchCommand + chaineParams);
        shellStream.flush();
    }

    private static String waitForBatchEnd(Reporter report, Long timeOut) throws IOException {
        LocalDateTime start = LocalDateTime.now();
        InputStream inps = channel.getInputStream();
        byte[] tmp = new byte[1024];
        String logBatch = "";
        String logBatchComplete = "";
        int i;
        while (start.plusSeconds(timeOut).isAfter(LocalDateTime.now()) && !logBatch.endsWith("]$ ")) {
            if (inps.available() > 0) {
                i = inps.read(tmp, 0, 1024);
                if (i < 0) break;
                logBatch = new String(tmp, 0, i);
                logBatchComplete += logBatch;
                log.info(logBatch);
            }
            if (channel.isClosed()) {
                log.info("exit-status: " + channel.getExitStatus());
                break;
            }
        }

        if (logBatchComplete.replace("FonctionnelleException", "").contains("Exception:") && report != null) {
            report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "Exception found in batch call : \n" + logBatch.replace("Exception", "<b>Exception</b>"));
        }
        if (logBatchComplete.contains("Error: Unable to access jarfile") && report != null) {
            report.log(Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT, "Error: Unable to access jarfile in batch call : \n" + logBatchComplete.replace("Exception", "<b>Exception</b>"));
        }
        return logBatchComplete;
    }
}
