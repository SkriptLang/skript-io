package org.skriptlang.skript_io.utility.web;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.Writable;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;

public record OutgoingRequest(HttpURLConnection exchange) implements Writable, Closeable, Request {
    
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
    public void close() throws IOException {
        this.exchange.getErrorStream().close();
        this.exchange.getInputStream().close();
        this.exchange.getOutputStream().close();
        this.exchange.disconnect();
    }
    
    @Override
    public @NotNull OutputStream acquireWriter() throws IOException {
        return exchange.getOutputStream();
    }
    
}
