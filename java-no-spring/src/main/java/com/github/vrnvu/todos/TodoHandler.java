package com.github.vrnvu.todos;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vrnvu.core.Handler;
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
            default -> exchange.sendResponseHeaders(405, 0);
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        var todo = objectMapper.readValue(exchange.getRequestBody(), Todo.class);
        try {
            todoService.insertTodo(todo);
            Handler.statusCode(exchange, 200);
        } catch (TodoService.TodoException e) {
            logger.log(Level.SEVERE, "Error inserting todo", e.getCause());
            Handler.statusCode(exchange, 500);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/todos".equals(path)) {
            handleGetTodos(exchange);
            return;
        }

        if (!path.startsWith("/todos/")) {
            Handler.statusCode(exchange, 404);
            return;
        }

        String id = path.substring("/todos/".length());
        if (id.isEmpty()) {
            Handler.statusCode(exchange, 404);
            return;
        }

        if (id.contains("/")) {
            Handler.statusCode(exchange, 404);
            return;
        }

        handleGetTodo(exchange, id);
    }

    private void handleGetTodo(HttpExchange exchange, String id) throws IOException {
        Optional<Todo> todo;
        try {
            todo = todoService.getTodo(id);
        } catch (TodoService.TodoException e) {
            logger.log(Level.SEVERE, "Error getting todo", e.getCause());
            Handler.statusCode(exchange, 500);
            return;
        }

        if (todo.isEmpty()) {
            Handler.statusCode(exchange, 404);
            return;
        }

        try {
            var response = objectMapper.writeValueAsBytes(todo.get());
            Handler.response(exchange, response);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error serializing todo", e);
            Handler.statusCode(exchange, 500);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing response", e);
            Handler.statusCode(exchange, 500);
        }
    }

    private void handleGetTodos(HttpExchange exchange) throws IOException {
        List<Todo> todos;
        try {
            todos = todoService.getTodos();
        } catch (TodoService.TodoException e) {
            logger.log(Level.SEVERE, "Error getting todos", e.getCause());
            Handler.statusCode(exchange, 500);
            return;
        }

        try {
            var response = objectMapper.writeValueAsBytes(todos);
            Handler.response(exchange, response);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error serializing todos", e);
            Handler.statusCode(exchange, 500);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing response", e);
            Handler.statusCode(exchange, 500);
        }
    }
}
