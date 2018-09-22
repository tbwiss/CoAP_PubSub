/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
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

        System.out.println("== START PUBLISHER SSL==");

        try {
            MqttClient client = new MqttClient(this.brokerAddr, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

            mqttConnectOptions.setSocketFactory(SslFactoryUtil.getTruststoreFactory());
            System.out.println("Pub: options done, try to connect...");

            
            client.connect(mqttConnectOptions);
            
            client.publish("iot_data", "1".getBytes(), 1, false);
            
            client.disconnect();
            System.out.println("== STOP MQTT .. ==");
            

            /*
            client.connect(mqttConnectOptions);
            System.out.println("Pub: Connect done");

            for (int i = 0; i < 2000; i++) {
                writer.writeContent(String.valueOf(i + "," + System.currentTimeMillis()));
                client.publish("iot_data", String.valueOf(i).getBytes(), 1, false);
                Thread.sleep(70);
            }

            client.disconnect();
            //client.disconnectForcibly();*/
        } catch (Exception e) {
            System.err.println(e);
        }

        System.out.println("== END PUBLISHER SSL==");

    }
}
