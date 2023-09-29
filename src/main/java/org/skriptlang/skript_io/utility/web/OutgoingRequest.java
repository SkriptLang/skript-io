package org.skriptlang.skript_io.utility.web;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.Writable;
import org.skriptlang.skript_io.utility.task.WriteTask;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public record OutgoingRequest(HttpURLConnection exchange,
                              AtomicBoolean complete) implements Writable, Closeable, Request {
    
    @Override
    public URI getPath() {
        try {
            return exchange.getURL().toURI();
        } catch (URISyntaxException e) {
            return URI.create(exchange.getURL().toString());
        }
    }
    
    @Override
    public String getSource() {
        return Bukkit.getIp();
    }
    
    @Override
    public String getMethod() {
        return exchange.getRequestMethod();
    }
    
    @Override
    public void setMethod(String mode) {
        try {
            this.exchange.setRequestMethod(mode);
        } catch (ProtocolException e) {
            SkriptIO.error(e);
        }
    }
    
    @Override
    public String getContentType() {
        return exchange.getContentType();
    }
    
    @Override
    public void setContentType(String type) {
        this.exchange.setRequestProperty("Content-Type", String.valueOf(type));
    }
    
    @Override
    public String getHeader(String header) {
        return exchange.getRequestProperty(header);
    }
    
    @Override
    public void setHeader(String header, String type) {
        this.exchange.setRequestProperty(header, type);
    }
    
    @Override
    public void close() throws IOException {
        this.exchange.disconnect();
        this.complete.set(true);
    }
    
    @Override
    public @NotNull OutputStream acquireWriter() throws IOException {
        exchange.setDoInput(true);
        return exchange.getOutputStream();
    }
    
    @Override
    public void write(String text) {
        SkriptIO.remoteQueue().queue(new WriteTask(this, text.getBytes(StandardCharsets.UTF_8)));
    }
    
    @Override
    public void clear() {
        SkriptIO.remoteQueue().queue(new WriteTask(this, new byte[0]));
    }
    
}
