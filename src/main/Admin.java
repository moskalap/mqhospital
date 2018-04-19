package main;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Admin extends SystemUser {
    public Admin(){

    }

    public void work() throws IOException, TimeoutException {
        listenAll();
        publishToAll();
    }

    private void publishToAll() {
        new Thread(){
            public void run(){

                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    Channel ch = createChannel();

                    ch.exchangeDeclare(Constans.ADMIN_EXCHANGE, BuiltinExchangeType.FANOUT);

                    boolean run = true;

                    while(run){
                        String msg = br.readLine();
                        if(msg.equals("-q")){
                            run = false;
                        }else{
                            ch.basicPublish(Constans.ADMIN_EXCHANGE,"", null, msg.getBytes());
                        }
                    }

                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }

            }
        }.start();


    }

    private void listenAll() throws IOException, TimeoutException {
        Channel channel = createChannel();
        String queueName = channel.queueDeclare().getQueue();

        //doctorexchaange
        captureAllFromExchange(Constans.DOCTOR_EXCHANGE, queueName, channel);
        captureAllFromExchange(Constans.TECHNICAN_EXCHANGE, queueName, channel);


    }

    private void captureAllFromExchange(String exchangeName, String queueName, Channel channel) throws IOException {
        channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);
        channel.queueBind(queueName, exchangeName, "*");
        channel.basicConsume(queueName, true, new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                logger.info(message);
            }
        });
    }
    public static void main(String[] args) throws IOException, TimeoutException {
        Admin a = new Admin();
        a.work();
    }
}
