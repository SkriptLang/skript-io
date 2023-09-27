package org.skriptlang.skript_io.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.elements.effects.SecAccessFile;
import org.skriptlang.skript_io.utility.FileController;

import java.net.URI;

@Name("Current File")
@Description("The currently-open file inside a file reading/editing section.")
@Examples({
    "create a new file ./test.txt:",
    "\tadd \"hello\" to the file"
})
@Since("1.0.0")
public class ExprCurrentFile extends SimpleExpression<Object> {
    
    static {
        if (Skript.isAcceptRegistrations())
            Skript.registerExpression(ExprCurrentFile.class, Object.class, ExpressionType.SIMPLE,
                "[the] [(current|open)] file"
            );
    }
    
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        final SkriptEvent event = this.getParser().getCurrentSkriptEvent();
        if (!this.getParser().isCurrentSection(
            SecAccessFile.class) && !(event instanceof SectionSkriptEvent && ((SectionSkriptEvent) event).isSection(
            SecAccessFile.class))) {
            Skript.error("You can't use '" + parseResult.expr + "' outside a file access section.");
            return false;
        }
        return true;
    }
    
    @Override
    protected URI @NotNull [] get(@NotNull Event event) {
        final FileController controller = FileController.currentSection(event);
        if (controller == null) return new URI[0];
        return new URI[]{controller.getPath()};
    }
    
    @Override
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET, ADD -> CollectionUtils.array(String.class);
            case RESET, DELETE -> CollectionUtils.array();
            default -> null;
        };
    }
    
    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        final FileController controller = FileController.currentSection(event);
        if (controller == null) return;
        switch (mode) {
            case ADD:
                if (delta == null) break;
                for (final Object thing : delta) {
                    if (thing == null) continue;
                    controller.append(String.valueOf(thing));
                }
                break;
            case SET:
                if (delta == null || delta.length < 1) break;
                final Object thing = delta[0];
                if (thing == null) controller.clear();
                else controller.write(String.valueOf(thing));
                break;
            case RESET, DELETE:
                controller.clear();
        }
    }
    
    @Override
    public boolean isSingle() {
        return true;
    }
    
    @Override
    public @NotNull Class<URI> getReturnType() {
        return URI.class;
    }
    
    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "the file";
    }
    
}
