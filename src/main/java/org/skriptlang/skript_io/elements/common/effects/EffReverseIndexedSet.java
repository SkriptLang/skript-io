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
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.format.Format;
import org.skriptlang.skript_io.format.FormatInfo;
import org.skriptlang.skript_io.utility.Resource;
import org.skriptlang.skript_io.utility.Writable;
import org.skriptlang.skript_io.utility.task.FormatTask;

import java.util.Map;

@Name("Change: Reverse Indexed Set")
@Description("A special edition of the set changer that can maintain the indices of target data.")
@Examples({"set yaml contents of file to {_options::*}"})
@Since("1.0.0")
public class EffReverseIndexedSet extends EffEncode {

    static {
        if (!SkriptIO.isTest())
            Skript.registerEffect(EffReverseIndexedSet.class,
                "set [the] %*classinfo% content[s] of %resource% to %objects%",
                "set %resource%'[s] %*classinfo% content[s] to %objects%");
    }

    private Expression<Resource> targetExpression;
    private Variable<?> source;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result) {
        this.targetExpression = (Expression<Resource>) expressions[1 - matchedPattern];
        if (((Literal<ClassInfo<?>>) expressions[matchedPattern]).getSingle() instanceof FormatInfo<?> info) {
            this.classInfo = info;
            if (!Map.class.isAssignableFrom(info.getFormat().getType())) return false;
        } else return false;
        if (expressions[2] instanceof Variable<?> variable) source = variable;
        else return false;
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        final Format<?> format = classInfo.getFormat();
        final String name = StringUtils.substring(source.getName().toString(event), 0, -1);
        final Object variable = Variables.getVariable(name + "*", event, source.isLocal());
        if (!(variable instanceof Map<?, ?> map)) return;
        this.convertLists(map);
        for (final Resource file : targetExpression.getArray(event))
            if (file instanceof Writable writable)
                SkriptIO.queue().queue(new FormatTask(format, writable, map)).await();
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "set the " + classInfo.getCodeName() + " contents of " +
            this.targetExpression.toString(event, debug) + " to " + source.toString(event, debug);
    }

}
