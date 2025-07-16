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
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Writable;
import org.skriptlang.skript_io.utility.file.FileController;
import org.skriptlang.skript_io.utility.task.CloseTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Name("Lines of Resource")
@Description("The lines of a currently-open resource as a list of texts.")
@Examples({
    "open file ./test.txt:",
    "\tloop the lines of file:",
    "\t\tbroadcast loop-value",
    "edit file ./something.txt:",
    "\tset the lines of file to {lines::*}"
})
@Since("1.0.0")
public class ExprLinesOfFile extends SimplePropertyExpression<Readable, String> {

    static {
        if (!SkriptIO.isTestMode())
            register(ExprLinesOfFile.class, String.class, "lines", "readable");
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "lines";
    }

    @Override
    public @Nullable String convert(Readable readable) {
        return readable.readAll();
    }

    @Override
    protected String @NotNull [] get(@NotNull Event event, Readable @NotNull [] source) {
        if (source.length == 0 || source[0] == null) return new String[0];
        List<String> list = source[0].readAll().lines().toList();
        return list.toArray(new String[0]);
    }

    @Override
    public Iterator<? extends String> iterator(@NotNull Event event) {
        Readable controller = getExpr().getSingle(event);
        if (controller == null) return Collections.emptyIterator();
        try {
            return new LineIterator(controller.acquireReader());
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
                String string = reader.readLine();
                if (string == null) {
                    SkriptIO.queue().queue(new CloseTask(reader));
                    return false;
                }
                nextLine.set(string);
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
            case SET, ADD -> Writable.class.isAssignableFrom(getExpr().getReturnType())
                ? CollectionUtils.array(String.class)
                : null;
            default -> null;
        };
    }

    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (delta == null || delta.length == 0) return;
        String[] strings = new String[delta.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = delta[i] != null ? String.valueOf(delta[i]) : "";
        }
        Readable[] files = getExpr().getArray(event);
        String text = String.join(System.lineSeparator(), strings);
        if (mode == Changer.ChangeMode.SET) {
            for (Readable readable : files) if (readable instanceof Writable file) file.write(text);
        } else if (mode == Changer.ChangeMode.ADD) {
            for (Readable readable : files) {
                if (readable instanceof FileController file) file.append(System.lineSeparator() + text);
                else if (readable instanceof Writable file) file.write(System.lineSeparator() + text);
            }
        }

    }

    @Override
    public boolean isSingle() {
        return false;
    }

}
