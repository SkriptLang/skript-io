package org.skriptlang.skript_io.utility.file;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Resource;
import org.skriptlang.skript_io.utility.Writable;
import org.skriptlang.skript_io.utility.task.AppendTask;
import org.skriptlang.skript_io.utility.task.ReadLineTask;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class FileController implements Closeable, Resource, Readable, Writable {

    public static final int READ = 0x0001, WRITE = 0x0010;

    private static final Map<File, FileController> handlers = new HashMap<>();
    private static final Map<Event, Stack<FileController>> current = new WeakHashMap<>();

    protected final File file;
    protected volatile boolean open;
    private FileOutputStream output;
    private FileInputStream input;
    private boolean appending, closed;

    FileController(File file) {
        this.file = file;
        open = true;
    }

    public static @Nullable FileController currentSection(Event event) {
        if (!current.containsKey(event)) return null;
        return current.get(event).peek();
    }

    public static void push(Event event, @NotNull FileController controller) {
        Stack<FileController> stack;
        if (!current.containsKey(event)) current.put(event, stack = new Stack<>());
        else stack = current.get(event);
        assert stack != null;
        stack.push(controller);
    }

    public static void pop(Event event) {
        Stack<FileController> stack;
        if (!current.containsKey(event)) return;
        else stack = current.get(event);
        assert stack != null;
        stack.pop();
        if (stack.isEmpty()) current.remove(event);
    }

    public static FileController getController(@NotNull File file, int mode) {
        synchronized (handlers) {
            FileController current = handlers.get(file), other;
            if (current != null && current.open) return current;
            other = new FileController(file);
            handlers.put(file, other);
            return other;
        }
    }

    protected static void close(FileController controller) {
        if (controller == null) return;
        synchronized (handlers) {
            handlers.remove(controller.file, controller);
        }
    }

    public static long sizeOf(File file) {
        try {
            if (file == null) return 0;
            return Files.size(file.toPath());
        } catch (IOException ex) {
            return -1;
        }
    }

    public static boolean isDirty(File file) {
        synchronized (handlers) {
            return handlers.containsKey(file);
        }
    }

    public static void flagDirty(File file) {
        synchronized (handlers) {
            handlers.putIfAbsent(file, null);
        }
    }

    public static void flagClean(File file) {
        synchronized (handlers) {
            handlers.remove(file, null);
        }
    }

    public File getFile() {
        return file;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        try {
            appending = false;
            if (output != null) try {
                output.flush();
                output.close();
            } finally {
                if (input != null) input.close();
            }
        } finally {
            open = false;
            FileController.close(this);
        }
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof File file) return this.file.equals(file);
        else if (o instanceof URI uri) return file.toURI().equals(uri);
        if (!(o instanceof FileController that)) return false;
        return open == that.open && Objects.equals(file, that.file);
    }

    @Override
    public String toString() {
        return file.toString();
    }

    public void append(String text) {
        SkriptIO.queue().queue(new AppendTask(this, text.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public @NotNull InputStream acquireReader() throws IOException {
        return acquireReader(true);
    }

    public void closeOutput() {
        appending = false;
        if (output != null) try {
            output.flush();
            output.close();
        } catch (IOException ignore) {
        } finally {
            output = null;
        }
    }

    public void closeInput() {
        if (input != null) try {
            input.close();
        } catch (IOException ignore) {
        } finally {
            input = null;
        }
    }

    public synchronized @NotNull InputStream acquireReader(boolean restart) throws IOException {
        if (closed) {
            SkriptIO.error("Tried to read a closed file (outside its file section).");
            throw new IOException("File closed.");
        }
        closeOutput();
        if (restart) closeInput();
        if (input == null) return input = new FileInputStream(file);
        else return input;
    }

    @Override
    public @NotNull OutputStream acquireWriter() throws IOException {
        return acquireWriter(false);
    }

    public synchronized @NotNull OutputStream acquireWriter(boolean append) throws IOException {
        if (closed) {
            SkriptIO.error("Tried to write to a closed file (outside its file section).");
            throw new IOException("File closed.");
        }
        closeInput();
        if (append && output != null) return output;
        closeOutput();
        return output = new FileOutputStream(file, append);
    }

    public URI getPath() {
        return file.toURI();
    }

    public boolean canRead() {
        return true;
    }

    public boolean canWrite() {
        return true;
    }

    public String getLine(int line) {
        if (!canRead()) return null;
        AtomicReference<String> reference = new AtomicReference<>();
        SkriptIO.queue().queue(new ReadLineTask(this, line, reference)).await();
        return reference.get();
    }

}
