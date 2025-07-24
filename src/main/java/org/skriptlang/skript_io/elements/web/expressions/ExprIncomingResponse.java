package org.skriptlang.skript_io.elements.web.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.elements.web.effects.EffAcceptResponse;
import org.skriptlang.skript_io.elements.web.sections.SecOpenRequest;
import org.skriptlang.skript_io.utility.web.IncomingResponse;

@Name("Incoming Response")
@Description("""
    A response to a request you made to a website.
    This resource can be read from (in order to receive data).
    """)
@Example("""
     open a web request to https://skriptlang.org:
        set the request's method to "GET"
        await the response:
            broadcast the response's text content
     """)
@Since("1.0.0")
public class ExprIncomingResponse extends SimpleExpression<IncomingResponse> {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerExpression(ExprIncomingResponse.class, IncomingResponse.class, ExpressionType.SIMPLE,
                "[the] [incoming] response");
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult result) {
        return getParser().isCurrentSection(SecOpenRequest.class);
    }

    @Override
    protected IncomingResponse @NotNull [] get(@NotNull Event event) {
        if (EffAcceptResponse.getCurrentResponse(event) instanceof IncomingResponse readable) {
            return new IncomingResponse[] {readable};
        }
        return new IncomingResponse[0];
    }

    @Override
    public Class<?> [] acceptChange(Changer.@NotNull ChangeMode mode) {
        return null;
    }

    @Override
    public @NotNull Class<IncomingResponse> getReturnType() {
        return IncomingResponse.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "the incoming response";
    }

}
