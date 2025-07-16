package org.skriptlang.skript_io.utility.file;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.ByteBufferInputStream;
import org.skriptlang.skript_io.utility.DummyOutputStream;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

public class ReadOnlyFileController extends FileController implements Resource, Readable {

    private final ByteBuffer buffer;

    ReadOnlyFileController(File file, int size) {
        super(file);
        buffer = ByteBuffer.allocateDirect(size);
        try (ReadableByteChannel channel = Files.newByteChannel(file.toPath())) {
            while (buffer.hasRemaining() && channel.read(buffer) != -1) ;
            buffer.flip();
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
        return DummyOutputStream.INSTANCE;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

}
