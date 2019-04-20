package com.hxht.dnsftp.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    //得到指定日期的前一天  yyyy-MM-dd
    public static String getYesterday(Date date) {
        SimpleDateFormat tarDf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -1);
        Date time = cal.getTime();
        return tarDf.format(time);
    }

    //得到指定日期的字符串的形式 yyyy-MM-dd
    public static String getCurrDay(Date date) {
        SimpleDateFormat tarDf = new SimpleDateFormat("yyyy-MM-dd");
        return tarDf.format(date);
    }

    //得到当天字符串的形式 yyyy-MM-dd
    public static String getCurrDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currDate = simpleDateFormat.format(new Date());
        return currDate;
    }

    //得到昨天字符串的形式 yyyy-MM-dd
    public static String getLastDate() {
        SimpleDateFormat tarDf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date time = cal.getTime();
        return tarDf.format(time);
    }

}
