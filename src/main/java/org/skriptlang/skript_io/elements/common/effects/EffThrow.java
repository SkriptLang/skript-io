package org.skriptlang.skript_io.elements.common.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.format.ErrorInfo;

@Name("Throw Error")
@Description("""
Produces an error that terminates the current trigger, unless it is 'caught' by a try/catch section.

This error will terminate each section in turn, propagating up the program,
until it reaches a 'breakpoint' (a delay, a function call, an event trigger) or a try/catch section.
No code after this error will be run, unless it was previously scheduled in the trigger.
""")
@Examples({
    """
    throw an error with message "oops!\"""",
    """
    throw an io error with message "file broke :(\"""",
    """
    throw a null pointer error""",
})
@Since("1.0.0")
public class EffThrow extends Effect {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerEffect(EffThrow.class, "throw a[n] %*classinfo%",
                "throw a[n] %*classinfo% with message %string%");
    }

    private boolean hasMessage;
    private ErrorInfo<?> errorType;
    private Expression<String> message;

    static void throwUncheckedException(Throwable exception) {
        SkriptIO.throwSafe(exception);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result) {
        hasMessage = matchedPattern == 1;
        if (expressions[0] instanceof Literal<?> literal
            && literal.getSingle() instanceof ErrorInfo<?> info) errorType = info;
        else {
            Skript.error("Only error types may be thrown.");
            return false;
        }
        if (hasMessage) message = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        Throwable throwable;
        if (hasMessage) throwable = errorType.create(message.getSingle(event));
        else throwable = errorType.create(null);
        throwException(throwable);
    }

    private void throwException(Throwable exception) {
        EffThrow.throwUncheckedException(exception);
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        if (hasMessage)
            return "throw an " + errorType.toString(event, debug) + " with message " + message.toString(event, debug);
        else return "throw an " + errorType.toString(event, debug);
    }

}
