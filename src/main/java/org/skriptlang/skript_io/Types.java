package org.skriptlang.skript_io;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.effects.EffChange;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript_io.elements.file.effects.EffDeleteFile;
import org.skriptlang.skript_io.format.ErrorInfo;
import org.skriptlang.skript_io.format.Format;
import org.skriptlang.skript_io.format.FormatInfo;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Resource;
import org.skriptlang.skript_io.utility.Writable;
import org.skriptlang.skript_io.utility.file.FileController;
import org.skriptlang.skript_io.utility.web.*;

import java.io.File;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;

public class Types {

    private SyntaxElementInfo<?> change;
    private Collection<SyntaxElementInfo<? extends Effect>> effects;
    private Collection<SyntaxElementInfo<? extends Statement>> syntax;

    public void registerPathArithmetic() {
        Arithmetics.registerOperation(Operator.ADDITION, URI.class, String.class, (uri, string) ->
            SkriptIO.uri(uri + string));
        Arithmetics.registerOperation(Operator.ADDITION, URI.class, URI.class, this::add);
        Arithmetics.registerOperation(Operator.SUBTRACTION, URI.class, URI.class, URI::relativize);
        Arithmetics.registerOperation(Operator.DIVISION, URI.class, URI.class, this::subPath);
    }

    private URI subPath(URI base, URI child) {
        final String initial = base.toString();
        String addendum = child.getPath().replaceAll("^\\w+://", "");
        if (initial.endsWith("/"))
            while (addendum.startsWith("/")) addendum = addendum.substring(1);
        else if (initial.endsWith(File.separator)) // windows patch
            while (addendum.startsWith(File.separator)) addendum = addendum.substring(File.separator.length());
        else if (!addendum.startsWith("/")) addendum = '/' + addendum;
        return SkriptIO.uri(initial + addendum);
    }

    private URI add(URI base, URI child) {
        if (!child.isOpaque()
            && !base.isOpaque()
            && child.getScheme() == null
            && child.getRawAuthority() == null
            && child.getPath().isEmpty()
            && child.getRawFragment() != null
            && child.getRawQuery() == null) return base.resolve(child);
        final String initial = base.toString();
        String addendum;
        if (initial.endsWith("/")) // universal separator
            addendum = child.getPath().replaceAll("^(?:\\w+://|/+)", "");
        else if (initial.endsWith(File.separator)) // windows patch
            addendum = child.getPath().replaceAll("^(?:\\w+://|" + File.separator + "+)", "");
        else addendum = child.getPath().replaceAll("^\\w+://", "");
        return SkriptIO.uri(initial + addendum);
    }

