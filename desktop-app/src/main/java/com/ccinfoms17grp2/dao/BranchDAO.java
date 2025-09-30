package com.ccinfoms17grp2.dao;

import com.ccinfoms17grp2.models.Branch;

public interface BranchDAO extends CrudRepository<Branch, Integer> {

    boolean existsByName(String name) throws DaoException;
}
