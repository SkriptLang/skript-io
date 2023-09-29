package org.skriptlang.skript_io.elements.web.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.web.IncomingResponse;
import org.skriptlang.skript_io.utility.web.OutgoingRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

@Name("Expect Response")
@Description("Notifies a connection that you expect a response (and waits for it).")
@Examples({"open a request to https://skriptlang.org:", "\taccept the response:", "\t\tbroadcast the response's content"})
@Since("1.0.0")
public class SecAcceptResponse extends EffectSection {
    
    private static final Map<Event, Stack<IncomingResponse>> requestMap = new WeakHashMap<>();
    
    static {
        if (!SkriptIO.isTest())
            Skript.registerSection(SecAcceptResponse.class, "accept [the] response", "expect [the] response",
                "await [the] response");
    }
    
    public static Readable getCurrentRequest(Event event) {
        if (event == null) return null;
        final Stack<IncomingResponse> stack = requestMap.get(event);
        if (stack == null) return null;
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
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list) {
        if (!this.getParser().isCurrentSection(SecOpenRequest.class)) {
            Skript.error("You can't use '" + result.expr + "' outside a request section.");
            return false;
        }
        this.getParser().setHasDelayBefore(Kleenean.TRUE);
        if (this.hasSection()) {
            assert sectionNode != null;
            this.loadOptionalCode(sectionNode);
            if (last != null) last.setNext(null);
        }
        return true;
    }
    
    
    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        if (!Skript.getInstance().isEnabled()) return this.walk(event, false);
        Delay.addDelayedEvent(event);
        Object localVars = Variables.removeLocals(event);
        final TriggerItem next = walk(event, false);
        SkriptIO.queue().queue(new DataTask() {
            @Override
            public void execute() throws IOException, InterruptedException {
                if (localVars != null)
                    Variables.setLocalVariables(event, localVars);
                SecAcceptResponse.this.execute(event);
                if (next == null) return;
                Object timing = null;
                if (SkriptTimings.enabled()) {
                    final Trigger trigger = getTrigger();
                    if (trigger != null) timing = SkriptTimings.start(trigger.getDebugLabel());
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(SkriptIO.getProvidingPlugin(SkriptIO.class), () -> {
                    TriggerItem.walk(next, event);
                });
                Variables.removeLocals(event); // Clean up local vars, we may be exiting now
                SkriptTimings.stop(timing); // Stop timing if it was even started
            }
        });
        return null;
    }
    
    protected void execute(Event event) {
        final IncomingResponse response;
        final OutgoingRequest request = SecOpenRequest.getCurrentRequest(event);
        if (request == null) return;
        try {
            if (request.exchange().getDoInput()) {
                final OutputStream stream = request.exchange().getOutputStream();
                stream.flush();
                stream.close();
            }
            request.exchange().connect();
        } catch (IOException ex) {
            SkriptIO.error(ex);
            return;
        }
        response = new IncomingResponse(request.exchange());
        push(event, response);
        try (response) {
            if (first == null) return;
            Bukkit.getScheduler().scheduleSyncDelayedTask(SkriptIO.getProvidingPlugin(SkriptIO.class), () -> {
                if (last != null) last.setNext(null);
                TriggerItem.walk(first, event);
            });
        } catch (IOException ex) {
            SkriptIO.error(ex);
        } finally {
            pop(event);
        }
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "accept the response";
    }
    
}
