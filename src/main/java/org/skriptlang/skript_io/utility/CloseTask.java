package org.skriptlang.skript_io.utility;

import mx.kenzie.clockwork.io.DataTask;

import java.io.Closeable;
import java.io.IOException;

public class CloseTask extends DataTask {
    
    private final Closeable closeable;
    
    public CloseTask(Closeable closeable) {this.closeable = closeable;}
    
    @Override
    public void execute() throws IOException, InterruptedException {
        this.closeable.close();
        
    }
    
}
