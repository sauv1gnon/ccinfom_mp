package com.ccinfoms17grp2.dao;

/**
 * Runtime exception that wraps SQL-related issues coming from the DAO layer.
 */
public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
