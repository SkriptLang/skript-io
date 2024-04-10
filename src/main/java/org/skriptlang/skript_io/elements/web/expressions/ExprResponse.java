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
import org.skriptlang.skript_io.elements.web.effects.SecAcceptResponse;
import org.skriptlang.skript_io.elements.web.effects.SecOpenRequest;
import org.skriptlang.skript_io.event.VisitWebsiteEvent;
import org.skriptlang.skript_io.utility.DummyOutputStream;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Resource;
import org.skriptlang.skript_io.utility.Writable;
import org.skriptlang.skript_io.utility.task.TransferTask;
import org.skriptlang.skript_io.utility.task.WriteTask;
import org.skriptlang.skript_io.utility.web.IncomingResponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Name("Web Response")
@Description("""
    Your website's response to a request made to your site.
    This resource can be written to (in order to send data back to the requester).
        
    This can be used to send data to a client, e.g. sending a page to a browser when requested.
    """)
@Examples({
    "open a website:",
    "\tadd {_greeting} to the response",
    "\ttransfer ./site/index.html to the response"
})
@Since("1.0.0")
public class ExprResponse extends SimpleExpression<Resource> {

    static {
        if (!SkriptIO.isTest())
            Skript.registerExpression(ExprResponse.class, Resource.class, ExpressionType.SIMPLE,
                                      "[the] response"
                                     );
    }

    private boolean outgoing;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult result) {
        if (this.getParser().isCurrentEvent(VisitWebsiteEvent.class)) {
            this.outgoing = true;
            return true;
        } else if (this.getParser().isCurrentSection(SecOpenRequest.class)) {
            return true;
        }
        Skript.error("You can't use '" + result.expr + "' outside a website section.");
        return false;
    }

    @Override
    protected Resource @NotNull [] get(@NotNull Event event) {
        if (event instanceof VisitWebsiteEvent visit)
            return new Resource[] {
                new Writable() {
                    @Override
                    public @NotNull OutputStream acquireWriter() {
                        if (!visit.isStatusCodeSet()) {
                            SkriptIO.error("Tried to send data before setting the status code.");
                            return DummyOutputStream.INSTANCE;
                        }
                        return visit.getExchange().getResponseBody();
                    }
                }
            };
        else if (SecAcceptResponse.getCurrentRequest(event) instanceof IncomingResponse readable) {
            return new Resource[] {readable};
        } else return new Resource[0];
    }

    @Override
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        if (outgoing && mode == Changer.ChangeMode.ADD) return CollectionUtils.array(String.class, Readable.class);
        return null;
    }

    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (event instanceof VisitWebsiteEvent visit) {
            final Writable writable = Writable.simple(visit.getExchange().getResponseBody());
            if (mode == Changer.ChangeMode.ADD) {
                if (!visit.isStatusCodeSet()) {
                    SkriptIO.error("Tried to send data before setting the status code.");
                    return;
                }
                if (delta == null) return;
                for (final Object thing : delta) {
                    if (thing == null) continue;
                    if (thing instanceof String string)
                        SkriptIO.queue().queue(new WriteTask(writable, string.getBytes(StandardCharsets.UTF_8)));
                    else if (thing instanceof InputStream stream)
                        SkriptIO.queue().queue(new TransferTask(writable, Readable.simple(stream)));
                    else if (thing instanceof Readable readable)
                        SkriptIO.queue().queue(new TransferTask(writable, readable));
                }
            }
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<Resource> getReturnType() {
        return Resource.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "the response";
    }

}
