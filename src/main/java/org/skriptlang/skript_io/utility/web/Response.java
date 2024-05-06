package org.skriptlang.skript_io.utility.web;

public interface Response extends Transaction {

    @Override
    default void setMethod(String mode) {

    }

    @Override
    default void setContentType(String type) {

    }

    @Override
    default void setHeader(String header, String type) {

    }

    int statusCode();

    default void setStatusCode(int status) {

    }

}
