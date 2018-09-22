/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

import com.wiss.thom.output.MeasurementWriter;

/**
 *
 * @author thomas
 */
public class Main {

    //
    //      sudo service rabbitmq-server start
    //
    //      Web GUI for management/permission rabbitmq-broker: http://192.168.2.45:15672/ 
    //      Login: admin  ;  pw: admin
    //
    //
    //      sudo tc qdisc add dev wlan0 root netem loss 5%
    //      sudo tc qdisc change dev wlan0 root netem loss 10%
    //      sudo tc qdisc change dev wlan0 root netem loss 25%
    //
    //      sudo tc qdisc show
    //
    //      sudo tc qdisc delete dev wlan0 root
    //
    //
    //      sudo tc qdisc add dev wlan0 handle 1: root htb default 11
    //      sudo tc class add dev wlan0 parent 1: classid 1:1 htb rate 100kbps
    //      sudo tc class add dev wlan0 parent 1:1 classid 1:11 htb rate 100kbps
    //  
    //      sudo tc qdisc delete dev wlan0 root
    //
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //      sudo tcpdump -w amqp_sec_consum_setuponly_0.pcap -i wlan0 port 5671
    //
    //
    //
    //      For CoAP PubSub:
    //      sudo tcpdump -w coap_nosec_com_overhead_loss_0_tls_allinone_0.pcap -i wlan0 port 5683
    //
    //      
    //
    //      sudo tcpdump -w coap_nosec_com_overhead_loss_0_wss_allinone_0.pcap -i wlan0 port 5683
    //
    //      sudo tcpdump -w coap_nosec_com_overhead_cap_100_dtls_allinone_0.pcap -i wlan0 port 5683
    //
    //
    //
    private static final String FILEPATH = "/home/pi/MBR/output_avpr3/resptime_plain/";
    private static final String FILENAME = "amqp_sec_resptime_plain_inclsetup_tls_test.txt";

    private static final String QUEUE_NAME = "queue12";
    private static final String EXCHANGE_NAME = "logs7";

    private static final String BROKER_ADDRESS = "192.168.2.45";

    public static void main(String[] args) {
        MeasurementWriter writer = new MeasurementWriter(FILEPATH + FILENAME);

        //Subscriber sub = new Subscriber(writer, QUEUE_NAME, EXCHANGE_NAME, BROKER_ADDRESS);
        //sub.execute();

        Publisher pub = new Publisher(writer, QUEUE_NAME, EXCHANGE_NAME, BROKER_ADDRESS);
        pub.execute();

        /*
        for (int s = 0; s < 6; s++) {
            pub = new Publisher(writer, QUEUE_NAME, EXCHANGE_NAME, BROKER_ADDRESS);
            pub.execute();

            System.out.println(s);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }*/
    }

}
