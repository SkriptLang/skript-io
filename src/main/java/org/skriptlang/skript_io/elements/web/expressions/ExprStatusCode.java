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
import org.skriptlang.skript_io.elements.web.effects.EffAcceptResponse;
import org.skriptlang.skript_io.elements.web.effects.SecAcceptResponse;
import org.skriptlang.skript_io.event.VisitWebsiteEvent;
import org.skriptlang.skript_io.utility.web.Response;
import org.skriptlang.skript_io.utility.web.Transaction;

@Name("Status Code")
@Description("""
    The status code of a web request.
    A `200` status is OK.
    
    When receiving a response, this is the status of your previous request.
    
    When responding to a request, this must be set before data can be transferred.
    """)
@Examples({
    """
    open a website:
        set the status code to 200 # OK
        transfer ./site/index.html to the response"""
})
@Since("1.0.0")
public class ExprStatusCode extends SimpleExpression<Number> {

    static {
        if (!SkriptIO.isTest())
            Skript.registerExpression(ExprStatusCode.class, Number.class, ExpressionType.SIMPLE,
                "[the] status code",
                "([the] status code of %-response%|%-response%'[s] status code)"
                                     );
    }

    private @Nullable Expression<Transaction> response;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult result) {
        if (this.getParser().isCurrentEvent(VisitWebsiteEvent.class)
            || this.getParser().isCurrentSection(SecAcceptResponse.class)) {
            return true;
        }
        if (matchedPattern == 0) {
            Skript.error("You can't use '" + result.expr + "' outside a website section.");
            return false;
        } else {
            this.response = (Expression<Transaction>) expressions[0];
            return true;
        }
    }

    @Override
    protected Number @NotNull [] get(@NotNull Event event) {
        if (response != null && response.getSingle(event) instanceof Response response) {
            return new Number[] {response.statusCode()};
        } else if (event instanceof VisitWebsiteEvent visit)
            return new Number[] {visit.getStatusCode()};
        else if (EffAcceptResponse.getCurrentResponse(event) instanceof Response response) {
            return new Number[] {response.statusCode()};
        } else return new Number[0];
    }

    @Override
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET) return CollectionUtils.array(Number.class, Integer.class, Long.class);
        return null;
    }

    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (response != null && response.getSingle(event) instanceof Response response) {
            if (mode == Changer.ChangeMode.SET) {
                if (delta == null) return;
                if (delta.length < 1) return;
                if (delta[0] instanceof Number number) response.setStatusCode(number.intValue());
            }
        } else if (event instanceof VisitWebsiteEvent visit) {
            if (mode == Changer.ChangeMode.SET) {
                if (delta == null) return;
                if (delta.length < 1) return;
                if (delta[0] instanceof Number number) visit.setStatusCode(number.intValue());
            }
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<Number> getReturnType() {
        return Number.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        if (response != null) return "the status code of " + response.toString(event, debug);
        return "the status code";
    }

}
