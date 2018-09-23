/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.elements.wss;

import com.wiss.thom.elements.ws.WsServerConnector;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.eclipse.californium.elements.tcp.TcpConnector;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

/**
 *
 * @author thomas
 */
public class WssServerConnector implements Connector, TcpConnector {

    private static final Logger LOGGER = Logger.getLogger(WsServerConnector.class.getName());
    private final String SUPPORTED_SCHEME = "coap+wss";

    private final int connectionIdleTimeoutSeconds;

    private RawDataChannel rawDataChannel;
    private InetSocketAddress localAddress;

    private WebSocketSecureServerImpl wssServer = null;
    private String keyStoreLocation;

    public WssServerConnector(InetSocketAddress localAddress, String keyStoreLocation, int idleTimeout) {
        this.connectionIdleTimeoutSeconds = idleTimeout;
        this.localAddress = localAddress;
        this.keyStoreLocation = keyStoreLocation;
    }

    @Override
    public void start() throws IOException {
        if (rawDataChannel == null) {
            throw new IllegalStateException("Cannot start without message handler.");
        }

        wssServer = new WebSocketSecureServerImpl(localAddress, rawDataChannel);
        wssServer.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(new SslUtil().getSSLContext(keyStoreLocation)));
        wssServer.setConnectionLostTimeout(connectionIdleTimeoutSeconds);
        wssServer.start();

        System.out.println("WebSocketSecureServerImpl started");
    }

    @Override

    public void stop() {
        try {
            wssServer.stop();
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
            if (wssServer.connections().size() > 0) {
                wssServer.broadcast(msg.getBytes());
            } else {
                System.out.println("WebSocketSecureServerImpl no clients connected");
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

    

    /**
     * WebSocketSecureServerImpl
     */
    private class WebSocketSecureServerImpl extends WebSocketServer {

        private RawDataChannel rawDataChannel;
        private InetSocketAddress address;

        public WebSocketSecureServerImpl(int port) throws UnknownHostException {
            super(new InetSocketAddress(port));
        }

        public WebSocketSecureServerImpl(InetSocketAddress address, RawDataChannel rawDataChannel) {
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
            System.out.println(conn + " disconnected");
        }

        @Override
        public void onMessage(org.java_websocket.WebSocket conn, String message) {
            System.out.println("WebSocketSecureServerImpl received: " + message);
        }

        @Override
        public void onMessage(org.java_websocket.WebSocket conn, ByteBuffer message) {
            rawDataChannel.receiveData(new RawData(message.array(), address));
        }

        @Override
        public void onError(org.java_websocket.WebSocket conn, Exception ex) {
            System.err.println(ex);
        }

        @Override
        public void onStart() {
            System.out.println("WebSocketSecureServerImpl started!");
        }

    }

}
