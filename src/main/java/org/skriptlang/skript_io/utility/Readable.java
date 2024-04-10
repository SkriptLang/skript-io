package org.skriptlang.skript_io.utility;

import mx.kenzie.clockwork.io.DataTask;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.SkriptIO;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public interface Readable extends Resource {

    static Readable simple(InputStream stream) {
        return new SimpleReadable(stream);
    }

    @NotNull
    InputStream acquireReader() throws IOException;

    default String readAll() {
        final AtomicReference<String> reference = new AtomicReference<>();
        SkriptIO.queue().queue(new DataTask() {
            @Override
            public void execute() throws IOException {
                try (final InputStream stream = acquireReader()) {
                    final byte[] bytes = stream.readAllBytes();
                    reference.set(new String(bytes, StandardCharsets.UTF_8));
                }
            }
        }).await();
        return reference.get();
    }

}

record SimpleReadable(InputStream acquireReader) implements Readable {

}
