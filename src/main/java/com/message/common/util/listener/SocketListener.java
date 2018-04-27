package com.message.common.util.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.message.common.util.websocket.SocketServer;
import com.message.common.util.websocket.SocketServerHandler;
import io.netty.channel.ChannelHandlerContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

public class SocketListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        SocketServer.runServer();
        new Thread(new Line()).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}

class Line implements Runnable{

    @Override
    public void run() {
        while(true){

            try {
                Thread.sleep(3000);
                Map<String, ChannelHandlerContext> lineUser = SocketServerHandler.lineUser;
                if(lineUser.size() > 0){
                    JSONObject message = new JSONObject();
                    message.put("lineUser",getLineUsers(lineUser));
                    message.put("type","lineUser");

                    for(Map.Entry<String,ChannelHandlerContext> entry : lineUser.entrySet()){

                        ChannelHandlerContext ctx = entry.getValue();
                        SocketServerHandler.send(ctx,message.toJSONString());

                    }
                }
            } catch (InterruptedException e) {

            }

        }
    }

    public JSONArray getLineUsers( Map<String, ChannelHandlerContext> lineUser){
        JSONArray arr = new JSONArray();
        if(lineUser.size() > 0){
            for(Map.Entry<String,ChannelHandlerContext> entry : lineUser.entrySet()){

                ChannelHandlerContext ctx = entry.getValue();
                if(ctx != null && !ctx.isRemoved() && ctx.channel().isOpen() && ctx.channel().isActive()){
                    arr.add(entry.getKey());

                }
            }
        }
        return arr;
    }
}