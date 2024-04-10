package org.skriptlang.skript_io.utility.task;

import mx.kenzie.clockwork.io.DataTask;

import java.io.IOException;

/**
 * This brings the queue to a head (e.g. making sure all file changes have closed before we read)
 */
public class TidyTask extends DataTask {

    @Override
    public void execute() throws IOException, InterruptedException {
    }

}
