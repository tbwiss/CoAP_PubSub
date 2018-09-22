package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Subscriber {
    
     private MeasurementWriter writer;
     private String brokerAddr;
    
    public Subscriber(String brokerAddr, MeasurementWriter writer){
        this.writer = writer;
        this.brokerAddr = brokerAddr;
    }

  public void run() throws MqttException {

    System.out.println("== START SUBSCRIBER ==");

    MqttClient client=new MqttClient(brokerAddr, MqttClient.generateClientId());
    client.setCallback( new SimpleMqttCallBack(writer) );
    client.connect();

    client.subscribe("iot_data");

  }

}
