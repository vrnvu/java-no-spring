package com.github.vrnvu.todos;

public sealed abstract class TodoError extends Exception permits TodoError.NotFound, TodoError.SystemError {
    public enum Type {
        NOT_FOUND,
        SYSTEM_ERROR
    }

    private final Type type;

    protected TodoError(String message, Type type) {
        super(message);
        this.type = type;
    }
    
    protected TodoError(String message, Throwable cause, Type type) {
        super(message, cause);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public static final class NotFound extends TodoError {
        public NotFound(String message) {
            super(message, Type.NOT_FOUND);
        }
    }
    
    public static final class SystemError extends TodoError {
        public SystemError(String message, Throwable cause) {
            super(message, cause, Type.SYSTEM_ERROR);
        }
    }
}