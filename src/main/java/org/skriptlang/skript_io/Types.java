package org.skriptlang.skript_io;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class Types {
    
    public void register() {
        Classes.registerClass(new ClassInfo<>(URI.class, "path")
            .user("(path|uri|url)")
            .name("Resource Path")
            .description("Represents a path to something, such as a relative file path or an internet URL.")
            .examples("TODO") // todo
            .since("1.0.0")
            .parser(new Parser<>() {
                
                @Override
                public boolean canParse(@NotNull ParseContext context) {
                    return true;
                }
                
                @Override
                public @Nullable URI parse(@NotNull String input, @NotNull ParseContext context) {
                    if (input.contains(" ")) return null;
                    if (!input.contains("/") && !input.contains(File.separator)) return null;
                    try {
                        return new URI(input);
                    } catch (URISyntaxException e) {
                        return null;
                    }
                }
                
                @Override
                public @NotNull String toString(URI uri, int flags) {
                    return uri.toString();
                }
                
                @Override
                public @NotNull String toVariableNameString(URI uri) {
                    return "uri:" + uri;
                }
            })
        );
        
    }
    
}
