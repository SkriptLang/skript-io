package org.skriptlang.skript_io.elements.web.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.web.Transaction;

@Name("Header of Request")
@Description("""
    A key/value-based header in a request, such as "Content-Type" -> "text/html".
    
    Request headers information about the client requesting the resource.
    Response headers hold information about the response.
    """)
@Examples({
    """
    open a web request to http://localhost:3000:
        set the request's "Content-Encoding" header to "gzip\""""
})
@Since("1.0.0")
public class ExprHeaderOfRequest extends SimpleExpression<String> {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerExpression(ExprHeaderOfRequest.class, String.class, ExpressionType.PROPERTY,
                "[the] %string% header of %transaction%",
                "%transaction%'[s] %string% header");
    }

    private Expression<Transaction> requestExpression;
    private Expression<String> headerExpression;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.ParseResult result) {
        requestExpression = ((Expression<Transaction>) expressions[1 - matchedPattern]);
        headerExpression = ((Expression<String>) expressions[matchedPattern]);
        return true;
    }

    @Override
    protected @Nullable String[] get(Event event) {
        Transaction request = requestExpression.getSingle(event);
        if (request == null) return new String[0];
        return new String[] {request.getHeader(headerExpression.getSingle(event))};
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
        Transaction request = requestExpression.getSingle(event);
        if (request == null) return;
        request.setHeader(headerExpression.getSingle(event), type);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "the " + headerExpression.toString(event, debug) + " of " + requestExpression.toString(event, debug);
    }

}
