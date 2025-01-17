package com.github.vrnvu.todos;

public enum TodoError {
    NOT_FOUND(404, "Resource not found"),
    SYSTEM_ERROR(500, "Internal system error"),
    BAD_REQUEST(400, "Bad request parameters");

    private final int code;
    private final String message;

    TodoError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}