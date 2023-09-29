package org.skriptlang.skript_io.elements.file.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.file.FileController;
import org.skriptlang.skript_io.utility.task.CloseTask;

import java.net.URI;
import java.util.List;

public abstract class SecAccessFile extends EffectSection {
    
    protected Expression<URI> pathExpression;
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list) {
        this.pathExpression = (Expression<URI>) expressions[0];
        if (this.hasSection()) {
            assert sectionNode != null;
            final Kleenean wasDelayed = this.getParser().getHasDelayBefore(), isDelayed;
            this.getParser().setHasDelayBefore(Kleenean.FALSE);
            this.loadOptionalCode(sectionNode);
            isDelayed = this.getParser().getHasDelayBefore();
            if (!isDelayed.isFalse()) {
                Skript.error("Section '" + result.expr + "' cannot contain a delay.");
                return false;
            }
            this.getParser().setHasDelayBefore(wasDelayed.or(isDelayed));
            if (last != null) last.setNext(null);
        }
        return true;
    }
    
    protected void walk(FileController controller, Event event) {
        if (first == null) return;
        if (last != null) last.setNext(null);
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
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "access file " + pathExpression.toString(event, debug);
    }
    
}
