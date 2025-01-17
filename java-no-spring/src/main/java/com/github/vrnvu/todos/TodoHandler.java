package com.github.vrnvu.todos;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TodoHandler implements HttpHandler {
    private static final Logger logger = Logger.getLogger(TodoHandler.class.getName());

    private final ObjectMapper objectMapper;
    private final TodoService todoService;

    public TodoHandler(ObjectMapper objectMapper, TodoService todoService) {
        this.objectMapper = objectMapper;
        this.todoService = todoService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET" -> handleGet(exchange);
            case "PUT" -> handlePut(exchange);
            case "DELETE" -> handleDelete(exchange);
            default -> exchange.sendResponseHeaders(405, 0);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!path.startsWith("/todos/")) {
            Handler.exception(exchange, new TodoError.BadRequest());
            return;
        }

        String id = path.substring("/todos/".length());
        if (id.isEmpty()) {
            Handler.exception(exchange, new TodoError.BadRequest());
            return;
        }

        if (id.contains("/")) {
            Handler.exception(exchange, new TodoError.BadRequest());
            return;
        }

        try {
            todoService.deleteTodoById(id);
            Handler.response(exchange, new byte[0]);
        } catch (TodoError e) {
            switch (e.getType()) {
                case NOT_FOUND -> Handler.exception(exchange, e);
                case SYSTEM_ERROR -> {
                    logger.log(Level.SEVERE, "Error deleting todo", e.getCause());
                    Handler.exception(exchange, e);
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + e.getType());
            }
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        var todo = objectMapper.readValue(exchange.getRequestBody(), Todo.class);
        try {
            todoService.insertTodo(todo);
            Handler.response(exchange, new byte[0]);
        } catch (TodoError e) {
            switch (e.getType()) {
                case NOT_FOUND -> Handler.exception(exchange, e);
                case SYSTEM_ERROR -> {
                    logger.log(Level.SEVERE, e.getMessage(), e.getCause());
                    Handler.exception(exchange, e);
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + e.getType());
            }
        }
    }

    // TODO AntPathMatcher
    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/todos/fetch".equals(path)) {
            handleTodosFetch(exchange);
            return;
        }

        if ("/todos".equals(path)) {
            handleGetTodos(exchange);
            return;
        }

        if (!path.startsWith("/todos/")) {
            Handler.exception(exchange, new TodoError.BadRequest());
            return;
        }

        String id = path.substring("/todos/".length());
        if (id.isEmpty()) {
            Handler.exception(exchange, new TodoError.BadRequest());
            return;
        }

        if (id.contains("/")) {
            Handler.exception(exchange, new TodoError.BadRequest());
            return;
        }

        handleGetTodo(exchange, id);
    }

    private void handleTodosFetch(HttpExchange exchange) throws IOException {
        try {
            var todos = todoService.fetchTodos(objectMapper);
            var response = objectMapper.writeValueAsBytes(todos);
            Handler.response(exchange, response);
        } catch (TodoError e) {
            switch (e.getType()) {
                case NOT_FOUND -> Handler.exception(exchange, e);
                case SYSTEM_ERROR -> {
                    logger.log(Level.SEVERE, e.getMessage(), e.getCause());
                    Handler.exception(exchange, e);
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + e.getType());
            }
        }
    }

    private void handleGetTodo(HttpExchange exchange, String id) throws IOException {
        Optional<Todo> todo = Optional.empty();
        try {
            todo = todoService.getTodo(id);
        } catch (TodoError e) {
            switch (e.getType()) {
                case NOT_FOUND -> Handler.exception(exchange, e);
                case SYSTEM_ERROR -> {
                    logger.log(Level.SEVERE, e.getMessage(), e.getCause());
                    Handler.exception(exchange, e);
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + e.getType());
            }
        }

        if (todo.isEmpty()) {
            Handler.exception(exchange, new TodoError.NotFound("Todo not found"));
            return;
        }

        try {
            var response = objectMapper.writeValueAsBytes(todo.get());
            Handler.response(exchange, response);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            Handler.exception(exchange, new TodoError.SystemError("Failed to parse todo", e));
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            Handler.exception(exchange, new TodoError.SystemError("Failed to parse todo", e));
        }
    }

    private void handleGetTodos(HttpExchange exchange) throws IOException {
        List<Todo> todos = List.of();
        try {
            todos = todoService.getTodos();
        } catch (TodoError e) {
            switch (e.getType()) {
                case SYSTEM_ERROR -> {
                    logger.log(Level.SEVERE, e.getMessage(), e.getCause());
                    Handler.exception(exchange, e);
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + e.getType());
            }
        }

        try {
            var response = objectMapper.writeValueAsBytes(todos);
            Handler.response(exchange, response);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            Handler.exception(exchange, new TodoError.SystemError("Failed to parse todos", e));
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            Handler.exception(exchange, new TodoError.SystemError("Failed to parse todos", e));
        }
    }
}
