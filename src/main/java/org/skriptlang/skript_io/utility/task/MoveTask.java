package org.skriptlang.skript_io.utility.task;

import mx.kenzie.clockwork.io.DataTask;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.file.FileController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MoveTask extends DataTask {

    private final File file;
    private final String name;

    public MoveTask(File file, String name) {
        this.file = file;
        this.name = name;
    }

    @Override
    public void execute() {
        try {
            if (file.isDirectory()) return;
            Path from = file.toPath();
            Files.move(from, from.resolveSibling(name));
        } catch (IOException ex) {
            SkriptIO.error(ex);
        } finally {
            FileController.flagClean(file);
        }
    }

}
