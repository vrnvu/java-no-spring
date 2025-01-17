package com.github.vrnvu.todos;

import java.nio.file.Path;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TodoSqliteTest {

    private TodoSqlite sqlite;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) throws SQLException {
        sqlite = TodoSqlite.open(tempDir.resolve("test.db").toString());
    }

    @AfterEach
    public void tearDown() throws SQLException {
        sqlite.close();
    }

    @Test
    public void whenInsertingGetAllAndGetById() throws TodoError {
        var todos = sqlite.getAllTodos();
        assertTrue(todos.isEmpty());

        var todo = sqlite.getTodoById("1");
        assertTrue(todo.isEmpty());

        sqlite.insertTodo(new Todo("1", "Test", false));
        todos = sqlite.getAllTodos();
        assertEquals(1, todos.size());

        todo = sqlite.getTodoById("1");
        assertTrue(todo.isPresent());
        assertEquals("1", todo.get().id());
        assertEquals("Test", todo.get().title());
        assertEquals(false, todo.get().completed());
    }
}
