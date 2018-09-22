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
    //      sudo tcpdump -w amqp_nosec_com_overhead_cap_5_allinone_0.pcap -i wlan0 tcp 'port 5672'
    //
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
    //
    //      For CoAP PubSub:
    //      sudo tcpdump -w coap_nosec_com_overhead_cap_100_udp_allinone_0.pcap -i wlan0 udp 'port 5683'
    //
    //      sudo tcpdump -w coap_nosec_com_overhead_cap_100_tcp_allinone_0.pcap -i wlan0 tcp 'port 5683'
    //
    //      sudo tcpdump -w coap_nosec_com_overhead_cap_100_ws_allinone_0.pcap -i wlan0 tcp 'port 5683'
    //
    //      sudo tcpdump -w coap_nosec_com_overhead_cap_100_sctp_allinone_0.pcap -i wlan0 sctp 'port 5683'
    //
    //
    //
    //  sudo tc qdisc add dev wlan0 handle 1: root htb default 11
    //  sudo tc class add dev wlan0 parent 1: classid 1:1 htb rate 100kbps
    //  sudo tc class add dev wlan0 parent 1:1 classid 1:11 htb rate 100kbps
    //  
    //  sudo tc qdisc delete dev wlan0 root
    //
    //
    private static final String FILEPATH = "/home/pi/MBR/output_avpr2/resptime_plain/";
    private static final String FILENAME = "amqp_nosec_responsetime_no_setup_test.txt";

    private static final String QUEUE_NAME = "queue11";
    private static final String EXCHANGE_NAME = "logs6";

    private static final String BROKER_ADDRESS = "192.168.2.45";

    public static void main(String[] args) {
        MeasurementWriter writer = new MeasurementWriter(FILEPATH + FILENAME);

        Subscriber sub = new Subscriber(writer, QUEUE_NAME, EXCHANGE_NAME, BROKER_ADDRESS);
        sub.execute();

        Publisher pub = null;

        for (int s = 0; s < 6; s++) {
            //PubSub pubSub = new PubSub(writer, QUEUE_NAME, EXCHANGE_NAME, BROKER_ADDRESS);
            pub = new Publisher(writer, QUEUE_NAME, EXCHANGE_NAME, BROKER_ADDRESS);
            pub.execute();
            //pubSub.execute();

            System.out.println(s);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
    }

}
