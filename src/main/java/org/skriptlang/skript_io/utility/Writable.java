package org.skriptlang.skript_io.utility;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.task.WriteTask;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public interface Writable extends Resource {
    
    static Writable simple(OutputStream stream) {
        return new SimpleWritable(stream);
    }
    
    default void write(String text) {
        SkriptIO.queue().queue(new WriteTask(this, text.getBytes(StandardCharsets.UTF_8)));
    }
    
    default void clear() {
        SkriptIO.queue().queue(new WriteTask(this, new byte[0]));
    }
    
    @NotNull OutputStream acquireWriter() throws IOException;
    
}

record SimpleWritable(OutputStream acquireWriter) implements Writable {

}
