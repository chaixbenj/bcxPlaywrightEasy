package bcx.automation.util.ws;

import lombok.extern.slf4j.Slf4j;
import bcx.automation.properties.GlobalProp;
import bcx.automation.report.Reporter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class WSUtil {
    public static final String HTTPS_PROXY_HOST = "https.proxyHost";
    public static final String HTTPS_PROXY_PORT = "https.proxyPort";

    private WSUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Appel d'un webservice.
     *
     * @param report       Le rapporteur pour les logs.
     * @param uri         L'URI du webservice.
     * @param method      La méthode HTTP à utiliser (GET, POST, etc.).
     * @param queryString Les paramètres de la requête.
     * @param headers     Les en-têtes HTTP.
     * @param payload     Le corps de la requête.
     * @param apiAuth     L'authentification API.
     * @param expectedStatus Le statut HTTP attendu.
     * @param log         Indique si les logs doivent être activés.
     * @param proxy       Indique si un proxy doit être utilisé.
     * @return La réponse du webservice.
     */
    public static String callWS(Reporter report, String uri, String method, String queryString, String headers, String payload, String apiAuth, int expectedStatus, boolean log, boolean proxy) {
        setProxy(proxy);

        String reponse = "";
        int status = 0;
        try {
            if (queryString != null && !queryString.equals("")) {
                queryString = URLEncoder.encode(queryString, "UTF-8");
                uri = uri + "?" + queryString;
            }
            URL url = (new URI(uri)).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            if (apiAuth != null) conn.setRequestProperty("Authorization", !(apiAuth.startsWith("Basic") || apiAuth.startsWith("Bearer")) ? "Basic " + apiAuth : apiAuth);
            if (headers != null) {
                for (String header : headers.split(";")) {
                    conn.setRequestProperty(header.split("=")[0], header.split("=")[1]);
                }
            }
            conn.setDoInput(true);
            if (payload != null) {
                conn.setDoOutput(true);
                try (OutputStream output = conn.getOutputStream()) {
                    output.write(payload.getBytes());
                } catch (Exception e) {
                    // On passe à la suite
                }
            }
            status = conn.getResponseCode();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = conn.getInputStream().read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }
            } catch (Exception errorStream) {
                while ((length = conn.getErrorStream().read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }
            }
            reponse = baos.toString(StandardCharsets.UTF_8);
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            reponse = "exception";
            if (report != null) {
                report.log(expectedStatus == -1 ? Reporter.WARNING_STATUS_NO_SCREENSHOT : Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT, "Exception in NetClient:- \n", e);
            }
        }
        String statusResultat = (expectedStatus == -1 || expectedStatus == status ? Reporter.PASS_STATUS : Reporter.FAIL_NEXT_STATUS_NO_SCREENSHOT);
        if (report != null && (log || !statusResultat.equals(Reporter.PASS_STATUS))) {
            report.log(statusResultat, "status attendu/constaté : " + expectedStatus + "/" + status + " on " + method + " " + uri + "  \n" + String.valueOf(payload).replace(",", ",  \n") + "  \n  \nréponse :" + (reponse.startsWith("%PDF") ? reponse.substring(0, 500) + ",... String tronquée dans le rapport" : reponse).replace(",", ",  \n"), false);
        }
        System.clearProperty(HTTPS_PROXY_HOST);
        System.clearProperty(HTTPS_PROXY_PORT);
        return reponse;
    }

    private static void setProxy(boolean proxy) {
        if (proxy) {
            System.setProperty(HTTPS_PROXY_HOST, GlobalProp.getProxyHost());
            System.setProperty(HTTPS_PROXY_PORT, GlobalProp.getProxyPort());
        } else {
            System.clearProperty(HTTPS_PROXY_HOST);
            System.clearProperty(HTTPS_PROXY_PORT);
        }
    }

    /**
     * Appel d'un webservice.
     *
     * @param report       Le rapporteur pour les logs.
     * @param uri         L'URI du webservice.
     * @param method      La méthode HTTP à utiliser (GET, POST, etc.).
     * @param queryString Les paramètres de la requête.
     * @param payload     Le corps de la requête.
     * @param apiAuth     L'authentification API.
     * @return La réponse du webservice.
     */
    public static String callWS(Reporter report, String uri, String method, String queryString, String payload, String apiAuth, int expectedStatus, boolean log) {
        return callWS(report, uri, method, queryString, "Accept=application/json;Content-Type=application/json", payload, apiAuth, expectedStatus, log, false);
    }

    /**
     * Appel d'un webservice.
     *
     * @param report       Le rapporteur pour les logs.
     * @param uri         L'URI du webservice.
     * @param method      La méthode HTTP à utiliser (GET, POST, etc.).
     * @param queryString Les paramètres de la requête.
     * @param payload     Le corps de la requête.
     * @param apiAuth     L'authentification API.
     * @return La réponse du webservice.
     */
    public static String callWS(Reporter report, String uri, String method, String queryString, String payload, String apiAuth, int expectedStatus) {
        return callWS(report, uri, method, queryString, payload, apiAuth, expectedStatus, true);
    }

    /**
     * Appel d'un webservice avec authentification basique.
     *
     * @param report       Le rapporteur pour les logs.
     * @param uri         L'URI du webservice.
     * @param method      La méthode HTTP à utiliser (GET, POST, etc.).
     * @param queryString Les paramètres de la requête.
     * @param payload     Le corps de la requête.
     * @param user        Le nom d'utilisateur.
     * @param password    Le mot de passe.
     * @return La réponse du webservice.
     */
    public static String callWS(Reporter report, String uri, String method, String queryString, String payload, String user, String password, int expectedStatus) {
        String apiAuth = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
        return callWS(report, uri, method, queryString, payload, apiAuth, expectedStatus);
    }

    /**
     * Appel d'un webservice.
     *
     * @param report       Le rapporteur pour les logs.
     * @param uri         L'URI du webservice.
     * @param method      La méthode HTTP à utiliser (GET, POST, etc.).
     * @param payload     Le corps de la requête.
     * @return La réponse du webservice.
     */
    public static String callWS(Reporter report, String uri, String method, String payload, int expectedStatus) {
        return callWS(report, uri, method, null, payload, null, expectedStatus);
    }
}
