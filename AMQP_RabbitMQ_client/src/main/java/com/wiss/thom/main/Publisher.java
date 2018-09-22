/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.wiss.thom.output.MeasurementWriter;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class Publisher {

    private MeasurementWriter writer;
    private String queueName;
    private String hostAdr;
    private String exchangeName;

    public Publisher(MeasurementWriter writer, String queueName, String exchangeName, String hostAdr) {
        this.writer = writer;
        this.queueName = queueName;
        this.hostAdr = hostAdr;
        this.exchangeName = exchangeName;
    }

    public void execute() {

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .contentType("text/plain")
                .priority(1)
                .userId("Thom")
                .build();

        System.out.println("== START AMQP PUBLISHER ==");

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(this.hostAdr);
            factory.setUsername("admin");
            factory.setPassword("admin");
            factory.setPort(5672);

            /*
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(this.queueName, false, false, false, null);
            //channel.exchangeDeclare(this.exchangeName, "fanout", true);*/
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(this.queueName, false, false, false, null);

            for (int i = 0; i < 200; i++) {

                String message = RandomData.getRandomData(400);
                channel.basicPublish("", queueName, null, message.getBytes());

                try {
                    Thread.sleep(30);
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                }

            }
            channel.close();
            connection.close();

            /*
            for (int i = 0; i < 1; i++) {
                writer.writeContent(String.valueOf(i) + "," + System.currentTimeMillis());

                String message = String.valueOf(i);
                channel.basicPublish("", queueName, null, message.getBytes());
                //System.out.println("AMQP Pub: publish '" + message + "'");

                try {
                    Thread.sleep(30);
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                }
            }

            channel.close();
            connection.close();*7

            /*
            for (int a = 0; a < 200; a++) {
                for (int i = 0; i < 5; i++) {
                    writer.writeContent(String.valueOf(i) + "," + System.currentTimeMillis());

                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel();
                    channel.queueDeclare(this.queueName, false, false, false, null);
                    //channel.exchangeDeclare(this.exchangeName, "fanout", true);

                    String message = String.valueOf(i);
                    channel.basicPublish("", queueName, null, message.getBytes());
                    System.out.println("AMQP Pub: publish '" + message + "'");
                    
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException ex) {
                        System.err.println(ex);
                    }

                    channel.close();
                    connection.close();
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                }
                System.out.println(a);
            }
            //channel.basicPublish("", this.queueName, null, String.valueOf(2).getBytes());
            //System.out.println("AMQP Pub: published '" + String.valueOf(2) + "'");
            
             */
            System.out.println("== END AMQP PUBLISHER ==");

        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
