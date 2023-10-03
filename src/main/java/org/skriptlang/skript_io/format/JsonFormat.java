package org.skriptlang.skript_io.format;

import mx.kenzie.argo.Json;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class JsonFormat extends Format<Map<String, Object>> {
    
    @SuppressWarnings("unchecked")
    public JsonFormat() {
        super("JSON", (Class<Map<String, Object>>) (Object) Map.class, "json");
    }
    
    @Override
    protected @Nullable Map<String, Object>[] from(InputStream stream) throws IOException {
        try (final Json json = new Json(stream)) {
            return new Map[]{json.toMap()};
        }
    }
    
    @Override
    protected void to(OutputStream stream, Map<String, Object> value) throws IOException {
        try (final Json json = new Json(stream)) {
            json.write(value);
        }
    }
    
}
