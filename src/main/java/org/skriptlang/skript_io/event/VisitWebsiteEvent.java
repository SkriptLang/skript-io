package org.skriptlang.skript_io.event;

import com.sun.net.httpserver.HttpExchange;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.skriptlang.skript_io.utility.WebServer;

import java.net.URI;

public class VisitWebsiteEvent extends Event implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    private final WebServer server;
    private final HttpExchange exchange;
    private final URI root;
    private boolean cancelled;
    
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
    public HandlerList getHandlers() {
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
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    
}
