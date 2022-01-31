package com.wanfuxiong.findfriends.util;

import android.util.Log;

import com.wanfuxiong.findfriends.pojo.User;

import java.io.File;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OkHttpUtils {

    private static final String PROTOCOL = MyUtils.PROTOCOL;
    private static final String SERVER = MyUtils.SERVER;
    private static final String PORT = MyUtils.PORT;

    private static final MediaType FROM_DATA = MediaType.parse("multipart/form-data");

    private static OkHttpUtils instance;

    private OkHttpUtils() {

    }

    public static OkHttpUtils getInstance() {
        if (instance == null) {
            instance = new OkHttpUtils();
        }
        return instance;
    }

    public Call get(String path) {
        String url = PROTOCOL + "://" + SERVER + ":" + PORT + "/" + path;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        return client.newCall(request);
    }

    public Call post(String path, Map<String, Object> map) {
        String url = PROTOCOL + "://" + SERVER + ":" + PORT + "/" + path;
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        Set<String> keys = map.keySet();
        for (String k : keys) {
            formBody.add(k, map.get(k).toString());
        }

        Request request = new Request.Builder()
                .url(url)
                .post(formBody.build())
                .build();
        return client.newCall(request);
    }

    public Call post(String path, String json) {
        String url = PROTOCOL + "://" + SERVER + ":" + PORT + "/" + path;
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);

        Request request = new Request.Builder()
                .url(url)//请求的url
                .post(requestBody)
                .build();
        return client.newCall(request);
    }

    public Call postFile(String filePath, String path, String id) {
        String url = PROTOCOL + "://" + SERVER + ":" + PORT + "/" + path;
        OkHttpClient client = new OkHttpClient();
        File file = new File(filePath);
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        String suffix = filePath.substring(filePath.lastIndexOf("."));//文件后缀
        String fileNameWithId = id + suffix;
        Log.d("文件路径 ===> ", filePath);
        Log.d("文件名 ===> ", fileName);
        Log.d("更改为id之后的文件名 ===> ", fileNameWithId);
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        //请求体
        RequestBody requestBody = new MultipartBody.Builder()
                // .setType(MultipartBody.FORM)
                .setType(FROM_DATA)
                .addFormDataPart("id", id)
                .addFormDataPart("file", fileName, fileBody)
                // .addPart(Headers.of(
                //         "Content-Disposition",
                //         "form-data; name=\"filename\""),
                //         RequestBody.create(null, "lzr"))//这里是携带上传的其他数据
                // .addPart(Headers.of(
                //         "Content-Disposition",
                //         "form-data; name=\"file\"; filename=\"" + fileNameWithId + "\""), fileBody)//name对应的是springboot的@RequestParam参数
                .build();
        //请求的地址
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Log.d("fileBody ======> ", fileBody.toString());
        Log.d("requestBody ======> ", requestBody.toString());
        Log.d("request ======> ", request.toString());
        return client.newCall(request);
    }
}

