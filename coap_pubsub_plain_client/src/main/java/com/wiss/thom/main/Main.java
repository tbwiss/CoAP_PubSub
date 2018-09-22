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
    //  sudo service rabbitmq-server start
    //
    //
    //      sudo tcpdump -w amqpt_nosec_responsetime_??_1.pcap -i wlan0 tcp 'port ????'
    //
    //      sudo tc qdisc add dev wlan0 root netem loss 25%
    private static final String FILEPATH = "/home/thomas/demoOutput/";
    private static final String FILENAME = "amqp_nosec_responsetime_1.txt";

    private static String hostIP = "127.0.0.1";

    public static void main(String[] args) {
        MeasurementWriter writer = new MeasurementWriter(FILEPATH + FILENAME);
        //Publisher pub = new Publisher(writer, hostIP);
        //Subscriber sub = new Subscriber(writer, hostIP);
        //pub.setupBaseTopic();
        //pub.createTopic();
        PubSub pubSub = new PubSub(writer, hostIP);
        pubSub.setupBaseTopic();
        pubSub.createTopic();
        for(int k = 0; k < 200; k++){
            writer.writeContent(String.valueOf(k) + "," + System.currentTimeMillis());
            pubSub.subscribeWithCoapHandler();
            pubSub.publishNbrOfData(5);
            writer.writeContent(String.valueOf(k) + "," + System.currentTimeMillis());
        }
    }

}
