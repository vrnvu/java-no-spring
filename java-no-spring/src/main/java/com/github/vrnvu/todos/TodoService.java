package com.github.vrnvu.todos;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TodoService {

    public static class TodoException extends Exception {
        public TodoException(String message) {
            super(message);
        }

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

    public List<Todo> fetchTodos(ObjectMapper objectMapper) throws TodoException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .uri(URI.create("http://localhost:8080/todos"))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                try {
                        return objectMapper.readValue(body, new TypeReference<List<Todo>>() {});
                    } catch (IOException e) {
                        throw new CompletionException(new TodoException("Failed to parse todos", e));
                    }
                })
                .join();
    }
}

