package org.skriptlang.skript_io.elements.web.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.*;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import mx.kenzie.clockwork.io.DataTask;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.web.IncomingResponse;
import org.skriptlang.skript_io.utility.web.OutgoingRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

@Name("Expect Response")
@Description("""
    Notifies a connection that you expect a response (and waits for it).
    
    Accepting a response marks the outgoing connection as complete
    (e.g. you cannot send more data in the request) and anything waiting to be sent will be dispatched.
    """)
@Examples({
    """
    open a request to https://skriptlang.org:
        expect the response
        broadcast the response's content""",
    """
    open a request to http://my-api-here:
        set the request's json content to {_data::*}
        accept the response
        set {_result::*} to the response's json content
    # {_result::*} is available here"""
})
@Since("1.0.0")
public class EffAcceptResponse extends Effect {

    private static final Map<Event, Stack<IncomingResponse>> responseMap = new WeakHashMap<>();

    static {
        if (!SkriptIO.isTest()) Skript.registerEffect(EffAcceptResponse.class,
            "accept [the|a] response",
            "expect [the|a] response",
            "await [the|a] response",
            "wait for [the|a] response");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean delayed,
                        SkriptParser.ParseResult result) {
        this.getParser().setHasDelayBefore(Kleenean.TRUE);
        return true;
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        this.debug(event, true);
        long start = Skript.debug() ? System.currentTimeMillis() : 0;
        TriggerItem next = this.getNext();
        if (next == null || !Skript.getInstance().isEnabled()) return null;
        Delay.addDelayedEvent(event);
        OutgoingRequest request = SecOpenRequest.getCurrentRequest(event);
        if (request == null) return null;

        // Back up local variables
        Object variables = Variables.removeLocals(event);

        SkriptIO.remoteQueue().queue(new DataTask() {
            @Override
            public void execute() throws InterruptedException {
                try {
                    EffAcceptResponse.this.execute(event, request, variables, next, start);
                } catch (ExecutionException ex) {
                    SkriptIO.error(ex);
                }
            }
        });
        return null;
    }

    protected void execute(Event event, OutgoingRequest request, Object variables, TriggerItem next, long start)
        throws ExecutionException, InterruptedException {
        IncomingResponse response;
        try {
            request.exchange().connect();
        } catch (IOException ex) {
            SkriptIO.error(ex);
            return;
        }
        response = new IncomingResponse(request.exchange());
        push(event, response);

        Bukkit.getScheduler().runTask(SkriptIO.getProvidingPlugin(SkriptIO.class), () -> {
            Skript.debug(getIndentation() + "... continuing after " + (System.currentTimeMillis() - start) + "ms");

            // Re-set local variables
            if (variables != null)
                Variables.setLocalVariables(event, variables);

            Object timing = null; // Timings reference must be kept so that it can be stopped after TriggerItem
            // execution
            if (SkriptTimings.enabled()) { // getTrigger call is not free, do it only if we must
                Trigger trigger = getTrigger();
                if (trigger != null)
                    timing = SkriptTimings.start(trigger.getDebugLabel());
            }

            TriggerItem.walk(next, event);
            Variables.removeLocals(event); // Clean up local vars, we may be exiting now

            SkriptTimings.stop(timing); // Stop timing if it was even started
        }); // Minimum delay is one tick, less than it is useless!
    }

    @Override
    protected void execute(@NotNull Event event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "expect the response";
    }

    public static Readable getCurrentResponse(Event event) {
        if (event == null) return null;
        Stack<IncomingResponse> stack = responseMap.get(event);
        if (stack == null) return null;
        if (stack.isEmpty()) return null;
        return stack.peek();
    }

    private static void push(Event event, IncomingResponse request) {
        if (event == null || request == null) return;
        Stack<IncomingResponse> stack;
        responseMap.putIfAbsent(event, new Stack<>());
        stack = responseMap.get(event);
        assert stack != null;
        stack.push(request);
    }

    static void pop(Event event) {
        if (event == null) return;
        Stack<IncomingResponse> stack = responseMap.get(event);
        if (stack == null) return;
        if (stack.isEmpty()) responseMap.remove(event);
        else {
            IncomingResponse response = stack.pop();
            try {
                response.close();
            } catch (IOException ignored) { // we can't actually catch an error from this
            }
        }
    }

}
