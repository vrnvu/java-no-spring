package com.github.vrnvu.todos;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TodoService {

    public static class TodoException extends Exception {
        public TodoException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public void insertTodo(Todo todo) throws TodoException {
        try {
            todoRepository.insertTodo(todo);
        } catch (SQLException e) {
            throw new TodoException("Failed to insert todo", e);
        }
    }

    public List<Todo> getTodos() throws TodoException {
        try {
            return todoRepository.getAllTodos();
        } catch (SQLException e) {
            throw new TodoException("Failed to get todos", e);
        }
    }

    public Optional<Todo> getTodo(String id) throws TodoException {
        try {
            return todoRepository.getTodoById(id);
        } catch (SQLException e) {
            throw new TodoException("Failed to get todo", e);
        }
    }
    
}
