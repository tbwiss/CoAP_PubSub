/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapPubSubClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.config.NetworkConfig;

/**
 *
 * @author thomas
 */
public class Subscriber {

    private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
    private CoapPubSubClient client;
    private final String BASE_URI;
    private MeasurementWriter writer;

    public Subscriber(MeasurementWriter writer, String hostIP) {
        this.writer = writer;
        client = new CoapPubSubClient();
        BASE_URI = "coap://" + hostIP + ":" + COAP_PORT + "/ps/";
        client.setURI(BASE_URI);
    }

    public void subscribeWithCoapHandler() {
        client.setURI(BASE_URI + "temperature/devRT45");

        // subscribe to topic "/ps/temperature/devRT45"
        client.subscribe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                System.out.println("ExampleSubscribeClient: subCoapHandler from devRT45, content is '" + response.getResponseText() + "'");
            }

            @Override
            public void onError() {
                System.err.println("ExampleSubscribeClient: Error in subscribeToDevRT45coapHandler");
            }

        }, MediaTypeRegistry.TEXT_PLAIN);
    }

}
