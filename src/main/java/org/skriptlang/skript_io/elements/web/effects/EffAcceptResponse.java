package org.skriptlang.skript_io.elements.web.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import mx.kenzie.clockwork.io.DataTask;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.elements.web.sections.SecOpenRequest;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.web.IncomingResponse;
import org.skriptlang.skript_io.utility.web.OutgoingRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

@Name("Expect Response")
@Description("""
    Notifies a connection that you expect a response (and waits for it).
    
    Accepting a response marks the outgoing connection as complete
    (e.g. you cannot send more data in the request) and anything waiting to be sent will be dispatched.
    """)
@Example("""
    open a request to https://skriptlang.org:
        expect the response
        broadcast the response's content
    """)
@Example("""
    open a request to http://my-api-here:
        set the request's json content to {_data::*}
        accept the response
        set {_result::*} to the response's json content
    # {_result::*} is available here
    """)
@Since("1.0.0")
public class EffAcceptResponse extends AsyncEffect {

    private static final Map<Event, Stack<IncomingResponse>> responseMap = new WeakHashMap<>();

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerEffect(EffAcceptResponse.class,
                    "accept [the|a] response",
                    "expect [the|a] response",
                    "await [the|a] response",
                    "wait for [the|a] response");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean delayed,
                        SkriptParser.ParseResult result) {
        getParser().setHasDelayBefore(Kleenean.TRUE);
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        SkriptIO.remoteQueue().queue(new DataTask() {
            @Override
            public void execute() {
                OutgoingRequest request = SecOpenRequest.getCurrentRequest(event);
                if (request == null) {
                    return;
                }

                try {
                    request.exchange().connect();
                } catch (IOException ex) {
                    SkriptIO.error(ex);
                    return;
                }
                IncomingResponse response = new IncomingResponse(request.exchange());
                push(event, response);
            }
        }).await();
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "accept the response";
    }

    public static Readable getCurrentResponse(Event event) {
        if (event == null) {
            return null;
        }
        Stack<IncomingResponse> stack = responseMap.get(event);
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        IncomingResponse response = stack.peek();
        if (stack.isEmpty()) {
            responseMap.remove(event);
        }

        return response;
    }

    private static void push(Event event, IncomingResponse request) {
        if (event == null || request == null) {
            return;
        }
        Stack<IncomingResponse> stack;
        responseMap.putIfAbsent(event, new Stack<>());
        stack = responseMap.get(event);
        assert stack != null;
        stack.push(request);
    }

    public static void pop(Event event) {
        if (event == null) {
            return;
        }
        Stack<IncomingResponse> stack = responseMap.get(event);
        if (stack == null) {
            return;
        }
        if (stack.isEmpty()) {
            responseMap.remove(event);
        } else {
            IncomingResponse response = stack.pop();
            response.close();
        }
    }

}
