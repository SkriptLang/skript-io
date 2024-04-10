package org.skriptlang.skript_io.format;

import ch.njol.skript.classes.ClassInfo;

public class FormatInfo<Type> extends ClassInfo<Format<Type>> {

    private final Format<Type> format;

    /**
     * @param type     The class
     * @param codeName The name used in patterns
     * @param format   The format
     */
    public FormatInfo(Class<Format<Type>> type, String codeName, Format<Type> format) {
        super(type, codeName);
        this.format = format;
    }

    public Format<Type> getFormat() {
        return format;
    }

}
