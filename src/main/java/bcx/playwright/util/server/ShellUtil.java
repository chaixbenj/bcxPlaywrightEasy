package bcx.playwright.util.server;

import bcx.playwright.test.TestContext;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import bcx.playwright.report.Reporter;

import java.io.PrintStream;

/**
 * Utilitaire de lancement de batch
 * @author bcx
 *
 */
@Slf4j
public class ShellUtil {
    static ChannelShell channel;
    static Session session;
    static PrintStream shellStream;

    protected ShellUtil() {
        throw new IllegalStateException("Utility class");
    }

    protected static void connectSession(Reporter report, String server, String user, String pwdUser) {
        try {
            JSch sch = new JSch();
            System.setProperty("javax.security.auth.useSubjectCredsOnly", "true");
            session = sch.getSession(user, server, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(pwdUser);
            session.connect(30000);
            if (report != null)
                report.log(Reporter.PASS_STATUS, "Connecter avec succès au serveur " + server);
        } catch (Exception e) {
            log.error("Echec de la connexion à " + server + " avec " + user);
            if (report != null)
                report.log(Reporter.ERROR_STATUS_NO_SCREENSHOT, "Impossible de se connecter au serveur " + server, e);
        }
    }
}