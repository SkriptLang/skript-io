package org.skriptlang.skript_io.utility.task;

import mx.kenzie.clockwork.io.DataTask;
import org.skriptlang.skript_io.utility.file.FileController;

import java.io.IOException;
import java.io.OutputStream;

public class AppendTask extends DataTask {

    protected final FileController controller;
    protected final byte[] content;

    public AppendTask(FileController controller, byte[] content) {
        this.controller = controller;
        this.content = content;
    }

    @Override
    public void execute() throws IOException {
        final OutputStream stream = controller.acquireWriter(true);
        stream.write(content);
    }

}
