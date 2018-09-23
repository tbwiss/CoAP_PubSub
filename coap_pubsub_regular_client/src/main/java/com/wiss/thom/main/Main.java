/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

import com.wiss.thom.output.MeasurementWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CaliforniumLogger;

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
    //
    //
    private static final String FILEPATH = "/home/pi/MBR/output_avpr2/resptime_plain/";
    private static final String FILENAME = "coap_nosec_responsetime_no_setup_sctp_test.txt";

    private static String brokerIP = "192.168.2.45";

    private static String connectorChoice = "tls";

    public static void main(String[] args) {
        try {
            CaliforniumLogger.initialize();
            CaliforniumLogger.setLevel(Level.WARNING);  // ----- Change to appropriate level! -----

            MeasurementWriter writer = new MeasurementWriter(FILEPATH + FILENAME);

            Publisher pub = new Publisher(connectorChoice, writer, brokerIP, 30000);
            Thread.sleep(1000);
            pub.createTopic();
            Thread.sleep(15000);
            pub.subscribeWithCoapHandlerNotNumber();
            Thread.sleep(10000);
            pub.publishAnAmount();

            // ------------------------------------------------------
            /* THIS works for DTLS, yes very high timeout times due to loading of certificates and such
                .... Cannot parse X.509 certificate chain provided by peer;  Normal output, works anyway...
            
            Publisher pub = new Publisher(connectorChoice, writer, brokerIP, 30000);
            Thread.sleep(2000);
            pub.createTopic();
            Thread.sleep(30000);
            pub.subscribeWithCoapHandlerNotNumber();
            Thread.sleep(15000);
            pub.publishAnAmount();
             */
            // ------------------------------------------------------
            /*
            THIS works for TLS and WSS, the timeout times need to be that big!
            ...... first attempet usually fails though...
            Publisher pub = new Publisher(connectorChoice, writer, brokerIP, 20000);
            Thread.sleep(1000);
            pub.createTopic();
            Thread.sleep(10000);
            pub.subscribeWithCoapHandlerNotNumber();
            Thread.sleep(10000);
            pub.publishAnAmount();*/
            // ------------------------------------------------------
            /*
            THIS works for TCP and WS (timeout in TcpConnectorThing should be 1000ms at least), and for SCTP add more to the timeout time
            ...... first attempet usually fails though...
                pub.createTopic();
                Thread.sleep(5000);
                pub.subscribeWithCoapHandlerNotNumber();
                Thread.sleep(5000);
                pub.publishAnAmount();
                Thread.sleep(200);
                System.out.println(i);
                pub.unSubscribeDev45();
                pub.stopEndpoint();
                Thread.sleep(5000);
             */
            // ------------------------------------------------------
            /*
             //Subscriber sub = new Subscriber(connectorChoice, writer, brokerIP);
            
             //pub.createTopic();
                pub.publishOne();

                Thread.sleep(5000);

                // INOFOOO:  The socket is closed, not kept open..
                pub.subscribeWithCoapHandler();

                Thread.sleep(5000);

                pub.publishAnAmount();
             */
            //pub.publishAnAmountWithResponse();
            //pub.publishOne();
            /*
            MeasurementWriter writer = new MeasurementWriter(FILEPATH + FILENAME);
            PubSub pubSub = new PubSub(writer, brokerIP);
            //pubSub.setupBaseTopic();
            //pubSub.createTopic();
            
            pubSub.sleepWakeDrill();
             */
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
