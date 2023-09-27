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

@Name("Create Directory")
@Description("Creates a new folder at the given path, if one does not exist.")
@Examples({
    "create a new folder ./test/"
})
@Since("1.0.0")
public class EffCreateDirectory extends Effect {
    
    static {
        if (!SkriptIO.isTest())
            Skript.registerEffect(EffCreateDirectory.class,
                "(create|make) [a] [new] folder [at] %path%",
                "(create|make) [a] [new] directory [at] %path%"
            );
    }
    
    private Expression<URI> pathExpression;
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult result) {
        this.pathExpression = (Expression<URI>) expressions[0];
        return true;
    }
    
    @Override
    protected void execute(@NotNull Event event) {
        final URI uri = pathExpression.getSingle(event);
        if (uri == null) return;
        final File file = SkriptIO.file(uri);
        if (file == null) return;
        if (file.exists() || file.isFile()) return;
        final boolean result = file.mkdirs();
        assert result : "Directories were not made for '" + file + "'";
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "create directory " + pathExpression.toString(event, debug);
    }
    
}
