package org.skriptlang.skript_io.elements.web.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import mx.kenzie.clockwork.io.DataTask;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.DummyCloseTrigger;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.web.IncomingResponse;
import org.skriptlang.skript_io.utility.web.OutgoingRequest;

import java.io.IOException;
import java.util.List;
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
public class SecAcceptResponse extends EffectSection {

    private static final Map<Event, Stack<IncomingResponse>> requestMap = new WeakHashMap<>();

    static {
        if (false)
            Skript.registerSection(SecAcceptResponse.class, "accept [the] response", "expect [the] response",
                                   "await [the] response");
    }

    private static Readable getCurrentRequest(Event event) {
        if (event == null) return null;
        final Stack<IncomingResponse> stack = requestMap.get(event);
        if (stack == null) return null;
        if (stack.isEmpty()) return null;
        return stack.peek();
    }

    private static void push(Event event, IncomingResponse request) {
        if (event == null || request == null) return;
        final Stack<IncomingResponse> stack;
        requestMap.putIfAbsent(event, new Stack<>());
        stack = requestMap.get(event);
        assert stack != null;
        stack.push(request);
    }

    private static void pop(Event event) {
        if (event == null) return;
        final Stack<IncomingResponse> stack = requestMap.get(event);
        if (stack == null) return;
        if (stack.isEmpty()) requestMap.remove(event);
        else stack.pop();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode,
                        @Nullable List<TriggerItem> list) {
        if (!this.getParser().isCurrentSection(SecOpenRequest.class)) {
            Skript.error("You can't use '" + result.expr + "' outside a request section.");
            return false;
        }
        if (this.hasSection()) {
            assert sectionNode != null;
            this.loadOptionalCode(sectionNode);
            if (last != null) last.setNext(null);
            this.getParser().setHasDelayBefore(Kleenean.TRUE);
        }
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        if (!Skript.getInstance().isEnabled()) return this.walk(event, false);
        Delay.addDelayedEvent(event);
        final OutgoingRequest request = SecOpenRequest.getCurrentRequest(event);
        final Object variables = Variables.copyLocalVariables(event);
        final TriggerItem next = this.walk(event, false);
        SkriptIO.remoteQueue().queue(new DataTask() {
            @Override
            public void execute() throws IOException, InterruptedException {
                try {
                    SecAcceptResponse.this.execute(event, request, variables, next);
                } catch (ExecutionException ex) {
                    SkriptIO.error(ex);
                }
            }
        });
        return null;
    }

    protected void execute(Event event, OutgoingRequest request, Object variables, TriggerItem next)
        throws ExecutionException, InterruptedException {
        final IncomingResponse response;
        if (request == null) return;
        try {
            request.exchange().connect();
        } catch (IOException ex) {
            SkriptIO.error(ex);
            return;
        }
        response = new IncomingResponse(request.exchange());
        push(event, response);
        if (first == null) { // we skip straight on
            Bukkit.getScheduler().runTask(SkriptIO.getProvidingPlugin(SkriptIO.class), () -> {
                if (variables != null)
                    Variables.setLocalVariables(event, variables);
                try {
                    response.close();
                } catch (IOException ex) {
                    SkriptIO.error(ex);
                }
                TriggerItem.walk(next, event);
            });
        } else {
            Bukkit.getScheduler().runTask(SkriptIO.getProvidingPlugin(SkriptIO.class), () -> {
                if (variables != null)
                    Variables.setLocalVariables(event, variables);
                if (last != null) last.setNext(new DummyCloseTrigger(request, next) {
                    @Override
                    protected boolean run(Event e) {
                        try {
                            response.close();
                        } catch (IOException ex) {
                            SkriptIO.error(ex);
                        }
                        pop(e);
                        return super.run(e);
                    }
                });
                TriggerItem.walk(first, event);
            });
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "accept the response";
    }

}
