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
public class HiveSubscriber {

    private MeasurementWriter writer;
    private String brokerAddr;

    public HiveSubscriber(String brokerAddr, MeasurementWriter writer) {
        this.writer = writer;
        this.brokerAddr = brokerAddr;
    }

    public void run() throws MqttException {

        System.out.println("== START SUBSCRIBER SSL/WSS==");

        try {
            MqttClient client = new MqttClient(this.brokerAddr, MqttClient.generateClientId(), new MemoryPersistence());

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setSocketFactory(SslFactoryUtil.getTruststoreFactory());
            System.out.println("Sub: options done, try to connect...");

            client.setCallback(new SimpleMqttCallBack(writer));
            client.connect(mqttConnectOptions);

            client.subscribe("iot_sec");

        } catch (Exception e) {
            System.err.println(e);
        }

        System.out.println("== END SUBSCRIBER SSL/WSS==");

    }

}