    public void registerTypes() {
        Classes.registerClass(new ClassInfo<>(URI.class, "path").user("(path|url)s?").name("Resource Path")
            .description("Represents a path to something, such as a relative file path or an internet URL.")
            .examples("set {_file} to ./test.txt").since("1.0.0").changer(new Changer<>() {
                @Override
                public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
                    if (mode == ChangeMode.DELETE) return new Class[0];
                    return null;
                }

                @Override
                public void change(URI[] what, @Nullable Object[] delta, ChangeMode mode) {
                    if (mode == ChangeMode.DELETE) {
                        for (final URI uri : what) {
                            final File file = SkriptIO.fileNoError(uri);
                            if (file == null) continue;
                            EffDeleteFile.delete(file, file.isDirectory(), true);
                        }
                    }
                }
            }).serializer(new Serializer<>() {
                @Override
                public Fields serialize(URI o) {
                    final Fields fields = new Fields();
                    fields.putObject("path", o.toString());
                    return fields;
                }

                @Override
                public void deserialize(URI o, Fields f) {
                }

                @Override
                public boolean mustSyncDeserialization() {
                    return false;
                }

                @Override
                protected boolean canBeInstantiated() {
                    return false;
                }

                @Override
                protected URI deserialize(Fields fields) throws StreamCorruptedException {
                    final String string = fields.getObject("path", String.class);
                    if (string == null) return null;
                    return SkriptIO.uri(string);
                }
            }).parser(new Parser<>() {

                @Override
                public @Nullable URI parse(@NotNull String input, @NotNull ParseContext context) {
                    if (input.length() < 2) return null;
                    if (input.contains(" ")) return null;
                    if (!(input.contains("/") || input.contains(File.separator))) {
                        if (input.contains(".")) {
                            final String extension = input.substring(input.lastIndexOf(".") + 1);
                            if (ContentType.isKnown(extension))
                                return SkriptIO.uri(input);
                        }
                        return null;
                    }
                    try {
                        return new URI(input);
                    } catch (URISyntaxException e) {
                        return null;
                    }
                }

                @Override
                public boolean canParse(@NotNull ParseContext context) {
                    return true;
                }

                @Override
                public @NotNull String toString(URI uri, int flags) {
                    return uri.toString();
                }

                @Override
                public @NotNull String toVariableNameString(URI uri) {
                    return "uri:" + uri;
                }
            }));
        Classes.registerClass(new ClassInfo<>(Readable.class, "readable").user("readables?").name("Readable Resource")
            .description("Represents a resource than can be read as text (e.g. a file, a webpage).")
            .examples("transfer the request to the current file").since("1.0.0"));
        Classes.registerClass(new ClassInfo<>(Writable.class, "writable").user("writables?").name("Writable Resource")
            .description("Represents a resource than can have text written to it (e.g. a file, a response).")
            .examples("transfer {text} to the response").since("1.0.0"));
        Classes.registerClass(new ClassInfo<>(Resource.class, "resource").user("resources?").name("Resource")
            .description("Represents a non-specific kind of i/o resource, such as a file, a request, etc.")
            .examples("the request", "the current file").since("1.0.0"));
        Classes.registerClass(new ClassInfo<>(FileController.class, "file").user("files?").name("File")
            .description("Represents a file that has been opened for access.")
            .examples("broadcast the contents of the file").since("1.0.0"));
        Classes.registerClass(new ClassInfo<>(WebServer.class, "website").user("website").name("Website")
            .description("Represents a hosted website when receiving a request.").examples("close the current website")
            .since("1.0.0"));
        Classes.registerClass(new ClassInfo<>(Transaction.class, "transaction").user("transactions?")
            .name("HTTP Transaction")
            .description("Represents an incoming HTTP request or an outgoing HTTP response (e.g. asking for, being " +
                "asked for, sending, or receiving data). Common request/response features are available here, such as" +
                " content types and status codes.")
            .examples("the response").since("1.0.0"));
        Classes.registerClass(new ClassInfo<>(Request.class, "request").user("(?:web)? requests?").name("Web Request")
            .description("Represents an incoming website request (a browser asking for a page or data), or an " +
                "outgoing request (your server contacting a website).")
            .examples("the request").since("1.0.0"));
        Classes.registerClass(new ClassInfo<>(Response.class, "response").user("(?:web)? responses?").name("Web Response")
            .description("Represents an incoming website response (a website sending you a page or data), or an " +
                "outgoing response (your server replying to a request).")
            .examples("the response").since("1.0.0"));
    }

    public void registerFileFormats() {

    }

    public void registerComparators() {
        Comparators.registerComparator(FileController.class, FileController.class, new Comparator<>() {
            @Override
            public @NotNull Relation compare(FileController o1, FileController o2) {
                if (o1.equals(o2)) return Relation.EQUAL;
                return Relation.NOT_EQUAL;
            }
        });
        Comparators.registerComparator(URI.class, URI.class, new Comparator<>() {
            @Override
            public @NotNull Relation compare(URI o1, URI o2) {
                if (o1.equals(o2)) return Relation.EQUAL;
                return Relation.NOT_EQUAL;
            }
        });
        Comparators.registerComparator(URI.class, FileController.class, new Comparator<>() {
            @Override
            public @NotNull Relation compare(URI o1, FileController o2) {
                if (o1.equals(o2.getPath())) return Relation.EQUAL;
                return Relation.NOT_EQUAL;
            }
        });
    }

    public void registerConverters() {
        Converters.registerConverter(FileController.class, URI.class, FileController::getPath);
        Converters.registerConverter(String.class, URI.class, text -> {
            try {
                return new URI(text);
            } catch (URISyntaxException e) {
                return null;
            }
        }, Converter.NO_LEFT_CHAINING);
    }

    public void loadFormat(Format<?> format, SkriptAddon addon) {
        final FormatInfo<?> info = format.getInfo();
        Classes.registerClass(info.name(format.getName() + " (File Format)")
            .description("A special converter for the " + info.getCodeName() + " file format.")
            .examples("the " + format.getName().toLowerCase() + " content of the file")
            .since(addon.version.toString()));
    }

    void removeEffChange() {
        this.effects = Skript.getEffects();
        this.syntax = Skript.getStatements();
        final Iterator<SyntaxElementInfo<? extends Effect>> iterator = effects.iterator();
        while (iterator.hasNext()) {
            final SyntaxElementInfo<?> next = iterator.next();
            if (next.elementClass != EffChange.class) continue;
            this.change = next;
            iterator.remove();
            break;
        }
        this.syntax.remove(change);
    }

    @SuppressWarnings("unchecked")
    void reAddEffChange() {
        this.effects.add((SyntaxElementInfo<? extends Effect>) change);
        this.syntax.add((SyntaxElementInfo<? extends Statement>) change);
        this.effects = null;
        this.syntax = null;
        this.change = null;
    }

    public void registerErrorTypes(SkriptAddon addon) {
        this.registerErrorType(new ErrorInfo<>("error", Exception.class, Exception::new, Exception::new).user("error"
            , "exception"), addon);
        this.registerErrorType(new ErrorInfo<>("ioexception", IOException.class, IOException::new, IOException::new).user("io exception", "io error"), addon);
        this.registerErrorType(new ErrorInfo<>("nullpointerexception", NullPointerException.class,
                NullPointerException::new, NullPointerException::new).user("null(-| )pointer (error|exception)", "npe"),
            addon);
    }

    private void registerErrorType(ClassInfo<?> info, SkriptAddon addon) {
        Classes.registerClass(info.name(info.getC().getSimpleName() + " (Error)")
            .description("A special converter for the " + info.getCodeName() + " file format.")
            .examples("throw an " + info.getCodeName()).since(addon.version.toString()));
    }

}
