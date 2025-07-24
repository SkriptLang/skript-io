package org.skriptlang.skript_io.elements.web.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.web.ContentType;

import java.util.Arrays;

@Name("Content Type")
@Description(LitContentType.DESCRIPTION)
@Example("""
    loop the files in ./test/:
        delete the file at loop-value
    """)
@Since("1.0.0")
public class LitContentType extends SimpleLiteral<String> {

    private static final ContentType[] contentTypes = ContentType.values();
    private static final String[] typeNames = Arrays.stream(contentTypes)
        .map(ContentType::name)
        .map(string -> string.replace('_', ' ').toLowerCase().replace("type_", ""))
        .toArray(String[]::new);

    protected static final String DESCRIPTION = """
        A content-type is sent with a web request (or response) to tell the receiver what kind of data
        will be submitted in the body of the exchange.
        
        Content types can be specified in a simple text (e.g. `"text/plain"` or `"image/gif"`) or by using
        one of the following supported content-type literals:
        
        ```applescript
        aac, abw, apng, arc, avif, avi, azw, bin, bmp, bz, bz2, cda, csh, css, csv, doc,
        docx, eot, epub, gz, gif, htm, html, ico, ics, jar, jpeg, jpg, js, json, jsonld,
        mid, midi, mjs, mp3, mp4, mpeg, mpkg, odp, ods, odt, oga, ogv, ogx, opus, otf,
        png, pdf, php, ppt, pptx, rar, rtf, sh, svg, tar, tif, tiff, ts, ttf, txt, sk,
        vsd, wav, weba, webm, webp, woff, woff2, xhtml, xls, xlsx, xml, xul, zip, 3gp,
        3g2, 7z
        ```
        """;

    static {
        if (!SkriptIO.isTestMode())
            Skript.registerExpression(LitContentType.class, String.class, ExpressionType.SIMPLE, typeNames);
    }

    private String type;

    public LitContentType() {
        super(new String[] {ContentType.TXT.mimeType()}, String.class, false);
    }

    @Override
    public @NotNull Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, SkriptParser.ParseResult result) {
        type = typeNames[pattern];
        data = new String[] {contentTypes[pattern].mimeType()};
        return true;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return type;
    }

}
