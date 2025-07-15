package org.skriptlang.skript_io.elements.common.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
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

@Name("Try")
@Description("""
    Attempts to run the code in the section. If any part of the code encounters a (recoverable) error,
    the section will exit immediately and any remaining code will not be run.
    
    This means that the script may continue in an unexpected state (i.e. some variables may be different from expected)
    and so the `try` section should be used with caution.
    > To properly recover the program, specify the error you expect using a catch section.
    
    Any kind of delay is prohibited within the try section.
    ```
    try:
        do something
        wait 1 second # NO!
        do something
    ```
    If a delay is needed, the try-section should be split into multiple blocks around the delay.
    ```
    try:
        do something
    wait 1 second # ok
    try:
        do something
    ```
    
    Note that some errors are considered unrecoverable and the trigger *must* terminate.
    These will not be suppressed by a `try` section.""")
@Examples({
    """
    try:
        add "hello" to the file""",
    """
    try:
        set {_text} to the text content of the response
    catch {_error}""",
    """
    try to kill player
    catch the null pointer error in {_error}"""
})
@Since("1.0.0")
public class EffSecTry extends EffectSection {

    private static final Method walkMethod;

    static {
        //<editor-fold desc="Find the TriggerItem walk method" defaultstate="collapsed">
        try {
            walkMethod = TriggerItem.class.getDeclaredMethod("walk", Event.class);
            walkMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new Error("This version of Skript is not compatible.", e);
            // if we don't have TriggerItem#walk we are in an irreparable situation
        }
        //</editor-fold>
        if (!SkriptIO.isTest())
            Skript.registerSection(EffSecTry.class, "try", "try to <.+>");
    }

    protected Throwable thrown;
    protected Effect effect;

    private static TriggerItem walk0(TriggerItem item, Event event)
        throws InvocationTargetException, IllegalAccessException {
        return (TriggerItem) walkMethod.invoke(item, event);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode,
                        @Nullable List<TriggerItem> list) {
        if (matchedPattern == 1) {
            //<editor-fold desc="Set up try inline effect" defaultstate="collapsed">
            if (this.hasSection()) {
                Skript.error("'try to' cannot be used as a section.");
                return false;
            }
            String effect = result.regexes.get(0).group();
            this.effect = Effect.parse(effect, "Can't understand this effect: " + effect);
            if (this.effect == null) {
                Skript.error("Couldn't parse 'try to' effect '" + effect + "'");
                return false;
            }
            //</editor-fold>
        } else {
            //<editor-fold desc="Set up try section" defaultstate="collapsed">
            if (!this.hasSection()) {
                Skript.error("'try' can be used only as a section.");
                return false;
            }
            assert sectionNode != null;
            ParserInstance parser = this.getParser();
            Kleenean wasDelayed = parser.getHasDelayBefore(), isDelayed;
            parser.setHasDelayBefore(Kleenean.FALSE);
            this.loadOptionalCode(sectionNode);
            isDelayed = parser.getHasDelayBefore();
            if (!isDelayed.isFalse()) {
                Skript.error("'try' sections cannot contain delays or delayed effects.");
                return false;
            }
            parser.setHasDelayBefore(isDelayed.or(wasDelayed));
            //</editor-fold>
        }
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        this.thrown = null;
        if (effect != null) {
            //<editor-fold desc="Attempt inline try" defaultstate="collapsed">
            try {
                this.effect.run(event);
            } catch (Exception | IOError ex) {
                this.thrown = ex;
                EffSecCatch.lastThrownError.set(ex);
            }
            return this.getNext();
            //</editor-fold>
        } else {
            //<editor-fold desc="Attempt try-section" defaultstate="collapsed">
            if (first == null) return this.walk(event, false);
            if (last != null) //noinspection DataFlowIssue
                this.last.setNext(null);
            try {
                this.walkUnsafe(first, event);
            } catch (Exception | IOError ex) {
                this.thrown = ex;
                EffSecCatch.lastThrownError.set(ex);
            }
            return this.walk(event, false);
            //</editor-fold>
        }
    }

    void walkUnsafe(TriggerItem start, Event event)
        throws IllegalAccessException { // Skript loves to report the exception and continue
        //<editor-fold desc="Walk through trigger tree" defaultstate="collapsed">
        assert start != null && event != null; // Obviously, we can't do that
        TriggerItem item = start;
        try {
            while (item != null) { // We have to duplicate `item = item.walk(event)`
                item = walk0(item, event);
            }
        } catch (InvocationTargetException ex) {
            EffThrow.throwUncheckedException(ex.getCause());
        }
        //</editor-fold>
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        if (effect != null) return "try to " + effect.toString(event, debug);
        return "try";
    }

}
