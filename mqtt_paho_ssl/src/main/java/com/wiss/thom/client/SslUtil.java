/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.client;

/**
 *
 * @author thomas
 */
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;

import org.bouncycastle.jce.provider.*;
import org.bouncycastle.openssl.*;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

public class SslUtil {

    static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile,
            final String password) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        PEMParser reader = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
        X509Certificate caCert = new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) reader.readObject());
        reader.close();

        // load client certificate
        reader = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(crtFile)))));
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) reader.readObject());
        reader.close();

        // load client private key
        reader = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(keyFile)))));
        KeyPair key = loadKey(keyFile, password);
        reader.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        //SSLContext context = SSLContext.getInstance("TLSv1");
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        /*
        System.out.println(context.getProvider().getInfo());
        System.out.println(context.getProvider().getName());
        String[] protocols = context.getDefaultSSLParameters().getCipherSuites();
        for(String s : protocols){
            System.out.println(s);
        }
         */
        
        /*
        String[] protocols = context.getSocketFactory().getDefaultCipherSuites();
        for(String s : protocols){
            System.out.println(s);
        }*/
        
        return context.getSocketFactory();
    }

    private static KeyPair loadKey(String keyFile, String password) {
        KeyPair kp = null;
        try {
            File privateKeyFile = new File(keyFile);
            PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile));
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().setProvider("BC").build(password.toCharArray());
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            Object object = pemParser.readObject();

            if (object instanceof PEMEncryptedKeyPair) {
                System.out.println("Encrypted key - we will use provided password");
                kp = converter.setProvider("BC").getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
            } else {
                System.out.println("Unencrypted key - no password needed");
                kp = converter.setProvider("BC").getKeyPair((PEMKeyPair) object);
            }

        } catch (IOException e) {
            System.err.println(e);
        }
        return kp;
    }
}
