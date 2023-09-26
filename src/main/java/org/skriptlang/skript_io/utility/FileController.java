package org.skriptlang.skript_io.utility;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileController implements Closeable {
    
    private static final Map<File, FileController> handlers = new HashMap<>();
    private static final Map<Event, Stack<FileController>> current = new WeakHashMap<>();
    
    protected final File file;
    protected volatile boolean open;
    private FileOutputStream output;
    private FileInputStream input;
    
    private FileController(File file) {
        this.file = file;
        this.open = true;
    }
    
    public static @Nullable FileController currentSection(Event event) {
        if (!current.containsKey(event)) return null;
        return current.get(event).peek();
    }
    
    public static void push(Event event, @NotNull FileController controller) {
        final Stack<FileController> stack;
        if (!current.containsKey(event)) current.put(event, stack = new Stack<>());
        else stack = current.get(event);
        assert stack != null;
        stack.push(controller);
    }
    
    public static void pop(Event event) {
        final Stack<FileController> stack;
        if (!current.containsKey(event)) return;
        else stack = current.get(event);
        assert stack != null;
        stack.pop();
        if (stack.isEmpty()) current.remove(event);
    }
    
    public static FileController getController(@NotNull File file) {
        synchronized (handlers) {
            final FileController current = handlers.get(file), other;
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
    
    @Override
    public void close() throws IOException {
        try {
            if (output != null) try {
                this.output.flush();
                this.output.close();
            } finally {
                if (input != null) input.close();
            }
        } finally {
            this.open = false;
            FileController.close(this);
        }
    }
    
    @Override
    public String toString() {
        return file.toString();
    }
    
    @Override
    public int hashCode() {
        return file.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof File file) return this.file.equals(file);
        else if (o instanceof URI uri) return this.file.toURI().equals(uri);
        if (!(o instanceof final FileController that)) return false;
        return open == that.open && Objects.equals(file, that.file);
    }
    
    public void write(String text) {
        SkriptIO.queue().queue(new WriteTask(this, text.getBytes(StandardCharsets.UTF_8)));
    }
    
    public void append(String text) {
    
    }

    public void clear() {
        SkriptIO.queue().queue(new WriteTask(this, new byte[0]));
    }
    
    public synchronized @NotNull FileInputStream acquireReader() throws IOException {
        if (output != null) try {
            this.output.flush();
            this.output.close();
        } finally {
            this.output = null;
        }
        if (input != null) return input;
        return input = new FileInputStream(file);
    }
    
    public synchronized @NotNull FileOutputStream acquireWriter() throws IOException {
        if (input != null) try {
            this.input.close();
        } finally {
            this.input = null;
        }
        if (output != null) return output;
        return output = new FileOutputStream(file);
    }
    
    public URI getPath() {
        return file.toURI();
    }
    
}
