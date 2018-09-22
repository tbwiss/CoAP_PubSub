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
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.wiss.thom.output.MeasurementWriter;
import java.io.IOException;

/**
 *
 * @author thomas
 */
public class PubSub {

    private MeasurementWriter writer;
    private String queueName;
    private String hostAdr;
    private String exchangeName;

    public PubSub(MeasurementWriter writer, String queueName, String exchangeName, String hostAdr) {
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

        System.out.println("== START AMQP PUBSUB ==");

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(this.hostAdr);
            factory.setUsername("admin");
            factory.setPassword("admin");
            factory.setPort(5672);

            for (int k = 0; k < 200; k++) {
                writer.writeContent(String.valueOf(k) + "," + System.currentTimeMillis());

                Connection connection = factory.newConnection();
                Channel sub = connection.createChannel();
                sub.queueDeclare(this.queueName, false, false, false, null);
                Channel pub = connection.createChannel();
                pub.queueDeclare(this.queueName, false, false, false, null);

                Consumer consumer = new DefaultConsumer(sub) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope,
                            AMQP.BasicProperties properties, byte[] body)
                            throws IOException {
                        String message = new String(body, "UTF-8");
                        System.out.println("Sub: '" + message + "'");
                    }
                };
                sub.basicConsume(this.queueName, true, consumer);

                for (int i = 0; i < 50; i++) {
                    String message = String.valueOf(i);
                    pub.basicPublish("", queueName, null, message.getBytes());
                }
                pub.close();
                sub.close();
                writer.writeContent(String.valueOf(k) + "," + System.currentTimeMillis());
                System.out.println("Publish cycle: " + k);
            }
            
            System.out.println("== END AMQP PUBSUB ==");

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}


  /*
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        System.err.println(ex);
                    }*/