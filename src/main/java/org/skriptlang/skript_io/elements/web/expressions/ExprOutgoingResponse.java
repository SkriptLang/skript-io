package org.skriptlang.skript_io.elements.web.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
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
import org.skriptlang.skript_io.event.VisitWebsiteEvent;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Writable;
import org.skriptlang.skript_io.utility.task.TransferTask;
import org.skriptlang.skript_io.utility.task.WriteTask;
import org.skriptlang.skript_io.utility.web.OutgoingResponse;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Name("Outgoing Response")
@Description("""
    Your website's response to a request made to your site.
    This resource can be written to (in order to send data back to the requester).
    
    This can be used to send data to a client, e.g. sending a page to a browser when requested.
    """)
@Example("""
        open a website:
            add {_greeting} to the response
            transfer ./site/index.html to the response
        """)
@Since("1.0.0")
public class ExprOutgoingResponse extends SimpleExpression<OutgoingResponse> {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerExpression(ExprOutgoingResponse.class, OutgoingResponse.class, ExpressionType.SIMPLE,
                "[the] [outgoing] response");
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult result) {
        return getParser().isCurrentEvent(VisitWebsiteEvent.class);
    }

    @Override
    protected OutgoingResponse @NotNull [] get(@NotNull Event event) {
        if (event instanceof VisitWebsiteEvent visit) {
            return new OutgoingResponse[] {new OutgoingResponse(visit, visit.getExchange())};
        }
        return new OutgoingResponse[0];
    }

    @Override
    public @NotNull Class<OutgoingResponse> getReturnType() {
        return OutgoingResponse.class;
    }

    @Override
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        if (mode == Changer.ChangeMode.ADD) {
            return CollectionUtils.array(String.class, Readable.class);
        }
        return null;
    }

    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (!(event instanceof VisitWebsiteEvent visit)) {
            return;
        }
        Writable writable = Writable.simple(visit.getExchange().getResponseBody());
        if (mode == Changer.ChangeMode.ADD) {
            if (!visit.isStatusCodeSet()) {
                SkriptIO.error("Tried to send data before setting the status code.");
                return;
            }
            if (delta == null) {
                return;
            }
            for (Object thing : delta) {
                switch (thing) {
                    case String string ->
                        SkriptIO.queue().queue(new WriteTask(writable, string.getBytes(StandardCharsets.UTF_8)));
                    case InputStream stream ->
                        SkriptIO.queue().queue(new TransferTask(writable, Readable.simple(stream)));
                    case Readable readable -> SkriptIO.queue().queue(new TransferTask(writable, readable));
                    case null, default -> {}
                }
            }
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "the outgoing response";
    }

}
