package org.skriptlang.skript_io.utility;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public interface PostHandler extends HttpHandler {
    
    default void write(HttpExchange exchange, int code, String body) throws IOException {
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
    
    default Map<String, String> decode(BufferedReader reader) throws IOException {
        final Map<String, String> map = new LinkedHashMap<>();
        final StringBuilder builder = new StringBuilder();
        int length = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) break;
            length += line.length();
            builder.append(line);
            builder.append(System.lineSeparator());
            length++;
            if (length > WebServer.MAX_DATA_LENGTH) throw new IOException("Too much data was provided.");
        }
        String string = builder.toString().trim();
        String[] strings = string.split("&");
        for (String s : strings) {
            if (!s.contains("=")) continue;
            String[] parts = s.split("=");
            map.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder
                .decode(parts[1], StandardCharsets.UTF_8));
        }
        return map;
    }
    
}
