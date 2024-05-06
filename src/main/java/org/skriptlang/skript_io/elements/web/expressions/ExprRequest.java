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
    "open a website:",
    "\tset {_file} to path of request",
    "\tif file {_file} exists:",
    "\t\tset the status code to 200",
    "\t\ttransfer {_file} to the response",
    "\telse:",
    "\t\tset the status code to 404",
    "\t\tadd \"Page not found.\" to the response",
})
@Since("1.0.0")
public class ExprRequest extends SimpleExpression<Request> {

    static {
        if (!SkriptIO.isTest())
            Skript.registerExpression(ExprRequest.class, Request.class, ExpressionType.SIMPLE,
                "[the] [current] request");
    }

    private boolean outgoing;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult result) {
        if (this.getParser().isCurrentEvent(VisitWebsiteEvent.class)) {
            return true;
        } else if (this.getParser().isCurrentSection(SecOpenRequest.class)) {
            this.outgoing = true;
            return true;
        }
        Skript.error("You can't use '" + result.expr + "' outside a website section.");
        return true;
    }

    @Override
    protected Request @NotNull [] get(@NotNull Event event) {
        if (event instanceof VisitWebsiteEvent visit)
            return new Request[] {new IncomingRequest(visit.getExchange())};
        if (outgoing) {
            final OutgoingRequest request = SecOpenRequest.getCurrentRequest(event);
            if (request != null) return new Request[] {request};
        }
        return new Request[0];
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
    public @NotNull Class<Request> getReturnType() {
        return Request.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "the request";
    }

}
