package com.message.common.util.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketServer {
    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);

    private void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {


            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SocketServerInitializer());

            Channel ch = b.bind(8888).sync().channel();
            ch.closeFuture().sync();
            log.debug(" socket server start by 8888");
        } catch (InterruptedException e) {

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }



    public static void runServer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                new SocketServer().run();
            }
        }).start();
    }



}
