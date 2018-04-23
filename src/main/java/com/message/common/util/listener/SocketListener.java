package com.message.common.util.listener;

import com.message.common.util.websocket.SocketServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SocketListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        SocketServer.runServer();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
