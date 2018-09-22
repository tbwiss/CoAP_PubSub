/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.client;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author thomas
 */
public class SslFactoryUtil {
    
     public static SocketFactory getTruststoreFactory() throws Exception {

        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream in = new FileInputStream("/home/pi/Certs/certs_hivemq/mqtt-client-trust-store.jks");
        trustStore.load(in, "MQTTmiun".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslCtx = SSLContext.getInstance("TLSv1.2");
        sslCtx.init(null, tmf.getTrustManagers(), null);
        return sslCtx.getSocketFactory();
    }
    
}
