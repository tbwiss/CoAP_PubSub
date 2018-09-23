/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.elements.dtls;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.logging.Logger;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig.Builder;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;

/**
 *
 * @author thomas
 */
public class DtlsServerConnector implements Connector, DtlsConnector {

    private final static Logger LOGGER = Logger.getLogger(DtlsServerConnector.class.getName());

    private final String SUPPORTED_SCHEME = "coap+dtls";

    private final int retransmissionTimeout;

    private RawDataChannel rawDataChannel;
    private InetSocketAddress localAddress;

    private DtlsServerHandler dtlsServerHandler = null;
    private String keyStoreLocation;
    private String trustStoreLocation;

    public DtlsServerConnector(InetSocketAddress localAddress, String keyStoreLocation, String trustStoreLocation, int retransmissionTimeout) {
        this.retransmissionTimeout = retransmissionTimeout;
        this.localAddress = localAddress;
        this.keyStoreLocation = keyStoreLocation;
        this.trustStoreLocation = trustStoreLocation;

        System.out.println("DtlsServerConnector started");
    }

    @Override
    public void start() throws IOException {
        if (rawDataChannel == null) {
            throw new IllegalStateException("Cannot start without message handler.");
        }
        dtlsServerHandler = new DtlsServerHandler(localAddress);
    }

    @Override
    public void stop() {
        dtlsServerHandler.stop();
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void send(RawData msg) {
        if (dtlsServerHandler != null) {
            dtlsServerHandler.sendRawData(msg);
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
        return localAddress;
    }

    private class DtlsServerHandler {

        private static final String TRUST_STORE_PASSWORD = "rootPass";
        private static final String KEY_STORE_PASSWORD = "endPass";
        private DtlsServerImpl connector;
        private InetSocketAddress localAddress;

        public DtlsServerHandler(InetSocketAddress localAddress) {
            this.localAddress = localAddress;
            try {
                connector = new DtlsServerImpl(getDTLSBuilder(localAddress));
                start();
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        public final void start() {
            try {
                connector.start();
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        public final void stop() {
            connector.stop();
        }

        public void sendRawData(RawData msg) {
            connector.sendData(msg);
        }

        public InetSocketAddress getLocalAddress() {
            return localAddress;
        }

        private Builder getDTLSBuilder(InetSocketAddress localAddress) {
            try {
                InMemoryPskStore pskStore = new InMemoryPskStore();
                // put in the PSK store the default identity/psk for tinydtls tests
                pskStore.setKey("Client_identity", "secretPSK".getBytes());

                // load the key store
                KeyStore keyStore = KeyStore.getInstance("JKS");
                InputStream in = getClass().getClassLoader().getResourceAsStream(keyStoreLocation);
                keyStore.load(in, KEY_STORE_PASSWORD.toCharArray());
                in.close();

                // load the trust store
                KeyStore trustStore = KeyStore.getInstance("JKS");
                InputStream inTrust = getClass().getClassLoader().getResourceAsStream(trustStoreLocation);
                trustStore.load(inTrust, TRUST_STORE_PASSWORD.toCharArray());

                // You can load multiple certificates if needed
                Certificate[] trustedCertificates = new Certificate[1];
                trustedCertificates[0] = trustStore.getCertificate("root");

                Builder builder = new Builder(localAddress);
                builder.setPskStore(pskStore);
                builder.setIdentity((PrivateKey) keyStore.getKey("server", KEY_STORE_PASSWORD.toCharArray()),
                        keyStore.getCertificateChain("server"), true);
                builder.setTrustStore(trustedCertificates);

                builder.setRetransmissionTimeout(retransmissionTimeout);
                return builder;
            } catch (Exception e) {
                System.err.println(e);
            }
            return null;
        }
    }

    private class DtlsServerImpl {

        private DTLSConnector dtlsConnector;

        public DtlsServerImpl(Builder builder) throws IOException {
            if (builder != null) {
                dtlsConnector = new DTLSConnector(builder.build());

                dtlsConnector.setRawDataReceiver(new RawDataChannel() {

                    @Override
                    public void receiveData(RawData raw) {
                        // System.out.println("DtlsServerImpl received: " + raw.getInetSocketAddress().toString());
                        rawDataChannel.receiveData(raw);
                    }
                });

            } else {
                System.out.println("DtlsServerImpl:: empty DTLSbuilder");
            }
        }

        public void start() throws IOException {
            dtlsConnector.start();
        }

        public void stop() {
            dtlsConnector.stop();
        }

        public void sendData(RawData msg) {
            dtlsConnector.send(msg);
            //System.out.println("DtlsServerImpl:: sent raw Msg");
        }

    }

}
