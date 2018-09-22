/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

/**
 *
 * @author thomas
 */
public class Main {

    public static void main(String[] args) {
        PubSubBroker broker = new PubSubBroker();
        broker.addEndpoints();
        broker.start();
    }

}
