package org.skriptlang.skript_io.elements.web.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.web.OutgoingRequest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

@Name("Send Web Request")
@Description("""
    Prepares an HTTP request to be sent to a website URL. This may have content written to it.
    
    Once the request has been dispatched, the response can be read.""")
@Examples({
    "open a request to https://skriptlang.org:",
    "\tset the request's method to \"GET\"",
    "\tawait the response",
    "\tbroadcast the response's content"
})
@Since("1.0.0")
public class SecOpenRequest extends EffectSection {
    
    private static final Map<Event, Stack<OutgoingRequest>> requestMap = new WeakHashMap<>();
    
    static {
        if (!SkriptIO.isTest())
            Skript.registerSection(SecOpenRequest.class,
                "(open|send) ([a] web|[an] http) request [to] %path%"
            );
    }
    
    protected Expression<URI> pathExpression;
    
    public static OutgoingRequest getCurrentRequest(Event event) {
        if (event == null) return null;
        final Stack<OutgoingRequest> stack = requestMap.get(event);
        if (stack == null) return null;
        return stack.peek();
    }
    
    private static void push(Event event, OutgoingRequest request) {
        if (event == null || request == null) return;
        final Stack<OutgoingRequest> stack;
        requestMap.putIfAbsent(event, new Stack<>());
        stack = requestMap.get(event);
        assert stack != null;
        stack.push(request);
    }
    
    private static void pop(Event event) {
        if (event == null) return;
        final Stack<OutgoingRequest> stack = requestMap.get(event);
        if (stack == null) return;
        if (stack.isEmpty()) requestMap.remove(event);
        else stack.pop();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list) {
        if (!this.hasSection()) return false;
        this.pathExpression = (Expression<URI>) expressions[0];
        assert sectionNode != null;
        this.loadOptionalCode(sectionNode);
        if (last != null) last.setNext(null);
        return true;
    }
    
    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        final URI uri = pathExpression.getSingle(event);
        if (uri == null) return this.walk(event, false);
        final URL url;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            return this.walk(event, false);
        }
        final OutgoingRequest request;
        try {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "text/html; charset=utf-8");
            connection.setDoOutput(true);
            request = new OutgoingRequest(connection);
        } catch (IOException e) {
            SkriptIO.error(e);
            return this.walk(event, false);
        }
        assert first != null;
        push(event, request);
        try (request) {
            TriggerItem.walk(first, event); // execute the section now
        } catch (IOException ex) {
            SkriptIO.error(ex);
        } finally {
            pop(event);
        }
        return this.walk(event, false);
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "open web request to " + pathExpression.toString(event, debug);
    }
    
}
