package org.skriptlang.skript_io.elements.common.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.StringMode;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.format.Format;
import org.skriptlang.skript_io.format.FormatInfo;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Resource;
import org.skriptlang.skript_io.utility.Writable;

@Name("Contents of Resource")
@Description("""
    The contents of (the stuff inside) a resource, such as an open file.
    This uses a type parser (e.g. the number context of X will parse X as a number).
    
    This will return nothing if the resource is unreadable.
    """)
@Examples({
    "open a website:",
    "\tbroadcast the text content of the request's body",
    "open file ./test.txt:",
    "\tbroadcast the text contents of file"
})
@Since("1.0.0")
public class ExprContentOfResource extends SimplePropertyExpression<Resource, Object> {
    
    static {
        if (!SkriptIO.isTest())
            register(ExprContentOfResource.class, Object.class, "%*classinfo% content[s]", "resource");
    }
    
    private ClassInfo<?> classInfo;
    private boolean isString, isFormat;
    private Class<?> returnType = Object.class;
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult result) {
        this.setExpr((Expression<Resource>) expressions[1 - matchedPattern]);
        this.classInfo = ((Literal<ClassInfo<?>>) expressions[matchedPattern]).getSingle();
        this.returnType = classInfo.getC();
        if (returnType == String.class) return isString = true;
        if (classInfo instanceof FormatInfo<?>) this.isFormat = true;
        else {
            final Parser<?> parser = classInfo.getParser();
            if (parser == null) { // TODO special parse context?
                Skript.error("Content cannot be parsed as " + classInfo.getName().withIndefiniteArticle());
                return false;
            }
        }
        return true;
    }
    
    @Override
    protected @NotNull String getPropertyName() {
        return classInfo.getCodeName() + " content";
    }
    
    @Override
    protected Object @NotNull [] get(@NotNull Event event, Resource[] source) {
        final Resource resource = source[0];
        return this.get(resource);
    }
    
    @Override
    public @Nullable Object convert(Resource resource) {
        final Object[] objects = this.get(resource);
        if (objects.length == 0) return null;
        return objects[0];
    }
    
    protected Object @NotNull [] get(Resource resource) {
        if (!(resource instanceof Readable readable)) return new Object[0];
        if (isString) return new String[]{readable.readAll()};
        else if (isFormat && classInfo instanceof FormatInfo<?> info) return info.getFormat().from(readable);
        final Parser<?> parser = classInfo.getParser();
        if (parser == null) return new Object[0];
        return new Object[]{parser.parse(readable.readAll(), ParseContext.DEFAULT)};
    }
    
    @Override
    public @NotNull Class<?> getReturnType() {
        return returnType;
    }
    
    @Override
    public Class<?> @Nullable [] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(returnType);
            case RESET, DELETE -> CollectionUtils.array();
            default -> null;
        };
    }
    
    @Override
    @SuppressWarnings({"unchecked", "RawUseOfParameterized"})
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET && delta != null && delta.length != 0 && delta[0] != null) {
            final Resource[] files = this.getExpr().getArray(event);
            if (isString) {
                this.writeString(files, delta);
            } else if (isFormat && classInfo instanceof FormatInfo<?> info) {
                final Format<?> format = info.getFormat();
                final Object[] array = new Object[delta.length];
                for (int i = 0; i < array.length; i++) array[i] = Converters.convert(delta[i], returnType);
                for (final Resource file : files)
                    if (file instanceof Writable writable) format.to(writable, array);
            } else {
                final Parser parser = classInfo.getParser();
                final Object object = Converters.convert(delta[0], returnType);
                final String content;
                if (parser == null) content = String.valueOf(object);
                else content = parser.toString(object, StringMode.COMMAND);
                for (final Resource file : files)
                    if (file instanceof Writable writable) writable.write(content);
            }
        } else {
            final Resource[] files = this.getExpr().getArray(event);
            for (final Resource file : files)
                if (file instanceof Writable writable) writable.clear();
        }
    }
    
    private void writeString(Resource[] resources, Object[] delta) {
        final String content = String.valueOf(delta[0]);
        for (final Resource file : resources)
            if (file instanceof Writable writable) writable.write(content);
    }
    
    @Override
    public boolean isSingle() {
        if (isFormat && classInfo instanceof FormatInfo<?> info) return info.getFormat().isSingular();
        return true;
    }
    
}
