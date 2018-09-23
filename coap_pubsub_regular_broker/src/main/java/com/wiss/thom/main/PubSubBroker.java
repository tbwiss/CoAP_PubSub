/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

import com.wiss.thom.elements.dtls.DtlsServerConnector;
import com.wiss.thom.elements.sctp.SctpServerConnector;
import com.wiss.thom.elements.ws.WsServerConnector;
import com.wiss.thom.elements.wss.WssServerConnector;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.eclipse.californium.core.CoapBroker;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.UDPConnector;
import org.eclipse.californium.elements.tcp.TcpServerConnector;
import org.eclipse.californium.elements.tcp.TlsServerConnector;

/**
 *
 * @author thomas
 */
public class PubSubBroker extends CoapBroker {

    private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
    private final String connectorChoice;

    public PubSubBroker(String connectorChoice) {
        this.connectorChoice = connectorChoice;
    }

    /**
     * Add individual endpoints listening on default CoAP port on all IPv4
     * addresses of all network interfaces.
     */
    public void addEndpoints() {
        for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
            // only binds to IPv4 addresses and localhost
            /*
            if (addr instanceof Inet4Address || addr instanceof Inet6Address || addr.isLoopbackAddress()) {
                InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
                addEndpoint(new CoapEndpoint(bindToAddress));
                System.out.println("PubSubBroker, Added endpoint at addr:: " + addr.getHostAddress());
            }
             */
            if (addr instanceof Inet4Address) {
                InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
                
                NetworkConfig config = NetworkConfig.getStandard();

                Connector connector = null;
                switch (connectorChoice) {
                    case "udp":
                        connector = new UDPConnector(bindToAddress);
                        break;
                    case "tcp":
                        connector = new TcpServerConnector(bindToAddress, 2, 1000);
                        break;
                    case "ws":
                        connector = new WsServerConnector(bindToAddress, 100);
                        break;
                    case "sctp":
                        connector = new SctpServerConnector(bindToAddress, 100);
                        break;
                    case "dtls":
                        connector = new DtlsServerConnector(bindToAddress, "certs/keyStore.jks", "certs/trustStore.jks", 2000);
                        break;
                    case "wss":
                        connector = new WssServerConnector(bindToAddress, "certs/keyStore.jks", 2000);
                        break;
                    case "tls":
                        connector = new TlsServerConnector(new SSLUtil("endPass", "keyStore.jks").getSSLContext(), bindToAddress, 2, 2000);
                        break;
                    default:
                        connector = new UDPConnector(bindToAddress);
                        break;
                }

                addEndpoint(new CoapEndpoint(connector, config));
                System.out.println("PubSubBroker, Added endpoint at addr:: " + addr.getHostAddress());
            }
        }
    }

}
