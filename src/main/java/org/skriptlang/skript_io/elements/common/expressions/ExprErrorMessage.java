package org.skriptlang.skript_io.elements.common.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

@Name("Message of Error")
@Description("""
    The text-message attached to an error (if one is present).
    Not all errors specify a message.
    """)
@Example("""
        try:
            broadcast the text content of the request's body
        catch {_error}:
            broadcast {_error}'s message
        """)
@Since("1.0.0")
public class ExprErrorMessage extends SimplePropertyExpression<Throwable, String> {

    static {
        if (!SkriptIO.isTestMode())
            register(ExprErrorMessage.class, String.class, "message", "error");
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "message";
    }

    @Override
    public @Nullable String convert(Throwable throwable) {
        return throwable.getMessage();
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

}
