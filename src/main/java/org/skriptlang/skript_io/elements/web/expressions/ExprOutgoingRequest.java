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
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.elements.web.effects.SecOpenRequest;
import org.skriptlang.skript_io.utility.web.OutgoingRequest;

@Name("Outgoing Request")
@Description("""
    The current request being made to a website.""")
@Examples({
    """
        open a request to http://my-api-here:
            set the request's json content to {_data::*}""",
})
@Since("1.0.0")
public class ExprOutgoingRequest extends SimpleExpression<OutgoingRequest> {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerExpression(ExprOutgoingRequest.class, OutgoingRequest.class, ExpressionType.SIMPLE,
                "[the] [outgoing] request");
    }

    private boolean outgoing;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult result) {
        if (getParser().isCurrentSection(SecOpenRequest.class)) {
            outgoing = true;
            return true;
        }
        return false;
    }

    @Override
    protected OutgoingRequest @NotNull [] get(@NotNull Event event) {
        OutgoingRequest request = SecOpenRequest.getCurrentRequest(event);
        if (request != null) return new OutgoingRequest[] {request};
        return new OutgoingRequest[0];
    }

    @Override
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<OutgoingRequest> getReturnType() {
        return OutgoingRequest.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "the outgoing request";
    }

}
