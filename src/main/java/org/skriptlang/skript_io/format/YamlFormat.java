package org.skriptlang.skript_io.format;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlFormat extends Format<Map<String, Object>> {

    @SuppressWarnings("unchecked")
    public YamlFormat() {
        super("YAML", (Class<Map<String, Object>>) (Object) Map.class, "y[a]ml");
    }

    @Override
    protected @Nullable Map<String, Object>[] from(InputStream stream) throws IOException {
        final Map<String, Object> map = new LinkedHashMap<>();
        try (final Reader reader = new InputStreamReader(stream)) {
            final YamlConfiguration file = YamlConfiguration.loadConfiguration(reader);
            this.read(file, map);
        }
        return new Map[] {map};
    }

    @Override
    protected void to(OutputStream stream, Map<String, Object> value) throws IOException {
        if (value == null) return;
        final YamlConfiguration file = new YamlConfiguration();
        this.write(file, value);
        final String data = file.saveToString();
        try (final Writer writer = new OutputStreamWriter(stream)) {
            writer.write(data);
        }
    }

    @SuppressWarnings("unchecked")
    private void read(ConfigurationSection section, Map<String, Object> map) {
        for (final String key : section.getKeys(false)) {
            final Object found = section.get(key), value;
            if (found instanceof ConfigurationSection child) {
                value = new LinkedHashMap<>();
                this.read(child, (Map<String, Object>) value);
            } else value = found;
            map.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private void write(ConfigurationSection section, Map<String, Object> map) {
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof Map<?, ?> child) {
                final ConfigurationSection inner = section.createSection(key);
                this.write(inner, (Map<String, Object>) child);
            } else section.set(key, value);
        }
    }

}
