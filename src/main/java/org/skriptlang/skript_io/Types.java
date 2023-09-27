package org.skriptlang.skript_io;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript_io.utility.file.FileController;
import org.skriptlang.skript_io.utility.web.IncomingRequest;
import org.skriptlang.skript_io.utility.web.WebServer;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class Types {
    
    public void register() {
        Classes.registerClass(new ClassInfo<>(URI.class, "path")
            .user("(path|url)[s]")
            .name("Resource Path")
            .description("Represents a path to something, such as a relative file path or an internet URL.")
            .examples("set {_file} to ./test.txt")
            .since("1.0.0")
            .parser(new Parser<>() {
                
                @Override
                public boolean canParse(@NotNull ParseContext context) {
                    return true;
                }
                
                @Override
                public @Nullable URI parse(@NotNull String input, @NotNull ParseContext context) {
                    if (input.length() < 2) return null;
                    if (!(input.contains("/") || input.contains(File.separator))) return null;
                    if (input.contains(" ")) return null;
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
        Classes.registerClass(new ClassInfo<>(FileController.class, "readable")
            .user("readable[s]")
            .name("Readable Resource")
            .description("Represents a resource than can be read as text (e.g. a file, a webpage).")
            .examples("transfer the request to the current file")
            .since("1.0.0")
        );
        Classes.registerClass(new ClassInfo<>(FileController.class, "writable")
            .user("writable[s]")
            .name("Writable Resource")
            .description("Represents a resource than can have text written to it (e.g. a file, a response).")
            .examples("transfer {text} to the response")
            .since("1.0.0")
        );
        Classes.registerClass(new ClassInfo<>(IncomingRequest.class, "resource")
            .user("resource[s]")
            .name("Resource")
            .description("Represents a non-specific kind of i/o resource, such as a file, a request, etc.")
            .examples("the request", "the current file")
            .since("1.0.0")
        );
        Classes.registerClass(new ClassInfo<>(FileController.class, "file")
            .user("file[s]")
            .name("File")
            .description("Represents a file that has been opened for access.")
            .examples("broadcast the contents of the file")
            .since("1.0.0")
        );
        Classes.registerClass(new ClassInfo<>(WebServer.class, "website")
            .user("website")
            .name("Website")
            .description("Represents a hosted website when receiving a request.")
            .examples("close the current website")
            .since("1.0.0")
        );
        Classes.registerClass(new ClassInfo<>(IncomingRequest.class, "request")
            .user("[web] request[s]")
            .name("Web Request")
            .description("Represents an incoming website request (a browser asking for a page or data)," +
                " or an outgoing request (your server contacting a website).")
            .examples("the request")
            .since("1.0.0")
        );
        Comparators.registerComparator(FileController.class, FileController.class, new Comparator<>() {
            @Override
            public @NotNull Relation compare(FileController o1, FileController o2) {
                if (o1.equals(o2))
                    return Relation.EQUAL;
                return Relation.NOT_EQUAL;
            }
        });
        Comparators.registerComparator(URI.class, URI.class, new Comparator<>() {
            @Override
            public @NotNull Relation compare(URI o1, URI o2) {
                if (o1.equals(o2))
                    return Relation.EQUAL;
                return Relation.NOT_EQUAL;
            }
        });
        Comparators.registerComparator(URI.class, FileController.class, new Comparator<>() {
            @Override
            public @NotNull Relation compare(URI o1, FileController o2) {
                if (o1.equals(o2.getPath()))
                    return Relation.EQUAL;
                return Relation.NOT_EQUAL;
            }
        });
        Converters.registerConverter(FileController.class, URI.class, FileController::getPath);
        Converters.registerConverter(String.class, URI.class, text -> {
            try {
                return new URI(text);
            } catch (URISyntaxException e) {
                return null;
            }
        }, Converter.NO_RIGHT_CHAINING);
    }
    
}
