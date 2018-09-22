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
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.wiss.thom.output.MeasurementWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author thomas
 */
public class Subscriber {

    private MeasurementWriter writer;
    private String queueName;
    private String hostAdr;
    private String exchangeName;

    public Subscriber(MeasurementWriter writer, String queueName, String exchangeName, String hostAdr) {
        this.writer = writer;
        this.queueName = queueName;
        this.hostAdr = hostAdr;
        this.exchangeName = exchangeName;
    }

    public void execute() {

        System.out.println("== START AMQP SUBSCRIBER ==");

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(this.hostAdr);
            factory.setUsername("admin");
            factory.setPassword("admin");
            factory.setPort(5671);
            factory.useSslProtocol(loadSSLContext());

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                        AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    long current = System.currentTimeMillis();
                    String message = new String(body, "UTF-8");
                    int messageValue = 0;
                    try {
                        messageValue = Integer.parseInt(message);
                    } catch (NumberFormatException e) {
                        System.out.println("AMQP Sub: Invalid message value, cannot parse int");
                        messageValue = 0;
                    }
                    writer.writeContent((messageValue + "," + String.valueOf(current)));
                    //writer.writeContent(message + "," + System.currentTimeMillis());
                    System.out.println("AMQP Sub: Received '" + messageValue + "'");
                }
            };
            channel.basicConsume(this.queueName, true, consumer);

            System.out.println("AMQP Sub: Waiting for messages...");

        } catch (IOException | TimeoutException e) {
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
            return c;
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }
}
