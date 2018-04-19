package main;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class MedicalStaff extends SystemUser {
    protected String staffId;
    protected static Logger logger = Logger.getLogger(MedicalStaff.class.getName());
    protected static final String ADMIN_EXCHANGE = "ADMIN_EXCHANGE";

    public void listenToAdmin() throws IOException, TimeoutException {
        //create a channel to listen admin messages
        Channel channel = createOwnChannel();

        // exchange - fanout - all will be receivers
        channel.exchangeDeclare(ADMIN_EXCHANGE, BuiltinExchangeType.FANOUT);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, ADMIN_EXCHANGE, "");
        logger.info(staffId + " started listening to admin");
        channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String message = new String(body, "UTF-8");
                        logger.info(String.format("ADMIN: %s", message));
                    }
                }
        );


    }
}
