package org.skriptlang.skript_io.utility.web;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.event.VisitWebsiteEvent;
import org.skriptlang.skript_io.utility.Writable;
import org.skriptlang.skript_io.utility.task.CloseTask;
import org.skriptlang.skript_io.utility.task.WriteTask;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public record OutgoingResponse(VisitWebsiteEvent event, com.sun.net.httpserver.HttpExchange exchange)
    implements Writable, Closeable, Response {

    @Override
    public String getMethod() {
        return exchange.getRequestMethod();
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void setContentType(String type) {
        exchange.getResponseHeaders().set("Content-Type", String.valueOf(type));
    }

    @Override
    public String getHeader(String header) {
        List<String> strings = exchange.getResponseHeaders().get(String.valueOf(header));
        if (strings == null || strings.isEmpty()) return null;
        return strings.getFirst();
    }

    @Override
    public void setHeader(String header, String type) {
        if (header == null) return;
        exchange.getResponseHeaders().set(header, String.valueOf(type));
    }

    @Override
    public void close() throws IOException {
        SkriptIO.queue(new CloseTask(exchange));
    }

    @Override
    public void write(String text) {
        SkriptIO.queue().queue(new WriteTask(this, text.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void clear() {
        SkriptIO.queue().queue(new WriteTask(this, new byte[0]));
    }

    @Override
    public @NotNull OutputStream acquireWriter() throws IOException {
        if (event != null && !event.isStatusCodeSet())
            setStatusCode(200);
        return exchange.getResponseBody();
    }

    @Override
    public int statusCode() {
        if (event != null) return event.getStatusCode();
        return exchange.getResponseCode();
    }

    @Override
    public void setStatusCode(int status) {
        try {
            exchange.sendResponseHeaders(status, 0);
        } catch (IOException ex) {
            SkriptIO.error(ex);
        }
    }

}
