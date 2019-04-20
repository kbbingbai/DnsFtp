package com.hxht.dnsftp.entity;

import java.sql.Timestamp;
import java.util.Date;

public class Test {

    public static String deleteflagSucc = "1";//成功删除的标志
    public static String deleteflagInit = "0";//删除标志的初始值

    public static String pullSucc = "-1";//成功拉取数据的标志
    public static String pullEnable = "0";//可拉取状态（文件大小稳定的状态）
    public static String pullInitState = "-3";//数据库默认值
    public static String pullingFile = "-2";//文件正在被拉取
    public static String pullFailOne = "1";//拉取失败一次
    public static String pullFailTwo = "2";//拉取失败两次
    public static String pullFailThree = "3";//拉取失败三次

    public static String delRemoteFileInterval = "3";//拉取失败三次

    private String id;
    private String filename;
    private String downflag;//拉取时间的标志
    private String analyseflag;//分析的标志
    private Date createtime;//创建时间
    private String deleteflag;//服务器上的数据是否已经被删除
    private Timestamp downtime;//从别人服务器上成功拉取文件的时间
    private long filelen;//文件的大小

    private String pullip;//哪个机器ip拉取了文件
    private Date startpulltime;//文件被拉取的开始时间
    private String pulltimeoutcount;//文件被拉取的超时次数


    public Test() {

    }

    public Test(String filename, long filelen) {
        this.filename = filename;
        this.filelen = filelen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDownflag() {
        return downflag;
    }

    public void setDownflag(String downflag) {
        this.downflag = downflag;
    }

    public String getAnalyseflag() {
        return analyseflag;
    }

    public void setAnalyseflag(String analyseflag) {
        this.analyseflag = analyseflag;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getDeleteflag() {
        return deleteflag;
    }

    public void setDeleteflag(String deleteflag) {
        this.deleteflag = deleteflag;
    }

    public Timestamp getDowntime() {
        return downtime;
    }

    public void setDowntime(Timestamp downtime) {
        this.downtime = downtime;
    }

    public long getFilelen() {
        return filelen;
    }

    public void setFilelen(long filelen) {
        this.filelen = filelen;
    }

    public String getPullip() {
        return pullip;
    }

    public void setPullip(String pullip) {
        this.pullip = pullip;
    }

    public Date getStartpulltime() {
        return startpulltime;
    }

    public void setStartpulltime(Date startpulltime) {
        this.startpulltime = startpulltime;
    }

    public String getPulltimeoutcount() {
        return pulltimeoutcount;
    }

    public void setPulltimeoutcount(String pulltimeoutcount) {
        this.pulltimeoutcount = pulltimeoutcount;
    }

    @Override
    public String toString() {
        return "Test{" +
                "id='" + id + '\'' +
                ", filename='" + filename + '\'' +
                ", downflag='" + downflag + '\'' +
                ", analyseflag='" + analyseflag + '\'' +
                ", createtime=" + createtime +
                ", deleteflag='" + deleteflag + '\'' +
                ", downtime=" + downtime +
                ", filelen=" + filelen +
                '}';
    }

}
