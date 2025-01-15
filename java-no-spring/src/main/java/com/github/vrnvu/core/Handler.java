package com.github.vrnvu.core;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public class Handler {
    public static void statusCode(HttpExchange exchange, int statusCode) throws IOException {
        try (exchange) {
            exchange.sendResponseHeaders(statusCode, 0);
        }
    }

    public static void response(HttpExchange exchange, byte[] response) throws IOException {
        try (exchange) {
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
        }
    }
}
