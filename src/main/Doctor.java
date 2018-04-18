package main;

import java.io.IOException;

import com.rabbitmq.client.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.RandomStringUtils;

public class Doctor extends MedicalStaff {
    public static int doctorsCnt = 0;
    private static int EXAMINATION_CNT = 40;
    private static int
    private int doctorId;
    private List<Pair<String, ExaminationType>> examinations;
    public Doctor(){
        this.doctorId = doctorsCnt++;
        this.examinations = generateExaminations(EXAMINATION_CNT);

    }

    private List<Pair<String, ExaminationType>> generateExaminations(int examinationCnt) {
        List<Pair<String, ExaminationType>> examinations = new ArrayList<>(EXAMINATION_CNT);
        for (int i = 0; i < examinationCnt; i++){
            int surnameLen = new Random().nextInt(10) + 3;
            int x = new Random().nextInt(ExaminationType.class.getEnumConstants().length);
            ExaminationType type = ExaminationType.class.getEnumConstants()[x];
            examinations.add(Pair.of(RandomStringUtils.random(surnameLen), type));
        }
        return examinations;
    }


    private void work() throws IOException, TimeoutException, InterruptedException {
        logger.info(String.format("doctor %d started working", this.doctorId));
        prepareSelf();
        examinePatients();


    }

    private void examinePatients() throws InterruptedException, IOException, TimeoutException {
        Channel channel = createOwnChannel();
        channel.basicQos(1);
        channel.exchangeDeclare(Constans.TECHNICAN_EXCHANGE, BuiltinExchangeType.TOPIC);

        for(Pair<String, ExaminationType> patient: examinations){
            Thread.sleep(2000);
            channel.basicPublish(Constans.TECHNICAN_EXCHANGE, patient.getRight().name(), null, generateMsg(doctorId,patient.getLeft(), patient.getRight()));
            logger.info(String.format("[%s]sent request for examintation (%s, %s)", doctorId, patient.getRight().name(), patient.getLeft()));
        }
    }

    private byte[] generateMsg(int doctorId, String name, ExaminationType type) {
        return String.format("%s;%s;%s", doctorId, type, name).getBytes();
    }

    private void prepareSelf() throws IOException, TimeoutException {
        this.listenToAdmin();
        this.listenTechnicans();
    }

    private void listenTechnicans() throws IOException, TimeoutException {
        Channel channel = createOwnChannel();

        //topic exchange
        channel.exchangeDeclare(Constans.DOCTOR_EXCHANGE, BuiltinExchangeType.TOPIC);
        //topic by doctor id
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, Constans.DOCTOR_EXCHANGE, Integer.toString(doctorId));
        channel.basicConsume(queueName, false, new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                logger.info(String.format("TECHNICAN: %s", message));
            }
        });

    }

    public static void main(String[] args) throws InterruptedException, TimeoutException, IOException {
        Doctor doctor = new Doctor();
        doctor.work();
    }



}
