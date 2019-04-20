package com.hxht.dnsftp.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * you yong
 *
 * @author root
 */

@Component
@ConfigurationProperties(prefix = "ftp")
public class FtpUtil {
    private boolean isConnect;
    private String ip;
    private int port;
    private String user;
    private String password;
    private String remoteDirectory;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemoteDirectory() {
        return remoteDirectory;
    }

    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    public FTPClient getFtpClient() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip, port);
            // FTP服务器连接回答
            int reply = ftpClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
            }
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            isConnect = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        ftpClient.setBufferSize(1024 * 1024 * 2);//缓存区大小
        ftpClient.setDataTimeout(0);//设置传输超时时间
        return ftpClient;
    }


}
