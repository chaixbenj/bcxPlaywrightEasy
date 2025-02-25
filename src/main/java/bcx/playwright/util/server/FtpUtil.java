package bcx.playwright.util.server;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import lombok.extern.slf4j.Slf4j;
import bcx.playwright.report.Reporter;

import java.io.File;
import java.nio.file.Paths;
import java.util.Vector;

/**
 * Utilitaire FTP
 * Permet d'uploader, downloader, supprimer des fichiers d'un ftp
 * @author bcx
 *
 */
@Slf4j
public class FtpUtil {
    private static Channel channel;
    private static Session session;

    private FtpUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * upload un fichier sur une machine distante
     * @param server
     * @param port
     * @param user
     * @param pass
     * @param remotePathRep
     * @param fichierLocal
     */
    public static void uploadFile(Reporter report, String server, int port, String user, String pass, String remotePathRep, String fichierLocal) {
        String remoteFile = remotePathRep + "/" + new File(fichierLocal).getName();

        try {
            ChannelSftp sftpChannel = connectChannel(report, server, port, user, pass);
            if (sftpChannel!=null) {
                sftpChannel.put(fichierLocal, remoteFile);
                sftpChannel.exit();
                report.log(Reporter.PASS_STATUS, "FTP.upload " + fichierLocal + " to " + server + " /" + remotePathRep);
            }
        } catch (Exception ex) {
            report.log(Reporter.ERROR_STATUS_NO_SCREENSHOT, "FTP.upload " + fichierLocal + " to "+ server + " /" + remotePathRep, ex);
        } finally {
            disconnectChannel();
        }
    }

    /**
     * download un fichier depuis un ftp
     * @param server
     * @param port
     * @param user
     * @param pass
     * @param fichier fichier à downloader (dans resources/GlobalVariable.gettestFileFolder())
     * @return
     */
    public static String downloadFile(Reporter report, String server, int port, String user, String pass, String remotePathRep, String fichier) {
        String action = "FTP.Download " + fichier + " to " + server + " /" + remotePathRep;
        String remoteFile = remotePathRep + "/";
        String localPathLogFile = Paths.get("").toAbsolutePath().toString() + File.separator + "target/test-classes/";
        try {
            ChannelSftp sftpChannel = connectChannel(report, server, port, user, pass);
            if (sftpChannel!=null) {
                Vector filelist = sftpChannel.ls(remotePathRep);
                boolean fileFound = false;
                for (Object o : filelist) {
                    LsEntry entry = (LsEntry) o;
                    if (entry.getFilename().startsWith(fichier)) {
                        String logFileName = entry.getFilename();
                        localPathLogFile += logFileName;
                        remoteFile += logFileName;
                        fileFound = true;
                        break;
                    }
                }
                if (fileFound) {
                    sftpChannel.get(remoteFile, localPathLogFile);
                    report.log(Reporter.PASS_STATUS, action);
                } else {
                    localPathLogFile = "ERROR FTP.download " + fichier + " file not found";
                    report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, action + "!! file not found");
                }
                sftpChannel.exit();
            }
        } catch (Exception ex) {
            localPathLogFile = "ERROR FTP.download " + fichier + " to "+ server + " /" + remotePathRep;
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, action, ex);
        } finally {
            disconnectChannel();
        }
        return localPathLogFile;
    }


    /**
     * remove un fichier d'un répertoire ftp
     * @param server
     * @param port
     * @param user
     * @param pass
     * @param rep
     * @param fichier
     */
    public static void removeFile(Reporter report, String server, int port, String user, String pass, String rep, String fichier) {
        String remoteFile = rep + "/" + fichier;
        try {
            ChannelSftp sftpChannel = connectChannel(report, server, port, user, pass);
            if (sftpChannel!=null) {
                sftpChannel.rm(remoteFile);
                sftpChannel.exit();
            }
        } catch (Exception ex) {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "FTP.removeSigerpFileFromAchive " + remoteFile, ex);
        } finally {
            disconnectChannel();
        }
    }

    /**
     * connection au channel
     * @param server
     * @param port
     * @param user
     * @param pass
     */
    private static ChannelSftp connectChannel(Reporter report, String server, int port, String user, String pass) {
        try {
            JSch sch = new JSch();
            JSch.setConfig("StrictHostKeyChecking", "no");
            session = sch.getSession(user, server, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(pass);
            session.connect(10000);
            channel = session.openChannel("sftp");
            channel.connect();
            return (ChannelSftp) channel;
        } catch (Exception e) {
            report.log(Reporter.ERROR_NEXT_STATUS_NO_SCREENSHOT, "connection ftp ko", e);
            return null;
        }
    }

    private static void disconnectChannel() {
        try {
            if (channel!=null)channel.disconnect();
            if (session!=null)session.disconnect();
        } catch (Exception ex) {
            // on passe à la suite
        }
    }
}