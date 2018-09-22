/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author thomas
 */
public class SimpleMqttCallBackTwo implements MqttCallback {

    public SimpleMqttCallBackTwo() {
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Connection to MQTT broker lost!");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        if (!mqttMessage.isDuplicate()) {
            System.out.println("Sub: message received:\t" + new String(mqttMessage.getPayload()));
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        try {
            System.out.println("delivery Completed: \t" + iMqttDeliveryToken.getMessage().toString());
        } catch (MqttException e) {
            System.err.println(e);
        }
    }
}
