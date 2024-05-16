package org.skriptlang.skript_io.utility.web;

import java.util.HashMap;
import java.util.Map;

public enum ContentType {
    AAC("aac", "audio/aac"), // AAC audio
    ABW("abw", "application/x-abiword"), // AbiWord document
    APNG("apng", "image/apng"), // Animated Portable Network Graphics (APNG) image
    ARC("arc", "application/x-freearc"), // Archive document (multiple files embedded)
    AVIF("avif", "image/avif"), // AVIF image
    AVI("avi", "video/x-msvideo"), // AVI: Audio Video Interleave
    AZW("azw", "application/vnd.amazon.ebook"), // Amazon Kindle eBook format
    BIN("bin", "application/octet-stream"), // Any kind of binary data
    BMP("bmp", "image/bmp"), // Windows OS/2 Bitmap Graphics
    BZ("bz", "application/x-bzip"), // BZip archive
    BZ2("bz2", "application/x-bzip2"), // BZip2 archive
    CDA("cda", "application/x-cdf"), // CD audio
    CSH("csh", "application/x-csh"), // C-Shell script
    CSS("css", "text/css"), // Cascading Style Sheets (CSS)
    CSV("csv", "text/csv"), // Comma-separated values (CSV)
    DOC("doc", "application/msword"), // Microsoft Word
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"), // Microsoft Word (OpenXML)
    EOT("eot", "application/vnd.ms-fontobject"), // MS Embedded OpenType fonts
    EPUB("epub", "application/epub+zip"), // Electronic publication (EPUB)
    GZ("gz", "application/gzip"), // GZip Compressed Archive
    GIF("gif", "image/gif"), // Graphics Interchange Format (GIF)
    HTM("htm", "text/html"), // HyperText Markup Language (HTML)
    HTML("html", "text/html"), // HyperText Markup Language (HTML)
    ICO("ico", "image/vnd.microsoft.icon"), // Icon format
    ICS("ics", "text/calendar"), // iCalendar format
    JAR("jar", "application/java-archive"), // Java Archive (JAR)
    JPEG("jpeg", "image/jpeg"), // JPEG images
    JPG("jpg", "image/jpeg"), // JPEG images
    JS("js", "text/javascript"), // (Specifications: HTML and RFC 9239) JavaScript
    JSON("json", "application/json"), // JSON format
    JSONLD("jsonld", "application/ld+json"), // JSON-LD format
    MID("mid", "audio/midi, audio/x-midi"), // Musical Instrument Digital Interface (MIDI)
    MIDI("midi", "audio/midi, audio/x-midi"), // Musical Instrument Digital Interface (MIDI)
    MJS("mjs", "text/javascript"), // JavaScript module
    MP3("mp3", "audio/mpeg"), // MP3 audio
    MP4("mp4", "video/mp4"), // MP4 video
    MPEG("mpeg", "video/mpeg"), // MPEG Video
    MPKG("mpkg", "application/vnd.apple.installer+xml"), // Apple Installer Package
    ODP("odp", "application/vnd.oasis.opendocument.presentation"), // OpenDocument presentation document
    ODS("ods", "application/vnd.oasis.opendocument.spreadsheet"), // OpenDocument spreadsheet document
    ODT("odt", "application/vnd.oasis.opendocument.text"), // OpenDocument text document
    OGA("oga", "audio/ogg"), // OGG audio
    OGV("ogv", "video/ogg"), // OGG video
    OGX("ogx", "application/ogg"), // OGG
    OPUS("opus", "audio/opus"), // Opus audio
    OTF("otf", "font/otf"), // OpenType font
    PNG("png", "image/png"), // Portable Network Graphics
    PDF("pdf", "application/pdf"), // Adobe Portable Document Format (PDF)
    PHP("php", "application/x-httpd-php"), // Hypertext Preprocessor (Personal Home Page)
    PPT("ppt", "application/vnd.ms-powerpoint"), // Microsoft PowerPoint
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"), // Microsoft
    // PowerPoint (OpenXML)
    RAR("rar", "application/vnd.rar"), // RAR archive
    RTF("rtf", "application/rtf"), // Rich Text Format (RTF)
    SH("sh", "application/x-sh"), // Bourne shell script
    SVG("svg", "image/svg+xml"), // Scalable Vector Graphics (SVG)
    TAR("tar", "application/x-tar"), // Tape Archive (TAR)
    TIF("tif", "image/tiff"), // Tagged Image File Format (TIFF)
    TIFF("tiff", "image/tiff"), // Tagged Image File Format (TIFF)
    TS("ts", "video/mp2t"), // MPEG transport stream
    TTF("ttf", "font/ttf"), // TrueType Font
    TXT("txt", "text/plain"), // Text, (generally ASCII or ISO 8859-n)
    SK("sk", "text/plain"), // Skript file -- ours!
    VSD("vsd", "application/vnd.visio"), // Microsoft Visio
    WAV("wav", "audio/wav"), // Waveform Audio Format
    WEBA("weba", "audio/webm"), // WEBM audio
    WEBM("webm", "video/webm"), // WEBM video
    WEBP("webp", "image/webp"), // WEBP image
    WOFF("woff", "font/woff"), // Web Open Font Format (WOFF)
    WOFF2("woff2", "font/woff2"), // Web Open Font Format (WOFF)
    XHTML("xhtml", "application/xhtml+xml"), // XHTML
    XLS("xls", "application/vnd.ms-excel"), // Microsoft Excel
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), // Microsoft Excel (OpenXML)
    XML("xml", "application/xml"), // XML
    XUL("xul", "application/vnd.mozilla.xul+xml"), // XUL
    ZIP("zip", "application/zip"), // ZIP archive
    TYPE_3GP("3gp", "video/3gpp"), // audio/3gpp if it doesn't contain video 3GPP audio/video container
    TYPE_3G2("3g2", "video/3gpp2"), // audio/3gpp2 if it doesn't contain video 3GPP2 audio/video container
    TYPE_7Z("7z", "application/x-7z-compressed"), // 7-zip archive
    UNKNOWN("unknown", "application/octet-stream"); // things we don't know about!

    private static final Map<String, ContentType> typesMap = new HashMap<>();

    static {
        for (ContentType type : ContentType.values()) typesMap.put(type.extension, type);
    }

    private final String mimeType;
    private final String extension;

    ContentType(String extension, String mimeType) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String mimeType() {
        return mimeType;
    }

    public String extension() {
        return extension;
    }

    public static ContentType byExtension(String extension) {
        if (extension == null || extension.isBlank()) return UNKNOWN;
        return typesMap.getOrDefault(extension, UNKNOWN);
    }

    public static String getMimeType(String extension) {
        final ContentType type = byExtension(extension);
        return type.mimeType();
    }

    public static boolean isKnown(String extension) {
        if (extension == null || extension.isBlank()) return false;
        return typesMap.containsKey(extension);
    }

}
