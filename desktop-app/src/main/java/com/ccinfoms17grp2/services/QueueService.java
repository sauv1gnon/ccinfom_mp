package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.QueueDAO;
import com.ccinfoms17grp2.models.Queue;
import com.ccinfoms17grp2.models.QueueStatus;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class QueueService {

    private final QueueDAO queueDAO;

    public QueueService(QueueDAO queueDAO) {
        this.queueDAO = Objects.requireNonNull(queueDAO);
    }

    public List<Queue> listQueues() {
        return queueDAO.findAll();
    }

    public List<Queue> listQueuesByBranch(int branchId) {
        if (branchId <= 0) {
            throw new ValidationException("Branch ID is invalid.");
        }
        return queueDAO.findByBranchId(branchId);
    }

    public List<Queue> listQueuesByPatient(int patientId) {
        if (patientId <= 0) {
            throw new ValidationException("Patient ID is invalid.");
        }
        return queueDAO.findByPatientId(patientId);
    }

    public List<Queue> listQueuesByBranchAndStatus(int branchId, QueueStatus status) {
        if (branchId <= 0) {
            throw new ValidationException("Branch ID is invalid.");
        }
        if (status == null) {
            throw new ValidationException("Queue status is required.");
        }
        return queueDAO.findByBranchIdAndStatus(branchId, status.getValue());
    }

    public Optional<Queue> getQueueById(int queueId) {
        if (queueId <= 0) {
            throw new ValidationException("Queue ID is invalid.");
        }
        return queueDAO.findById(queueId);
    }

    public Queue createQueue(Queue queue) {
        validate(queue, false);
        if (queue.getQueueNumber() <= 0) {
            int nextNumber = queueDAO.getNextQueueNumber(queue.getBranchId());
            queue.setQueueNumber(nextNumber);
        }
        return queueDAO.create(queue);
    }

    public Queue updateQueue(Queue queue) {
        validate(queue, true);
        boolean updated = queueDAO.update(queue);
        if (!updated) {
            throw new ValidationException("Queue record could not be updated. It may have been removed by another user.");
        }
        Optional<Queue> refreshed = queueDAO.findById(queue.getQueueId());
        return refreshed.orElse(queue);
    }

    public void deleteQueue(int queueId) {
        if (queueId <= 0) {
            throw new ValidationException("Queue ID is invalid.");
        }
        boolean deleted = queueDAO.delete(queueId);
        if (!deleted) {
            throw new ValidationException("Queue record could not be deleted. It may have already been removed.");
        }
    }

    public Queue updateQueueStatus(int queueId, QueueStatus newStatus) {
        if (queueId <= 0) {
            throw new ValidationException("Queue ID is invalid.");
        }
        if (newStatus == null) {
            throw new ValidationException("Queue status is required.");
        }
        Optional<Queue> optQueue = queueDAO.findById(queueId);
        if (!optQueue.isPresent()) {
            throw new ValidationException("Queue not found with ID: " + queueId);
        }
        Queue queue = optQueue.get();
        queue.setStatus(newStatus);
        boolean updated = queueDAO.update(queue);
        if (!updated) {
            throw new ValidationException("Failed to update queue status.");
        }
        return queueDAO.findById(queueId).orElse(queue);
    }

    public int getNextQueueNumber(int branchId) {
        if (branchId <= 0) {
            throw new ValidationException("Branch ID is invalid.");
        }
        return queueDAO.getNextQueueNumber(branchId);
    }
    
    public List<Queue> listTodaysQueueByBranch(int branchId) {
        if (branchId <= 0) {
            throw new ValidationException("Branch ID is invalid.");
        }
        return queueDAO.findTodaysQueueByBranch(branchId);
    }
    
    public List<Queue> listTodaysQueue() {
        return queueDAO.findTodaysQueue();
    }

    private void validate(Queue queue, boolean requireId) {
        if (queue == null) {
            throw new ValidationException("Queue information is required.");
        }
        if (requireId && queue.getQueueId() <= 0) {
            throw new ValidationException("Queue ID is invalid.");
        }
        if (queue.getPatientId() <= 0) {
            throw new ValidationException("Patient ID is required.");
        }
        if (queue.getBranchId() <= 0) {
            throw new ValidationException("Branch ID is required.");
        }
        if (queue.getQueueNumber() < 0) {
            throw new ValidationException("Queue number must be non-negative.");
        }
        if (queue.getStatus() == null) {
            throw new ValidationException("Queue status is required.");
        }
    }
}
