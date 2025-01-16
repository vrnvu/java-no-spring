package com.github.vrnvu.todos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TodoSqlite implements AutoCloseable, TodoRepository {

    private static final Logger logger = Logger.getLogger(TodoSqlite.class.getName());
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Connection connection;
    private final PreparedStatement stmtGetAllTodos;
    private final PreparedStatement stmtGetTodoById;
    private final PreparedStatement stmtInsertTodo;

    public static TodoSqlite open(String path) throws SQLException {
        return new TodoSqlite(path);
    }

    @Override
    public void close() throws SQLException {
        connection.close();
        logger.info("Connection to SQLite has been closed.");
    }

    private TodoSqlite(String path) throws SQLException {
        var url = "jdbc:sqlite:" + path;
        connection = DriverManager.getConnection(url);
        connection.createStatement()
            .execute("CREATE TABLE IF NOT EXISTS todos (id TEXT PRIMARY KEY, title TEXT, completed BOOLEAN)");
        stmtGetAllTodos = connection.prepareStatement("SELECT * FROM todos");
        stmtGetTodoById = connection.prepareStatement("SELECT * FROM todos WHERE id = ?");
        stmtInsertTodo = connection.prepareStatement("INSERT OR REPLACE INTO todos (id, title, completed) VALUES (?, ?, ?)");
    }

    @Override
    public List<Todo> getAllTodos() throws SQLException {
        lock.readLock().lock();
        logger.log(Level.INFO, "Getting all todos");
        try (var resultSet = stmtGetAllTodos.executeQuery()) {
            var todos = new ArrayList<Todo>();
            while (resultSet.next()) {
                todos.add(new Todo(
                    resultSet.getString("id"),
                    resultSet.getString("title"),
                    resultSet.getBoolean("completed"))
                );
            }
            return todos;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Todo> getTodoById(String id) throws SQLException {
        lock.readLock().lock();
        logger.log(Level.INFO, "Getting todo by id {0}", id);
        stmtGetTodoById.setString(1, id);
        try (var resultSet = stmtGetTodoById.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(new Todo(
                    resultSet.getString("id"),
                    resultSet.getString("title"),
                    resultSet.getBoolean("completed"))
                );
            }
        } finally {
            lock.readLock().unlock();
        }
        return Optional.empty();
    }

    @Override
    public void insertTodo(Todo todo) throws SQLException {
        lock.writeLock().lock();
        try {
            logger.log(Level.INFO, "Inserting todo {0}", todo);
            stmtInsertTodo.setString(1, todo.id());
            stmtInsertTodo.setString(2, todo.title());
            stmtInsertTodo.setBoolean(3, todo.completed());
            int rows = stmtInsertTodo.executeUpdate();
            if (rows != 1) {
                throw new SQLException("Failed to insert todo");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
}
