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

@Name("File in Directory")
@Description("Returns a file relative to a directory.")
@Examples({
    """
        set {_folder} to ./test/
        set {_file} to ./MyFile.txt in directory {_folder}
        """
})
@Since("1.0.0")
public class ExprFileInDirectory extends SimpleExpression<URI> {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerExpression(ExprFileInDirectory.class, URI.class, ExpressionType.SIMPLE,
                "[[the] file] %path% in [(directory|folder)] %path%"
            );
    }

    private Expression<URI> targetExpression, sourceExpression;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult result) {
        sourceExpression = (Expression<URI>) expressions[0];
        targetExpression = (Expression<URI>) expressions[1];
        return true;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends URI> getReturnType() {
        return URI.class;
    }

    @Override
    protected URI @NotNull [] get(@NotNull Event event) {
        URI target = targetExpression.getSingle(event), source = sourceExpression.getSingle(event);
        File file = SkriptIO.fileNoError(target);
        if (source == null) return new URI[0];
        if (source.getPath().isEmpty()) return new URI[0];
        return new URI[] {new File(file, source.getPath()).toURI()};
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "file " + sourceExpression.toString(event, debug)
            + " in directory " + targetExpression.toString(event, debug);
    }

}
