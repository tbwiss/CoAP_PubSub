package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SimpleMqttCallBack implements MqttCallback {
    
    private MeasurementWriter writer;
    
    public SimpleMqttCallBack(MeasurementWriter writer){
        this.writer = writer;
    }

    public void connectionLost(Throwable throwable) {
        System.out.println("Connection to MQTT broker lost!");
    }

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        if (!mqttMessage.isDuplicate()) {
            long current = System.currentTimeMillis();
            writer.writeContent(new String(mqttMessage.getPayload()) + "," + String.valueOf(current));
            
            System.out.println("Message received:\t" + new String(mqttMessage.getPayload()));
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        try {
            System.out.println("delivery Completed: \t" + iMqttDeliveryToken.getMessage().toString());
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
