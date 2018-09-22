package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Publisher {

    private final MeasurementWriter writer;
    private final String hostAdr;

    public Publisher(MeasurementWriter writer, String hostAdr) {
        this.writer = writer;
        this.hostAdr = hostAdr;
    }

    public void run() throws MqttException {

        System.out.println("== START PUBLISHER ==");

        try {
            String address = "tcp://" + this.hostAdr;
            MqttClient client = new MqttClient(address, MqttClient.generateClientId());

            client.connect();
            int QoS = 1;
            
            for (int i = 0; i < 1; i++) {
                writer.writeContent(String.valueOf(i) + "," + System.currentTimeMillis());
                client.publish("iot_data_two", String.valueOf(i).getBytes(), QoS, false);
                System.out.println("pub of data " + i);
                Thread.sleep(50);
            }
            client.disconnect();
            
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

        System.out.println("== END PUBLISHER ==");
    }

}
