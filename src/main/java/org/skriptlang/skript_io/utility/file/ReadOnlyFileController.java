package org.skriptlang.skript_io.utility.file;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.ByteBufferInputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

public class ReadOnlyFileController extends FileController {
    
    private final ByteBuffer buffer;
    
    ReadOnlyFileController(File file, int size) {
        super(file);
        this.buffer = ByteBuffer.allocateDirect(size);
        try (final ReadableByteChannel channel = Files.newByteChannel(file.toPath())) {
            while (buffer.hasRemaining() && channel.read(buffer) != -1) ;
            this.buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public synchronized @NotNull InputStream acquireReader() {
        return new ByteBufferInputStream(buffer);
    }
    
    @Override
    public synchronized @NotNull OutputStream acquireWriter() {
        SkriptIO.error("Tried to edit a read-only file.");
        return new ByteArrayOutputStream(0);
    }
    
    @Override
    public boolean canWrite() {
        return false;
    }
    
}
