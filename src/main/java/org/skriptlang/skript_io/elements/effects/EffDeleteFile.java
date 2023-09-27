package org.skriptlang.skript_io.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.File;
import java.net.URI;

@Name("Delete File/Directory")
@Description("Deletes the folder or file at the given path.")
@Examples({"recursively delete folder ./test/", "delete folder ./test/", "delete the file at ./config.txt"})
@Since("1.0.0")
public class EffDeleteFile extends Effect {
    
    static {
        Skript.registerEffect(EffDeleteFile.class, "[recursive:recursive[ly]] delete [the] folder [at] %path%",
            "[recursive:recursive[ly]] delete [the] directory [at] %path%", "delete [the] file [at] %path%");
    }
    
    private boolean recursive, folder;
    private Expression<URI> pathExpression;
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult result) {
        this.pathExpression = (Expression<URI>) expressions[0];
        this.recursive = result.hasTag("recursive");
        this.folder = matchedPattern < 2;
        return true;
    }
    
    @Override
    protected void execute(@NotNull Event event) {
        final URI uri = pathExpression.getSingle(event);
        if (uri == null) return;
        final File file = SkriptIO.file(uri);
        if (file == null) return;
        if (!file.exists()) return;
        if (folder && !file.isDirectory()) return;
        else if (!folder && !file.isFile()) return;
        if (folder) {
            if (!file.isDirectory()) return;
            if (recursive) this.emptyDirectory(file);
        } else {
            if (!file.isFile()) return;
        }
        final boolean result = file.delete();
        assert result : "File '" + file + "' was not deleted.";
    }
    
    protected void emptyDirectory(File file) {
        final File[] files = file.listFiles();
        if (files == null) return;
        for (final File child : files) {
            if (child.isDirectory()) this.emptyDirectory(child);
            final boolean result = child.delete();
            assert result : "Inner file '" + file + "' was not deleted.";
        }
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return (recursive ? "recursively " : "") + "delete " + (folder ? "folder " : "file ") + this.pathExpression.toString(
            event, debug);
    }
    
}
