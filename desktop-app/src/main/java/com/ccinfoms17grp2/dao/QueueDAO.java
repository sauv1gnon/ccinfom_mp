package com.ccinfoms17grp2.dao;

import com.ccinfoms17grp2.models.Queue;
import java.util.List;

public interface QueueDAO extends CrudRepository<Queue, Integer> {
    
    /**
     * Finds all queue entries for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of queue entries
     * @throws DaoException if a database error occurs
     */
    List<Queue> findByBranchId(int branchId) throws DaoException;
    
    /**
     * Finds all queue entries for a specific patient.
     *
     * @param patientId the patient ID
     * @return list of queue entries
     * @throws DaoException if a database error occurs
     */
    List<Queue> findByPatientId(int patientId) throws DaoException;
    
    /**
     * Finds queue entries by branch and status.
     *
     * @param branchId the branch ID
     * @param status the queue status
     * @return list of queue entries
     * @throws DaoException if a database error occurs
     */
    List<Queue> findByBranchIdAndStatus(int branchId, String status) throws DaoException;
    
    /**
     * Gets the next available queue number for a branch.
     *
     * @param branchId the branch ID
     * @return the next queue number
     * @throws DaoException if a database error occurs
     */
    int getNextQueueNumber(int branchId) throws DaoException;
    
    /**
     * Finds today's queue entries for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of today's queue entries
     * @throws DaoException if a database error occurs
     */
    List<Queue> findTodaysQueueByBranch(int branchId) throws DaoException;
    
    /**
     * Finds all today's queue entries across all branches.
     *
     * @return list of today's queue entries
     * @throws DaoException if a database error occurs
     */
    List<Queue> findTodaysQueue() throws DaoException;
}
