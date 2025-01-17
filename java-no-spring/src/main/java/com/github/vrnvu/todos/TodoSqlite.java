package com.github.vrnvu.todos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
    private final PreparedStatement stmtDeleteTodoById;
    private final PreparedStatement stmtUpdateTodo;

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
        stmtDeleteTodoById = connection.prepareStatement("DELETE FROM todos WHERE id = ?");
        stmtUpdateTodo = connection.prepareStatement(
            "UPDATE todos SET " +
            "title = COALESCE(?, title), " +
            "completed = COALESCE(?, completed) " +
            "WHERE id = ?"
        );
    }

    @Override
    public Result<List<Todo>> getAllTodos() {
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
            return new Result.Ok<>(todos);
        } catch (SQLException e) {
            return new Result.Err<>(TodoError.SYSTEM_ERROR);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Result<Todo> getTodoById(String id) {
        lock.readLock().lock();
        logger.log(Level.INFO, "Getting todo by id {0}", id);
        try (var resultSet = stmtGetTodoById.executeQuery()) {
            stmtGetTodoById.setString(1, id);
            if (resultSet.next()) {
                return new Result.Ok<>(new Todo(
                    resultSet.getString("id"),
                    resultSet.getString("title"),
                    resultSet.getBoolean("completed"))
                );
            }
        } catch (SQLException e) {
            return new Result.Err<>(TodoError.SYSTEM_ERROR);
        } finally {
            lock.readLock().unlock();
        }
        return new Result.Err<>(TodoError.NOT_FOUND);
    }

    @Override
    public Result<Void> insertTodo(Todo todo) {
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
        } catch (SQLException e) {
            return new Result.Err<>(TodoError.SYSTEM_ERROR);
        } finally {
            lock.writeLock().unlock();
        }
        return Result.Ok.empty();
    }

    @Override
    public Result<Void> deleteTodoById(String id) {
        lock.writeLock().lock();
        try {
            logger.log(Level.INFO, "Deleting todo by id {0}", id);
            stmtDeleteTodoById.setString(1, id);
            int rows = stmtDeleteTodoById.executeUpdate();
            if (rows != 1) {
                return new Result.Err<>(TodoError.NOT_FOUND);
            }
        } catch (SQLException e) {
            return new Result.Err<>(TodoError.SYSTEM_ERROR);
        } finally {
            lock.writeLock().unlock();
        }
        return Result.Ok.empty();
    }

    @Override
    public Result<Void> patchTodo(Todo todo) {
        lock.writeLock().lock();
        try {
            logger.log(Level.INFO, "Patching todo {0}", todo);
            
            stmtUpdateTodo.setObject(1, todo.title());
            stmtUpdateTodo.setObject(2, todo.completed());
            stmtUpdateTodo.setString(3, todo.id());
            
            int updated = stmtUpdateTodo.executeUpdate();
            if (updated != 1) {
                return new Result.Err<>(TodoError.NOT_FOUND);
            }
            return Result.Ok.empty();
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating todo", e);
            return new Result.Err<>(TodoError.SYSTEM_ERROR);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
