package org.skriptlang.skript_io.utility.web;

import mx.kenzie.clockwork.io.DataTask;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Resource;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public record IncomingResponse(HttpURLConnection exchange) implements Readable, Resource, Closeable {
    
    @Override
    public void close() throws IOException {
        this.exchange.disconnect();
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
    
    @Override
    public String readAll() {
        final AtomicReference<String> reference = new AtomicReference<>();
        SkriptIO.remoteQueue().queue(new DataTask() {
            @Override
            public void execute() throws IOException {
                try (final InputStream stream = acquireReader()) {
                    final byte[] bytes = stream.readAllBytes();
                    reference.set(new String(bytes, StandardCharsets.UTF_8));
                }
            }
        }).await();
        return reference.get();
    }
    
}
