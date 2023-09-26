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
        this.limit = buffer.limit();
        this.position = 0;
    }
    
    public synchronized int read() {
        if (position < limit) return buffer.get(position++) & 0xFF;
        else return -1;
    }
    
    public synchronized int read(byte @NotNull [] bytes, int off, int length) {
        final int amount = Math.min(length, this.limit - position);
        this.buffer.get(position, bytes, off, amount);
        this.position += amount;
        return amount;
    }
    
}
