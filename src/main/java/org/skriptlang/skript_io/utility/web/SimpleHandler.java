package org.skriptlang.skript_io.utility.web;

import ch.njol.skript.lang.Trigger;
import com.sun.net.httpserver.HttpExchange;
import org.bukkit.Bukkit;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.event.VisitWebsiteEvent;
import org.skriptlang.skript_io.utility.task.CloseTask;

import java.net.URI;

public record SimpleHandler(WebServer server, URI path, Trigger trigger) implements PostHandler {
    
    @Override
    public void handle(HttpExchange exchange) {
        try {
            final VisitWebsiteEvent event = new VisitWebsiteEvent(server, exchange, path);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return; // this doesn't go to our handler
            this.trigger.execute(event);
        } finally {
            SkriptIO.queue().queue(new CloseTask(exchange));
        }
    }
    
}
