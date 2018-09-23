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
public class Subscriber {

    private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
    private CoapPubSubClient client;
    private final String BASE_URI;
    private MeasurementWriter writer;

    public Subscriber(String connectorChoice, MeasurementWriter writer, String hostIP) {
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
                connector = new SctpClientConnector(1000);
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
        client.setURI(BASE_URI);
        client.useCONs();
    }

    public void subscribeWithCoapHandler() {
        client.setURI(BASE_URI + "devRT45/");

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

}
