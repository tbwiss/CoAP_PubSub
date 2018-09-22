package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Subscriber {

    private MeasurementWriter writer;
    private String brokerAddr;

    public Subscriber(String brokerAddr, MeasurementWriter writer) {
        this.writer = writer;
        this.brokerAddr = brokerAddr;
    }

    public void run() throws MqttException {

        System.out.println("== START SUBSCRIBER SSL ==");

        try {
            MqttClient client = new MqttClient(this.brokerAddr, MqttClient.generateClientId(), null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setConnectionTimeout(60);
            options.setKeepAliveInterval(60);
            options.setSocketFactory(SslUtil.getSocketFactory("/home/pi/Downloads/certs_v2/ca.crt", "/home/pi/Downloads/certs_v2/client.crt", "/home/pi/Downloads/certs_v2/client.key", "miun"));
            System.out.println("Sub: options done, try to connect...");

            client.setCallback(new SimpleMqttCallBack(writer));
            client.connect(options);

            client.subscribe("iot_data");

        } catch (Exception e) {
            System.err.println(e);
        }
        System.out.println("== END SUBSCRIBER SSL ==");
    }

}
