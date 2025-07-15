package org.skriptlang.skript_io.elements.file.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import mx.kenzie.clockwork.io.DataTask;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.file.FileController;

import java.io.File;
import java.net.URI;

@Name("Delete File/Directory")
@Description("Deletes the folder or file at the given path.")
@Examples({
    """
    recursively delete folder ./test/""",
    """
    delete folder ./test/""",
    """
    delete the file at ./config.txt"""
})
@Since("1.0.0")
public class EffDeleteFile extends Effect {

    static {
        if (!SkriptIO.isTest())
            Skript.registerEffect(EffDeleteFile.class, "[recursive:recursive[ly]] delete [the] folder [at] %path%",
                                  "[recursive:recursive[ly]] delete [the] directory [at] %path%", "delete [the] file " +
                                      "[at] %path%");
    }

    private boolean recursive, folder;
    private Expression<URI> pathExpression;

    public static void delete(File file, boolean folder, boolean recursive) {
        if (file == null) return;
        FileController.flagDirty(file);
        SkriptIO.queue().queue(new DataTask() {
            @Override
            public void execute() {
                try {
                    if (!file.exists()) return;
                    if (folder && !file.isDirectory()) return;
                    else if (!folder && !file.isFile()) return;
                    if (folder) {
                        if (!file.isDirectory()) return;
                        if (recursive) emptyDirectory(file);
                    } else {
                        if (!file.isFile()) return;
                    }
                    boolean result = file.delete();
                    assert result : "File '" + file + "' was not deleted.";
                } finally {
                    FileController.flagClean(file);
                }
            }
        });

    }

    protected static void emptyDirectory(File file) {
        File[] files = file.listFiles();
        if (files == null) return;
        for (File child : files) {
            if (child.isDirectory()) emptyDirectory(child);
            boolean result = child.delete();
            assert result : "Inner file '" + file + "' was not deleted.";
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result) {
        this.pathExpression = (Expression<URI>) expressions[0];
        this.recursive = result.hasTag("recursive");
        this.folder = matchedPattern < 2;
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        URI uri = pathExpression.getSingle(event);
        if (uri == null) return;
        File file = SkriptIO.file(uri);
        delete(file, folder, recursive);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return (recursive ? "recursively " : "") + "delete " + (folder ? "folder " : "file ") + this.pathExpression.toString(
            event, debug);
    }

}
