package org.skriptlang.skript_io.utility.web;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Resource;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public record IncomingResponse(HttpURLConnection exchange) implements Readable, Resource, Closeable {
    
    @Override
    public void close() throws IOException {
        this.exchange.getInputStream().close();
    }
    
    @Override
    public @NotNull InputStream acquireReader() throws IOException {
        return exchange.getInputStream();
    }
    
    public int statusCode() {
        try {
            return exchange.getResponseCode();
        } catch (IOException ex) {
            return 500;
        }
    }
    
}
