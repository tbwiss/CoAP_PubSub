/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.elements.wss;

import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author thomas
 */
public class SslUtil {
    
    public SslUtil(){
    }
    
    private static final String STORETYPE = "JKS";
    private static final String KEY_STORE_PASSWORD = "endPass";

    public SSLContext getSSLContext(String keyStoreLocation) {
        try {
            KeyStore ks = KeyStore.getInstance(STORETYPE);
            InputStream in = getClass().getClassLoader().getResourceAsStream(keyStoreLocation);
            ks.load(in, KEY_STORE_PASSWORD.toCharArray());
            in.close();

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

            kmf.init(ks, KEY_STORE_PASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
