package com.ccinfoms17grp2.models;

@Deprecated(forRemoval = true)
public class QueuePosition extends Queue {

    public QueuePosition() {
        super();
    }

    public QueuePosition(int queueId, int appointmentId, int position, Integer estimatedWaitTimeMinutes) {
        super();
        setQueueId(queueId);
        setAppointmentId(appointmentId);
        setPosition(position);
        setEstimatedWaitTimeMinutes(estimatedWaitTimeMinutes);
    }
}
