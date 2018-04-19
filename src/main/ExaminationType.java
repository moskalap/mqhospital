package main;

public enum ExaminationType {
    KNEE, ELBOW, HIP;

    public String getQueueName(){
        return this.name()+"QUEUE";
    }
}
