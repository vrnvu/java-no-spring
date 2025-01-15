package com.github.vrnvu;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vrnvu.todos.TodoHandler;
import com.github.vrnvu.todos.TodoService;
import com.github.vrnvu.todos.TodoSqlite;
import com.sun.net.httpserver.HttpServer;

public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws IOException, SQLException {
        var port = 8080;
        var server = HttpServer.create(new InetSocketAddress(port), 0);

        // virtual threads
        // server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.setExecutor(Executors.newWorkStealingPool());

        server.createContext("/todos", new TodoHandler(
            new ObjectMapper(),
            new TodoService(
                TodoSqlite.open("todos.db")
            )
        ));

        logger.log(Level.INFO, "Server started on port {0}", port);
        server.start();
    }
}