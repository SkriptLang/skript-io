package org.skriptlang.skript_io.utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class DummyOutputStream extends OutputStream {
    
    public static final DummyOutputStream INSTANCE = new DummyOutputStream();
    
    @Override
    public void write(int b) throws IOException {
    }
    
    @Override
    public void write(byte @NotNull [] b) throws IOException {
    }
    
    @Override
    public void write(byte @NotNull [] b, int off, int len) throws IOException {
    }
    
}
