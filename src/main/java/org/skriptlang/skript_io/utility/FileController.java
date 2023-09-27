package org.skriptlang.skript_io.utility;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class FileController implements Closeable, Readable, Writable {
    
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
    
    public static FileController getController(@NotNull File file, int mode) {
        if ((mode & READ) == READ || !file.canWrite()) load:{
            final long size = FileController.sizeOf(file);
            if (size > Math.min(Runtime.getRuntime().freeMemory(), 500_000_000)) break load;
            return new ReadOnlyFileController(file, (int) size);
        }
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
    
    public static long sizeOf(File file) {
        try {
            return Files.size(file.toPath());
        } catch (IOException ex) {
            return -1;
        }
    }
    
    public File getFile() {
        return file;
    }
    
    @Override
    public void close() throws IOException {
        this.closed = true;
        try {
            this.appending = false;
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
        SkriptIO.queue().queue(new AppendTask(this, text.getBytes(StandardCharsets.UTF_8)));
    }
    
    public void clear() {
        SkriptIO.queue().queue(new WriteTask(this, new byte[0]));
    }
    
    @Override
    public synchronized @NotNull InputStream acquireReader() throws IOException {
        return this.acquireReader(true);
    }
    
    public synchronized @NotNull InputStream acquireReader(boolean restart) throws IOException {
        if (this.closed) {
            SkriptIO.error("Tried to read a closed file (outside its file section).");
            throw new IOException("File closed.");
        }
        this.appending = false;
        if (output != null) try {
            this.output.flush();
            this.output.close();
        } finally {
            this.output = null;
        }
        if (input != null) return input;
        return input = new FileInputStream(file);
    }
    
    @Override
    public @NotNull OutputStream acquireWriter() throws IOException {
        return this.acquireWriter(false);
    }
    
    public synchronized @NotNull OutputStream acquireWriter(boolean append) throws IOException {
        if (this.closed) {
            SkriptIO.error("Tried to write to a closed file (outside its file section).");
            throw new IOException("File closed.");
        }
        if (input != null) try {
            this.input.close();
        } finally {
            this.input = null;
        }
        if ((appending != append) && output != null) try {
            this.output.flush();
            this.output.close();
        } finally {
            this.output = null;
        }
        else if (output != null) return output;
        this.appending = append;
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
    
    public String readAll() {
        if (!this.canRead()) return null;
        try {
            final byte[] bytes = this.acquireReader().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return null;
        }
    }
    
    public String getLine(int line) {
        if (!this.canRead()) return null;
        final AtomicReference<String> reference = new AtomicReference<>();
        SkriptIO.queue().queue(new ReadLineTask(this, line, reference)).await();
        return reference.get();
    }
    
}
