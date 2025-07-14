package org.skriptlang.skript_io.format;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64EncodedFormat extends Format<String> {

    public Base64EncodedFormat() {
        super("Base64Encoded", String.class, "base ?64 (de|en)coded");
    }

    @Override
    protected @Nullable String[] from(InputStream stream) throws IOException {
        final StringBuilder builder = new StringBuilder();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Base64.getDecoder().wrap(stream)))) {
            String read;
            while ((read = reader.readLine()) != null) {
                builder.append(read);
            }
        }
        return new String[] {builder.toString()};
    }

    @Override
    protected void to(OutputStream stream, String... values) {
        try (final Writer writer = new OutputStreamWriter(Base64.getEncoder().wrap(stream))) {
            for (final String value : values) {
                if (value == null) continue;
                writer.write(value);
            }
        } catch (IOException ex) {
            SkriptIO.error(ex);
        }
    }

    @Override
    protected void to(OutputStream stream, @Nullable String value) throws IOException {
        throw new IllegalStateException();
    }

}
