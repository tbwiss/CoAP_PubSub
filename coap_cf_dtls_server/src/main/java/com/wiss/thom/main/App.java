/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

import com.wiss.thom.server.CFTestServer;

/**
 *
 * @author thomas
 */
public class App {

    private static final int DEFAULT_PORT = 5684;
    private static final String IP_ADDR = "127.0.0.1";

    public static void main(String[] args) {

        try {

            CFTestServer server = new CFTestServer(IP_ADDR);
            server.start();

        } catch (Exception e) {
            System.err.print(e);
        }

    }
}
