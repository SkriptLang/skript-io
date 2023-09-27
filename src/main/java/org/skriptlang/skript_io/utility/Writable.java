package org.skriptlang.skript_io.utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public interface Writable {
    
    @NotNull OutputStream acquireWriter() throws IOException;
    
}
