package org.skriptlang.skript_io.elements.file.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.file.FileController;

import java.io.File;
import java.net.URI;

import static org.skriptlang.skript_io.utility.file.FileController.READ;

@Name("Read File")
@Description("""
    Opens a file at a path only for reading.
    The file **cannot** be written to.
    If the file does not exist or is unreadable, the section will not be run.""")
@Example("""
        read file ./test.txt:
            loop the lines of the file:
                broadcast loop-value
        """)
@Example("""
        read file ./test.txt:
            broadcast the text contents of the file
    """)
@Since("1.0.0")
public class SecReadFile extends SecAccessFile {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerSection(SecReadFile.class, "read [(a|the)] file [at] %path%");
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        URI uri = pathExpression.getSingle(event);
        if (uri == null) {
            return walk(event, false);
        }
        File file = SkriptIO.file(uri);
        if (file == null) {
            return walk(event, false);
        }
        return read(file, event);
    }

    protected @Nullable TriggerItem read(File file, Event event) {
        if (!file.exists() || !file.isFile()) {
            return walk(event, false);
        }
        assert first != null;
        FileController controller = FileController.getController(file, READ);
        return walk(controller, event);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "read file " + pathExpression.toString(event, debug);
    }

}
