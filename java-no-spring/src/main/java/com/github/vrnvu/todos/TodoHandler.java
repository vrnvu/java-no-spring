package com.github.vrnvu.todos;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TodoHandler implements HttpHandler {
    private final Logger logger = Logger.getLogger(TodoHandler.class.getName());
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
            case "PATCH" -> handlePatch(exchange);
            case "DELETE" -> handleDelete(exchange);
            default -> exchange.sendResponseHeaders(405, 0);
        }
    }

    private void handlePatch(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!path.equals("/todos")) {
            logger.log(Level.INFO, "BAD REQUEST Patching todo {0}", path);
            Handler.exception(exchange, TodoError.BAD_REQUEST);
            return;
        }

        logger.log(Level.INFO, "Patching todo {0}", path);
        Todo partialTodo;
        try {
            partialTodo = objectMapper.readValue(exchange.getRequestBody(), Todo.class);
        } catch (IOException e) {
            logger.log(Level.INFO, "BAD REQUEST OBJECT MAPPER Patching todo {0}, with trace {1}", new Object[] { path, e.getMessage() });
            Handler.exception(exchange, TodoError.BAD_REQUEST);
            return;
        }

        if (partialTodo.id().isEmpty()) {
            logger.log(Level.INFO, "BAD REQUEST EMPTY Patching todo {0}", path);
            Handler.exception(exchange, TodoError.BAD_REQUEST);
            return;
        }

        var result = todoService.patchTodo(partialTodo);
        if (result.isOk()) {
            Handler.response(exchange, new byte[0]);
        } else {
            Handler.exception(exchange, result.unwrapErr());
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!path.startsWith("/todos/")) {
            Handler.exception(exchange, TodoError.BAD_REQUEST);
            return;
        }

        String id = path.substring("/todos/".length());
        if (id.isEmpty()) {
            Handler.exception(exchange, TodoError.BAD_REQUEST);
            return;
        }

        if (id.contains("/")) {
            Handler.exception(exchange, TodoError.BAD_REQUEST);
            return;
        }

        var result = todoService.deleteTodoById(id);
        if (result.isOk()) {
            Handler.response(exchange, new byte[0]);
        } else {
            Handler.exception(exchange, result.unwrapErr());
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        var todo = objectMapper.readValue(exchange.getRequestBody(), Todo.class);
        var result = todoService.insertTodo(todo);
        if (result.isOk()) {
            Handler.response(exchange, new byte[0]);
        } else {
            Handler.exception(exchange, result.unwrapErr());
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
            Handler.exception(exchange, TodoError.BAD_REQUEST);
            return;
        }

        String id = path.substring("/todos/".length());
        if (id.isEmpty()) {
            Handler.exception(exchange, TodoError.BAD_REQUEST);
            return;
        }

        if (id.contains("/")) {
            Handler.exception(exchange, TodoError.BAD_REQUEST);
            return;
        }

        handleGetTodo(exchange, id);
    }

    private void handleTodosFetch(HttpExchange exchange) throws IOException {
        var result = todoService.fetchTodos(objectMapper);
        if (result.isOk()) {
            var response = objectMapper.writeValueAsBytes(result.unwrap());
            Handler.response(exchange, response);
        } else {
            Handler.exception(exchange, result.unwrapErr());
        }
    }


    private void handleGetTodo(HttpExchange exchange, String id) throws IOException {
        var result = todoService.getTodo(id);
        if (result.isOk()) {
            var response = objectMapper.writeValueAsBytes(result.unwrap());
            Handler.response(exchange, response);
        } else {
            Handler.exception(exchange, result.unwrapErr());
        }
    }

    private void handleGetTodos(HttpExchange exchange) throws IOException {
        var result = todoService.getTodos();
        if (result.isOk()) {
            var response = objectMapper.writeValueAsBytes(result.unwrap());
            Handler.response(exchange, response);
        } else {
            Handler.exception(exchange, result.unwrapErr());
        }
    }
}
