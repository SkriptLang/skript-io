package org.skriptlang.skript_io.elements.common.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.util.List;

@Name("Catch Error")
@Description("""
    Obtains the error from the previous `try` section and stores it in a variable.
    This can also be used as a section that will run only if an error occurred.
    
    The catch section can also be used to filter specific errors by type, as long
    as an 'error info' is provided for the error class.
    """)
@Example("""
        try:
            add "hello" to the file
        catch {_error}
        # _error will be empty if no error occurred
        if {_error} exists:
            broadcast "An error occurred!"
        """)
@Example("""
        try:
            add "hello" to the file
        catch {_error}:
            # run if an error occurred
            broadcast "Error! " + {_error}'s message
        """)
@Example("""
        try:
            add "hello" to the file
        catch the io error in {_io}:
            # run if an 'io exception' occurred
            broadcast "Unable to write to the file."
        catch the null pointer error in {_null}:
            # run if a 'null pointer exception' occurred
            broadcast "Something was null."
        """)
@Since("1.0.0")
public class EffSecCatch extends EffectSection {

    /*
    Since Skript can't obtain the previous parsed TriggerItem unless the requester is a section,
    we have to hope that the previous `catch` is known from SecTry.

    Ideally, this will be fixed by Skript, eventually.
     */
    static final ThreadLocal<Throwable> lastThrownError = new ThreadLocal<>();

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerSection(EffSecCatch.class, "catch %~object%", "catch [the|a[n]] %*classinfo% in %~object%");
    }

    protected EffSecTry source;
    private Class<?> errorType = Throwable.class;
    private Expression<?> catcher;
    private ClassInfo<?> info;

    @Nullable
    private static EffSecTry getTrySection(List<TriggerItem> triggerItems) {
        // Find the EffSecTry right before this EffSecCatch
        TriggerItem triggerItem = triggerItems.isEmpty()
                ? null
                : triggerItems.getLast();

        if (triggerItem instanceof EffSecCatch secCatch) { // there was already a catch section
            return secCatch.source;
        }

        if (triggerItem instanceof EffSecTry secTry) {
            return secTry;
        }

        return null;
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode,
                        List<TriggerItem> list) {
        // Look for our preceding EffSecTry
        if (list != null) {
            source = getTrySection(list);
            if (source == null) {
                Skript.error("The 'catch' effect must immediately follow either a 'try' or another 'catch' section.");
                return false;
            }
        }
        // Load our section code (if present)
        if (hasSection()) {
            assert sectionNode != null;
            loadOptionalCode(sectionNode);
        }

        // Get our error storage variable
        if (expressions[matchedPattern] instanceof Variable<?> variable) {
            catcher = variable;
        } else {
            Skript.error("The input for the 'catch' effect must be a variable to store the error.");
            return false;
        }
        // Make sure ClassInfo is an error type
        // noinspection PatternVariableHidesField
        if (matchedPattern == 1 && expressions[0] instanceof Literal<?> literal
                && literal.getSingle() instanceof ClassInfo<?> info) {
            this.info = info;
            errorType = info.getC();
            if (!Throwable.class.isAssignableFrom(errorType)) {
                Skript.error("Errors may only be caught by error-type, but found '" + info.getCodeName() + "'");
                return false;
            }
        }
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        // Find the last thrown error
        Throwable error;
        if (source != null) {
            error = source.thrown;
        } else {
            error = lastThrownError.get();
        }
        boolean hasError = error != null;
        if (hasError && errorType.isInstance(error)) {
            // Store the error, run the catch section
            EffSecCatch.lastThrownError.remove(); // we consumed it here
            catcher.change(event, new Object[] {error}, Changer.ChangeMode.SET);
            if (first == null) {
                return walk(event, false);
            } else {
                return first;
            }
        } else if (!hasError) {
            catcher.change(event, new Object[0], Changer.ChangeMode.DELETE);
        }
        if (first == null) {
            return walk(event, false);
        }
        return getNext();
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        if (info != null) {
            return "catch " + info.toString(event, debug) + " in " + catcher.toString(event, debug);
        }
        return "catch " + catcher.toString(event, debug);
    }

}
