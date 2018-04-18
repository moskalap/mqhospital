package main;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class MedicalStaff extends SystemUser {
    protected static Logger logger = Logger.getLogger(MedicalStaff.class.getName()));
    protected static final String ADMIN_EXCHANGE_NAME = "ADMIN_EXCHANGE";
    public void listenToAdmin() throws IOException, TimeoutException {
        //create a channel to listen admin messages
        Channel channel = createOwnChannel();

        // exchange - fanout - all will be receivers
        channel.exchangeDeclare(ADMIN_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, ADMIN_EXCHANGE_NAME, "");
        channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
            }
        }
        );


    }
}
