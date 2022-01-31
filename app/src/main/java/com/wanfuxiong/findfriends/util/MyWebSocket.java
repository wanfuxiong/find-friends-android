package com.wanfuxiong.findfriends.util;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.wanfuxiong.findfriends.pojo.Message;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

public class MyWebSocket extends WebSocketClient implements Serializable {

    private static final String TAG = "MyWebSocket";

    // url:"ws://服务器地址:端口号/websocket"
    public MyWebSocket(String url) throws URISyntaxException {
        super(new URI(url));
        // EventBus.getDefault().register(this);
    }

    // 打开webSocket时回调
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Log.i(TAG, "已打开webSocket连接");
    }

    // 接收到消息时回调
    @Override
    public void onMessage(String s) {
        Log.i(TAG, "收到消息" + s);
        Message message = JSON.parseObject(s, Message.class);
        EventBus.getDefault().post(message);
    }

    // 断开连接时回调
    @Override
    public void onClose(int i, String s, boolean b) {
        // Log.d(TAG, "正在断开webSocket连接");
        // try {
        //     this.closeBlocking();
        //     Log.d(TAG, "已断开webSocket连接");
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
    }

    // 出现异常时回调
    @Override
    public void onError(Exception e) {
        Log.d(TAG, "出错了，正在断开webSocket连接");
        try {
            this.closeBlocking();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        Log.d(TAG, e.getMessage());
    }
}

