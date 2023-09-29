package org.skriptlang.skript_io.utility;

import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.task.CloseTask;

import java.io.Closeable;

public class DummyCloseTrigger extends TriggerItem {
    
    private final Closeable closeable;
    private final @Nullable TriggerItem next;
    
    public DummyCloseTrigger(Closeable closeable, @Nullable TriggerItem walk) {
        this.closeable = closeable;
        this.next = walk;
    }
    
    @Override
    protected @Nullable TriggerItem walk(Event e) {
        this.run(e);
        return next;
    }
    
    @Override
    protected boolean run(Event e) {
        SkriptIO.remoteQueue().queue(new CloseTask(closeable));
        return true;
    }
    
    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "kenzie's illegal closing task";
    }
    
}
