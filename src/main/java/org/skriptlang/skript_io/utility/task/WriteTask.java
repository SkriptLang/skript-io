package org.skriptlang.skript_io.utility.task;

import mx.kenzie.clockwork.io.DataTask;
import org.skriptlang.skript_io.utility.Writable;

import java.io.IOException;
import java.io.OutputStream;

public class WriteTask extends DataTask {

    protected final Writable writable;
    protected final byte[] content;
    private final IOException exception;

    public WriteTask(Writable writable, byte[] content) {
        exception = new IOException("Error in queued task");
        exception.fillInStackTrace();
        this.writable = writable;
        this.content = content;
    }

    @Override
    public void execute() throws IOException {
        try {
            OutputStream stream = writable.acquireWriter();
            stream.write(content);
        } catch (IOException ex) {
            exception.addSuppressed(ex);
            throw exception;
        }
    }

}
