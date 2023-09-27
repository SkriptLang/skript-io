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

import java.net.URI;

@Name("Path of Request")
@Description("""
    The (file) path a web request is asking for.
    This is typically a browser asking for a page, e.g. `/something/page.html`.
    
    Properly-formatted requests typically start with an absolute `/...` - be careful when serving content.
    """)
@Examples({
    "open a website:",
    "\tbroadcast the request's path"
})
@Since("1.0.0")
public class ExprPathOfRequest extends SimplePropertyExpression<Request, URI> {
    
    static {
        if (!SkriptIO.isTest())
            register(ExprPathOfRequest.class, URI.class, "path", "request");
    }
    
    @Override
    protected @NotNull String getPropertyName() {
        return "path";
    }
    
    @Override
    public @Nullable URI convert(Request request) {
        return request.getPath();
    }
    
    @Override
    @Nullable
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return mode == Changer.ChangeMode.SET ? CollectionUtils.array(URI.class, String.class) : null;
    }
    
    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (delta == null) return;
        final URI path;
        if (delta[0] instanceof String string) path = URI.create(string);
        else if (delta[0] instanceof URI uri) path = uri;
        else return;
        final Request request = this.getExpr().getSingle(event);
        if (request == null) return;
        request.setPath(path);
    }
    
    @Override
    public @NotNull Class<? extends URI> getReturnType() {
        return URI.class;
    }
    
    @Override
    public boolean isSingle() {
        return true;
    }
    
}
