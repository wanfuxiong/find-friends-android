package com.wanfuxiong.findfriends.util;

import android.util.Log;

import com.wanfuxiong.findfriends.pojo.User;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

public class HttpUtils {

    private static final String PROTOCOL = MyUtils.PROTOCOL;
    private static final String SERVER = "192.168.0.105";
    private static final String PORT = "80";
    private static final String FILE_NAME = "profile.jpg";

    private static final int TIME_OUT = 10 * 1000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";

    public static String doPostRequest(String path, Object content) {

        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        StringBuilder result = new StringBuilder();

        try {
            System.out.println("要发送的信息是：" + content);

            String address = PROTOCOL + "://" + SERVER + ":" + PORT + "/" + path;
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

            printWriter = new PrintWriter(httpURLConnection.getOutputStream());
            //在输出流中写入参数
            printWriter.print(content);
            printWriter.flush();

            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }

            // Log.d("服务器返回的结果是：" , result);
            return result.toString();

        } catch (MalformedURLException e) {
            System.out.println("URL异常");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO异常");
            e.printStackTrace();
        } finally {
            try {
                if (printWriter != null)
                    printWriter.close();
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static String doPostRequest1(String path, Object content) {

        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        StringBuilder result = new StringBuilder();

        try {
            System.out.println("要发送的信息是：" + content);

            String address = PROTOCOL + "://" + SERVER + ":" + PORT + "/" + path;
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

            printWriter = new PrintWriter(httpURLConnection.getOutputStream());
            //在输出流中写入参数
            printWriter.print(content);
            printWriter.flush();

            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }

            System.out.println("服务器返回的结果是：" + result);
            return result.toString();

        } catch (MalformedURLException e) {
            System.out.println("URL异常");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO异常");
            e.printStackTrace();
        } finally {
            try {
                if (printWriter != null)
                    printWriter.close();
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    /**
     * 实现将User打包，并通过Http传给服务器
     *
     * @param path        请求的路径
     * @param params      字符串类型的数据
     * @param inputstream 头像文件
     */
    public static void doPostRequest2(String path, Map<String, String> params, InputStream inputstream) {

        BufferedReader bufferedReader = null;
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;

        String BOUNDARY = UUID.randomUUID().toString();                                             //边界标识 随机生成
        String CONTENT_TYPE = "multipart/form-data";                                                //内容类型
        String address = PROTOCOL + "://" + SERVER + ":" + PORT + "/" + path;

        try {
            URL url = new URL(address);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);                                                         //允许输入流
            httpURLConnection.setDoOutput(true);                                                        //允许输出流
            httpURLConnection.setUseCaches(false);                                                      //不允许缓存
            httpURLConnection.setReadTimeout(TIME_OUT);
            httpURLConnection.setConnectTimeout(TIME_OUT);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Charset", CHARSET);                                   //设置编码
            httpURLConnection.setRequestProperty("connection", "keep-alive");
            httpURLConnection.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

            outputStream = httpURLConnection.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);

            // httpURLConnection.connect();

            // 开始拼接参数
            StringBuilder sb = new StringBuilder();
            // 先拼接字符串（文本）参数
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);//分界符
                    sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(LINE_END);
                    sb.append("Content-Type: text/plain; charset=" + CHARSET + LINE_END);
                    sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                    sb.append(LINE_END);
                    sb.append(entry.getValue());
                    sb.append(LINE_END);//换行！
                }
            }
            // 再拼接头像参数（给的是输入流）
            sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
            sb.append("Content-Disposition: form-data; name=\"" + "profile" + "\"; filename=\"" + FILE_NAME + "\"" + LINE_END);
            sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
            sb.append(LINE_END);
            // 写入文件数据
            dataOutputStream.write(sb.toString().getBytes());
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputstream.read(bytes)) != -1) {
                dataOutputStream.write(bytes, 0, len);
            }
            // inputstream.close();
            dataOutputStream.write(LINE_END.getBytes());//一定还有换行
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
            dataOutputStream.write(end_data);
            dataOutputStream.flush();
            // dataOutputStream.close();

            // 看看响应的东西
            int code = httpURLConnection.getResponseCode();
            Log.d("响应码", code + "");
            sb.setLength(0);
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);//sb是相应的结果
            }
            System.out.println(sb);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void doPostRequest3(String path, Map<String, String> params) {

        BufferedReader bufferedReader = null;
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;

        String BOUNDARY = UUID.randomUUID().toString();                                             //边界标识 随机生成
        String CONTENT_TYPE = "multipart/form-data";                                                //内容类型
        String address = PROTOCOL + "://" + SERVER + ":" + PORT + "/" + path;

        try {
            URL url = new URL(address);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);                                                         //允许输入流
            httpURLConnection.setDoOutput(true);                                                        //允许输出流
            httpURLConnection.setUseCaches(false);                                                      //不允许缓存
            httpURLConnection.setReadTimeout(TIME_OUT);
            httpURLConnection.setConnectTimeout(TIME_OUT);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Charset", CHARSET);                                   //设置编码
            httpURLConnection.setRequestProperty("connection", "keep-alive");
            httpURLConnection.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

            outputStream = httpURLConnection.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);

            // httpURLConnection.connect();

            // 开始拼接参数
            StringBuilder sb = new StringBuilder();
            // 先拼接字符串（文本）参数
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);//分界符
                    sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(LINE_END);
                    sb.append("Content-Type: text/plain; charset=" + CHARSET + LINE_END);
                    sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                    sb.append(LINE_END);
                    sb.append(entry.getValue());
                    sb.append(LINE_END);//换行！
                }
            }
            // // 再拼接头像参数（给的是输入流）
            // sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
            // sb.append("Content-Disposition: form-data; name=\"" + "profile" + "\"; filename=\"" + FILE_NAME + "\"" + LINE_END);
            // sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
            // sb.append(LINE_END);
            // // 写入文件数据
            // dataOutputStream.write(sb.toString().getBytes());
            // byte[] bytes = new byte[1024];
            // int len;
            // while ((len = inputstream.read(bytes)) != -1) {
            //     dataOutputStream.write(bytes, 0, len);
            // }
            // inputstream.close();
            dataOutputStream.write(LINE_END.getBytes());//一定还有换行
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
            dataOutputStream.write(end_data);
            dataOutputStream.flush();
            // dataOutputStream.close();


            // 看看响应的东西
            int code = httpURLConnection.getResponseCode();
            Log.d("响应码", code + "");
            sb.setLength(0);
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);//sb是相应的结果
            }
            System.out.println("服务器返回的数据  ===>  "+sb);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

