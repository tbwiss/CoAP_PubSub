/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

import com.wiss.thom.elements.dtls.DtlsClientConnector;
import com.wiss.thom.elements.sctp.SctpClientConnector;
import com.wiss.thom.elements.ws.WsClientConnector;
import com.wiss.thom.elements.wss.WssClientConnector;
import com.wiss.thom.output.MeasurementWriter;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapPubSubClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.tcp.TcpClientConnector;
import org.eclipse.californium.elements.tcp.TlsClientConnector;

/**
 *
 * @author thomas
 */
public class Publisher {

    private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
    private CoapPubSubClient client;
    private final String BASE_URI;
    private String currentURI;
    private MeasurementWriter writer;

    public Publisher(String connectorChoice, MeasurementWriter writer, String hostIP, int timeout) {
        this.writer = writer;
        client = new CoapPubSubClient();
        NetworkConfig config = NetworkConfig.getStandard();

        Connector connector = null;
        switch (connectorChoice) {
            case "udp":
                break;
            case "tcp":
                connector = new TcpClientConnector(1, 1000, 1000);
                break;
            case "ws":
                connector = new WsClientConnector(100);
                break;
            case "sctp":
                connector = new SctpClientConnector(100);
                break;
            case "dtls":
                connector = new DtlsClientConnector("certs/keyStore.jks", "certs/trustStore.jks", 2000);
                break;
            case "wss":
                connector = new WssClientConnector("certs/keyStore.jks", 2000);
                break;
            case "tls":
                connector = new TlsClientConnector(new SSLUtil("endPass", "keyStore.jks").getSSLContext(), 1, 2000, 2000);
                break;
            default:
                break;
        }

        CoapEndpoint endpoint = null;
        if (connector == null) {
            endpoint = new CoapEndpoint(config);
        } else {
            endpoint = new CoapEndpoint(connector, config);
        }

        client.setEndpoint(endpoint);

        BASE_URI = "coap://" + hostIP + ":" + COAP_PORT + "/ps/";
        client.setTimeout(timeout);
        client.useCONs();
    }

    public void createTopic() {
        // Create a new sub-topic "devRT45" for plain text content
        client.setURI(BASE_URI);

        client.create(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                System.out.println("ExamplePublishClient: responseCode to create sub-topic 'devRT45' is " + response.getCode());
            }

            @Override
            public void onError() {
                System.err.println("ExamplePublishClient: Error in createDevRT45SenosorData");
            }

        }, "devRT45", MediaTypeRegistry.TEXT_PLAIN);
    }

    public void publishAnAmount() {
        // Publish a random content to /ps/devRT45
        client.setURI(BASE_URI + "devRT45");

        for (int i = 0; i < 200; i++) {
            try {
                String incrementNumber = String.valueOf(i);
                writer.writeContent(incrementNumber + ", " + System.currentTimeMillis());
                client.publish(incrementNumber, MediaTypeRegistry.TEXT_PLAIN);
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
        System.out.println("Pub: done");
    }

    public void publishAnAmountWithResponse() {
        // Publish a random content to /ps/devRT45
        client.setURI(BASE_URI + "devRT45");

        for (int i = 0; i < 2000; i++) {
            try {
                String incrementNumber = String.valueOf(i);
                writer.writeContent(incrementNumber + ", " + System.currentTimeMillis());
                CoapResponse resp = client.publish(incrementNumber, MediaTypeRegistry.TEXT_PLAIN);
                writer.writeContent(incrementNumber + ", " + System.currentTimeMillis());
                System.out.println("ExamplePublishClient: responseCode to pub sub-topic 'devRT45' is " + resp.getCode());
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
        System.out.println("Pub: done");
    }

    public void publishOne() {
        // Publish a random content to /ps/devRT45
        client.setURI(BASE_URI + "devRT45");

        for (int i = 0; i < 1; i++) {
            try {
                String incrementNumber = String.valueOf(i);
                //writer.writeContent(incrementNumber + ", " + System.currentTimeMillis());
                System.out.println("Pub: pubOne, code:" + client.publish(incrementNumber, MediaTypeRegistry.TEXT_PLAIN).getCode());
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
        System.out.println("Pub: done");
    }

    public void subscribeWithCoapHandler() {
        client.setURI(BASE_URI + "devRT45");

        // subscribe to topic "/ps/devRT45"
        client.subscribe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                long current = System.currentTimeMillis();
                String payload = response.getResponseText();
                int messageValue = 0;
                try {
                    messageValue = Integer.parseInt(payload);
                } catch (NumberFormatException e) {
                    System.err.println(e);
                }
                writer.writeContent(String.valueOf(messageValue) + ", " + current);
                System.out.println("Sub: '" + response.getResponseText() + "'");
            }

            @Override
            public void onError() {
                System.err.println("ExampleSubscribeClient: Error in subscribeToDevRT45coapHandler");
            }

        }, MediaTypeRegistry.TEXT_PLAIN);
    }

    public void subscribeWithCoapHandlerNotNumber() {
        client.setURI(BASE_URI + "devRT45/");

        // subscribe to topic "/ps/devRT45"
        client.subscribe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                System.out.println("Sub: '" + response.getResponseText() + "'");
            }

            @Override
            public void onError() {
                System.err.println("ExampleSubscribeClient: Error in subscribeToDevRT45coapHandler");
            }

        }, MediaTypeRegistry.TEXT_PLAIN);
    }

    public void unSubscribeDev45() {
        client.setURI(BASE_URI + "devRT45/");

        // unSubscribe to topic "/ps/devRT45"
        System.out.println("Sub: unsubscribe dev45, code: " + client.unsubscribe().getCode());
    }

    public final void stopEndpoint() {
        client.stopEndpoint();
    }

    
    
    
    
    
    /**
     *  All in one to avoid timeout times with secure connections!
     */
    public void allInOneMethod() {
        client.setURI(BASE_URI);

        client.create(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                System.out.println("ExamplePublishClient: responseCode to create sub-topic 'devRT45' is " + response.getCode());
                subscribeAllInOneMthod();
            }

            @Override
            public void onError() {
                System.err.println("ExamplePublishClient: Error in createDevRT45SenosorData");
            }

        }, "devRT45", MediaTypeRegistry.TEXT_PLAIN);
    }

    public void subscribeAllInOneMthod() {
        client.setURI(BASE_URI + "devRT45/");

        // subscribe to topic "/ps/devRT45"
        client.subscribe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                System.out.println("Sub: '" + response.getResponseText() + "'");
                publishAnAmount();
            }

            @Override
            public void onError() {
                System.err.println("ExampleSubscribeClient: Error in subscribeToDevRT45coapHandler");
            }

        }, MediaTypeRegistry.TEXT_PLAIN);

    }
}
