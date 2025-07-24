package org.skriptlang.skript_io.elements.file.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.elements.file.effects.SecAccessFile;
import org.skriptlang.skript_io.utility.file.FileController;

@Name("Current File")
@Description("The currently-open file inside a file reading/editing section.")
@Example("""
        create a new file ./test.txt:
            add "hello" to the file
        """)
@Since("1.0.0")
public class ExprCurrentFile extends SimpleExpression<FileController> {

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerExpression(ExprCurrentFile.class, FileController.class, ExpressionType.SIMPLE,
                                        "[the] [(current|open)] file");
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, Kleenean isDelayed,
                        SkriptParser.ParseResult parseResult) {
        if (!getParser().isCurrentSection(SecAccessFile.class)) {
            Skript.error("You can't use '" + parseResult.expr + "' outside a file access section.");
            return false;
        }
        return true;
    }

    @Override
    protected FileController @NotNull [] get(@NotNull Event event) {
        FileController controller = FileController.currentSection(event);
        if (controller == null) {
            return new FileController[0];
        }
        return new FileController[] {controller};
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
        FileController controller = FileController.currentSection(event);
        if (controller == null) {
            return;
        }
        switch (mode) {
            case ADD:
                if (delta == null) {
                    break;
                }
                for (Object thing : delta) {
                    if (thing == null) {
                        continue;
                    }
                    controller.append(String.valueOf(thing));
                }
                break;
            case SET:
                if (delta == null || delta.length < 1) {
                    break;
                }
                Object thing = delta[0];
                if (thing == null) {
                    controller.clear();
                } else {
                    controller.write(String.valueOf(thing));
                }
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
    public @NotNull Class<FileController> getReturnType() {
        return FileController.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "the file";
    }

}
