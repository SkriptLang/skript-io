package org.skriptlang.skript_io.elements.common.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
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
    This can also be used as a section that will run only if an error occurred.""")
@Examples({
    "try:",
    "\tadd \"hello\" to the file",
    "catch {error}"
})
@Since("1.0.0")
public class EffSecCatch extends EffectSection {

    /*
    Since Skript can't obtain the previous parsed TriggerItem unless the requester is a section,
    we have to hope that the previous `catch` is known from SecTry.

    Ideally, this will be fixed by Skript, eventually.
     */
    static ThreadLocal<Throwable> lastThrownError = new ThreadLocal<>();

    static {
        if (!SkriptIO.isTest())
            Skript.registerSection(EffSecCatch.class, "catch %~object%", "catch [the|a[n]] %*classinfo% in %~object%");
    }

    protected EffSecTry source;
    private Class<?> errorType = Throwable.class;
    private Expression<?> catcher;
    private ClassInfo<?> info;

    @Nullable
    private static EffSecTry getTrySection(List<TriggerItem> triggerItems) {
        //<editor-fold desc="Find the EffSecTry right before this EffSecCatch" defaultstate="collapsed">
        TriggerItem triggerItem = triggerItems.get(triggerItems.size() - 1);
        if (triggerItem instanceof EffSecCatch secCatch) // there was already a catch section
            return secCatch.source;
        if (triggerItem instanceof EffSecTry secTry) {
            return secTry;
        } else {
            return null;
        }
        //</editor-fold>
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode,
                        List<TriggerItem> list) {
        //<editor-fold desc="Look for our preceding EffSecTry" defaultstate="collapsed">
        if (list != null) {
            this.source = getTrySection(list);
            if (source == null) {
                Skript.error("The 'catch' effect must immediately follow either a 'try' or another 'catch' section.");
                return false;
            }
        }
        //</editor-fold>
        //<editor-fold desc="Load our section code (if present)" defaultstate="collapsed">
        if (this.hasSection()) {
            assert sectionNode != null;
            this.loadOptionalCode(sectionNode);
        }
        //</editor-fold>
        //<editor-fold desc="Get our error storage variable" defaultstate="collapsed">
        if (expressions[matchedPattern] instanceof Variable<?> variable) catcher = variable;
        else {
            Skript.error("The input for the 'catch' effect must be a variable to store the error.");
            return false;
        }
        //</editor-fold>
        //<editor-fold desc="Make sure ClassInfo is an error type" defaultstate="collapsed">
        //noinspection PatternVariableHidesField
        if (matchedPattern == 1 && expressions[0] instanceof Literal<?> literal
            && literal.getSingle() instanceof ClassInfo<?> info) {
            this.info = info;
            this.errorType = info.getC();
            if (!Throwable.class.isAssignableFrom(errorType)) {
                Skript.error("Errors may only be caught by error-type, but found '" + info.getCodeName() + "'");
                return false;
            }
        }
        //</editor-fold>
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        //<editor-fold desc="Find the last thrown error" defaultstate="collapsed">
        final Throwable error;
        if (source != null) error = source.thrown;
        else error = lastThrownError.get();
        final boolean hasError = error != null;
        //</editor-fold>
        if (hasError && errorType.isInstance(error)) {
            //<editor-fold desc="Store the error, run the catch section" defaultstate="collapsed">
            EffSecCatch.lastThrownError.remove(); // we consumed it here
            this.catcher.change(event, new Object[] {error}, Changer.ChangeMode.SET);
            if (first == null) return this.walk(event, false);
            else return this.first;
            //</editor-fold>
        } else if (!hasError) catcher.change(event, new Object[0], Changer.ChangeMode.DELETE);
        if (first == null) return this.walk(event, false);
        return this.getNext();
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        if (info != null) return "catch " + info.toString(event, debug) + " in " + catcher.toString(event, debug);
        return "catch " + catcher.toString(event, debug);
    }

}
