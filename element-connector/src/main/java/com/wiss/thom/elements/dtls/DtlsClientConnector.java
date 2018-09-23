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
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig.Builder;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;

/**
 *
 * @author thomas
 */
public class DtlsClientConnector implements Connector, DtlsConnector {

    private final static Logger LOGGER = Logger.getLogger(DtlsClientConnector.class.getName());

    private final int retransmissionTimeout;
    private RawDataChannel rawDataChannel;
    private DtlsClientHandler dtlsClientHandler = null;
    private String keyStoreLocation;
    private String trustStoreLocation;

    public DtlsClientConnector(String keyStoreLocation, String trustStoreLocation, int retransmissionTimeout) {
        this.retransmissionTimeout = retransmissionTimeout;
        this.keyStoreLocation = keyStoreLocation;
        this.trustStoreLocation = trustStoreLocation;
    }

    @Override
    public void start() throws IOException {
        if (rawDataChannel == null) {
            throw new IllegalStateException("Cannot start without message handler.");
        }
        dtlsClientHandler = new DtlsClientHandler();
    }

    @Override
    public void stop() {
        dtlsClientHandler.stop();
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void send(RawData msg) {
        if (dtlsClientHandler != null) {
            dtlsClientHandler.sendRawData(msg);
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
     * DtlsClientHandler
     */
    private class DtlsClientHandler {

        private static final String TRUST_STORE_PASSWORD = "rootPass";
        private static final String KEY_STORE_PASSWORD = "endPass";
        private DtlsClientImpl connector;

        public DtlsClientHandler() {
            try {
                connector = new DtlsClientImpl(getDTLSBuilder());
                //start();
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        public final void start() {
            try {
                if (connector != null) {
                    connector.start();
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        public final void stop() {
            if (connector != null) {
                connector.stop();
            }
        }

        public void sendRawData(RawData msg) {
            if (connector != null) {

                connector.sendData(msg);
            }
        }

        private Builder getDTLSBuilder() {
            try {
                InputStream inTrust;
                InputStream in;
                // load key store
                KeyStore keyStore = KeyStore.getInstance("JKS");
                in = getClass().getClassLoader().getResourceAsStream(keyStoreLocation);
                keyStore.load(in, KEY_STORE_PASSWORD.toCharArray());
                in.close();

                // load trust store
                KeyStore trustStore = KeyStore.getInstance("JKS");
                inTrust = getClass().getClassLoader().getResourceAsStream(trustStoreLocation);
                trustStore.load(inTrust, TRUST_STORE_PASSWORD.toCharArray());

                // You can load multiple certificates if needed
                Certificate[] trustedCertificates = new Certificate[1];
                trustedCertificates[0] = trustStore.getCertificate("root");

                Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
                builder.setPskStore(new StaticPskStore("Client_identity", "secretPSK".getBytes()));
                builder.setIdentity((PrivateKey) keyStore.getKey("client", KEY_STORE_PASSWORD.toCharArray()),
                        keyStore.getCertificateChain("client"), true);
                builder.setTrustStore(trustedCertificates);

                //      TLS version: can't set it
                /*
                String[] cipherSuites = {
                    "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                    "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"
                };
                builder.setSupportedCipherSuites(cipherSuites);*/
                builder.setRetransmissionTimeout(retransmissionTimeout);
                return builder;
            } catch (Exception e) {
                System.err.println(e);
            }

            return null;
        }
    }

    private class DtlsClientImpl {

        private DTLSConnector dtlsConnector;

        public DtlsClientImpl(Builder builder) throws IOException {
            if (builder != null) {
                dtlsConnector = new DTLSConnector(builder.build());

                dtlsConnector.setRawDataReceiver(new RawDataChannel() {

                    @Override
                    public void receiveData(RawData raw) {
                        //System.out.println("DtlsClientImpl received: " + raw.getInetSocketAddress().toString());
                        rawDataChannel.receiveData(raw);
                    }
                });
                if (!dtlsConnector.isRunning()) {
                    dtlsConnector.start();
                }
            } else {
                System.out.println("DtlsClientImpl:: empty DTLSbuilder");
            }
        }

        public void start() throws IOException {
            if (dtlsConnector != null) {
                dtlsConnector.start();
            }
        }

        public void stop() {
            if (dtlsConnector.isRunning()) {
                dtlsConnector.stop();
            }
        }

        public void sendData(RawData msg) {
            if (dtlsConnector.isRunning()) {
                dtlsConnector.send(msg);
                //System.out.println("DtlsClientImpl:: sent raw Msg");
            }
        }

    }

}
