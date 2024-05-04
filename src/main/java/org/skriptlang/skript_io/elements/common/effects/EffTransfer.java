package org.skriptlang.skript_io.elements.common.effects;

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
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Writable;
import org.skriptlang.skript_io.utility.task.TransferTask;

import java.io.File;
import java.net.URI;
import java.util.Arrays;

@Name("Transfer")
@Description("""
    Safely copies data from one (readable) resource to another (writable) resource.
    Useful for responding to a web request with a file (or copying one file into another).
    """)
@Examples({
    "transfer {input} to {output}",
    "transfer ./test.html to the response"
})
@Since("1.0.0")
public class EffTransfer extends Effect {

    static {
        if (!SkriptIO.isTest())
            Skript.registerEffect(EffTransfer.class, "transfer %readable% [in]to %writable%",
                                  "transfer %path% [in]to %writable%");
    }

    private boolean path;
    private Expression<Readable> sourceExpression;
    private Expression<Writable> targetExpression;
    private Expression<URI> pathExpression;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result) {
        System.out.println("HELLO? " + Arrays.toString(expressions)); // todo
        if (matchedPattern == 0) sourceExpression = (Expression<Readable>) expressions[0];
        else pathExpression = (Expression<URI>) expressions[0];
        this.targetExpression = (Expression<Writable>) expressions[1];
        this.path = matchedPattern == 1;
        return true;
    }

    @Override
    protected void execute(@NotNull Event event) {
        final Writable target = targetExpression.getSingle(event);
        if (target == null) return;
        if (path) {
            final File file = SkriptIO.file(pathExpression.getSingle(event));
            if (file == null || !file.isFile()) return;
            SkriptIO.queue().queue(TransferTask.forFile(file, target));
            return;
        }
        final Readable source = sourceExpression.getSingle(event);
        if (source == null) return;
        SkriptIO.queue().queue(new TransferTask(target, source));
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        final String part = path ? pathExpression.toString(event, debug) : sourceExpression.toString(event, debug);
        return "transfer " + part + " to " + targetExpression.toString(event, debug);
    }

}
