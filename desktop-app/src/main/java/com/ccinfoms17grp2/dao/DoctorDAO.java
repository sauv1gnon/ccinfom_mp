package com.ccinfoms17grp2.dao;

import com.ccinfoms17grp2.models.Doctor;

import java.util.List;

public interface DoctorDAO extends CrudRepository<Doctor, Integer> {

    List<Doctor> findBySpecialization(int specializationId) throws DaoException;
}
