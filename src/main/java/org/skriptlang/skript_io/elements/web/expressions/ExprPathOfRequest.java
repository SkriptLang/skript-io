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
    
    Properly-formatted requests typically start with an absolute `/...`.
    
    > Be careful when serving content based on a request!
    > If you do not filter requests (e.g. prohibiting `../`) then the requester may be able to
    access system files outside the server directory.
    """)
@Examples({
    """
    open a website:
        broadcast the request's path"""
})
@Since("1.0.0")
public class ExprPathOfRequest extends SimplePropertyExpression<Request, URI> {

    static {
        if (!SkriptIO.isTestMode())
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
        URI path;
        if (delta[0] instanceof String string) path = URI.create(string);
        else if (delta[0] instanceof URI uri) path = uri;
        else return;
        Request request = getExpr().getSingle(event);
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
