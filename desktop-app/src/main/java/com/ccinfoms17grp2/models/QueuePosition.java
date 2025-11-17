package com.ccinfoms17grp2.models;

@Deprecated(forRemoval = true)
public class QueuePosition extends Queue {

    private int appointmentId;
    private int position;
    private Integer estimatedWaitTimeMinutes;

    public QueuePosition() {
        super();
    }

    public QueuePosition(int queueId, int appointmentId, int position, Integer estimatedWaitTimeMinutes) {
        super();
        setQueueId(queueId);
        this.appointmentId = appointmentId;
        this.position = position;
        this.estimatedWaitTimeMinutes = estimatedWaitTimeMinutes;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Integer getEstimatedWaitTimeMinutes() {
        return estimatedWaitTimeMinutes;
    }

    public void setEstimatedWaitTimeMinutes(Integer estimatedWaitTimeMinutes) {
        this.estimatedWaitTimeMinutes = estimatedWaitTimeMinutes;
    }
}
