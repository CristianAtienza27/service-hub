package com.servicehub.auth.domain;

public class DuplicateSlugException extends RuntimeException {

    public DuplicateSlugException(String slug) {
        super("A business with slug '" + slug + "' already exists");
    }
}