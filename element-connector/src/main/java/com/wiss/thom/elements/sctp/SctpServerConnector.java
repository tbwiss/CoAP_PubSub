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
import com.sun.nio.sctp.SctpServerChannel;
import com.sun.nio.sctp.ShutdownNotification;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.eclipse.californium.elements.tcp.TcpConnector;

/**
 *
 * @author thomas
 */
public class SctpServerConnector implements Connector, TcpConnector {

    private static final Logger LOGGER = Logger.getLogger(SctpServerConnector.class.getName());
    private final String SUPPORTED_SCHEME = "coap+sctp";

    private final int connectionIdleTimeoutSeconds;

    private RawDataChannel rawDataChannel;
    private InetSocketAddress localAddress;

    private SctpServerHandler sctpServerHandler;
    private boolean isStop = false;

    public SctpServerConnector(InetSocketAddress localAddress, int idleTimeout) {
        this.connectionIdleTimeoutSeconds = idleTimeout;
        this.localAddress = localAddress;
    }

    @Override
    public void start() throws IOException {
        if (rawDataChannel == null) {
            throw new IllegalStateException("Cannot start without message handler.");
        }

        sctpServerHandler = new SctpServerHandler(localAddress);
        sctpServerHandler.start();
        System.out.println("SctpServerHandler started");
    }

    @Override

    public void stop() {
        try {
            isStop = true;
            sctpServerHandler.interrupt();
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
            if (sctpServerHandler.isAlive()) {
                sctpServerHandler.sendData(msg);
            } else {
                System.out.println("SctpServerHandler is not alive");
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
     * The SctpServerHandler
     */
    private class SctpServerHandler extends Thread {

        private SctpServerChannel ssc;
        private InetSocketAddress localAddress;
        private Map serverThreads;
        private SctpServerThread serverThread;

        public SctpServerHandler(InetSocketAddress localAddress) throws IOException {
            this.localAddress = localAddress;
            ssc = SctpServerChannel.open();
            ssc.bind(localAddress);

            serverThreads = new HashMap<InetSocketAddress, SctpServerThread>();
        }

        @Override
        public void run() {
            try {
                while (!isStop) {
                    SctpChannel sc = ssc.accept();

                    serverThread = new SctpServerThread(sc, localAddress);
                    serverThread.start();

                    InetSocketAddress bindAddress = getBindAddress(sc);
                    if (bindAddress != null) {
                        serverThreads.put(bindAddress, serverThread);
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }

        public void sendData(RawData msg) throws IOException {

            try {

                if (serverThread != null) {
                    //System.out.println("SctpServerHandler existing server to client: " + msg.getAddress().toString());
                    if (serverThread.isAlive()) {
                        serverThread.sendRawData(msg);
                    } else {
                        System.out.println("SCTPServerHandler:: serverThread not alive");
                    }
                } else {
                    // This case should never be reached since the sc needs to be open to reveice data from the client
                    System.out.println("SctpServerHandler no serverThread found");

                }
                
                /*
                if (serverThreads.containsKey(msg.getInetSocketAddress())) {
                    SctpServerThread serverThread = (SctpServerThread) serverThreads.get(msg.getInetSocketAddress());
                    System.out.println("SctpServerHandler existing client to server: " + serverThread.toString());
                    if (serverThread.isAlive()) {
                        serverThread.sendRawData(msg);
                    } else {
                        System.out.println("SCTPServerHandler:: serverThread not alive");
                    }
                } else {
                    // This case should never be reached since the sc needs to be open to reveice data from the client
                    System.out.println("SctpServerHandler no serverThread found");
                    
                }
                 */
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        public InetSocketAddress getBindAddress(SctpChannel sc) {
            try {
                Set<SocketAddress> set = sc.getRemoteAddresses();
                Iterator iter = set.iterator();
                List<InetSocketAddress> bindAddress = new ArrayList<InetSocketAddress>();
                while (iter.hasNext()) {
                    InetSocketAddress address = (InetSocketAddress) iter.next();
                    if (address.getHostName().length() > 1) {
                        bindAddress.add(address);
                    }
                }
                InetSocketAddress returnAddress = bindAddress.get(0);
                return returnAddress != null ? returnAddress : null;
            } catch (Exception e) {
                System.err.println("SctpServerHandler:: getBindAddress , " + e);
                return null;
            }
        }
    }

    /**
     *
     */
    private class SctpServerThread extends Thread {

        private SctpServerChannel ssc;
        private SctpChannel sc;
        private InetSocketAddress localAddress;
        private InetSocketAddress clientsAddress = null;
        private MessageInfo messageInfo;

        public SctpServerThread(SctpChannel sc, InetSocketAddress localAddress) {
            this.sc = sc;
            this.localAddress = localAddress;
            messageInfo = MessageInfo.createOutgoing(null, 0);
        }

        @Override
        public void run() {
            try {
                ByteBuffer buf = ByteBuffer.allocateDirect(2400);

                AssociationHandler assocHandler = new AssociationHandler();

                while (!isStop) {
                    // Maybe make this mulit threaded here as well, inclduig the sendData method..
                    // but that means you need to be able to track back from where the msg originated
                    MessageInfo info = sc.receive(buf, System.out, assocHandler);

                    InetSocketAddress address = (InetSocketAddress) info.address();
                    if (address.getHostName().length() > 1 && clientsAddress == null) {
                        clientsAddress = address;
                    }

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

        public void sendRawData(RawData msg) throws IOException {
            if (sc.isOpen()) {
                ByteBuffer buff = ByteBuffer.allocateDirect(msg.getSize());
                System.out.println(msg.getAddress() + "  " + msg.getPort());
                buff.put(msg.getBytes()).flip();
                sc.send(buff, messageInfo);
                buff.clear();
                //System.out.println("SCTPServer:: sent RawData");
            } else {
                System.out.println("SCTPServer:: SctpChannel is not open");
            }
        }

        public byte[] getByteArray(ByteBuffer bb) {
            byte[] ba = new byte[bb.limit()];
            if (bb.remaining() > 0) {
                bb.get(ba);
            }
            return ba;
        }

        public InetSocketAddress getClientsAddress() {
            return clientsAddress;
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
