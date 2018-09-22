/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author thomas
 */
public class PubSub {

    private final MeasurementWriter writer;
    private final String hostAdr;

    public PubSub(MeasurementWriter writer, String hostAdr) {
        this.writer = writer;
        this.hostAdr = hostAdr;
    }

    public void run() throws MqttException {

        System.out.println("== START PUBSUB ==");

        try {
            String address = "tcp://" + this.hostAdr;

            int QoS = 1;

            MqttClient pub = new MqttClient(address, MqttClient.generateClientId());
            MqttClient sub = new MqttClient(address, MqttClient.generateClientId());
            sub.setCallback(new SimpleMqttCallBackTwo());
            
            for (int k = 0; k < 200; k++) {
                writer.writeContent(String.valueOf(k) + "," + System.currentTimeMillis());
                sub.connect();
                sub.subscribe("iot_data_three");
                
                pub.connect();
                
                for (int i = 0; i < 50; i++) {
                    pub.publish("iot_data_three", String.valueOf(i).getBytes(), QoS, false);
                    
                    //Thread.sleep(50);
                }
                pub.disconnect();
                sub.disconnect();
                writer.writeContent(String.valueOf(k) + "," + System.currentTimeMillis());
                System.out.println("pub cycle: " + k);
            }

            //pub.disconnect();

            /*
            client.connect();
            int QoS = 1;
            
            for (int i = 0; i < 1; i++) {
                writer.writeContent(String.valueOf(i) + "," + System.currentTimeMillis());
                client.publish("iot_data_two", String.valueOf(i).getBytes(), QoS, false);
                System.out.println("pub of data " + i);
                Thread.sleep(50);
            }
            client.disconnect();
             */
 /*
            for (int a = 0; a < 200; a++) {
                for (int i = 0; i < 5; i++) {
                    writer.writeContent(String.valueOf(i) + "," + System.currentTimeMillis());
                    client.connect();
                    //MqttMessage message = new MqttMessage();
                    //message.setPayload(messageString.getBytes());
                    int QoS = 1;

                    client.publish("iot_data_two", String.valueOf(i).getBytes(), QoS, false);

                    client.disconnect();

                    Thread.sleep(30);
                }
                Thread.sleep(50);
                System.out.println(a);
            }*/
            //client.disconnect();
        } catch (Exception e) {
            System.err.println(e);
        }

        System.out.println("== END PUBSUB ==");
    }

}
