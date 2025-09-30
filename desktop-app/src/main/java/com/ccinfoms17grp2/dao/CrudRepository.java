package com.ccinfoms17grp2.dao;

import java.util.List;
import java.util.Optional;

/**
 * Basic CRUD contract for persistence layers.
 */
public interface CrudRepository<T, ID> {

    List<T> findAll() throws DaoException;

    Optional<T> findById(ID id) throws DaoException;

    T create(T entity) throws DaoException;

    boolean update(T entity) throws DaoException;

    boolean delete(ID id) throws DaoException;
}
