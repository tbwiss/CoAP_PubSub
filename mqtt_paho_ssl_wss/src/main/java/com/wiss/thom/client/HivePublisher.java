/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author thomas
 */
public class HivePublisher {

    private MeasurementWriter writer;
    private String brokerAddr;

    public HivePublisher(String brokerAddr, MeasurementWriter writer) {
        this.writer = writer;
        this.brokerAddr = brokerAddr;
    }

    public void run() throws MqttException {

        System.out.println("== START PUBLISHER SSL/WSS==");

        try {
            MqttClient client = new MqttClient(this.brokerAddr, MqttClient.generateClientId(), new MemoryPersistence());

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setSocketFactory(SslFactoryUtil.getTruststoreFactory());

            client.connect(mqttConnectOptions);

            for (int i = 0; i < 100; i++) {
                writer.writeContent(String.valueOf(i + "," + System.currentTimeMillis()));
                client.publish("iot_sec", String.valueOf(i).getBytes(), 1, false);
                Thread.sleep(120);
            }
            /*
            
            for (int i = 0; i <= 40; i++) {
                long before = System.currentTimeMillis();
                client.connect(options);
                long difference = System.currentTimeMillis() - before;
                System.out.println("Difference: " + difference);
                writer.writeContent( String.valueOf(difference) );
                client.disconnect();
                Thread.sleep(100);
            }
            
             */

            client.disconnect();

        } catch (Exception e) {
            System.err.println(e);
        }

        System.out.println("== END PUBLISHER SSL/WSS==");

    }
}
