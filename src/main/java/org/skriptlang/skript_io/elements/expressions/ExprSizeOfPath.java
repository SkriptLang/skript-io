package org.skriptlang.skript_io.elements.expressions;

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
import org.skriptlang.skript_io.utility.FileController;

import java.io.File;
import java.net.URI;

@Name("Size of File Path")
@Description("The size (in bytes) of a file by path. Non-files have a size of zero.")
@Examples({
    "set {_size} to the file size of ./test.txt"
})
@Since("1.0.0")
public class ExprSizeOfPath extends SimpleExpression<Number> {
    
    static {
        Skript.registerExpression(ExprSizeOfFile.class, Number.class, ExpressionType.SIMPLE,
            "[the] [file[ ]]size[s] of %paths%"
        );
    }
    
    private Expression<URI> pathExpression;
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult result) {
        this.pathExpression = (Expression<URI>) expressions[0];
        return true;
    }
    
    @Override
    public @NotNull Class<? extends Number> getReturnType() {
        return Long.class;
    }
    
    @Override
    public boolean isSingle() {
        return false;
    }
    
    @Override
    protected Number @NotNull [] get(@NotNull Event event) {
        final @Nullable URI[] array = pathExpression.getArray(event);
        if (array == null || array.length == 0) return new Number[0];
        final Number[] numbers = new Number[array.length];
        for (int i = 0; i < array.length; i++) {
            final File file = SkriptIO.fileNoError(array[i]);
            if (file == null || !file.isFile()) numbers[i] = 0;
            else numbers[i] = FileController.sizeOf(file);
        }
        return numbers;
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "file size of " + pathExpression.toString(event, debug);
    }
    
}
