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
import org.skriptlang.skript_io.utility.web.Request;

@Name("Method of Request")
@Description("""
    The type of a web request, such as "GET" or "POST" or "PATCH".
    
    Requests for data (e.g. asking for a webpage) typically use "GET".
    Sending data (e.g. submitting a form, searching) typically uses "POST".
    """)
@Examples({
    "open a website:",
    "\tif method of request is \"GET\":"
})
@Since("1.0.0")
public class ExprMethodOfRequest extends SimplePropertyExpression<Request, String> {
    
    static {
        if (!SkriptIO.isTest())
            register(ExprMethodOfRequest.class, String.class, "method", "request");
    }
    
    @Override
    protected @NotNull String getPropertyName() {
        return "method";
    }
    
    @Override
    public @Nullable String convert(Request request) {
        return request.setMethod();
    }
    
    @Override
    @Nullable
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return mode == Changer.ChangeMode.SET ? CollectionUtils.array(String.class) : null;
    }
    
    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (delta == null) return;
        final String method = (String) delta[0];
        if (method == null) return;
        final Request request = this.getExpr().getSingle(event);
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
