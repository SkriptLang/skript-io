package org.skriptlang.skript_io.utility.task;

import mx.kenzie.clockwork.io.DataTask;
import org.skriptlang.skript_io.format.Format;
import org.skriptlang.skript_io.utility.Writable;

import java.io.IOException;
import java.util.Map;

public class FormatTask extends DataTask {

    private final Format<?> format;
    private final Writable writable;
    private final Map<?, ?> map;

    public FormatTask(Format<?> format, Writable writable, Map<?, ?> map) {
        this.format = format;
        this.writable = writable;
        this.map = map;
    }

    @Override
    public void execute() throws IOException, InterruptedException {
        format.to(writable, map);
    }

}
