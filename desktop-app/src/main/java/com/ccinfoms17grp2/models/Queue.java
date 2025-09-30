package com.ccinfoms17grp2.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Queue {

    private int queueId;
    private int appointmentId;
    private int position;
    private Integer estimatedWaitTimeMinutes;
    private LocalDateTime updatedAt;

    public Queue() {
    }

    public Queue(int queueId, int appointmentId, int position, Integer estimatedWaitTimeMinutes, LocalDateTime updatedAt) {
        this.queueId = queueId;
        this.appointmentId = appointmentId;
        this.position = position;
        this.estimatedWaitTimeMinutes = estimatedWaitTimeMinutes;
        this.updatedAt = updatedAt;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Queue)) {
            return false;
        }
        Queue queue = (Queue) o;
        return queueId == queue.queueId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(queueId);
    }

    @Override
    public String toString() {
        return "Queue{" +
                "queueId=" + queueId +
                ", appointmentId=" + appointmentId +
                ", position=" + position +
                ", estimatedWaitTimeMinutes=" + estimatedWaitTimeMinutes +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
