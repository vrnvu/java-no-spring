package com.github.vrnvu.todos;

import java.io.IOException;
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
            default -> exchange.sendResponseHeaders(405, 0);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        if ("/todos".equals(path)) {
            handleGetTodos(exchange);
            return;
        }
        
        if (!path.startsWith("/todos/")) {
            exchange.sendResponseHeaders(404, 0);
            return;
        }
        
        String id = path.substring("/todos/".length());
        if (id.contains("/")) {
            exchange.sendResponseHeaders(404, 0);
            return;
        }
        
        handleGetTodo(exchange, id);
    }

    private void handleGetTodo(HttpExchange exchange, String id) throws IOException {
        var todo = todoService.getTodo(id);
        if (todo.isEmpty()) {
            exchange.sendResponseHeaders(404, 0);
            return;
        }

        try (var responseBody = exchange.getResponseBody()) {
            var response = objectMapper.writeValueAsBytes(todo.get());
            exchange.sendResponseHeaders(200, response.length);
            responseBody.write(response);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error serializing todo", e);
            exchange.sendResponseHeaders(500, 0);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing response", e);
            exchange.sendResponseHeaders(500, 0);
        }
    }

    private void handleGetTodos(HttpExchange exchange) throws IOException {
        var todos = todoService.getTodos();
        try (var responseBody = exchange.getResponseBody()) {
            var response = objectMapper.writeValueAsBytes(todos);
            exchange.sendResponseHeaders(200, response.length);
            responseBody.write(response);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error serializing todos", e);
            exchange.sendResponseHeaders(500, 0);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing response", e);
            exchange.sendResponseHeaders(500, 0);
        }
    }
}
