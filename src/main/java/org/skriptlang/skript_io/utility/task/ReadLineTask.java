package org.skriptlang.skript_io.utility.task;

import mx.kenzie.clockwork.io.DataTask;
import org.skriptlang.skript_io.utility.file.FileController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

public class ReadLineTask extends DataTask {

    protected final FileController controller;
    protected final int line;
    protected final AtomicReference<String> reference;

    public ReadLineTask(FileController controller, int line, AtomicReference<String> reference) {
        this.controller = controller;
        this.line = line;
        this.reference = reference;
    }

    @Override
    public void execute() throws IOException {
        try (final InputStream stream = controller.acquireReader();
             final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            int counter = 0;
            String line;
            do line = reader.readLine(); while (counter++ < this.line);
            this.reference.set(line);
        }
    }

}
