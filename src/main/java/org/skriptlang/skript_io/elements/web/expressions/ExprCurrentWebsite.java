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
import org.skriptlang.skript_io.event.VisitWebsiteEvent;
import org.skriptlang.skript_io.utility.web.WebServer;

@Name("Current Website")
@Description("The current website, in a website section.")
@Examples({
    "open a website:",
    "\tclose the current website"
})
@Since("1.0.0")
public class ExprCurrentWebsite extends SimpleExpression<WebServer> {

    static {
        if (!SkriptIO.isTest())
            Skript.registerExpression(ExprCurrentWebsite.class, WebServer.class, ExpressionType.SIMPLE,
                                      "[the] [current] web[ ]site",
                                      "[the] [current] web[ ]server",
                                      "[the] [current] http server"
                                     );
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult result) {
        if (!this.getParser().isCurrentEvent(VisitWebsiteEvent.class)) {
            Skript.error("You can't use '" + result.expr + "' outside a website section.");
            return false;
        }
        return true;
    }

    @Override
    protected WebServer @NotNull [] get(@NotNull Event event) {
        if (event instanceof VisitWebsiteEvent visit) return new WebServer[] {visit.getServer()};
        else return new WebServer[0];
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
    public @NotNull Class<WebServer> getReturnType() {
        return WebServer.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "the website";
    }

}
