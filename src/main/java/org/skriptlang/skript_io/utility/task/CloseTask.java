package org.skriptlang.skript_io.utility.task;

import mx.kenzie.clockwork.io.DataTask;
import org.skriptlang.skript_io.SkriptIO;

import java.io.IOException;

public class CloseTask extends DataTask {
    
    private final AutoCloseable closeable;
    
    public CloseTask(AutoCloseable closeable) {this.closeable = closeable;}
    
    @Override
    public void execute() throws IOException, InterruptedException {
        try {
            this.closeable.close();
        } catch (IOException | InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            SkriptIO.error(ex);
        }
        
    }
    
}
