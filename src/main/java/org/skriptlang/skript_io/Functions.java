package org.skriptlang.skript_io;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class Functions {

    private static final ClassInfo<URI> PATH = Classes.getExactClassInfo(URI.class);

    public static void registerFunctions() {
        ch.njol.skript.lang.function.Functions.registerFunction(new SimpleJavaFunction<>("file",
            new Parameter[] {new Parameter<>("path", DefaultClasses.OBJECT, true, null)}, PATH, true) {
            @Override
            public URI @NotNull [] executeSimple(Object @NotNull [] @NotNull [] params) {
                Object object = params[0][0];
                if (object == null) return new URI[0];
                try {
                    return new URI[] {switch (object) {
                        case URI uri -> uri;
                        case URL url -> url.toURI();
                        case File file -> file.toURI();
                        case Path path -> path.toUri();
                        default -> SkriptIO.uri(Classes.toString(object));
                    }};
                } catch (URISyntaxException | IllegalArgumentException ex) {
                    return new URI[0];
                }
            }
        }.description("Converts something into a file/resource path.")
            .examples(
                """
                    set {_file} to file("MyFile.txt")""",
                """
                    create file file("./storage/%now%.json"):
                        set the json content of the file to {_data::*}""",
                """
                    set {_image} to random element out of {_images::*}
                    set {_file} to file("images/%{_image}%.png")
                    transfer {_file} to the response"""
            ).since("1.0.0"));
        ch.njol.skript.lang.function.Functions.registerFunction(new SimpleJavaFunction<>("joinPaths", new Parameter[] {
            new Parameter<>("paths", DefaultClasses.OBJECT, false, null)
        }, PATH, true) {
            @Override
            public URI @NotNull [] executeSimple(Object @NotNull [] @NotNull [] params) {
                StringBuilder builder = new StringBuilder();
                for (Object object : params[0]) {
                    String part;
                    if (object instanceof URI uri) {
                        part = uri.getPath();
                    } else {
                        part = Classes.toString(object);
                    }

                    if (part.startsWith("/") && builder.charAt(builder.length() - 1) == '/') {
                        builder.append(part.substring(1));
                    } else {
                        builder.append(part);
                    }
                }
                return new URI[] {SkriptIO.uri(builder.toString())};
            }
        }.description("Joins multiple parts into a single file/resource path.")
            .examples(
                """
                    set {_folder} to ./folder/
                    set {_file} to joinPaths({_folder}, "MyFile.txt")
                    # ./folder/MyFile.txt""",
                """
                    create file joinPaths("./storage/", {_file name}, ".json"):
                        set the json content of the file to {_data::*}
                    # ./storage/<file name>.json""",
                """
                    set {_folder} to ./website/
                    open a website:
                        set {_file} to joinPaths({_folder}, the request's path)
                        transfer {_file} to the response
                        # ./website/..."""
            ).since("1.0.0"));
    }

}
