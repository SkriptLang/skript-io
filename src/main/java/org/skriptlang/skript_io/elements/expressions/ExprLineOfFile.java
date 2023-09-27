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

@Name("Line of File")
@Description("Reads an individual line of a file. Line indexing begins at 1. The value will be empty if the file ended or could not be read.")
@Examples({
    "broadcast line 1 of the current file"
})
@Since("1.0.0")
public class ExprLineOfFile extends SimpleExpression<String> {
    
    static {
        if (!SkriptIO.isTest())
            Skript.registerExpression(ExprLineOfFile.class, String.class, ExpressionType.SIMPLE,
                "line %number% of %file%"
            );
    }
    
    private Expression<Number> lineExpression;
    private Expression<FileController> fileExpression;
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult result) {
        this.lineExpression = (Expression<Number>) expressions[0];
        this.fileExpression = (Expression<FileController>) expressions[1];
        return true;
    }
    
    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }
    
    @Override
    public boolean isSingle() {
        return true;
    }
    
    @Override
    protected String @NotNull [] get(@NotNull Event event) {
        final Number lineNumber = lineExpression.getSingle(event);
        final int line = lineNumber != null ? Math.max(0, (lineNumber.intValue() - 1)) : 0;
        final FileController file = fileExpression.getSingle(event);
        if (file == null) return new String[0];
        final String text = file.getLine(line);
        return new String[]{text};
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "line " + " of " + fileExpression.toString(event, debug);
    }
    
}
