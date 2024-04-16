package org.skriptlang.skript_io.elements.common.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

@Name("Message of Error")
@Description("""
    The message attached to an error (if one is present).""")
@Examples({
    "try:",
    "\tbroadcast the text content of the request's body",
    "catch {_error}:",
    "\tbroadcast {_error}'s message"
})
@Since("1.0.0")
public class ExprErrorMessage extends SimplePropertyExpression<Throwable, String> {

    static {
        if (!SkriptIO.isTest())
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
