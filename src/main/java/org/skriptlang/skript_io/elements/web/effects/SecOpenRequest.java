package org.skriptlang.skript_io.elements.web.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.DummyCloseTrigger;
import org.skriptlang.skript_io.utility.task.CloseTask;
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
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Send Web Request")
@Description("""
    Prepares an HTTP request to be sent to a website URL. This may have content written to it.
    
    Once the request has been dispatched, the response can be read using the `accept the response` section.""")
@Example("""
        open a web request to https://skriptlang.org:
            set the request's method to "GET"
            await the response
            broadcast the response's text content
        """)
@Example("""
        open an http request to http://my-api-here:
            set the request's json content to {_data::*}
        """)
@Since("1.0.0")
public class SecOpenRequest extends EffectSection {

    private static final Map<Event, Stack<OutgoingRequest>> requestMap = new WeakHashMap<>();

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerSection(SecOpenRequest.class, "(open|send) ([a] web|[an] http) request [to] %path%");
    }

    protected Expression<URI> pathExpression;

    public static OutgoingRequest getCurrentRequest(Event event) {
        if (event == null) {
            return null;
        }
        Stack<OutgoingRequest> stack = requestMap.get(event);
        if (stack == null) {
            return null;
        }
        if (stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }

    private static void push(Event event, OutgoingRequest request) {
        if (event == null || request == null) {
            return;
        }
        Stack<OutgoingRequest> stack;
        requestMap.putIfAbsent(event, new Stack<>());
        stack = requestMap.get(event);
        assert stack != null;
        stack.push(request);
    }

    private static void pop(Event event) {
        if (event == null) {
            return;
        }
        Stack<OutgoingRequest> stack = requestMap.get(event);
        if (stack == null) {
            return;
        }
        if (stack.isEmpty()) {
            requestMap.remove(event);
        } else {
            stack.pop();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode,
                        @Nullable List<TriggerItem> list) {
        if (!hasSection()) {
            return false;
        }
        pathExpression = (Expression<URI>) expressions[0];
        assert sectionNode != null;
        loadOptionalCode(sectionNode);
        if (last != null) {
            last.setNext(null);
        }
        getParser().setHasDelayBefore(Kleenean.TRUE);
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        URI uri = pathExpression.getSingle(event);
        if (uri == null) {
            return walk(event, false);
        }
        if (!Skript.getInstance().isEnabled()) {
            return walk(event, false);
        }
        OutgoingRequest request = createRequest(uri);
        if (request == null || first == null) {
            return walk(event, false);
        }
        if (last != null) {
            last.setNext(new DummyCloseTrigger(request, walk(event, false)) {
                @Override
                protected boolean run(Event e) {
                    EffAcceptResponse.pop(e);
                    pop(event);
                    return super.run(e);
                }
            });
            push(event, request);
            TriggerItem.walk(first, event); // execute the section now
            return null; // the pop is done in our close dummy
        } else {
            try {
                push(event, request);
                TriggerItem.walk(first, event); // execute the section now
            } finally {
                EffAcceptResponse.pop(event);
                pop(event);
                SkriptIO.queue(new CloseTask(request));
            }
            return walk(event, false);
        }
    }

    protected OutgoingRequest createRequest(URI uri) {
        URL url;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            SkriptIO.error(e);
            return null;
        }
        OutgoingRequest request;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "text/html; charset=utf-8");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);
            connection.setDoOutput(true);
            request = new OutgoingRequest(connection, new AtomicBoolean(false));
        } catch (IOException e) {
            SkriptIO.error(e);
            return null;
        }
        return request;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "open web request to " + pathExpression.toString(event, debug);
    }

}
