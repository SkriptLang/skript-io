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

@Name("Content Type of Request/Response")
@Description("""
    The data format a web request will use, such as "application/json" or "text/html".
    
    When making a request you may have to use a specific content type (and format your data accordingly!)
    When receiving a request, this should indicate the format of the incoming data.
    Not all web requests will have data attached.
    """)
@Examples({
    """
    open a web request to http://localhost:3000:
        set the request's content-type to "application/json\""""
})
@Since("1.0.0")
public class ExprContentTypeOfRequest extends SimplePropertyExpression<Transaction, String> {

    static {
        if (!SkriptIO.isTest())
            register(ExprContentTypeOfRequest.class, String.class, "content(-| )type", "transaction");
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "content type";
    }

    @Override
    public @Nullable String convert(Transaction request) {
        return request.getContentType();
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return mode == Changer.ChangeMode.SET ? CollectionUtils.array(String.class) : null;
    }

    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (delta == null || delta.length < 1) return;
        String type = String.valueOf(delta[0]);
        if (type == null) return;
        Transaction request = this.getExpr().getSingle(event);
        if (request == null) return;
        request.setContentType(type);
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
