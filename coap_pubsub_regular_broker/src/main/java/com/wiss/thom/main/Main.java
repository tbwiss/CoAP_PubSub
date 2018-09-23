/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

import java.util.logging.Level;
import org.eclipse.californium.core.CaliforniumLogger;


/**
 *
 * @author thomas
 */
public class Main {

    public static void main(String[] args) {
        CaliforniumLogger.initialize();
        CaliforniumLogger.setLevel(Level.WARNING);  // ----- Change to appropriate level! -----
        
        String connectorChoice = "tls";
        
        PubSubBroker broker = new PubSubBroker(connectorChoice);
        broker.addEndpoints();
        broker.start();
    }

}
