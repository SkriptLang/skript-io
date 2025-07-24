package org.skriptlang.skript_io.elements.file.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.File;
import java.io.IOException;
import java.net.URI;

@Name("Create File")
@Description("""
    Creates a new file at a path.
    If the file already exists or was successfully created, opens an editing section.
    If creation fails and the file does not exist, the editing section will be skipped.
    """)
@Example("""
    create file ./test.txt:
        add "hello" to the file
    """)
@Since("1.0.0")
public class SecCreateFile extends SecEditFile {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerSection(SecCreateFile.class, "(create|make) [a] [new] file [at] %path%");
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        URI uri = pathExpression.getSingle(event);
        if (uri == null) {
            return walk(event, false);
        }
        File file = SkriptIO.file(uri);
        if (file == null || file.isDirectory()) {
            return walk(event, false);
        } else if (!file.exists()) {
            try {
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return edit(file, event);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "create file " + pathExpression.toString(event, debug);
    }

}
