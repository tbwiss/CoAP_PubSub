/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.elements.wss;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.eclipse.californium.elements.tcp.TcpConnector;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author thomas
 */
public class WssClientConnector implements Connector, TcpConnector {

    private final static Logger LOGGER = Logger.getLogger(WssClientConnector.class.getName());

    private final int connectTimeoutMillis;
    private RawDataChannel rawDataChannel;
    private WebSocketSecureClientHandler wssClientHandler = null;
    private String keyStoreLocation;

    public WssClientConnector(String keyStoreLocation, int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.keyStoreLocation = keyStoreLocation;
    }

    @Override
    public void start() throws IOException {
        if (rawDataChannel == null) {
            throw new IllegalStateException("Cannot start without message handler.");
        }

        wssClientHandler = new WebSocketSecureClientHandler(rawDataChannel);
    }

    @Override
    public void stop() {
        wssClientHandler.stop();
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void send(RawData msg) {
        if (wssClientHandler != null) {
            wssClientHandler.sendData(msg);
        }
    }

    @Override
    public void setRawDataReceiver(RawDataChannel messageHandler) {
        if (rawDataChannel != null) {
            throw new IllegalStateException("Raw data channel already set.");
        }

        this.rawDataChannel = messageHandler;
    }

    @Override
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(0);
    }

    /**
     * WebSocketSecureClientHandler
     */
    private class WebSocketSecureClientHandler {

        private RawDataChannel rawDataChannel;
        private Map clientThreads;

        public WebSocketSecureClientHandler(RawDataChannel rawDataChannel) {
            this.rawDataChannel = rawDataChannel;
            clientThreads = new HashMap<InetSocketAddress, WebSocketSecureClientThread>();
        }

        public void sendData(RawData msg) {
            try {
                if (clientThreads.containsKey(msg.getInetSocketAddress())) {
                    WebSocketSecureClientThread client = (WebSocketSecureClientThread) clientThreads.get(msg.getInetSocketAddress());
                    // System.out.println("WebSocketSecureClientHandler existing client: " + client.getAddress().toString());
                    client.sendRawData(msg);
                } else {
                    URI serverUri = new URI("wss:/" + msg.getInetSocketAddress());
                    // System.out.println("WebSocketSecureClientHandler sets serverURI: " + serverUri.toString());
                    WebSocketSecureClientThread newClient = new WebSocketSecureClientThread(serverUri, msg, rawDataChannel);
                    clientThreads.put(msg.getInetSocketAddress(), newClient);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        public void stop() {
            Iterator iter = clientThreads.values().iterator();
            while (iter.hasNext()) {
                WebSocketSecureClientThread clientThread = (WebSocketSecureClientThread) iter.next();
                clientThread.close();
            }
            clientThreads.clear();
        }

    }

    private class WebSocketSecureClientThread extends WebSocketClient {
        private RawDataChannel rawDataChannel;
        private RawData rawData;
        private InetSocketAddress serverAddress;

        public WebSocketSecureClientThread(URI serverUri, Draft draft) {
            super(serverUri, draft);
        }

        public WebSocketSecureClientThread(URI serverURI, RawData rawData, RawDataChannel rawDataChannel) {
            super(serverURI);
            this.rawDataChannel = rawDataChannel;
            this.rawData = rawData;
            serverAddress = rawData.getInetSocketAddress();

            try {
                this.setSocket(new SslUtil().getSSLContext(keyStoreLocation).getSocketFactory().createSocket());
                //
                // cipher suite somewhere here
                //
                //
                this.setConnectionLostTimeout(connectTimeoutMillis);
                this.connect();
            } catch (IOException ex) {
                System.out.println("WebSocketSecureClientThread cant setSocket()");
            }
        }

        public void sendRawData(RawData msg) {
            if (this.isOpen()) {
                this.send(msg.getBytes());
            } else {
                System.out.println("WebSocketSecureClientThread is not open ");
            }
        }

        public InetSocketAddress getAddress() {
            return serverAddress;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            if (this.isOpen()) {
                this.send(rawData.getBytes());
            } else {
                System.out.println("WebSocketSecureClientThread is not open, could not send data");
            }
        }

        @Override
        public void onMessage(ByteBuffer message) {
            rawDataChannel.receiveData(new RawData(message.array(), rawData.getInetSocketAddress()));
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
        }

        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onMessage(String string) {
            System.out.println("String message: " + string);
        }
    }

}
