package com.github.vrnvu.todos;

import java.util.List;

public class TodoRepository {

    public List<Todo> getTodos() {
        return List.of(
                new Todo("1", "Buy groceries", false),
                new Todo("2", "Do laundry", false),
                new Todo("3", "Finish homework", false)
        );
    }
    
}
