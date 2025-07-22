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
        super("YAML", (Class<Map<String, Object>>) (Object) Map.class, "ya?ml");
    }

    @Override
    protected @Nullable Map<String, Object>[] from(InputStream stream) throws IOException {
        Map<String, Object> map = new LinkedHashMap<>();
        try (Reader reader = new InputStreamReader(stream)) {
            YamlConfiguration file = YamlConfiguration.loadConfiguration(reader);
            read(file, map);
        }
        // noinspection unchecked
        return new Map[]{ map };
    }

    @Override
    protected void to(OutputStream stream, Map<String, Object> value) throws IOException {
        if (value == null) {
            return;
        }
        YamlConfiguration file = new YamlConfiguration();
        write(file, value);
        String data = file.saveToString();
        try (Writer writer = new OutputStreamWriter(stream)) {
            writer.write(data);
        }
    }

    @SuppressWarnings("unchecked")
    private void read(ConfigurationSection section, Map<String, Object> map) {
        for (String key : section.getKeys(false)) {
            Object found = section.get(key), value;
            if (found instanceof ConfigurationSection child) {
                value = new LinkedHashMap<>();
                read(child, (Map<String, Object>) value);
            } else {
                value = found;
            }
            map.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private void write(ConfigurationSection section, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> child) {
                ConfigurationSection inner = section.createSection(key);
                write(inner, (Map<String, Object>) child);
            } else {
                section.set(key, value);
            }
        }
    }

}
