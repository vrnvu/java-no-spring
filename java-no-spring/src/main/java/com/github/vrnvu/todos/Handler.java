package com.github.vrnvu.todos;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public class Handler {
    public static void exception(HttpExchange exchange, TodoError e) throws IOException {
        try (exchange) {
            switch (e) {
                case BAD_REQUEST -> {
                    exchange.sendResponseHeaders(400, 0);
                }
                case NOT_FOUND -> {
                    exchange.sendResponseHeaders(404, 0);
                }
                case SYSTEM_ERROR -> {
                    exchange.sendResponseHeaders(500, 0);
                }
            }
        }
    }

    public static void response(HttpExchange exchange, byte[] response) throws IOException {
        try (exchange) {
            if (response.length > 0) {
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            } else {
                exchange.sendResponseHeaders(204, 0);
            }
        }
    }
}
