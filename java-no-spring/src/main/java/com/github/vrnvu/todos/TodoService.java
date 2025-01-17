package com.github.vrnvu.todos;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TodoService {

    public record Configuration(
        TodoRepository todoRepository,
        HttpClient client,
        URI fetchTodosUri
    ) {}

    private final TodoRepository todoRepository;
    private final HttpClient client;
    private final URI fetchTodosUri;

    public TodoService(Configuration configuration) {
        this.todoRepository = configuration.todoRepository;
        this.client = configuration.client;
        this.fetchTodosUri = configuration.fetchTodosUri;
    }

    public void insertTodo(Todo todo) throws TodoError {
        todoRepository.insertTodo(todo);
    }

    public List<Todo> getTodos() throws TodoError {
        return todoRepository.getAllTodos();
    }

    public Optional<Todo> getTodo(String id) throws TodoError {
        return todoRepository.getTodoById(id);
    }

    public List<Todo> fetchTodos(ObjectMapper objectMapper) throws TodoError {
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .uri(fetchTodosUri)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                try {
                        return objectMapper.readValue(body, new TypeReference<List<Todo>>() {});
                    } catch (IOException e) {
                        throw new CompletionException(new TodoError.SystemError("Failed to parse todos", e));
                    }
                })
                .join();
    }

    public void deleteTodoById(String id) throws TodoError {
        todoRepository.deleteTodoById(id);
    }
}

