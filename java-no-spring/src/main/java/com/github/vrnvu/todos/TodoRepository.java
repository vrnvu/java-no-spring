package com.github.vrnvu.todos;

import java.util.List;
import java.util.Optional;

public interface TodoRepository {
    List<Todo> getAllTodos() throws TodoError;
    Optional<Todo> getTodoById(String id) throws TodoError;
    void insertTodo(Todo todo) throws TodoError;
    void deleteTodoById(String id) throws TodoError;
}
