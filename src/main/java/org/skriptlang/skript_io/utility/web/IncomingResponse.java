package org.skriptlang.skript_io.utility.web;

import mx.kenzie.clockwork.io.DataTask;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Resource;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public record IncomingResponse(HttpURLConnection exchange,
                               AtomicBoolean wasRead) implements Response, Readable, Resource, Closeable {

    public IncomingResponse(HttpURLConnection exchange) {
        this(exchange, new AtomicBoolean());
    }

    @Override
    public void close() {
        if (!wasRead.get() && exchange.getDoOutput()) SkriptIO.remoteQueue().queue(new DataTask() {
            @Override
            public void execute() throws IOException {
                try (InputStream stream = acquireReader()) {
                    while (stream.skip(Long.MAX_VALUE) == Long.MAX_VALUE) ;
                }
            }
        }).await();
        exchange.disconnect();
    }

    @Override
    public @NotNull InputStream acquireReader() throws IOException {
        try {
            wasRead.set(true);
            return exchange.getInputStream();
        } catch (FileNotFoundException ex) {
            SkriptIO.error("Unable to connect to website '" + ex.getMessage() + "'");
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public String readAll() {
        AtomicReference<String> reference = new AtomicReference<>();
        SkriptIO.remoteQueue().queue(new DataTask() {
            @Override
            public void execute() throws IOException {
                try (InputStream stream = acquireReader()) {
                    byte[] bytes = stream.readAllBytes();
                    reference.set(new String(bytes, StandardCharsets.UTF_8));
                }
            }
        }).await();
        return reference.get();
    }

    @Override
    public int statusCode() {
        try {
            return exchange.getResponseCode();
        } catch (IOException ex) {
            return 500;
        }
    }

    @Override
    public String getMethod() {
        return exchange.getRequestMethod();
    }

    @Override
    public String getContentType() {
        return exchange.getContentType();
    }

    @Override
    public String getHeader(String header) {
        return exchange.getHeaderField(header);
    }

}
