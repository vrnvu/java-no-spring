package com.github.vrnvu.todos;

public sealed interface Result<T> permits Result.Ok, Result.Err {
    record Ok<T>(T value) implements Result<T> {
        public static Result<Void> empty() {
            return new Ok<>(null);
        }
    }
    record Err<T>(TodoError error) implements Result<T> {}

    default boolean isOk() {
        return this instanceof Ok;
    }

    default boolean isErr() {
        return this instanceof Err;
    }

    default T unwrap() {
        if (this instanceof Ok<T> ok) {
            return ok.value();
        }
        throw new RuntimeException("Called unwrap on an Err value");
    }

    default TodoError unwrapErr() {
        if (this instanceof Err<T> err) {
            return err.error();
        }
        throw new RuntimeException("Called unwrapErr on an Ok value");
    }
}