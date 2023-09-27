package org.skriptlang.skript_io.utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public interface Readable {
    
    @NotNull InputStream acquireReader() throws IOException;
    
}
