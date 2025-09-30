package com.ccinfoms17grp2.dao;

import com.ccinfoms17grp2.models.Specialization;

public interface SpecializationDAO extends CrudRepository<Specialization, Integer> {

    boolean existsByCode(String code) throws DaoException;
}
