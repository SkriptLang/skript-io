package org.skriptlang.skript_io.elements.web.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.web.Request;

@Name("Header of Request")
@Description("""
    A key/value-based header in a request, such as "Content-Type" -> "text/html".
    
    Request headers information about the client requesting the resource.
    Response headers hold information about the response.
    """)
@Examples({
    "open a web request to http://localhost:3000:",
    "\tset the request's \"Content-Encoding\" header to \"gzip\""
})
@Since("1.0.0")
public class ExprHeaderOfRequest extends SimplePropertyExpression<Request, String> {
    
    static {
        if (!SkriptIO.isTest())
            register(ExprHeaderOfRequest.class, String.class, "%*string% header", "request");
    }
    
    private String header;
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.ParseResult result) {
        this.setExpr((Expression<Request>) expressions[1 - matchedPattern]);
        this.header = ((Literal<String>) expressions[matchedPattern]).getSingle();
        return true;
    }
    
    @Override
    protected @NotNull String getPropertyName() {
        return header + " header";
    }
    
    @Override
    public @Nullable String convert(Request request) {
        return request.getHeader(header);
    }
    
    @Override
    @Nullable
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return mode == Changer.ChangeMode.SET ? CollectionUtils.array(String.class) : null;
    }
    
    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (delta == null || delta.length < 1) return;
        final String type = String.valueOf(delta[0]);
        if (type == null) return;
        final Request request = this.getExpr().getSingle(event);
        if (request == null) return;
        request.setHeader(header, type);
    }
    
    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }
    
    @Override
    public boolean isSingle() {
        return true;
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "the \"" + header + "\" of " + this.getExpr().toString(event, debug);
    }
    
}
