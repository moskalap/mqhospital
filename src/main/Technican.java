package main;

import com.rabbitmq.client.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Technican extends MedicalStaff {
    private final Pair<ExaminationType, ExaminationType> abilities;
    private static int technicans = 1;


    private Channel doctorChannel;

    public Technican(ExaminationType ab1, ExaminationType ab2) {
        this.abilities = Pair.of(ab1,ab2);
        this.staffId = String.valueOf(ab1.name().charAt(0)).concat(String.valueOf(ab2.name().charAt(0)));
    }



    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {

                Technican technican = new Technican(ExaminationType.ELBOW, ExaminationType.KNEE);
                technican.work();

    }

    private void work() throws IOException, TimeoutException {
        logger.info(String.format("Technican(%s, %s)", abilities.getLeft(), abilities.getRight()));
        this.listenToAdmin();
        this.cooperateWithDoctor();
    }

    private void cooperateWithDoctor() throws IOException, TimeoutException {
        //to sending to doctor by its id
        doctorChannel = createChannel();
        doctorChannel.exchangeDeclare(Constans.DOCTOR_EXCHANGE, BuiltinExchangeType.TOPIC);


        Channel channel = createChannel();
        channel.exchangeDeclare(Constans.TECHNICAN_EXCHANGE, BuiltinExchangeType.TOPIC);

        channel.basicQos(1);
        bindToQueue(channel, abilities.getLeft());
        bindToQueue(channel, abilities.getRight());

    }

    private void bindToQueue(Channel channel, ExaminationType type) throws IOException {
        channel.queueDeclare(type.getQueueName(), false, false, false, null);
        channel.queueBind(type.getQueueName(), Constans.TECHNICAN_EXCHANGE, type.name());
        channel.basicConsume(type.getQueueName(), false, new DefaultConsumer(channel){
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                String[] splitted = message.split(";");
                String doctorId = splitted[0];
                String type = splitted[1];
                String surname = splitted[2];

                try {
                    Thread.sleep((long)(Math.random() * 5000));
                    String msg =staffId+";"+surname+ ";" + envelope.getRoutingKey();
                    doctorChannel.basicPublish(Constans.DOCTOR_EXCHANGE, doctorId, null, msg.getBytes("UTF-8"));
                    logger.info(String.format("[technican %s] sending result\t(%s, %s)\tto\t%s",staffId, type, surname, doctorId ));
                    this.getChannel().basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
    }


    private void listenToDoctor() {
    }

}
