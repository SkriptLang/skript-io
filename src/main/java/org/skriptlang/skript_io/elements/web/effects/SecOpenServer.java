package org.skriptlang.skript_io.elements.web.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.event.VisitWebsiteEvent;
import org.skriptlang.skript_io.utility.web.PostHandler;
import org.skriptlang.skript_io.utility.web.SimpleHandler;
import org.skriptlang.skript_io.utility.web.WebServer;

import java.net.URI;
import java.util.List;

@Name("Open Website")
@Description("""
    Opens a website at the provided path and port, defaulting to the root path `/` on port 80.
    Whenever a request is received, the code inside the section will be run.
        
    Responses to a request should start by sending a status code (e.g. 200 = OK) and then any data.
        
    Website paths should end in a separator `/`, and will handle any requests to their directory.
    A website on the root path `/` will accept any unhandled requests.
        
    Multiple websites cannot be opened on the same port and path.
    Multiple websites can be opened on *different* paths with the same port, such as `/foo/` and `/bar/`
    """)
@Examples({
    "open a website on port 12345:",
    "\tset the status code to 200",
    "\tadd \"<body>\" to the response",
    "\tadd \"<h1>hello!!!</h1>\" to the response",
    "\tadd \"<p>there are %size of all players% players online</p>\" to the response",
    "\tadd \"</body>\" to the response"
})
@Since("1.0.0")
public class SecOpenServer extends EffectSection {

    static {
        if (!SkriptIO.isTest())
            Skript.registerSection(SecOpenServer.class,
                                   "open ([a] web[ ]|[an] http )server [for %-path%] [(on|with) port %-number%]",
                                   "open [a] web[ ]site [for %-path%] [(on|with) port %-number%]"
                                  );
    }

    protected @Nullable Expression<URI> pathExpression;

    protected @Nullable Expression<Number> portExpression;
    protected Trigger trigger;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode,
                        @Nullable List<TriggerItem> list) {
        if (this.getParser().isCurrentSection(SecOpenServer.class)
            || this.getParser().isCurrentEvent(VisitWebsiteEvent.class)) {
            Skript.error("You can't open a webserver inside a webserver response.");
            return false;
        }
        if (!this.hasSection()) {
            Skript.error("A webserver requires a response section.");
            return false;
        }
        this.pathExpression = (Expression<URI>) expressions[0];
        this.portExpression = (Expression<Number>) expressions[1];
        if (portExpression != null
            && portExpression instanceof Literal<Number> literal
            && !this.validatePort(literal.getSingle().intValue())) {
            Skript.error("Valid webserver ports are between 1 and 65535.");
            return false;
        }
        assert sectionNode != null;
        this.trigger = this.loadCode(sectionNode, "website visit event", VisitWebsiteEvent.class);
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        final URI path;
        final int port;
        if (pathExpression != null) path = pathExpression.getSingle(event);
        else path = SkriptIO.ROOT;
        if (path == null) return this.walk(event, false);
        if (portExpression != null) {
            final Number number = portExpression.getSingle(event);
            if (number == null) port = WebServer.DEFAULT_PORT;
            else port = number.intValue();
        } else port = WebServer.DEFAULT_PORT;
        if (!this.validatePort(port)) {
            SkriptIO.error("Invalid webserver port provided, port must be between 1 and 65535.");
            return this.walk(event, false);
        }
        if (!this.validatePath(path)) return this.walk(event, false);
        final WebServer server = WebServer.getOrCreate(port);
        server.registerHandler(path, this.createHandler(server, path));
        server.prepareIfNecessary();
        return this.walk(event, false);
    }

    protected PostHandler createHandler(WebServer server, URI path) {
        assert first != null;
        return new SimpleHandler(server, path, trigger);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "open a website"
            + (pathExpression != null ? " for " + pathExpression.toString(event, debug) : "")
            + (portExpression != null ? " with port " + portExpression.toString(event, debug) : "");
    }

    private boolean validatePath(URI path) {
        if (path == null) return false;
        final String string = path.toString();
        if (string.equals("/")) return true;
        if (string.isEmpty() || string.isBlank()) {
            SkriptIO.error("Webserver path must not be blank.");
            return false;
        }
        if (!string.startsWith("/")) {
            SkriptIO.error("Webserver path `" + string + " ` should start with a slash `/` character.");
            return false;
        }
        if (!string.endsWith("/")) {
            SkriptIO.error("Webserver path `" + string + " ` should end with a slash `/` character.");
            return false;
        }
        if (string.contains(" ")) {
            SkriptIO.error("Webserver path `" + string + " ` must not contain whitespace.");
            return false;
        }
        return true;
    }

    private boolean validatePort(int port) {
        return port > 0 && port < 65535;
    }

}
