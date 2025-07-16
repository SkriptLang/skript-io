package org.skriptlang.skript_io.utility;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

    private final ByteBuffer buffer;
    private final int limit;
    private volatile int position;

    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
        limit = buffer.limit();
        position = 0;
    }

    public synchronized int read() {
        if (position < limit) return buffer.get(position++) & 0xFF;
        else return -1;
    }

    public synchronized int read(byte @NotNull [] bytes, int off, int length) {
        int amount = Math.min(length, limit - position);
        buffer.get(position, bytes, off, amount);
        position += amount;
        return amount;
    }

}
