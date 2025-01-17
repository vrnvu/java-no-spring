package com.github.vrnvu.todos;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

public record Todo(
    String id,
    @JsonSetter(nulls = Nulls.SKIP) 
    String title,
    @JsonSetter(nulls = Nulls.SKIP) 
    Boolean completed
) {}