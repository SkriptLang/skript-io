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
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Name("Rename File")
@Description("Renames a file or directory. To rename a directory please use the 'move' effect.")
@Examples({
    "rename file ./example/test.txt to \"blob.txt\""
})
@Since("1.0.0")
public class EffRenameFile extends Effect {
    
    static {
        if (!SkriptIO.isTest())
            Skript.registerEffect(EffRenameFile.class,
                "rename [the] file [at] %path% to %string%",
                "rename %*path% to %string%");
    }
    
    private Expression<URI> pathExpression;
    private Expression<String> stringExpression;
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult result) {
        this.pathExpression = (Expression<URI>) expressions[0];
        this.stringExpression = (Expression<String>) expressions[1];
        return true;
    }
    
    @Override
    protected void execute(@NotNull Event event) {
        final URI uri = pathExpression.getSingle(event);
        final String name = stringExpression.getSingle(event);
        if (name == null || name.isBlank()) return;
        if (uri == null) return;
        final File file = SkriptIO.file(uri);
        if (file == null || file.isDirectory()) return;
        final Path from = file.toPath();
        try {
            Files.move(from, from.resolveSibling(name));
        } catch (IOException ex) {
            SkriptIO.error(ex);
        }
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "rename file " + pathExpression.toString(event, debug) + " to " + this.stringExpression.toString(event,
            debug);
    }
    
}
