package com.github.vrnvu.todos;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
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

    public Result<Void> insertTodo(Todo todo) {
        return todoRepository.insertTodo(todo);
    }

    public Result<List<Todo>> getTodos() {
        return todoRepository.getAllTodos();
    }

    public Result<Todo> getTodo(String id) {
        return todoRepository.getTodoById(id);
    }

    public Result<List<Todo>> fetchTodos(ObjectMapper objectMapper) {
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .uri(fetchTodosUri)
                .GET()
                .build();

        List<Todo> todos;
        try { 
            todos = client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                try {
                        return objectMapper.readValue(body, new TypeReference<List<Todo>>() {});
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                })
                .join();
        } catch (CompletionException e) {
            return new Result.Err<>(TodoError.SYSTEM_ERROR);
        }
        return new Result.Ok<>(todos);
    }

    public Result<Void> deleteTodoById(String id) {
        return todoRepository.deleteTodoById(id);
    }
}

