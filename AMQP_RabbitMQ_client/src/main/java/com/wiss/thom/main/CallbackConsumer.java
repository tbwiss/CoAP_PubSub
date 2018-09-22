/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.main;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.wiss.thom.output.MeasurementWriter;
import java.io.IOException;

/**
 *
 * @author thomas
 */
public class CallbackConsumer implements Consumer {

    private MeasurementWriter writer;

    public CallbackConsumer(MeasurementWriter writer) {
        this.writer = writer;
    }

    @Override
    public void handleConsumeOk(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleCancelOk(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleCancel(String string) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleShutdownSignal(String string, ShutdownSignalException sse) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleRecoverOk(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleDelivery(String string, Envelope envlp, AMQP.BasicProperties bp, byte[] bytes) throws IOException {
        long current = System.currentTimeMillis();
        String message = new String(bytes, "UTF-8");
        int messageValue;
        try {
            messageValue = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            System.out.println("AMQP Sub: Invalid message value, cannot parse int");
            messageValue = 0;
        }
        writer.writeContent((messageValue + "," + String.valueOf(current)));
        System.out.println("AMQP Sub: Received '" + message + "'");
    }

}
