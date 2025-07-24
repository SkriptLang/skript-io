package org.skriptlang.skript_io.elements.file.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.file.FileController;
import org.skriptlang.skript_io.utility.task.MoveTask;

import java.io.File;
import java.net.URI;

@Name("Rename File")
@Description("Renames a file or directory. To rename a directory please use the 'move' effect.")
@Example("rename file ./example/test.txt to \"blob.txt\"")
@Since("1.0.0")
public class EffRenameFile extends Effect {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerEffect(EffRenameFile.class,
                                  "rename [the] file [at] %path% to %string%",
                                  "rename %*path% to %string%");
    }

    private Expression<URI> pathExpression;
    private Expression<String> stringExpression;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result) {
        pathExpression = (Expression<URI>) expressions[0];
        stringExpression = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        URI uri = pathExpression.getSingle(event);
        String name = stringExpression.getSingle(event);
        if (name == null || name.isBlank()) {
            return;
        }
        if (uri == null) {
            return;
        }
        File file = SkriptIO.file(uri);
        if (file == null) {
            return;
        }
        FileController.flagDirty(file);
        SkriptIO.queue().queue(new MoveTask(file, name));
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "rename file " + pathExpression.toString(event, debug) + " to " + stringExpression.toString(event, debug);
    }

}
