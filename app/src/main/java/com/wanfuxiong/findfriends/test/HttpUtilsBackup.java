package com.wanfuxiong.findfriends.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtilsBackup {
    public static String doPostRequest(String path, Object content) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            String protocol = "http";
            String serverIP = "192.168.0.103";
            String serverPort = "80";

            System.out.println("要发送的信息是：" + content);

            String address = protocol + "://" + serverIP + ":" + serverPort + "/" + path;
            URL url = new URL(address);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //这两个参数必须加
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            //设置超时时间
            httpURLConnection.setReadTimeout(10 * 1000);
            httpURLConnection.setConnectTimeout(10 * 1000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.connect();

            out = new PrintWriter(httpURLConnection.getOutputStream());
            //在输出流中写入参数
            out.print(content);
            out.flush();

            in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

            System.out.println("服务器返回的结果是：" + result);
            return result;
        } catch (MalformedURLException e) {
            System.out.println("URL异常");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO异常");
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static String doGetRequest(String path) {
        BufferedReader in = null;
        String result = "";
        try {
            String serverIP = "192.168.0.103";
            String address = "http://" + serverIP + ":8080/" + path;
            URL url = new URL(address);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //这两个参数必须加
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            //设置超时时间
            httpURLConnection.setReadTimeout(10 * 1000);
            httpURLConnection.setConnectTimeout(10 * 1000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

            System.out.println("服务器返回的结果是：" + result);
            return result;
        } catch (MalformedURLException e) {
            System.out.println("URL异常");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO异常");
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}

