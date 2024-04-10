package org.skriptlang.skript_io.utility.web;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.utility.Readable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public record IncomingRequest(HttpExchange exchange) implements Readable, Closeable, Request {

    @Override
    public @NotNull InputStream acquireReader() throws IOException {
        return exchange.getRequestBody();
    }

    @Override
    public URI getPath() {
        return exchange.getRequestURI();
    }

    @Override
    public void setPath(URI path) {
        Request.super.setPath(path);
    }

    @Override
    public String getSource() {
        return exchange.getRemoteAddress().getHostString();
    }

    @Override
    public String getMethod() {
        return exchange.getRequestMethod();
    }

    @Override
    public void setMethod(String mode) {
        Request.super.setMethod(mode);
    }

    @Override
    public String getContentType() {
        return exchange.getRequestHeaders().getFirst("Content-Type");
    }

    @Override
    public void setContentType(String type) {
        Request.super.setContentType(type);
    }

    @Override
    public String getHeader(String header) {
        return exchange.getRequestHeaders().getFirst(header);
    }

    @Override
    public void setHeader(String header, String type) {
        this.exchange.getRequestHeaders().set(header, type);
    }

    @Override
    public void close() throws IOException {
        this.exchange.close();
    }

}
