package org.skriptlang.skript_io.elements.common.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.format.Format;
import org.skriptlang.skript_io.format.FormatInfo;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Resource;
import org.skriptlang.skript_io.utility.Writable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("Encode")
@Description("""
    Used for converting data from one format to another.
    The `encode %source% as %format% (in|[in]to) %target%` pattern converts string text into
    the format, and stores the result in the target variable.
    The `decode %source% from %format% (in|[in]to) %target%` pattern converts an encoded variable into
    text, and stores the result in the target.
    
    Some data formats encode from one text to another (e.g. text -> url-encoded text, text -> gzip).
    These formats require a regular variable as their encoding target/decoding source.
    
    Other data formats support special text <-> list variable mapping, (e.g. json objects, yaml trees).
    These formats require a **list** variable as their encoding target/decoding source.
    """)
@Examples({
    """
    set {_raw text} to "{""key"": ""value""}"
    encode {_raw text} as json to {_json::*}
    # _json::key = "value"\s""",
    """
    set {_json::key} to "value"
    set {_json::number} to 5.5
    decode {_json::*} from json to {_raw text}
    # {"key": "value", "number": 5.5}""",
    """
    encode "hello there" as gzip to {_compressed}""",
    """
    decode {_compressed} from gzip to {_raw text}""",
    """
    decode {_config::*} from yaml to {_raw text}""",
})
@Since("1.0.0")
public class EffEncode extends Effect {

    private static final String SEPARATOR = Variable.SEPARATOR;

    static {
        if (!SkriptIO.isTest())
            Skript.registerEffect(EffEncode.class,
                                  "encode %object% as %*classinfo% (in|[in]to) %~objects%",
                                  "decode %~objects% from %*classinfo% (in|[in]to) %object%"
                                 );
    }

