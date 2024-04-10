package org.skriptlang.skript_io.elements.file.effects;

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
import org.skriptlang.skript_io.utility.file.FileController;

import java.io.File;
import java.net.URI;

import static org.skriptlang.skript_io.utility.file.FileController.READ;
import static org.skriptlang.skript_io.utility.file.FileController.WRITE;

@Name("Edit File")
@Description("Opens a file at a path for reading and writing. If the file does not exist or is inaccessible, the " +
    "section will not be run.")
@Examples({
    "edit file ./test.txt:",
    "\tset the text contents of the file to \"line 1\"",
    "\tadd \"line 2\" to the lines of the file"
})
@Since("1.0.0")
public class SecEditFile extends SecAccessFile {

    static {
        if (!SkriptIO.isTest())
            Skript.registerSection(SecEditFile.class,
                                   "(edit|open) [(a|the)] file [at] %path%"
                                  );
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        final URI uri = pathExpression.getSingle(event);
        if (uri == null) return this.walk(event, false);
        final File file = SkriptIO.file(uri);
        if (file == null) return this.walk(event, false);
        return this.edit(file, event);
    }

    protected @Nullable TriggerItem edit(File file, Event event) {
        if (!file.exists() || !file.isFile()) return this.walk(event, false);
        assert first != null;
        final FileController controller = FileController.getController(file, READ | WRITE);
        return this.walk(controller, event);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "edit file " + pathExpression.toString(event, debug);
    }

}
