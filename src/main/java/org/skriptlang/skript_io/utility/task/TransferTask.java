package org.skriptlang.skript_io.utility.task;

import mx.kenzie.clockwork.io.DataTask;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Writable;

import java.io.*;

public class TransferTask extends DataTask {

    protected final Writable writable;
    protected final Readable readable;

    public TransferTask(Writable writable, Readable readable) {
        this.writable = writable;
        this.readable = readable;
    }

    public static DataTask forFile(File file, Writable target) {
        return new TransferFileTask(file, target);
    }

    @Override
    public void execute() throws IOException {
        try (final InputStream stream = readable.acquireReader();
             final OutputStream output = writable.acquireWriter()) {
            stream.transferTo(output);
        }
    }

}

class TransferFileTask extends DataTask {

    protected final File source;
    protected final Writable target;

    TransferFileTask(File source, Writable target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public void execute() throws IOException, InterruptedException {
        if (source == null || target == null) return;
        if (!source.isFile()) return;
        try (final FileInputStream stream = new FileInputStream(source);
             final OutputStream output = target.acquireWriter()) {
            stream.transferTo(output);
        }
    }

}
