package com.wanfuxiong.findfriends.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.wanfuxiong.findfriends.activity.LoadActivity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyUtils {

    public static final String PROTOCOL = "https";
    public static final String WEBSOCKET = "ws";
    // public static final String SERVER = "192.168.0.102";
    public static final String SERVER = "ff.wanfuxiong.com";
    // public static final String SERVER = "1.15.175.51";
    // public static final String SERVER = "10.230.152.202";//CHD
    // public static final String SERVER = "172.20.10.2";// 热点
    public static final String PORT = "443";

    // 把形如yyyy-MM-dd HH:mm:ss的时间字符串转换成需要的字符串
    public static String dateFormate(Context context,String dateString) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        Calendar calendarNow = Calendar.getInstance();
        assert date != null;
        calendar.setTime(date);
        calendarNow.setTime(new Date());
        // 获取语言
        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPreferences.getString("language","");
        if (language.equals("zh")||language.equals("")){
            if (calendarNow.get(Calendar.DATE) - calendar.get(Calendar.DATE) < 1) {
                return dateString.substring(11, 16);
            } else if (calendarNow.get(Calendar.DATE) - calendar.get(Calendar.DATE) < 2) {
                return "昨天" +" "+ dateString.substring(11, 16);
            } else if (calendarNow.get(Calendar.DATE) - calendar.get(Calendar.DATE) < 3) {
                return "前天" +" "+ dateString.substring(11, 16);
            } else if (calendarNow.get(Calendar.YEAR) - calendar.get(Calendar.YEAR) < 1) {
                return (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DATE) + "日";
            } else {
                return calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DATE) + "日";
            }
        } else if (language.equals("en")){
            if (calendarNow.get(Calendar.DATE) - calendar.get(Calendar.DATE) < 1) {
                return dateString.substring(11, 16);
            } else if (calendarNow.get(Calendar.DATE) - calendar.get(Calendar.DATE) < 2) {
                return "Yesterday" +" "+ dateString.substring(11, 16);
            } else if (calendarNow.get(Calendar.YEAR) - calendar.get(Calendar.YEAR) < 1) {
                return (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE);
            } else {
                return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE);
            }
        } else {
            return "未知错误";
        }
    }

    // 字符串(Timestamp)转Date
    public static Date convertStringToDate(String str){
        Date date = null;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = simpleDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    // Date转字符串(Timestamp)
    public static String convertDateToString(Date date){
        String str;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        str = simpleDateFormat.format(date);
        return str;
    }

    // 动态规划
    public static double calculateSimilarity(String str1, String str2) {
        // 计算两个字符串的长度。
        int len1 = str1.length();
        int len2 = str2.length();

        // 建立上面说的数组，比字符长度大一个空间
        int[][] dif = new int[len1 + 1][len2 + 1];

        //赋初值，步骤B。
        for (int a = 0; a <= len1; a++) {
            dif[a][0] = a;
        }

        for (int a = 0; a <= len2; a++) {
            dif[0][a] = a;
        }

        //计算两个字符是否一样，计算左上的值
        int temp;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 取三个值中最小的
                dif[i][j] = min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1, dif[i - 1][j] + 1);
            }
        }

        // 取数组右下角的值，同样不同位置代表不同字符串的比较
        // System.out.println("差异步骤："+dif[len1][len2]);

        // 计算相似度
        float similarity = 1 - (float) dif[len1][len2] / Math.max(str1.length(), str2.length());

        return BigDecimal.valueOf(similarity * 100).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    // 取最小值
    public static int min(int... is) {
        int min = Integer.MAX_VALUE;
        for (int i : is) {
            if (min > i) {
                min = i;
            }
        }
        return min;
    }

    public static void updateResources(Context context) {
        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPreferences.getString("language","");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
