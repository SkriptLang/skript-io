package org.skriptlang.skript_io.format;

import ch.njol.skript.classes.ClassInfo;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A special kind of class-info representing an error type.
 *
 * @param <Type>
 */
public class ErrorInfo<Type extends Throwable> extends ClassInfo<Type> {

    protected final Supplier<Type> creator;
    protected final Function<String, Type> withMessage;

    public ErrorInfo(String codeName, Class<Type> type, Supplier<Type> creator, Function<String, Type> withMessage) {
        super(type, codeName == null ? type.getSimpleName().toLowerCase() : codeName);
        this.creator = creator;
        this.withMessage = withMessage;
    }

    public Type create(@Nullable String message) {
        if (message != null) return withMessage.apply(message);
        else return creator.get();
    }

}
