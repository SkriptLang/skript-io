package org.skriptlang.skript_io.elements.file.expressions;

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
import org.skriptlang.skript_io.utility.file.FileController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Name("Lines of File")
@Description("The lines of a currently-open file as a list of text.")
@Examples({
    "open file ./test.txt:",
    "\tloop the lines of file:",
    "\t\tbroadcast loop-value",
    "\tset the lines of file to \"hello\" and \"there\""
})
@Since("1.0.0")
public class ExprLinesOfFile extends SimplePropertyExpression<FileController, String> {

    static {
        if (!SkriptIO.isTest())
            register(ExprLinesOfFile.class, String.class, "lines", "file");
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "lines";
    }

    @Override
    public @Nullable String convert(FileController controller) {
        return controller.readAll();
    }

    @Override
    protected String @NotNull [] get(@NotNull Event event, FileController @NotNull [] source) {
        if (source.length == 0 || source[0] == null) return new String[0];
        final List<String> list = source[0].readAll().lines().toList();
        return list.toArray(new String[0]);
    }

    @Override
    public Iterator<? extends String> iterator(@NotNull Event event) {
        final FileController controller = this.getExpr().getSingle(event);
        if (controller == null) return Collections.emptyIterator();
        try {
            return new LineIterator(controller.acquireReader(true));
        } catch (IOException e) {
            SkriptIO.throwSafe(e);
            return Collections.emptyIterator();
        }
    }

    private record LineIterator(BufferedReader reader, AtomicReference<String> nextLine) implements Iterator<String> {

        //<editor-fold desc="Iterate lines directly from reader." defaultstate="collapsed">
        public LineIterator(InputStream stream) {
            this(new BufferedReader(new InputStreamReader(stream)), new AtomicReference<>());
        }

        @Override
        public boolean hasNext() {
            if (nextLine.get() != null) return true;
            try {
                final String string = reader.readLine();
                if (string == null) {
                    this.reader.close();
                    return false;
                }
                this.nextLine.set(string);
                return true;
            } catch (IOException ex) {
                SkriptIO.throwSafe(ex);
                return false;
            }
        }

        @Override
        public String next() {
            return nextLine.getAndSet(null);
        }
        //</editor-fold>

    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET, ADD -> CollectionUtils.array(String.class);
            default -> null;
        };
    }

    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (delta == null || delta.length == 0) return;
        final String[] strings = new String[delta.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = delta[i] != null ? String.valueOf(delta[i]) : "";
        }
        final FileController[] files = this.getExpr().getArray(event);
        final String text = String.join(System.lineSeparator(), strings);
        if (mode == Changer.ChangeMode.SET)
            for (final FileController file : files) file.write(text);
        else if (mode == Changer.ChangeMode.ADD)
            for (final FileController file : files) file.append(System.lineSeparator() + text);

    }

    @Override
    public boolean isSingle() {
        return false;
    }

}