    protected FormatInfo<?> classInfo;
    private Expression<Object> sourceExpression;
    private Expression<Object> targetExpression;
    private Variable<?> variable;
    private boolean isEncoding, isMap;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result) {
        if (((Literal<ClassInfo<?>>) expressions[1]).getSingle() instanceof FormatInfo<?> info) classInfo = info;
        else {
            Skript.error("The encoding must be a registered format.");
            return false;
        }
        this.sourceExpression = (Expression<Object>) expressions[0];
        this.targetExpression = (Expression<Object>) expressions[2];
        this.isEncoding = matchedPattern == 0;
        this.isMap = Map.class.isAssignableFrom(info.getFormat().getType());
        if (isEncoding) {
            if (!(targetExpression instanceof Variable<Object> variable) || (isMap && !variable.isList())) {
                Skript.error("The encoding target must be a " + (isMap ? "list " : "") + "variable.");
                return false;
            }
            this.variable = variable;
        } else {
            if (!(sourceExpression instanceof Variable<Object> variable) || (isMap && !variable.isList())) {
                Skript.error("The decoding source must be a " + (isMap ? "list " : "") + "variable.");
                return false;
            }
            this.variable = variable;
        }
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        //<editor-fold desc="Converts the source to the target" defaultstate="collapsed">
        final Object source, target;
        final Object converted;
        if (isEncoding) {
            if (isMap) {
                source = sourceExpression.getSingle(event);
                if (source instanceof Resource resource) converted = this.deserialise(resource);
                else converted = this.mapFormat(String.valueOf(source));
            } else {
                source = sourceExpression.getSingle(event);
                if (source instanceof Resource resource) converted = this.deserialiseSingle(resource);
                else converted = this.mapFormatSingle(String.valueOf(source));
            }
            this.change(variable, converted, event);
        } else {
            if (isMap) {
                target = targetExpression.getSingle(event);
                final String name = StringUtils.substring(variable.getName().toString(event), 0, -1);
                converted = Variables.getVariable(name + "*", event, this.variable.isLocal());
                if (converted instanceof Map<?, ?> map) this.convertLists(map);
            } else {
                target = targetExpression.getSingle(event);
                converted = variable.getSingle(event);
            }
            final Format<?> format = classInfo.getFormat();
            if (target instanceof Writable writable) format.to(writable, converted);
            else targetExpression.change(event, new Object[] {Writable.format(format, converted)},
                                         Changer.ChangeMode.SET);
        }
        //</editor-fold>
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        if (isEncoding) return "encode " + sourceExpression.toString(event, debug) + " as " +
            this.classInfo.toString(event, debug) + " into " + targetExpression.toString(event, debug);
        else return "decode " + sourceExpression.toString(event, debug) + " from " +
            this.classInfo.toString(event, debug) + " into " + targetExpression.toString(event, debug);
    }

    protected void change(Variable<?> target, Object source, Event event) {
        //<editor-fold desc="Changes a variable value" defaultstate="collapsed">
        if (!(source instanceof Map<?, ?> map) || !target.isList()) {
            final Object[] array;
            if (source instanceof Object[] objects) array = objects;
            else array = new Object[] {source};
            target.change(event, array, Changer.ChangeMode.SET);
        } else this.set(event, target, map);
        //</editor-fold>
    }

    protected Object mapFormat(String source) {
        //<editor-fold desc="Converts a text source for a map formatter" defaultstate="collapsed">
        final Readable readable = Readable.simple(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
        return this.deserialise(readable);
        //</editor-fold>
    }

    protected Object mapFormatSingle(String source) {
        //<editor-fold desc="Converts a text source for a map formatter" defaultstate="collapsed">
        final Readable readable = Readable.simple(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
        return this.deserialise(readable);
        //</editor-fold>
    }

    protected Object deserialise(Resource resource) {
        //<editor-fold desc="Gets the formatted content of a resource" defaultstate="collapsed">
        if (!(resource instanceof Readable readable)) return new HashMap<>();
        final Object[] things = classInfo.getFormat().from(readable);
        if (things.length == 0) return new HashMap<>();
        return things[0];
        //</editor-fold>
    }

    protected Object deserialiseSingle(Resource resource) {
        //<editor-fold desc="Gets the single content of a resource" defaultstate="collapsed">
        if (!(resource instanceof Readable readable)) return null;
        final Object[] things = classInfo.getFormat().from(readable);
        if (things.length == 0) return null;
        return things[0];
        //</editor-fold>
    }

    protected void set(Event event, Variable<?> variable, Map<?, ?> map) {
        //<editor-fold desc="Sets a list variable to an indexed map" defaultstate="collapsed">
        this.set(event, variable, (Object) null);
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            final String key = String.valueOf(entry.getKey());
            final Object value = entry.getValue();
            this.set(event, variable, key, value);
        }
        //</editor-fold>
    }

    protected void set(Event event, Variable<?> variable, String key, Object value) {
        //<editor-fold desc="Sets an individual index of a variable to a value" defaultstate="collapsed">
        if (value instanceof Map<?, ?> map) for (final Map.Entry<?, ?> entry : map.entrySet()) {
            final String key2 = String.valueOf(entry.getKey());
            final Object value2 = entry.getValue();
            this.set(event, variable, key + SEPARATOR + key2, value2);
        }
        else if (value instanceof List<?> list) {
            int index = 0;
            for (final Object object : list) this.set(event, variable, key + SEPARATOR + ++index, object);
        } else this.setIndex(event, variable, key, value);
        //</editor-fold>
    }

    private void set(Event event, Variable<?> target, Object value) {
        Variables.setVariable(target.getName().toString(event), value, event, target.isLocal());
    }

    private void setIndex(Event event, Variable<?> target, String index, @Nullable Object value) {
        final String name = target.getName().toString(event);
        Variables.setVariable(name.substring(0, name.length() - 1) + index, value, event, target.isLocal());
    }

    @SuppressWarnings({"unchecked", "RawUseOfParameterized"})
    protected void convertLists(Map<?, ?> map) {
        //<editor-fold desc="Checks whether entries in a map could be a list" defaultstate="collapsed">
        for (final Map.Entry entry : map.entrySet()) {
            final Object value = entry.getValue();
            if (!(value instanceof Map<?, ?> child)) continue;
            if (this.couldBeList(child)) entry.setValue(this.convertToList(child));
            else this.convertLists(child);
        }
        //</editor-fold>
    }

    private List<?> convertToList(Map<?, ?> map) {
        //<editor-fold desc="Converts a map to an ordered list" defaultstate="collapsed">
        final HashMap<Integer, Object> sorter = new HashMap<>(map.size());
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            final String key = String.valueOf(entry.getKey());
            final int number = Integer.parseInt(key);
            sorter.put(number, entry.getValue());
        }
        return new ArrayList<>(sorter.values());
        //</editor-fold>
    }

    private boolean couldBeList(Map<?, ?> map) {
        //<editor-fold desc="Checks whether a map could be a list" defaultstate="collapsed">
        for (final Object object : map.keySet()) {
            if (object == null) return false;
            final String string = String.valueOf(object);
            for (final char c : string.toCharArray()) {
                if (c < '0') return false;
                if (c > '9') return false;
            }
        }
        return true;
        //</editor-fold>
    }

}
