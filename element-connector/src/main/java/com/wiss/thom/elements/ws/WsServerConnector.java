/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.elements.ws;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.eclipse.californium.elements.tcp.TcpServerConnector;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 *
 * @author thomas
 */
public class WsServerConnector implements Connector, WsConnector {

    private static final Logger LOGGER = Logger.getLogger(WsServerConnector.class.getName());
    private final String SUPPORTED_SCHEME = "coap+ws";

    private final int connectionIdleTimeoutSeconds;

    private RawDataChannel rawDataChannel;
    private InetSocketAddress localAddress;

    private WebSocketServerImpl wsServer = null;

    public WsServerConnector(InetSocketAddress localAddress, int idleTimeout) {
        this.connectionIdleTimeoutSeconds = idleTimeout;
        this.localAddress = localAddress;
    }

    @Override
    public void start() throws IOException {
        if (rawDataChannel == null) {
            throw new IllegalStateException("Cannot start without message handler.");
        }

        wsServer = new WebSocketServerImpl(localAddress, rawDataChannel);
        wsServer.start();
        wsServer.setConnectionLostTimeout(connectionIdleTimeoutSeconds);
        
        System.out.println("WebSocketServerImpl started");
    }

    @Override

    public void stop() {
        try {
            wsServer.stop();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void send(RawData msg) {
        try {
            if (wsServer.connections().size() > 0) {
                wsServer.broadcast(msg.getBytes());
            } else {
                System.out.println("WebSocketServerImpl no clients connected");
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    @Override
    public void setRawDataReceiver(RawDataChannel messageHandler) {
        if (rawDataChannel != null) {
            throw new IllegalStateException("RawDataChannel alrady set");
        }
        this.rawDataChannel = messageHandler;
    }

    @Override
    public synchronized InetSocketAddress getAddress() {
        return localAddress;
    }

    private class WebSocketServerImpl extends WebSocketServer {

        private RawDataChannel rawDataChannel;
        private InetSocketAddress address;

        public WebSocketServerImpl(int port) throws UnknownHostException {
            super(new InetSocketAddress(port));
        }

        public WebSocketServerImpl(InetSocketAddress address, RawDataChannel rawDataChannel) {
            super(address);
            this.address = address;
            this.rawDataChannel = rawDataChannel;
        }

        @Override
        public void onOpen(org.java_websocket.WebSocket conn, ClientHandshake handshake) {
            System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected");
        }

        @Override
        public void onClose(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {
            conn.close();
            System.out.println(conn + " disconnected");
        }

        @Override
        public void onMessage(org.java_websocket.WebSocket conn, String message) {
            System.out.println("wsServer received: " + message);
        }

        @Override
        public void onMessage(org.java_websocket.WebSocket conn, ByteBuffer message) {
            //System.out.println("wsServer: " + conn.getResourceDescriptor());
            //System.out.println("wsServer received: " + message.toString());
            rawDataChannel.receiveData(new RawData(message.array(), address));
        }

        @Override
        public void onError(org.java_websocket.WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            System.out.println("WsServer started!");
        }

    }

}
