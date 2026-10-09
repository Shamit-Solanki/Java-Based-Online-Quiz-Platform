package com.quiz.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic contract shared by DAOs that manage one entity type.
 *
 * @param <T> entity type
 */
public interface CrudDAO<T> {

    T findById(int id) throws SQLException;

    List<T> findAll() throws SQLException;

    boolean delete(int id) throws SQLException;
}
