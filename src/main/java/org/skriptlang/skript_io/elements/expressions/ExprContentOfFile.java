package org.skriptlang.skript_io.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.FileController;

@Name("Contents of File")
@Description("The contents of (the text inside) a currently-open file. This will be blank if the file is empty or unreadable.")
@Examples({
    "open file ./test.txt:",
    "\tbroadcast the contents of file"
})
@Since("1.0.0")
public class ExprContentOfFile extends SimplePropertyExpression<FileController, String> {
    
    static {
        if (!SkriptIO.isTest())
            register(ExprContentOfFile.class, String.class, "content[s]", "file");
    }
    
    @Override
    protected @NotNull String getPropertyName() {
        return "content";
    }
    
    @Override
    public @Nullable String convert(FileController controller) {
        return controller.readAll();
    }
    
    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }
    
    @Override
    public Class<?> @Nullable [] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(String.class);
            case RESET, DELETE -> CollectionUtils.array();
            default -> null;
        };
    }
    
    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (delta == null || delta.length == 0 || delta[0] == null) return;
        if (mode == Changer.ChangeMode.SET) {
            if (!(delta[0] instanceof String content)) return;
            final FileController[] files = this.getExpr().getArray(event);
            for (final FileController file : files) file.write(content);
        } else {
            final FileController[] files = this.getExpr().getArray(event);
            for (final FileController file : files) file.clear();
        }
    }
    
    @Override
    public boolean isSingle() {
        return true;
    }
    
}
