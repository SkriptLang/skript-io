package org.skriptlang.skript_io.elements.file.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.file.FileController;

import java.net.URI;

@Name("Size of Path")
@Description("The size (in bytes) of a file by path. Non-files have a size of zero.")
@Examples({"set {_size} to the size of ./test.txt"})
@Since("1.0.0")
public class ExprSizeOfPath extends SimplePropertyExpression<URI, Number> {

    static {
        if (!SkriptIO.isTest()) register(ExprSizeOfPath.class, Number.class, "[file] size", "path");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed,
                        SkriptParser.ParseResult parseResult) {
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "size";
    }

    @Override
    public @Nullable Number convert(URI uri) {
        return FileController.sizeOf(SkriptIO.fileNoError(uri));
    }

    @Override
    public @NotNull Class<? extends Number> getReturnType() {
        return Long.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

}
