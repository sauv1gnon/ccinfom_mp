package com.ccinfoms17grp2.dao;

import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;

import java.util.List;

public interface DoctorDAO extends CrudRepository<Doctor, Integer> {

    List<Doctor> findBySpecialization(int specializationId) throws DaoException;

    List<Doctor> findByBranchId(int branchId) throws DaoException;

    List<Doctor> findByBranchAndSpecializations(int branchId, List<Integer> specializationIds) throws DaoException;

    List<Doctor> findByAvailabilityStatus(DoctorAvailabilityStatus status) throws DaoException;

    List<Branch> findBranchesForDoctor(int doctorId) throws DaoException;

    void updateBranchAssignments(int doctorId, List<Integer> branchIds) throws DaoException;
}
