package bcx.playwright.util.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Objects;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;

import lombok.extern.slf4j.Slf4j;
import bcx.playwright.report.Reporter;

/**
 * Utilitaire de lancement de batch
 * @author bcx
 *
 */
@Slf4j
public class BatchUtil extends ShellUtil {
    private BatchUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Lance un batch
     * @param serverBatch
     * @param userBatch
     * @param pwdUserBatch
     * @param batchPath
     * @param batchCommand la commande sh du batch à lancer (ex : ./ecriture_compta.sh)
     * @param timeOut le temps en secondes maximum que le batch prendra. A la fin de ce temps, le process est arrêté que le batch soit fini ou non.
     * @param params
     */
    public static String launchBatch(Reporter report, String serverBatch, String userBatch, String pwdUserBatch, String batchPath, String batchCommand, Long timeOut, String[] params){
        String logBatch = "";
        if (String.valueOf(System.getProperty("batchManuel")).equals("true")) {
            batchCommand = batchCommand.replace("./", batchPath + "/").replace("//", "/");
            StringBuilder chaineParams = new StringBuilder();
            if (params != null) {
                for (String param : params) {
                    chaineParams.append(" ").append(param);
                }
            }
            log.info(batchCommand+chaineParams);
            return "Batch terminé";
        } else {
            connectSession(report, serverBatch, userBatch, pwdUserBatch);
            try {
                launchBatchWithParams(batchPath, batchCommand, params);
                logBatch = waitForBatchEnd(report, timeOut);
            } catch (Exception e) {
                if (report != null)
                    report.log(Reporter.ERROR_STATUS_NO_SCREENSHOT, "call batch " + serverBatch + " " + batchPath + " " + batchCommand, e);
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
        //Mise de la sortie du channel en console
        channel.setOutputStream(System.out);

        shellStream = new PrintStream(channel.getOutputStream());
        channel.connect();
        batchCommand = batchCommand.replace("./", batchPath + "/").replace("//", "/");
        //Concaténation des paramètres si présents
        StringBuilder chaineParams = new StringBuilder();
        if (params != null) {
            for (String param : params) {
                chaineParams.append(" ").append(param);
            }
        }
        //Lancement du batch
        shellStream.println(batchCommand+chaineParams);
        shellStream.flush();
    }

    private static String waitForBatchEnd(Reporter report, Long timeOut) throws IOException {
        LocalDateTime start = LocalDateTime.now();
        InputStream inps=channel.getInputStream();
        byte[] tmp=new byte[1024];
        String logBatch = "";
        String logBatchComplete = "";
        int i;
        while(start.plusSeconds(timeOut).isAfter(LocalDateTime.now()) && !logBatch.endsWith("]$ ")){
            if (inps.available()>0) {
                i=inps.read(tmp, 0, 1024);
                if (i < 0) break;
                logBatch = new String(tmp, 0, i);
                logBatchComplete += logBatch;
                log.info(logBatch);
            }
            if(channel.isClosed()){
                log.info("exit-status: "+channel.getExitStatus());
                break;
            }
        }

        if (logBatchComplete.replace("FonctionnelleException", "").contains("Exception:") && report != null) {
            report.log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "Exception found in batch call : \n" + logBatch.replace("Exception", "<b>Exception</b>"));
        }
        if (logBatchComplete.contains("Error: Unable to access jarfile") && report != null) {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "Error: Unable to access jarfile in batch call : \n" + logBatchComplete.replace("Exception", "<b>Exception</b>"));
        }
        return logBatchComplete;
    }
}