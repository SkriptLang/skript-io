package org.skriptlang.skript_io.elements.web.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.web.Transaction;

@Name("Method of Request")
@Description("""
    The type of a web request, such as "GET" or "POST" or "PATCH".
    
    Requests for data (e.g. asking for a webpage) typically use "GET".
    Sending data (e.g. submitting a form, searching) typically uses "POST".
    """)
@Examples({
    """
    open a website:
        if method of request is "GET":
            # ..."""
})
@Since("1.0.0")
public class ExprMethodOfRequest extends SimplePropertyExpression<Transaction, String> {

    static {
        if (!SkriptIO.isTest())
            register(ExprMethodOfRequest.class, String.class, "method", "transaction");
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "method";
    }

    @Override
    public @Nullable String convert(Transaction request) {
        return request.getMethod();
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return mode == Changer.ChangeMode.SET ? CollectionUtils.array(String.class) : null;
    }

    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (delta == null) return;
        String method = (String) delta[0];
        if (method == null) return;
        Transaction request = this.getExpr().getSingle(event);
        if (request == null) return;
        request.setMethod(method);
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
