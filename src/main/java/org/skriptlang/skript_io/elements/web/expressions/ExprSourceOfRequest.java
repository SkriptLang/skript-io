package org.skriptlang.skript_io.elements.web.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.web.Request;

@Name("Source of Request")
@Description("""
    The address a request was made from, in IP format.
    If present, this will be a text, e.g. `"127.0.0.1"`.
    """)
@Examples({
    """
    open a website:
        broadcast the request's source"""
})
@Since("1.0.0")
public class ExprSourceOfRequest extends SimplePropertyExpression<Request, String> {

    static {
        if (!SkriptIO.isTestMode())
            register(ExprSourceOfRequest.class, String.class, "source", "request");
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "source";
    }

    @Override
    public @Nullable String convert(Request request) {
        return request.getSource();
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

}
