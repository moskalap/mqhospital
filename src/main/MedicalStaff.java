package main;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static main.Constans.ADMIN_EXCHANGE;

public class MedicalStaff extends SystemUser {


    protected String staffId;


    public void listenToAdmin() throws IOException, TimeoutException {
        //create a channel to listen admin messages
        Channel channel = createChannel();

        // exchange - fanout - all will be receivers
        channel.exchangeDeclare(ADMIN_EXCHANGE, BuiltinExchangeType.FANOUT);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, ADMIN_EXCHANGE, "");
        logger.info(staffId + " started listening to admin");
        channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String message = new String(body, "UTF-8");
                        logger.info(String.format("" +
                                "\t\t---ADMIN: %s", message));
                    }
                }
        );


    }
}
