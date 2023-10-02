package org.skriptlang.skript_io.elements.common.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.util.Map;

@Name("Change: Indexed Set")
@Description("A special edition of the set changer that can maintain the indices of source data.")
@Examples({"set {_options::*} to yaml contents of file"})
@Since("1.0.0")
public class EffIndexedSet extends Effect { // todo make sure this actually works
    
    
    private static final String SEPARATOR = Variable.SEPARATOR;
    
    static {
        if (!SkriptIO.isTest())
            Skript.registerEffect(EffIndexedSet.class, "set %~objects% to %objects%", "map %~objects% to %objects%");
    }
    
    private Expression<Object> sourceExpression;
    private Variable<?> target;
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult result) {
        this.sourceExpression = (Expression<Object>) expressions[1];
        if (expressions[0] instanceof Variable<?> variable) target = variable;
        else return false;
        return true;
    }
    
    @Override
    protected void execute(@NotNull Event event) {
        final Object source = sourceExpression.getSingle(event);
        if (!(source instanceof Map<?, ?> map) || !target.isList()) {
            this.target.change(event, sourceExpression.getArray(event), Changer.ChangeMode.SET);
            return;
        }
        this.set(event, target, map);
    }
    
    protected void set(Event event, Variable<?> variable, Map<?, ?> map) {
        this.set(event, variable, (Object) null);
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            final String key = String.valueOf(entry.getKey());
            final Object value = entry.getValue();
            this.set(event, variable, key, value);
        }
    }
    
    protected void set(Event event, Variable<?> variable, String key, Object value) {
        if (value instanceof Map<?, ?> map) for (final Map.Entry<?, ?> entry : map.entrySet()) {
            final String key2 = String.valueOf(entry.getKey());
            final Object value2 = entry.getValue();
            this.set(event, variable, key + SEPARATOR + key2, value2);
        }
        else this.setIndex(event, variable, key, value);
    }
    
    private void set(Event event, Variable<?> target, Object value) {
        Variables.setVariable(target.getName().toString(event), value, event, target.isLocal());
    }
    
    private void setIndex(Event event, Variable<?> target, String index, @Nullable Object value) {
        final String name = target.getName().toString(event);
        Variables.setVariable(name.substring(0, name.length() - 1) + index, value, event, target.isLocal());
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "map " + target.toString(event, debug) + " to " + sourceExpression.toString(event, debug);
    }
    
}
