package org.skriptlang.skript_io.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.FileController;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.skriptlang.skript_io.utility.FileController.READ;

@Name("Read File")
@Description("""
    Opens a file at a path only for reading.
    The file cannot be written to.
    If the file does not exist or is unreadable, the section will not be run.""")
@Examples({
    "read file ./test.txt:",
    "\tloop the lines of the file:",
    "\t\tbroadcast loop-value"
})
@Since("1.0.0")
public class SecReadFile extends SecAccessFile {
    
    static {
        Skript.registerSection(SecReadFile.class,
            "read [(a|the)] file [at] %path%"
        );
    }
    
    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        final URI uri = pathExpression.getSingle(event);
        if (uri == null) return this.walk(event, false);
        final File file = SkriptIO.file(uri);
        if (file == null) return this.walk(event, false);
        return this.read(file, event);
    }
    
    protected @Nullable TriggerItem read(File file, Event event) {
        if (!file.exists() || !file.isFile()) return this.walk(event, false);
        assert first != null;
        try (final FileController controller = FileController.getController(file, READ)) {
            FileController.push(event, controller);
            TriggerItem.walk(first, event); // execute the section now
        } catch (IOException ex) {
            SkriptIO.error(ex);
        } finally {
            FileController.pop(event);
        }
        return this.walk(event, false);
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "read file " + pathExpression.toString(event, debug);
    }
    
}
