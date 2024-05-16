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
import org.skriptlang.skript_io.event.VisitWebsiteEvent;
import org.skriptlang.skript_io.utility.web.IncomingRequest;
import org.skriptlang.skript_io.utility.web.OutgoingRequest;
import org.skriptlang.skript_io.utility.web.Request;

@Name("Incoming Request")
@Description("""
    The current request being made of your website.
    This is typically a browser asking for a page.
    """)
@Examples({
    """
    open a website:
        set {_file} to path of request
        if file {_file} exists:
            set the status code to 200
            transfer {_file} to the response
            # Be careful!
            # This example doesn't restrict access to any files
        else:
            set the status code to 404
            add "Page not found." to the response""",
})
@Since("1.0.0")
public class ExprIncomingRequest extends SimpleExpression<IncomingRequest> {

    static {
        if (!SkriptIO.isTest())
            Skript.registerExpression(ExprIncomingRequest.class, IncomingRequest.class, ExpressionType.SIMPLE,
                "[the] [incoming] request");
    }

    private boolean outgoing;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult result) {
        return this.getParser().isCurrentEvent(VisitWebsiteEvent.class);
    }

    @Override
    protected IncomingRequest @NotNull [] get(@NotNull Event event) {
        if (event instanceof VisitWebsiteEvent visit)
            return new IncomingRequest[] {new IncomingRequest(visit.getExchange())};
        return new IncomingRequest[0];
    }

    @Override
    public Class<?> [] acceptChange(Changer.@NotNull ChangeMode mode) {
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<IncomingRequest> getReturnType() {
        return IncomingRequest.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "the incoming request";
    }

}
