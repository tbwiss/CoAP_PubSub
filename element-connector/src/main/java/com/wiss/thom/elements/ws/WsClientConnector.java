/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.elements.ws;

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
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author thomas
 */
public class WsClientConnector implements Connector, WsConnector {

    private final static Logger LOGGER = Logger.getLogger(WsClientConnector.class.getName());

    private final int connectTimeoutMillis;
    private RawDataChannel rawDataChannel;
    private WebSocketClientHandler wsClientHandler = null;

    public WsClientConnector(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    @Override
    public void start() throws IOException {
        if (rawDataChannel == null) {
            throw new IllegalStateException("Cannot start without message handler.");
        }

        wsClientHandler = new WebSocketClientHandler(rawDataChannel);
    }

    @Override
    public void stop() {
        wsClientHandler.stop();
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void send(RawData msg) {
        if (wsClientHandler != null) {
            wsClientHandler.sendData(msg);
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
     * Handler
     */
    private class WebSocketClientHandler {

        private RawDataChannel rawDataChannel;
        private Map clientThreads;

        public WebSocketClientHandler(RawDataChannel rawDataChannel) {
            this.rawDataChannel = rawDataChannel;
            clientThreads = new HashMap<InetSocketAddress, WebSocketClientThread>();
        }

        public void sendData(RawData msg) {
            try {
                if (clientThreads.containsKey(msg.getInetSocketAddress())) {
                    WebSocketClientThread client = (WebSocketClientThread) clientThreads.get(msg.getInetSocketAddress());
                    //System.out.println("WebSocketClientHandler existing client: " + client.getAddress().toString());
                    client.sendRawData(msg);
                } else {
                    URI serverUri = new URI("ws:/" + msg.getInetSocketAddress());
                    //System.out.println("WebSocketClientHandler sets serverURI: " + serverUri.toString());
                    WebSocketClientThread newClient = new WebSocketClientThread(serverUri, msg, rawDataChannel);
                    clientThreads.put(msg.getInetSocketAddress(), newClient);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        public void stop() {
            Iterator iter = clientThreads.values().iterator();
            while (iter.hasNext()) {
                WebSocketClientThread clientThread = (WebSocketClientThread) iter.next();
                clientThread.close();
            }
            clientThreads.clear();
        }
    }

    private class WebSocketClientThread extends WebSocketClient {

        private RawDataChannel rawDataChannel;
        private RawData rawData;
        private InetSocketAddress serverAddress;

        public WebSocketClientThread(URI serverUri, Draft draft) {
            super(serverUri, draft);
        }

        public WebSocketClientThread(URI serverURI, RawData rawData, RawDataChannel rawDataChannel) {
            super(serverURI);
            this.rawDataChannel = rawDataChannel;
            this.rawData = rawData;
            serverAddress = rawData.getInetSocketAddress();
            this.setConnectionLostTimeout(connectTimeoutMillis);
            this.connect();

        }

        public void sendRawData(RawData msg) {
            if (this.isOpen()) {
                this.send(msg.getBytes());
                //System.out.println("WebSocketClientThread sent RAWDATA");
            } else {
                System.out.println("WebSocketClientThread is not open");
            }
        }

        public InetSocketAddress getAddress() {
            return serverAddress;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            if (this.isOpen()) {
                this.send(rawData.getBytes());
                //System.out.println("WebSocketClientThread sent data");
            } else {
                System.out.println("WebSocketClientThread is not open, could not send data");
            }
        }

        @Override
        public void onMessage(ByteBuffer message) {
            //System.out.println("WebSocketClientThread received buffer: " + message);
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
