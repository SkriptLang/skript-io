package org.skriptlang.skript_io.utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public interface Readable extends Resource {
    
    static Readable simple(InputStream stream) {
        return new SimpleReadable(stream);
    }
    
    @NotNull InputStream acquireReader() throws IOException;
    
    default String readAll() {
        try {
            final byte[] bytes = this.acquireReader().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return null;
        }
    }
    
}

record SimpleReadable(InputStream acquireReader) implements Readable {

}
