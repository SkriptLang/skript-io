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
    public String getSource() {
        return exchange.getRemoteAddress().getHostString();
    }
    
    @Override
    public String setMethod() {
        return exchange.getRequestMethod();
    }
    
    @Override
    public void close() throws IOException {
        this.exchange.close();
    }
    
}
