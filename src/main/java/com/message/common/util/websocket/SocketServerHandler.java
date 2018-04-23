package com.message.common.util.websocket;

import com.alibaba.fastjson.JSONObject;
import com.message.common.util.JDBCUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by oneapm on 2018/1/24.
 */
public class SocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger log = LoggerFactory.getLogger(SocketServerHandler.class);

    public static Map<String,ChannelHandlerContext> lineUser = new ConcurrentHashMap<>();


    public static boolean send(ChannelHandlerContext ctx, String message) {

        if(ctx != null && !ctx.isRemoved() && ctx.channel().isOpen() && ctx.channel().isActive()){
            ctx.channel().writeAndFlush(new TextWebSocketFrame(message));
            return true;
        }
        return false;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

        if(o instanceof TextWebSocketFrame){
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame)o;
            String text = textWebSocketFrame.text();
            try{
                JSONObject object = JSONObject.parseObject(text);
                String type = object.getString("type");
                if("init".equals(type)){
                    String id = object.getString("id");
                    lineUser.put(id,channelHandlerContext);
                }else if("message".equals(type)){


                    String sql = "insert into chat_message (auto_id,sender,recver,message,create_time) values(?,?,?,?,?)";

                    JDBCUtil.getInstance().execute(sql,new Object[]{
                            UUID.randomUUID().toString().replace("-",""),
                            object.getString("userId"),//send
                            object.getString("id"),
                            object.getString("content"),
                            new Date()
                    });
                    send(lineUser.get(object.getString("id")),object.toJSONString());
                }

            }catch (Exception e){

            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        ctx.close();

    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.debug(toString());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    }
}
