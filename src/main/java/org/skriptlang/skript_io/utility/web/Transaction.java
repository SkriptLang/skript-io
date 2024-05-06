package org.skriptlang.skript_io.utility.web;

import org.skriptlang.skript_io.utility.Resource;

import java.io.Closeable;

public interface Transaction extends Resource, Closeable {

    String getMethod();

    void setMethod(String mode);

    String getContentType();

    void setContentType(String type);

    String getHeader(String header);

    void setHeader(String header, String type);

}
