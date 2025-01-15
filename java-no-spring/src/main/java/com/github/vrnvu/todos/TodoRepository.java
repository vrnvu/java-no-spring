package com.github.vrnvu.todos;

import java.util.List;
import java.util.Optional;

public class TodoRepository {

    public Optional<Todo> getTodo(String id) {
        return Optional.of(new Todo(id, "Buy groceries", false));
    }

    public List<Todo> getTodos() {
        return List.of(
                new Todo("1", "Buy groceries", false),
                new Todo("2", "Do laundry", false),
                new Todo("3", "Finish homework", false)
        );
    }
    
}
