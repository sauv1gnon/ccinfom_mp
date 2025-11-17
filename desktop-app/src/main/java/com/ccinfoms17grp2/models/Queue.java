package com.ccinfoms17grp2.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Queue {

    private int queueId;
    private int patientId;
    private int branchId;
    private int queueNumber;
    private QueueStatus status;
    private LocalDateTime createdAt;

    public Queue() {
        this.status = QueueStatus.WAITING;
    }

    public Queue(int queueId, int patientId, int branchId, int queueNumber, QueueStatus status, LocalDateTime createdAt) {
        this.queueId = queueId;
        this.patientId = patientId;
        this.branchId = branchId;
        this.queueNumber = queueNumber;
        this.status = status == null ? QueueStatus.WAITING : status;
        this.createdAt = createdAt;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(int queueNumber) {
        this.queueNumber = queueNumber;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public void setStatus(QueueStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
                ", patientId=" + patientId +
                ", branchId=" + branchId +
                ", queueNumber=" + queueNumber +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
