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
import org.skriptlang.skript_io.utility.task.TidyTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Name("Move File/Directory")
@Description("""
    Moves a file or folder to a target position, overwriting the previous file.
    The source file or folder will replace whatever is at the destination path.
    If the 'into' version is used, the source will be moved inside the target directory, rather than replacing it.
    """)
@Examples({
    "move file ./example/test.txt to ./config/test.txt",
    "move file ./test.txt to ./blob.txt",
    "move file ./test.txt into ./config/",
    "move folder ./example/ to ./config/",
    "move folder ./example/ into ./config/"
})
@Since("1.0.0")
public class EffMoveFile extends Effect {
    
    static {
        if (!SkriptIO.isTest())
            Skript.registerEffect(EffMoveFile.class, "move [the] file [at] %path% [into:in]to %path%",
                "move [the] (folder|directory) [at] %path% [into:in]to %path%");
    }
    
    private boolean folder;
    private boolean into;
    private Expression<URI> pathExpression, targetExpression;
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult result) {
        this.pathExpression = (Expression<URI>) expressions[0];
        this.targetExpression = (Expression<URI>) expressions[1];
        this.folder = matchedPattern > 0;
        this.into = result.hasTag("into");
        return true;
    }
    
    @Override
    protected void execute(@NotNull Event event) {
        final URI uri = pathExpression.getSingle(event), result = targetExpression.getSingle(event);
        if (uri == null || result == null) return;
        final File file = SkriptIO.file(uri);
        final File target = SkriptIO.file(result);
        if (file == null || target == null) return;
        if (!file.exists()) return;
        if (FileController.isDirty(file)) SkriptIO.queue().queue(new TidyTask()).await();
        final Path from = file.toPath();
        FileController.flagDirty(file);
        SkriptIO.queue().queue(new DataTask() {
            @Override
            public void execute() throws IOException {
                try {
                    final Path to;
                    if (into && !target.isDirectory()) {
                        SkriptIO.error("Tried to move file '" + file + "' into non-directory '" + target + "'");
                        return;
                    } else if (into) to = target.toPath().resolve(from.getFileName());
                    else if (file.isFile() && target.isFile()) to = target.toPath();
                    else if (file.isFile() && target.isDirectory()) to = target.toPath().resolve(from.getFileName());
                    else to = target.toPath();
                    Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    FileController.flagClean(file);
                }
            }
        });
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        final String into = this.into ? " into " : " to ", type = folder ? "move folder " : "move file ";
        return type + pathExpression.toString(event, debug) + into + this.targetExpression.toString(event, debug);
    }
    
}
