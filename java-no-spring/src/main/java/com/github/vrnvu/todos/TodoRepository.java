package com.github.vrnvu.todos;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TodoRepository {
    List<Todo> getAllTodos() throws SQLException;
    Optional<Todo> getTodoById(String id) throws SQLException;
    void insertTodo(Todo todo) throws SQLException;
}
