package org.skriptlang.skript_io.elements.web.events;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.event.VisitWebsiteEvent;
import org.skriptlang.skript_io.utility.web.WebServer;

import static org.skriptlang.skript_io.elements.web.events.EvtVisitWebsite.*;

@Name(NAME)
@Description(DESCRIPTION)
@Examples({
    """
    on website visit:
        set the status code to 200"""})
@Since(SINCE)
public class EvtVisitWebsite extends SkriptEvent {

    protected static final String SINCE = "1.0.0", NAME = "Visit Website", DESCRIPTION = """
        Called when a website running from this server is visited.
        This could be from a browser asking for a page or a web request.
        
        While requests can be read and responded to in this event listener,
        it is much safer to use the dedicated website section.""";

    static {
        if (false) { // todo this is dangerous currently
            Skript.registerEvent(NAME, EvtVisitWebsite.class, VisitWebsiteEvent.class,
                                 "website visit", "visiting [a] website", "web[site] request")
                  .description(DESCRIPTION)
                  .examples("on website visit:",
                            "\tset the status code to 200")
                  .since(SINCE);
            EventValues.registerEventValue(VisitWebsiteEvent.class, WebServer.class, new Getter<>() {
                @Override
                public WebServer get(final VisitWebsiteEvent e) {
                    return e.getServer();
                }
            }, 0);
        }
    }

    public EvtVisitWebsite() {
    }

    @Override
    public boolean init(final Literal<?>[] args, final int matchedPattern, final SkriptParser.ParseResult parser) {
        return true;
    }

    @Override
    public boolean check(final Event e) {
        return true;
    }

    @Override
    public @NotNull String toString(final Event e, final boolean debug) {
        return "website visit";
    }

}
