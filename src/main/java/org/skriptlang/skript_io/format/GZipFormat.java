package org.skriptlang.skript_io.format;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipFormat extends Format<String> {

    public GZipFormat() {
        super("GZip", String.class, "g?zip");
    }

    @Override
    protected @Nullable String[] from(InputStream stream) throws IOException {
        try (final GZIPInputStream input = new GZIPInputStream(stream)) {
            final byte[] bytes = input.readAllBytes();
            return new String[] {new String(bytes, StandardCharsets.UTF_8)};
        }
    }

    @Override
    protected void to(OutputStream stream, @Nullable String value) throws IOException {
        if (value == null) return;
        try (final GZIPOutputStream output = new GZIPOutputStream(stream)) {
            output.write(value.getBytes(StandardCharsets.UTF_8));
            output.flush();
        }
    }

}
