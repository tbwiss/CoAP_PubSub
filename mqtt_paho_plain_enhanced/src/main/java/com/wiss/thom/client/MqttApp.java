package com.wiss.thom.client;

import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Basic launcher for Publisher and Subscriber
 */
public class MqttApp {

    //
    //      /etc/mosquitto/mosquitto.conf
    //
    //      sudo tcpdump -w mqtt_nosec_scenario_50_1.pcap -i wlan0 tcp 'port 1883'
    //
    //      sudo tc qdisc add dev wlan0 root netem loss 2.5%
    //
    //      sudo tc qdisc change dev wlan0 root netem loss 25%
    //
    //
    //
    //  sudo tc qdisc add dev wlan0 handle 1: root htb default 11
    //  sudo tc class change dev wlan0 parent 1: classid 1:1 htb rate 25kbps
    //  sudo tc class change dev wlan0 parent 1:1 classid 1:11 htb rate 25kbps
    //
    //
    private static final String FILEPATH = "/home/pi/MBR/output_pubsub/scenario/";
    private static final String FILENAME = "mqtt_nosec_scenario_50_1.txt";

    private static final String BROKER_ADR = "192.168.2.45:1883";

    public static void main(String[] args) throws MqttException {

        try {
            MeasurementWriter writer = new MeasurementWriter(FILEPATH + FILENAME);
            //Subscriber sub = new Subscriber(writer, BROKER_ADR);
            //sub.run();

            //Publisher pub = new Publisher(writer, BROKER_ADR);
            // pub.run();
           
            PubSub pubSub = new PubSub(writer, BROKER_ADR);
            pubSub.run();
            
        } catch (MqttException e) {
            System.err.println(e);
        }

    }
}
