package org.skriptlang.skript_io.format;

import mx.kenzie.jupiter.stream.Stream;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

public abstract class Format<Type> {

    protected final String name;
    protected final String pattern;
    protected final Class<Type> type;

    protected Format(String name, Class<Type> type, String pattern) {
        this.name = name;
        this.type = type;
        this.pattern = pattern;
    }

    protected Format(String name, Class<Type> type) {
        this(name, type, name.toLowerCase());
    }

    public @Nullable Type[] from(Readable readable) {
        try (InputStream stream = readable.acquireReader()) {
            return from(stream);
        } catch (IOException ex) {
            SkriptIO.error(ex);
            // noinspection unchecked
            return (Type[]) Array.newInstance(type, 0);
        }
    }

    protected abstract @Nullable Type[] from(InputStream stream) throws IOException;

    @SuppressWarnings("unchecked")
    public void to(Writable writable, @Nullable Object... values) {
        Type[] types = Arrays.copyOf(values, values.length, (Class<Type[]>) type.arrayType());
        try (OutputStream stream = Stream.keepalive(writable.acquireWriter())) {
            to(stream, types);
        } catch (IOException ex) {
            SkriptIO.error(ex);
        }
    }

    protected void to(OutputStream stream, Type... values) {
        try (OutputStream output = Stream.keepalive(stream)) {
            for (Type value : values) {
                to(output, value);
            }
        } catch (IOException ex) {
            SkriptIO.error(ex);
        }
    }

    protected abstract void to(OutputStream stream, @Nullable Type value) throws IOException;

    @SuppressWarnings("unchecked")
    public FormatInfo<Type> getInfo() {
        return (FormatInfo<Type>) new FormatInfo<>((Class<Format<Type>>) getClass(), name.toLowerCase(), this)
            .user(pattern);
    }

    public String getName() {
        return name;
    }

    public Class<Type> getType() {
        return type;
    }

    public boolean isSingular() {
        return true;
    }

}
