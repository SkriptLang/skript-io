package org.skriptlang.skript_io;

import ch.njol.skript.config.Config;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class IOConfig extends Config {

    public IOConfig(InputStream source, String fileName, boolean simple, boolean allowEmptySections,
                    String defaultSeparator)
        throws IOException {
        super(source, fileName, simple, allowEmptySections, defaultSeparator);
    }

    public IOConfig(InputStream source, String fileName, @Nullable File file, boolean simple,
                    boolean allowEmptySections, String defaultSeparator)
        throws IOException {
        super(source, fileName, file, simple, allowEmptySections, defaultSeparator);
    }

    public IOConfig(File file, boolean simple, boolean allowEmptySections, String defaultSeparator) throws IOException {
        super(file, simple, allowEmptySections, defaultSeparator);
    }

    public IOConfig(Path file, boolean simple, boolean allowEmptySections, String defaultSeparator) throws IOException {
        super(file, simple, allowEmptySections, defaultSeparator);
    }

    public IOConfig(String s, String fileName, boolean simple, boolean allowEmptySections, String defaultSeparator)
        throws IOException {
        super(s, fileName, simple, allowEmptySections, defaultSeparator);
    }

}
