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
    //      /etc/mosquitto/c.conf/ssl.conf
    //
    //      
    //
    //      sudo tc qdisc add dev wlan0 root netem loss 5%
    //
    //
    //      sudo tcpdump -w mqtt_sec_test_0.pcap -i wlan0 port 8883
    //
    //
    //
    //  sudo tc qdisc add dev wlan0 handle 1: root htb default 11
    //  sudo tc class add dev wlan0 parent 1: classid 1:1 htb rate 5kbps
    //  sudo tc class add dev wlan0 parent 1:1 classid 1:11 htb rate 5kbps
    //  
    //  sudo tc qdisc delete dev wlan0 root
    //////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //
    //      sudo tcpdump -w mqtt_sec_com_overhead_loss_tls_0_allinone_0.pcap -i wlan0 port 8883
    //
    //      sudo tcpdump -w mqtt_sec_consum_setuponly_0.pcap -i wlan0 port 9883
    //
    //
    //
    private static final String FILEPATH = "/home/pi/MBR/output_avpr3/resptime_plain/";
    private static final String FILENAME = "mqtt_sec_resptime_plain_inclsetup_ssl_test.txt";
    private static final String BROKER_ADDR = "ssl://192.168.2.45:8883"; // ** forcibly disconnenct!

    public static void main(String[] args) throws MqttException {

        try {
            MeasurementWriter writer = new MeasurementWriter(FILEPATH + FILENAME);
            //HiveSubscriber sub = new HiveSubscriber(BROKER_ADDR, writer);
            //sub.run();

            HivePublisher pub = new HivePublisher(BROKER_ADDR, writer);
            pub.run();

            /*
            for (int s = 0; s < 6; s++) {
                pub = new HivePublisher(BROKER_ADDR, writer);
                pub.run();

                System.out.println(s);
                Thread.sleep(5000);
            }*/
        } catch (Exception e) {
            System.err.println(e);
        }

    }
}
