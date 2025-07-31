package org.skriptlang.skript_io.elements.file.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.file.FileController;
import org.skriptlang.skript_io.utility.task.TidyTask;

import java.io.File;
import java.net.URI;

@Name("File/Directory Exists")
@Description("Checks whether the given path is a file that exists.")
@Example("""
    if file ./test.txt exists:
        delete file ./test.txt
    """)
@Since("1.0.0")
public class CondFileExists extends Condition {

    private static final int ALL = 0, FILE = 1, FOLDER = 2;

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerCondition(CondFileExists.class,
                                     "path[s] %paths% (exist[s]|negated:do[es](n't| not) exist)",
                                     "file[s] %paths% (exist[s]|negated:do[es](n't| not) exist)",
                                     "folder[s] %paths% (exist[s]|negated:do[es](n't| not) exist)",
                                     "director(y|ies) %paths% (exist[s]|negated:do[es](n't| not) exist)");
    }

    private Expression<URI> uriExpression;
    private int mode;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull Kleenean kleenean,
                        SkriptParser.@NotNull ParseResult result) {
        uriExpression = (Expression<URI>) expressions[0];
        mode = Math.min(matchedPattern, 2);
        setNegated(result.hasTag("negated"));
        return true;
    }

    @Override
    public boolean check(@NotNull Event event) {
        return uriExpression.check(event, uri -> {
            if (uri == null) {
                return false;
            }
            File file = SkriptIO.file(uri);
            if (FileController.isDirty(file)) {
                SkriptIO.queue().queue(new TidyTask()).await();
            }
            if (file == null || !file.exists()) {
                return false;
            }
            return mode == 0 || mode == 1 && file.isFile() || mode == 2 && file.isDirectory();
        }, isNegated());
    }

    @Override
    @NotNull
    public String toString(@Nullable Event e, boolean debug) {
        String what = switch (mode) {
            case FILE -> "files ";
            case FOLDER -> "folders ";
            default -> "paths ";
        };
        String exists = isNegated() ? " do not exist" : " exist";
        return what + uriExpression.toString(e, debug) + exists;
    }

}
