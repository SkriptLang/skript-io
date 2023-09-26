package org.skriptlang.skript_io.utility;

import mx.kenzie.clockwork.io.DataTask;

import java.io.FileOutputStream;
import java.io.IOException;

public class WriteTask extends DataTask {
    
    protected final FileController controller;
    protected final byte[] content;
    
    public WriteTask(FileController controller, byte[] content) {
        this.controller = controller;
        this.content = content;
    }
    
    @Override
    public void execute() throws IOException {
        final FileOutputStream stream = controller.acquireWriter();
        stream.write(content);
    }
    
}
