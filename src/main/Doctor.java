package main;

import java.io.IOException;

import com.rabbitmq.client.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.tuple.Pair;

public class Doctor extends MedicalStaff {
    private static int EXAMINATION_CNT = 40;
    private List<Pair<String, ExaminationType>> examinations;
    public Doctor(String doctorId) throws IOException {
        this.staffId = doctorId;
        this.examinations = generateExaminations(EXAMINATION_CNT);
    }

    private List<Pair<String, ExaminationType>> generateExaminations(int examinationCnt) throws IOException {
        List<Pair<String, ExaminationType>> examinations = new ArrayList<>(EXAMINATION_CNT);
            for (int i = 0; i < examinationCnt; i++){
                int surnameIndex = new Random().nextInt(Constans.names.length);
                int x = new Random().nextInt(ExaminationType.class.getEnumConstants().length);
                ExaminationType type = ExaminationType.class.getEnumConstants()[x];
                examinations.add(Pair.of(Constans.names[surnameIndex], type));
            }

            return examinations;


    }


    private void work() throws IOException, TimeoutException, InterruptedException {
        logger.info(String.format("doctor %s started working", this.staffId));
        prepareSelf();
        examinePatients();
    }

    private void examinePatients() throws InterruptedException, IOException, TimeoutException {
        Channel channel = createChannel();
        //rownowazenie obciazenia techników
        channel.basicQos(1);
        channel.exchangeDeclare(Constans.TECHNICAN_EXCHANGE, BuiltinExchangeType.TOPIC);

        for(Pair<String, ExaminationType> patient: examinations){
            Thread.sleep((long)(Math.random() * 10000));
            //KEY - EXAMINATION TYPE
            channel.basicPublish(Constans.TECHNICAN_EXCHANGE, patient.getRight().name(), null, generateMsg(staffId,patient.getLeft(), patient.getRight()));
            logger.info(String.format("[doctor: %s] sent request for examintation \t(%s, %s)", staffId, patient.getRight().name(), patient.getLeft()));
        }
    }

    private byte[] generateMsg(String doctorId, String name, ExaminationType type) {
        return String.format("%s;%s;%s", doctorId, type, name).getBytes();
    }

    private void prepareSelf() throws IOException, TimeoutException {
        this.listenToAdmin();
        this.listenTechnicans();
    }

    private void listenTechnicans() throws IOException, TimeoutException {
        Channel channel = createChannel();

        //topic exchange
        channel.exchangeDeclare(Constans.DOCTOR_EXCHANGE, BuiltinExchangeType.TOPIC);

        String queueName = channel.queueDeclare().getQueue();
        //topic by doctor id
        channel.queueBind(queueName, Constans.DOCTOR_EXCHANGE, staffId);
        channel.basicConsume(queueName, false, new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                String[] msg = message.split(";");
                String tech = msg[0];
                String patientName = msg[1];
                String exam = msg[2];

                logger.info(String.format("\t\t\t[doctor: %s] got examination\t (%s, %s) \tfrom technican %s", staffId, patientName, exam, tech));
            }
        });

    }

    public static void main(String[] args) throws InterruptedException, TimeoutException, IOException {
                    Thread.sleep(5000);

                    Doctor doctor = null;
                    doctor = new Doctor("Y");
                    doctor.work();




    }



}
