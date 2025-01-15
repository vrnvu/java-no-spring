package com.github.vrnvu;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpServer;

/**
 * Hello world!
 */
public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws IOException {
        var port = 8080;
        var server = HttpServer.create(new InetSocketAddress(port), 0);

        // virtual threads
        // server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.setExecutor(Executors.newWorkStealingPool());

        server.createContext("/", (exchange) -> {
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("Hello World!".getBytes());
            exchange.getResponseBody().close();
        });
        logger.log(Level.INFO, "Server started on port {0}", port);
        server.start();
    }
}