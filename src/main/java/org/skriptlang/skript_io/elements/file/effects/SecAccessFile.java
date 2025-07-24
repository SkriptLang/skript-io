package org.skriptlang.skript_io.elements.file.effects;

import ch.njol.skript.config.SectionNode;
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
import org.skriptlang.skript_io.utility.file.FileController;
import org.skriptlang.skript_io.utility.task.CloseTask;

import java.net.URI;
import java.util.List;

public abstract class SecAccessFile extends EffectSection {

    protected Expression<URI> pathExpression;
    protected boolean async;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode,
                        @Nullable List<TriggerItem> list) {
        pathExpression = (Expression<URI>) expressions[0];
        if (hasSection()) {
            assert sectionNode != null;
            Kleenean isDelayed;
            getParser().setHasDelayBefore(Kleenean.FALSE);
            loadOptionalCode(sectionNode);
            isDelayed = getParser().getHasDelayBefore();
            async = isDelayed.isTrue();
            getParser().setHasDelayBefore(Kleenean.TRUE);
        }
        return true;
    }

    protected TriggerItem walk(FileController controller, Event event) {
        if (first == null) {
            return walk(event, false);
        }
        if (async) {
            if (last == null) {
                return walk(event, false);
            }
            FileController.push(event, controller);
            Delay.addDelayedEvent(event);
            Object variables = Variables.removeLocals(event);
            TriggerItem next = walk(event, false);
            SkriptIO.queue().queue(new AccessTask(variables, next, event, controller));
            return null;
        } else {
            if (last != null) {
                last.setNext(null);
            }
            FileController.push(event, controller);
            try {
                TriggerItem.walk(first, event); // execute the section now
            } catch (Exception ex) {
                SkriptIO.error(ex);
            } finally {
                FileController.pop(event);
                SkriptIO.queue().queue(new CloseTask(controller)).await();
            }
        }
        return walk(event, false);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "access file " + pathExpression.toString(event, debug);
    }

    class AccessTask extends DataTask {

        private final Object variables;
        private final TriggerItem next;
        private final Event event;
        private final FileController controller;

        AccessTask(Object variables, TriggerItem next, Event event, FileController controller) {
            this.variables = variables;
            this.next = next;
            this.event = event;
            this.controller = controller;
        }

        @Override
        public void execute() {
            if (first == null) { // we skip straight on
                Bukkit.getScheduler().runTask(SkriptIO.getInstance(), () -> {
                    FileController.pop(event);
                    if (variables != null) {
                        Variables.setLocalVariables(event, variables);
                    }
                    TriggerItem.walk(next, event);
                });
            } else {
                Bukkit.getScheduler().runTask(SkriptIO.getInstance(), () -> {
                    if (variables != null) {
                        Variables.setLocalVariables(event, variables);
                    }
                    if (last != null) {
                        last.setNext(new DummyCloseTrigger(controller, next) {
                            @Override
                            protected boolean run(Event e) {
                                FileController.pop(event);
                                return super.run(e);
                            }
                        });
                    }
                    TriggerItem.walk(first, event);
                });
            }
        }

    }

}
