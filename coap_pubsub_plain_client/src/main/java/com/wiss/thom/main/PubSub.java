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
public class PubSub {

    private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
    private CoapPubSubClient client;
    private final String BASE_URI;
    private String currentURI;
    private MeasurementWriter writer;

    public PubSub(MeasurementWriter writer, String hostIP) {
        this.writer = writer;
        client = new CoapPubSubClient();
        BASE_URI = "coap://" + hostIP + ":" + COAP_PORT + "/ps/";
    }

    public void setupBaseTopic() {

        client.setURI(BASE_URI);

        // Create a new topic "temperature" as link_format 
        CoapResponse respOne = client.create("temperature", MediaTypeRegistry.APPLICATION_LINK_FORMAT);
        System.out.println("ExamplePublishClient: responseCode to create topic 'temperature' is " + respOne.getCode());
        if (respOne.getCode().toString().equals("2.01")) {
            currentURI = client.getURI() + "temperature/";
            client.setURI(currentURI);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void createTopic() {
        // Create a new sub-topic "devRT45" for plain text content
        client.create(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                System.out.println("ExamplePublishClient: responseCode to create sub-topic 'devRT45' is " + response.getCode());
                if (response.getCode().toString().equals("2.01")) {
                    currentURI = client.getURI() + "devRT45/";
                    client.setURI(currentURI);
                } else {
                    this.onError();
                }
            }

            @Override
            public void onError() {
                System.err.println("ExamplePublishClient: Error in createDevRT45SenosorData");
            }

        }, "devRT45", MediaTypeRegistry.TEXT_PLAIN);
    }

    public void publishNbrOfData(int number) {
        // Publish a random content to /ps/temperature/devRT45
        client.setURI(BASE_URI + "temperature/devRT45");

        for (int i = 0; i < number; i++) {
            CoapResponse respOne = client.publish(String.valueOf(number), MediaTypeRegistry.TEXT_PLAIN);
            System.out.println("ExamplePublishClient: responseCode to publish content is " + respOne.getCode().toString());
        }
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
