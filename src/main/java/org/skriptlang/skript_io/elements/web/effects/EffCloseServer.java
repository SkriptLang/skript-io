package org.skriptlang.skript_io.elements.web.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.event.VisitWebsiteEvent;
import org.skriptlang.skript_io.utility.web.WebServer;

import java.net.URI;

@Name("Close Website")
@Description("""
    Closes the website at a given path and/or port, or the current website.
    The website will stop accepting connections.
    
    Any currently-open tasks may continue to run in the background.
    
    After closing a website, its path will become available for reuse.
    After closing all websites running on a port, it may take a little time before the
    operating system frees the port for reuse.
    """)
@Examples({
    """
    open a website for /landing/:
        transfer ./site/welcome.html to the response
        close the current website"""
})
@Since("1.0.0")
public class EffCloseServer extends Effect {

    static {
        if (!SkriptIO.isTest())
            Skript.registerEffect(EffCloseServer.class,
                                  "close [the] (web[ ]|http )server [at %-path%] [(on|with) port %-number%]",
                                  "close [the] web[ ]site [at %-path%] [(on|with) port %-number%]",
                                  "close [the] [current] web[ ](site|server)");
    }

    protected @Nullable Expression<URI> pathExpression;
    protected @Nullable Expression<Number> portExpression;
    protected boolean current;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result) {
        if (matchedPattern == 2) {
            if (!this.getParser().isCurrentEvent(VisitWebsiteEvent.class)) {
                Skript.error("You can't use '" + result.expr + "' outside a website section.");
                return false;
            }
            this.current = true;
        } else {
            this.pathExpression = (Expression<URI>) expressions[0];
            this.portExpression = (Expression<Number>) expressions[1];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        if (current && event instanceof VisitWebsiteEvent visit) {
            visit.getServer().closeHandler(visit.getHandlerRoot());
        }
        boolean hasPath, hasPort;
        URI uri = null;
        int port = 0;
        path:
        if (pathExpression != null) {
            uri = pathExpression.getSingle(event);
            if (uri == null) {
                hasPath = false;
                break path;
            }
            hasPath = true;
        } else hasPath = false;
        port:
        if (portExpression != null) {
            Number number = portExpression.getSingle(event);
            if (number == null) {
                hasPort = false;
                break port;
            }
            hasPort = true;
            port = number.intValue();
        } else hasPort = false;
        WebServer server;
        if (hasPort) server = WebServer.get(port);
        else server = WebServer.get(WebServer.DEFAULT_PORT);
        if (server == null) return;
        if (hasPath) server.closeHandler(uri);
        else server.closeAll();
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        if (current) return "close the current website";
        return "close the website"
            + (pathExpression != null ? "at " + pathExpression.toString(event, debug) : "")
            + (portExpression != null ? "with port " + portExpression.toString(event, debug) : "");
    }

}
