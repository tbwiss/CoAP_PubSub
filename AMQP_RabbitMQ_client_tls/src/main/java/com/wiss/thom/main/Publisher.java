/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.wiss.thom.output.MeasurementWriter;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author thomas
 */
public class Publisher {

    private MeasurementWriter writer;
    private String queueName;
    private String hostAdr;
    private String exchangeName;

    public Publisher(MeasurementWriter writer, String queueName, String exchangeName, String hostAdr) {
        this.writer = writer;
        this.queueName = queueName;
        this.hostAdr = hostAdr;
        this.exchangeName = exchangeName;
    }

    public void execute() {
        System.out.println("== START AMQP PUBLISHER ==");

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(this.hostAdr);
            factory.setUsername("admin");
            factory.setPassword("admin");
            factory.setPort(5671);
            //factory.useSslProtocol();
            factory.useSslProtocol(loadSSLContext());

            //factory.useSslProtocol("TLSv1.2");
            //factory.useSslProtocol(SslUtil.getSSLContext("/home/thomas/Documents/certs_v2_3/ca.crt", "/home/thomas/Documents/certs_v2_3/client.crt", "/home/thomas/Documents/certs_v2_3/client.key", "miun"));
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(this.queueName, false, false, false, null);
            channel.basicPublish("", queueName, null, "1".getBytes());
            
            channel.close();
            connection.close();
            System.out.println("== STOP AMQP .. ==");

            /*
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(this.queueName, false, false, false, null);

            for (int i = 0; i < 2000; i++) {
                //String message = RandomData.getRandomData(400);
                writer.writeContent(String.valueOf(i + "," + System.currentTimeMillis()));
                channel.basicPublish("", queueName, null, String.valueOf(i).getBytes());

                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                }

            }
            channel.close();
            connection.close();*/
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public SSLContext loadSSLContext() {
        try {
            char[] keyPassphrase = "MySecretPassword".toCharArray();
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream("/home/pi/Documents/certs/client/keycert.p12"), keyPassphrase);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, "MySecretPassword".toCharArray());

            char[] trustPassphrase = "rabbitstore".toCharArray();
            KeyStore tks = KeyStore.getInstance("JKS");
            tks.load(new FileInputStream("/home/pi/Documents/certs/rabbitstore"), trustPassphrase);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(tks);

            SSLContext c = SSLContext.getInstance("TLSv1.2");
            c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            /*
            // Get SSL params and set all necessary things!
            SSLParameters params = c.getDefaultSSLParameters();
            System.out.println(params.getUseCipherSuitesOrder());
            
                    
            String[] cSuites = params.getCipherSuites();
            for (int i = 0; i < cSuites.length; i++) {
                System.out.println(cSuites[i]);
            }
            

            String[] protocols = params.getProtocols();
            for (int i = 0; i < protocols.length; i++) {
                System.out.println(protocols[i]);
            }
            System.out.println("---------------------------");

            params.setUseCipherSuitesOrder(true);
            String[] ciphers = {
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384" 
            };
            /*String[] ciphers = {
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA"
            };*/
            /**
             * Set the ciphers on the server side!
             *
             * https://www.rabbitmq.com/ssl.html
             */
            /*
            params.setCipherSuites(ciphers);
            
            SSLContext.setDefault(c);
            SSLContext con = SSLContext.getDefault();
            
            String[] defaultSuites = con.getSocketFactory().getDefaultCipherSuites();
            for (int i = 0; i < defaultSuites.length; i++) {
                System.out.println(defaultSuites[i]);
            }
            System.out.println(con.getDefaultSSLParameters().getUseCipherSuitesOrder());
            
             */
            return c;
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }
}
