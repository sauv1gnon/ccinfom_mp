package com.ccinfoms17grp2.dao;

import com.ccinfoms17grp2.models.Patient;

import java.util.List;

public interface PatientDAO extends CrudRepository<Patient, Integer> {

    List<Patient> searchByName(String keyword) throws DaoException;
}
