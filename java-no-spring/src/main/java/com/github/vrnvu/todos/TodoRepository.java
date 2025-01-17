package com.github.vrnvu.todos;

import java.util.List;

public interface TodoRepository {
    Result<List<Todo>> getAllTodos();
    Result<Todo> getTodoById(String id);
    Result<Void> insertTodo(Todo todo);
    Result<Void> deleteTodoById(String id);
    Result<Void> patchTodo(Todo todo);
}
