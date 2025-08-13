package org.skriptlang.skript_io.elements.common.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.*;
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
@Example("""
        set {_raw text} to "{""key"": ""value""}"
        encode {_raw text} as json to {_json::*}
        # _json::key = "value"
        """)
@Example("""
        set {_json::key} to "value"
        set {_json::number} to 5.5
        decode {_json::*} from json to {_raw text}
        # {"key": "value", "number": 5.5}
        """)
@Example("encode \"hello there\" as gzip to {_compressed}")
@Example("decode {_compressed} from gzip to {_raw text}")
@Example("decode {_config::*} from yaml to {_raw text}")
@Since("1.0.0")
public class EffEncode extends Effect {

    private static final String SEPARATOR = Variable.SEPARATOR;

    static {
        if (!SkriptIO.isTestMode()) {
            Skript.registerEffect(EffEncode.class,
                    "encode %object% as %*classinfo% (in|[in]to) %~objects%",
                    "decode %~objects% from %*classinfo% (in|[in]to) %object%"
            );
        }
    }

    protected FormatInfo<?> classInfo;
    private Expression<Object> sourceExpression;
    private Expression<Object> targetExpression;
    private Variable<?> targetVariable;
    private boolean isEncoding, isMap;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result) {
        if (((Literal<ClassInfo<?>>) expressions[1]).getSingle() instanceof FormatInfo<?> info) {
            classInfo = info;
        } else {
            Skript.error("The encoding must be a registered format.");
            return false;
        }
        sourceExpression = (Expression<Object>) expressions[0];
        targetExpression = (Expression<Object>) expressions[2];
        isEncoding = matchedPattern == 0;
        isMap = Map.class.isAssignableFrom(info.getFormat().getType());
        if (isEncoding) {
            if (!(targetExpression instanceof Variable<Object> variable) || (isMap && !variable.isList())) {
                Skript.error("The encoding target must be a " + (isMap ? "list " : "") + "variable.");
                return false;
            }
            targetVariable = variable;
        } else if (isMap || (!sourceExpression.isSingle() && !(sourceExpression instanceof Variable<?>))) {
            if (!(sourceExpression instanceof Variable<Object> variable) || !variable.isList()) {
                Skript.error("The decoding source must be a " + (isMap ? "list " : "") + "variable.");
                return false;
            }
            targetVariable = variable;
        }
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        // Converts the source to the target
        Object source, target;
        Object converted;
        if (isEncoding) {
            source = sourceExpression.getSingle(event);
            if (isMap) {
                if (source instanceof Resource resource) {
                    converted = deserialize(resource);
                } else {
                    converted = mapFormat(String.valueOf(source));
                }
            } else {
                if (source instanceof Resource resource) {
                    converted = deserializeSingle(resource);
                } else {
                    converted = mapFormatSingle(String.valueOf(source));
                }
            }
            change(targetVariable, converted, event);
        } else {
            if (isMap) {
                assert targetVariable != null;
                target = targetExpression.getSingle(event);
                String name = StringUtils.substring(targetVariable.getName().toString(event), 0, -1);
                converted = Variables.getVariable(name + "*", event, targetVariable.isLocal());
                if (converted instanceof Map<?, ?> map) {
                    convertLists(map);
                }
            } else {
                target = targetExpression.getSingle(event);
                converted = sourceExpression.getSingle(event);
            }
            Format<?> format = classInfo.getFormat();
            if (target instanceof Writable writable) {
                format.to(writable, converted);
            } else {
                targetExpression.change(event, new Object[]{Writable.format(format, converted)}, Changer.ChangeMode.SET);
            }
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        if (isEncoding) {
            return "encode " + sourceExpression.toString(event, debug) + " as " +
                    classInfo.toString(event, debug) + " into " + targetExpression.toString(event, debug);
        } else {
            return "decode " + sourceExpression.toString(event, debug) + " from " +
                    classInfo.toString(event, debug) + " into " + targetExpression.toString(event, debug);
        }
    }

    protected void change(Variable<?> target, Object source, Event event) {
        // Changes a variable value
        if (!(source instanceof Map<?, ?> map) || !target.isList()) {
            Object[] array;
            if (source instanceof Object[] objects) {
                array = objects;
            } else {
                array = new Object[] {source};
            }
            target.change(event, array, Changer.ChangeMode.SET);
        } else {
            set(event, target, map);
        }
    }

    protected Object mapFormat(String source) {
        // Converts a text source for a map formatter
        Readable readable = Readable.simple(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
        return deserialize(readable);
    }

    protected Object mapFormatSingle(String source) {
        // Converts a text source for a map formatter
        Readable readable = Readable.simple(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
        return deserialize(readable);
    }

    protected Object deserialize(Resource resource) {
        // Gets the formatted content of a resource
        if (!(resource instanceof Readable readable)) {
            return new HashMap<>();
        }
        Object[] things = classInfo.getFormat().from(readable);
        if (things.length == 0) {
            return new HashMap<>();
        }
        return things[0];
    }

    protected Object deserializeSingle(Resource resource) {
        // Gets the single content of a resource
        if (!(resource instanceof Readable readable)) {
            return null;
        }
        Object[] things = classInfo.getFormat().from(readable);
        if (things.length == 0) {
            return null;
        }
        return things[0];
    }

    protected void set(Event event, Variable<?> variable, Map<?, ?> map) {
        // Sets a list variable to an indexed map
        set(event, variable, (Object) null);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            set(event, variable, key, value);
        }
    }

    protected void set(Event event, Variable<?> variable, String key, Object value) {
        // Sets an individual index of a variable to a value
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key2 = String.valueOf(entry.getKey());
                Object value2 = entry.getValue();
                set(event, variable, key + SEPARATOR + key2, value2);
            }
        }
        else if (value instanceof List<?> list) {
            int index = 0;
            for (Object object : list) {
                set(event, variable, key + SEPARATOR + ++index, object);
            }
        } else {
            setIndex(event, variable, key, value);
        }
    }

    private void set(Event event, Variable<?> target, Object value) {
        Variables.setVariable(target.getName().toString(event), value, event, target.isLocal());
    }

    private void setIndex(Event event, Variable<?> target, String index, @Nullable Object value) {
        String name = target.getName().toString(event);
        Variables.setVariable(name.substring(0, name.length() - 1) + index, value, event, target.isLocal());
    }

    @SuppressWarnings({"unchecked", "RawUseOfParameterized"})
    public static void convertLists(Map<?, ?> map) {
        // Checks whether entries in a map could be a list
        for (Map.Entry entry : map.entrySet()) {
            Object value = entry.getValue();
            if (!(value instanceof Map<?, ?> child)) {
                continue;
            }
            if (couldBeList(child)) {
                entry.setValue(convertToList(child));
            } else {
                convertLists(child);
            }
        }
    }

    private static List<?> convertToList(Map<?, ?> map) {
        // Converts a map to an ordered list
        HashMap<Integer, Object> sorter = new HashMap<>(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            int number = Integer.parseInt(key);
            sorter.put(number, entry.getValue());
        }
        return new ArrayList<>(sorter.values());
    }

    private static boolean couldBeList(Map<?, ?> map) {
        // Checks whether a map could be a list
        for (Object object : map.keySet()) {
            if (object == null) {
                return false;
            }
            String string = String.valueOf(object);
            for (char c : string.toCharArray()) {
                if (c < '0' || c > '9') {
                    return false;
                }
            }
        }
        return true;
    }

}
