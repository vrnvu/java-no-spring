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
    public void whenInsertingGetAllAndGetById() {
        var todos = sqlite.getAllTodos();
        assertTrue(todos.isOk());
        assertTrue(todos.unwrap().isEmpty());

        var todo = sqlite.getTodoById("1");
        assertTrue(todo.isErr());
        assertEquals(TodoError.NOT_FOUND, todo.unwrapErr());

        sqlite.insertTodo(new Todo("1", "Test", false));
        todos = sqlite.getAllTodos();
        assertTrue(todos.isOk());
        assertEquals(1, todos.unwrap().size());

        todo = sqlite.getTodoById("1");
        assertTrue(todo.isOk());
        assertEquals("1", todo.unwrap().id());
        assertEquals("Test", todo.unwrap().title());
        assertEquals(false, todo.unwrap().completed());
    }
}
