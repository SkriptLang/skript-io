package org.skriptlang.skript_io.format;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class URLEncodedFormat extends Format<String> {

    public URLEncodedFormat() {
        super("URLEncoded", String.class, "url(-| )(de|en)coded");
    }

    @Override
    protected @Nullable String[] from(InputStream stream) throws IOException {
        final StringBuilder builder = new StringBuilder();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String read;
            while ((read = reader.readLine()) != null) {
                builder.append(read);
            }
        }
        return new String[] {URLDecoder.decode(builder.toString(), StandardCharsets.UTF_8)};
    }

    @Override
    protected void to(OutputStream stream, String... values) {
        try (final Writer writer = new OutputStreamWriter(stream)) {
            for (final String value : values) {
                if (value == null) continue;
                writer.write(URLEncoder.encode(value, StandardCharsets.UTF_8));
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
