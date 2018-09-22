package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Subscriber {

    private final MeasurementWriter writer;
    private final String hostAdr;

    public Subscriber(MeasurementWriter writer, String hostAdr) {
        this.writer = writer;
        this.hostAdr = hostAdr;
    }

    public void run() throws MqttException {

        System.out.println("== START SUBSCRIBER ==");

        String address = "tcp://"  + this.hostAdr;
        MqttClient client = new MqttClient(address, MqttClient.generateClientId());
        client.setCallback(new SimpleMqttCallBack(writer));
        client.connect();

        client.subscribe("iot_data_two");
        System.out.println("Waiting for content...");
    }

}
