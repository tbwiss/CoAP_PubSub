package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Publisher {

    private MeasurementWriter writer;
    private String brokerAddr;

    public Publisher(String brokerAddr, MeasurementWriter writer) {
        this.writer = writer;
        this.brokerAddr = brokerAddr;
    }

    public void run() throws MqttException {

        System.out.println("== START PUBLISHER SSL==");
        
        try {
            MqttClient client = new MqttClient(this.brokerAddr, MqttClient.generateClientId(), null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setConnectionTimeout(60);
            options.setKeepAliveInterval(60);
            options.setSocketFactory(SslUtil.getSocketFactory("/home/pi/Downloads/certs_v2/ca.crt", "/home/pi/Downloads/certs_v2/client.crt", "/home/pi/Downloads/certs_v2/client.key", "miun"));
            System.out.println("Pub: options done, try to connect...");

            client.connect(options);
            System.out.println("Connect done");
            
            //MqttMessage message = new MqttMessage();
            //message.setPayload(messageString.getBytes());
            //client.publish("iot_data", message);

            
            for (int i = 0; i < 100; i++) {
                writer.writeContent( String.valueOf(i + "," + System.currentTimeMillis()) );
                client.publish("iot_data", RandomData.getRandomData(400).getBytes(), 1, false);
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
            }*/
            //System.out.println("\tMessage '" + messageString + "' to 'iot_data'");
            // client.disconnect();
        } catch (Exception e) {
            System.err.println(e);
        }

        System.out.println("== END PUBLISHER SSL==");

    }

}
