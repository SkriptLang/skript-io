package org.skriptlang.skript_io.format;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class URLEncodedFormat extends Format<String> {

    public URLEncodedFormat() {
        super("URLEncoded", String.class, "url[- ]escaped?");
    }

    @Override
    protected @Nullable String[] from(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String read;
            while ((read = reader.readLine()) != null) {
                builder.append(read);
            }
        }
        return new String[] {URLDecoder.decode(builder.toString(), StandardCharsets.UTF_8)};
    }

    @Override
    protected void to(OutputStream stream, String... values) {
        try (Writer writer = new OutputStreamWriter(stream)) {
            for (String value : values) {
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
