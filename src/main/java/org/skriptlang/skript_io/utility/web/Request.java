package org.skriptlang.skript_io.utility.web;

import org.skriptlang.skript_io.utility.Resource;

import java.io.Closeable;
import java.net.URI;

public interface Request extends Transaction {

    URI getPath();

    default void setPath(URI path) {

    }

    String getSource();

    @Override
    default void setMethod(String mode) {

    }

    @Override
    default void setContentType(String type) {

    }

}
