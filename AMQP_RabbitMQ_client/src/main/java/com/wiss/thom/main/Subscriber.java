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
import java.util.concurrent.TimeoutException;

/**
 *
 * @author thomas
 */
public class Subscriber {

    private MeasurementWriter writer;
    private String queueName;
    private String hostAdr;
    private String exchangeName;

    public Subscriber(MeasurementWriter writer, String queueName, String exchangeName, String hostAdr) {
        this.writer = writer;
        this.queueName = queueName;
        this.hostAdr = hostAdr;
        this.exchangeName = exchangeName;
    }

    public void execute() {

        System.out.println("== START AMQP SUBSCRIBER ==");

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(this.hostAdr);
            factory.setUsername("admin");
            factory.setPassword("admin");
            factory.setPort(5672);
            
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String routingKey = "key";

            //channel.exchangeDeclare(this.exchangeName, "fanout", true);
            channel.queueDeclare(queueName, false, false, false, null);
            //String queueNameRandom = channel.queueDeclare().getQueue();
            //channel.queueBind(queueNameRandom, exchangeName, routingKey);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                        AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    long current = System.currentTimeMillis();
                    String message = new String(body, "UTF-8");
                    /*int messageValue = 0;
                    try {
                        messageValue = Integer.parseInt(message);
                    } catch (NumberFormatException e) {
                        System.out.println("AMQP Sub: Invalid message value, cannot parse int");
                        messageValue = 0;
                    }*/
                    //writer.writeContent((messageValue + "," + String.valueOf(current)));
                    System.out.println("AMQP Sub: Received '" + message + "'");
                }
            };
            //Consumer consumer = new CallbackConsumer(writer);
            channel.basicConsume(this.queueName, true, consumer);

            // maybe execute as a thread so that the channel stays open....
            System.out.println("AMQP Sub: Waiting for messages...");

        } catch (IOException | TimeoutException e) {
            System.err.println(e);
        }
    }

}
