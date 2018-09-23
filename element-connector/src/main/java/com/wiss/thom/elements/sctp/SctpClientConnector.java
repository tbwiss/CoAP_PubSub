/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.elements.sctp;

import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.AssociationChangeNotification;
import static com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.COMM_UP;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.ShutdownNotification;
import com.wiss.thom.elements.ws.WsClientConnector;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.eclipse.californium.elements.tcp.TcpConnector;

/**
 *
 * @author thomas
 */
public class SctpClientConnector implements Connector, TcpConnector {

    private static final Logger LOGGER = Logger.getLogger(SctpClientConnector.class.getName());
    private final String SUPPORTED_SCHEME = "coap+sctp";

    private final int connectTimeoutMillis;
    private RawDataChannel rawDataChannel;

    private SctpClientHandler sctpClientHandler;
    private boolean isStop = false;

    public SctpClientConnector(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    @Override
    public void start() throws IOException {
        if (rawDataChannel == null) {
            throw new IllegalStateException("Cannot start without message handler.");
        }

        sctpClientHandler = new SctpClientHandler();
        System.out.println("SctpClientHandler started");
    }

    @Override

    public void stop() {
        try {
            isStop = true;
            sctpClientHandler.stop();
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
            if (sctpClientHandler != null) {
                sctpClientHandler.sendData(msg);
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
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(0);
    }

    /**
     * The SctpClienthandler
     */
    private class SctpClientHandler {

        private Map clientThreads;

        public SctpClientHandler() {
            clientThreads = new HashMap<InetSocketAddress, SctpClientImpl>();
        }

        public void sendData(RawData msg) {
            try {
                if (clientThreads.containsKey(msg.getInetSocketAddress())) {
                    SctpClientImpl client = (SctpClientImpl) clientThreads.get(msg.getInetSocketAddress());
                    //System.out.println("SctpClientHandler existing client to server: " + client.getAddress().toString());
                    client.sendRawData(msg);
                } else {
                    //System.out.println("SctpClientHandler sets serverAddress: " + msg.getInetSocketAddress().toString());
                    SctpClientImpl client = new SctpClientImpl(msg.getInetSocketAddress());
                    clientThreads.put(msg.getInetSocketAddress(), client);
                    client.sendRawData(msg);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        public void stop() {
            Iterator iter = clientThreads.values().iterator();
            while (iter.hasNext()) {
                SctpClientImpl clientImpl = (SctpClientImpl) iter.next();
                clientImpl.stop();
            }
            clientThreads.clear();
        }

    }

    /**
     * The SctpClientIimpl
     */
    private class SctpClientImpl {

        private SctpChannel sc;
        private MessageInfo messageInfo;
        private InetSocketAddress serverAddress;
        private SctpClientReceiveThread receiveThread;

        public SctpClientImpl(InetSocketAddress serverAddress) throws IOException {
            this.serverAddress = serverAddress;
            sc = SctpChannel.open(this.serverAddress, 0, 0);
            messageInfo = MessageInfo.createOutgoing(null, 0);
            startNewReceiveThread();
        }

        public final void startNewReceiveThread() {
            receiveThread = new SctpClientReceiveThread(sc, serverAddress);
            receiveThread.start();
        }

        public void sendRawData(RawData msg) throws IOException {
            if (sc.isOpen()) {
                /* THIS here works!!!
                ByteBuffer buff = ByteBuffer.allocateDirect("test".length());
                buff.put("test".getBytes()).flip();
                System.out.println(buff.toString());*/

                ByteBuffer buff = ByteBuffer.allocateDirect(msg.getSize());
                buff.put(msg.getBytes()).flip();
                sc.send(buff, messageInfo);
                buff.clear();
                //System.out.println("SCTPClient:: sent RawData");
            } else {
                System.out.println("SCTPClient:: SctpChannel is not open");
            }
        }

        public InetSocketAddress getAddress() {
            return serverAddress;
        }
        
        public void stop(){
            try {
                sc.shutdown();
                receiveThread.interrupt();
            } catch (IOException ex) {
                Logger.getLogger(SctpClientConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }

    }

    /**
     * The SctpClientReceiveThread
     */
    private class SctpClientReceiveThread extends Thread {

        private SctpChannel sc;
        private InetSocketAddress serverAddress;

        public SctpClientReceiveThread(SctpChannel sc, InetSocketAddress serverAddress) {
            this.serverAddress = serverAddress;
            this.sc = sc;
        }

        @Override
        public void run() {
            try {
                ByteBuffer buf = ByteBuffer.allocateDirect(2400);

                AssociationHandler assocHandler = new AssociationHandler();

                while (!isStop) {
                    MessageInfo info = sc.receive(buf, System.out, assocHandler);
                    InetSocketAddress address = (InetSocketAddress) info.address();
                    
                    System.out.println(address.getAddress());
                    
                    buf.flip();
                    if (buf.remaining() > 0) {
                        rawDataChannel.receiveData(new RawData(getByteArray(buf), address));
                    }
                    buf.clear();

                }
            } catch (Exception e) {
                System.err.println(e);
            }

        }

        public byte[] getByteArray(ByteBuffer bb) {
            byte[] ba = new byte[bb.limit()];
            if (bb.remaining() > 0) {
                bb.get(ba);
            }
            return ba;
        }

        public InetSocketAddress getServerAddress() {
            return serverAddress;
        }

        private class AssociationHandler extends AbstractNotificationHandler {

            public HandlerResult handleNotification(AssociationChangeNotification not,
                    PrintStream stream) {
                if (not.event().equals(COMM_UP)) {
                    int outbound = not.association().maxOutboundStreams();
                    int inbound = not.association().maxInboundStreams();
                    stream.printf("New association setup with %d outbound streams"
                            + ", and %d inbound streams.\n", outbound, inbound);
                }

                return HandlerResult.CONTINUE;
            }

            public HandlerResult handleNotification(ShutdownNotification not,
                    PrintStream stream) {
                stream.printf("The association has been shutdown.\n");
                return HandlerResult.RETURN;
            }
        }

    }

}
