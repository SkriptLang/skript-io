package org.skriptlang.skript_io.elements.common.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.format.FormatInfo;
import org.skriptlang.skript_io.utility.Resource;

import java.util.Map;

@Name("Change: Indexed Set")
@Description("A special edition of the set changer that can maintain the indices of source data.")
@Examples({"set {_options::*} to yaml contents of file"})
@Since("1.0.0")
public class EffIndexedSet extends EffEncode {

    static {
        if (!SkriptIO.isTest())
            Skript.registerEffect(EffIndexedSet.class,
                                  "set %~objects% to [the] %*classinfo% content[s] of %resource%",
                                  "set %~objects% to %resource%'[s] %*classinfo% content[s]");
    }

    private Expression<Resource> sourceExpression;
    private Variable<?> target;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result) {
        this.sourceExpression = (Expression<Resource>) expressions[2 - matchedPattern];
        if (((Literal<ClassInfo<?>>) expressions[1 + matchedPattern]).getSingle() instanceof FormatInfo<?> info) {
            this.classInfo = info;
            if (!Map.class.isAssignableFrom(info.getFormat().getType())) return false;
        } else return false;
        if (expressions[0] instanceof Variable<?> variable) target = variable;
        else return false;
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        final Object source = this.deserialise(sourceExpression.getSingle(event));
        this.change(this.target, source, event);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "set " + target.toString(event, debug) + " to the " +
            this.classInfo.getCodeName() + " contents of " + sourceExpression.toString(event, debug);
    }

}
