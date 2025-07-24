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
import static org.skriptlang.skript_io.utility.file.FileController.WRITE;

@Name("Edit File")
@Description("""
    Opens a file at a path for reading and writing.
    If the file does not exist or is inaccessible, the section will not be run.
    """)
@Example("""
    edit file ./test.txt:
        set the text contents of the file to "line 1"
        add "line 2" to the lines of the file
    """)
@Since("1.0.0")
public class SecEditFile extends SecAccessFile {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerSection(SecEditFile.class, "(edit|open) [(a|the)] file [at] %path%");
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
        return edit(file, event);
    }

    protected @Nullable TriggerItem edit(File file, Event event) {
        if (!file.exists() || !file.isFile()) {
            return walk(event, false);
        }
        assert first != null;
        FileController controller = FileController.getController(file, READ | WRITE);
        return walk(controller, event);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "edit file " + pathExpression.toString(event, debug);
    }

}
