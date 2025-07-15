package org.skriptlang.skript_io.elements.file.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.File;
import java.net.URI;

@Name("Files in Directory")
@Description("Returns a list of (file/folder) paths in the given directory.")
@Examples({
    """
        loop the files in ./test/:
            delete the file at loop-value"""
})
@Since("1.0.0")
public class ExprFilesInDirectory extends SimpleExpression<URI> {

    static {
        if (!SkriptIO.isTest())
            Skript.registerExpression(ExprFilesInDirectory.class, URI.class, ExpressionType.SIMPLE,
                                      "[the] files in [(directory|folder)] %path%",
                                      "[the] contents of [(directory|folder)] %path%"
                                     );
    }

    private Expression<URI> pathExpression;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult result) {
        this.pathExpression = (Expression<URI>) expressions[0];
        return true;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends URI> getReturnType() {
        return URI.class;
    }

    @Override
    protected URI @NotNull [] get(@NotNull Event event) {
        URI uri = pathExpression.getSingle(event);
        File file = SkriptIO.fileNoError(uri);
        if (file == null || !file.isDirectory()) return new URI[0];
        File[] files = file.listFiles();
        if (files == null || files.length == 0) return new URI[0];
        URI[] uris = new URI[files.length];
        for (int i = 0; i < files.length; i++) uris[i] = files[i].toURI();
        return uris;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "files in directory " + pathExpression.toString(event, debug);
    }

}
