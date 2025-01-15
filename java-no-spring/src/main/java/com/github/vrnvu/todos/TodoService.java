package com.github.vrnvu.todos;

import java.util.List;
import java.util.Optional;

public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> getTodos() {
        return todoRepository.getTodos();
    }

    public Optional<Todo> getTodo(String id) {
        return todoRepository.getTodo(id);
    }
    
}
