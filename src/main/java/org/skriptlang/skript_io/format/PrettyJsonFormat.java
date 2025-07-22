package org.skriptlang.skript_io.format;

import mx.kenzie.argo.Json;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class PrettyJsonFormat extends JsonFormat {

    public PrettyJsonFormat() {
        super("PrettyJson", "(pretty|formatted) json");
    }

    @Override
    protected void to(OutputStream stream, Map<String, Object> value) {
        try (Json json = new Json(stream)) {
            json.write(value, "\t");
        }
    }

}
