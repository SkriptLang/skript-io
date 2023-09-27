package org.skriptlang.skript_io.utility.task;

import mx.kenzie.clockwork.io.DataTask;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Writable;

import java.io.IOException;

public class TransferTask extends DataTask {
    
    protected final Writable writable;
    protected final Readable readable;
    
    public TransferTask(Writable writable, Readable readable) {
        this.writable = writable;
        this.readable = readable;
    }
    
    @Override
    public void execute() throws IOException {
        this.readable.acquireReader().transferTo(writable.acquireWriter());
    }
    
}
