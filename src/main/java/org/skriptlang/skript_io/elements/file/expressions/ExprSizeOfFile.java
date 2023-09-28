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

@Name("Size of File")
@Description("The size (in bytes) of the currently-open file.")
@Examples({
    "open file ./test.txt:",
    "\tbroadcast \"the file-size is %size of file%\""
})
@Since("1.0.0")
public class ExprSizeOfFile extends SimplePropertyExpression<FileController, Number> {
    
    static {
        if (!SkriptIO.isTest())
            register(ExprSizeOfFile.class, Number.class, "size", "file");
    }
    
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!(exprs[0] instanceof ExprCurrentFile)) return false;
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }
    
    @Override
    protected @NotNull String getPropertyName() {
        return "size";
    }
    
    @Override
    public @Nullable Number convert(FileController controller) {
        return FileController.sizeOf(controller.getFile());
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
