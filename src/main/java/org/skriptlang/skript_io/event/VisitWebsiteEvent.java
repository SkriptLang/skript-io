package org.skriptlang.skript_io.event;

import com.sun.net.httpserver.HttpExchange;
import mx.kenzie.clockwork.io.DataTask;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.web.WebServer;

import java.io.IOException;
import java.net.URI;

public class VisitWebsiteEvent extends Event implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    private final WebServer server;
    private final HttpExchange exchange;
    private final URI root;
    private boolean cancelled;
    private int statusCode = -1;
    
    public VisitWebsiteEvent(WebServer server, HttpExchange exchange, URI root) {
        super(true);
        this.server = server;
        this.exchange = exchange;
        this.root = root;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    
    public WebServer getServer() {
        return server;
    }
    
    public HttpExchange getExchange() {
        return exchange;
    }
    
    public URI getHandlerRoot() {
        return root;
    }
    
    public boolean isStatusCodeSet() {
        return statusCode != -1;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int code) {
        if (this.isStatusCodeSet()) return;
        this.statusCode = code;
        SkriptIO.queue().queue(new DataTask() {
            @Override
            public void execute() throws IOException {
                exchange.sendResponseHeaders(code, 0);
            }
        });
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    
}
