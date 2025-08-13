package org.skriptlang.skript_io.elements.file.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
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
import java.util.stream.Stream;

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
        if (!SkriptIO.isTestMode())
            Skript.registerExpression(ExprFilesInDirectory.class, URI.class, ExpressionType.SIMPLE,
                                      "[the] [recursive:recursive] files in [(directory|folder)] %path%",
                                      "[the] [recursive:recursive] contents of [(directory|folder)] %path%");
    }

    private boolean isRecursive;
    private Expression<URI> pathExpression;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        @NotNull ParseResult result) {
        isRecursive = result.hasTag("recursive");
        pathExpression = (Expression<URI>) expressions[0];
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

        if (file == null || !file.isDirectory()) {
            return new URI[0];
        }

        int maxDepth = isRecursive ? Integer.MAX_VALUE : 1;

        try (Stream<Path> files = Files.walk(file.toPath(), maxDepth)) {
            return (URI[]) files.map(Path::toUri)
                    .skip(1) // the first one is always expr-1
                    .toArray();
        } catch (IOException e) {
            SkriptIO.error(e);
        }

        return new URI[0];
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return (isRecursive ? "recursive " : "") + "files in directory " + pathExpression.toString(event, debug);
    }

}
