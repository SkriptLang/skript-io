package org.skriptlang.skript_io.elements.common.effects;

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
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.IOError;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Name("Try (Section)")
@Description("""
    Attempts to run the code in the section. If any part of the code encounters an error,\
    the section will exit immediately and any remaining code will not be run.
    This means that the script may continue in an unexpected state (i.e. some variables may be different from expected)\
    and so the `try` section should be used with caution.
    Any kind of delay is prohibited within the try section.""")
@Examples({
    "try:",
    "\tadd \"hello\" to the file",
    "catch {error}"
})
@Since("1.0.0")
public class SecTry extends EffectSection {

    private static final Method walkMethod;

    static {
        if (!SkriptIO.isTest())
            Skript.registerSection(SecTry.class, "try");
    }

    static {
        try {
            walkMethod = TriggerItem.class.getDeclaredMethod("walk", Event.class);
            walkMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new Error("This version of Skript is not compatible.", e);
            // if we don't have TriggerItem#walk we are in an irreparable situation
        }
    }

    protected Throwable thrown;

    private static TriggerItem walk0(TriggerItem item, Event event)
        throws InvocationTargetException, IllegalAccessException {
        return (TriggerItem) walkMethod.invoke(item, event);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode,
                        @Nullable List<TriggerItem> list) {
        if (!this.hasSection()) {
            Skript.error("'try' can be used only as a section.");
            return false;
        }
        assert sectionNode != null;
        final ParserInstance parser = this.getParser();
        final Kleenean wasDelayed = parser.getHasDelayBefore(), isDelayed;
        parser.setHasDelayBefore(Kleenean.FALSE);
        this.loadOptionalCode(sectionNode);
        isDelayed = parser.getHasDelayBefore();
        if (!isDelayed.isFalse()) {
            Skript.error("'try' sections cannot contain delays or delayed effects.");
            return false;
        }
        parser.setHasDelayBefore(isDelayed.or(wasDelayed));
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        this.thrown = null;
        if (first == null) return this.walk(event, false);
        if (last != null) last.setNext(null);
        try {
            this.walkUnsafe(first, event);
        } catch (Exception | IOError ex) {
            this.thrown = ex;
            EffSecCatch.lastThrownError.set(ex);
        }
        return this.walk(event, false);
    }

    void walkUnsafe(final TriggerItem start, final Event event)
        throws IllegalAccessException { // Skript loves to report the exception and continue
        assert start != null && event != null; // Obviously, we can't do that
        TriggerItem item = start;
        try {
            while (item != null) { // We have to duplicate `item = item.walk(event)`
                item = walk0(item, event);
            }
        } catch (InvocationTargetException ex) {
            EffThrow.throwUncheckedException(ex.getCause());
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "try";
    }

}
