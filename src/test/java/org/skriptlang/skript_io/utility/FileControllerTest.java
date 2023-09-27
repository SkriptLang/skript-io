package org.skriptlang.skript_io.utility;

import org.junit.Test;

import java.net.URI;

import static org.skriptlang.skript_io.utility.file.FileController.READ;
import static org.skriptlang.skript_io.utility.file.FileController.WRITE;

public class FileControllerTest {
    
    @Test
    public void modeTest() {
        int mode = READ;
        assert (mode & READ) == READ;
        assert (mode & WRITE) != WRITE;
        mode = WRITE;
        assert (mode & READ) != READ;
        assert (mode & WRITE) == WRITE;
        mode = READ | WRITE;
        assert (mode & READ) == READ;
        assert (mode & WRITE) == WRITE;
    }
    
    @Test
    public void uriIpTest() {
        assert URI.create("localhost") != null;
        assert URI.create("0.0.0.0") != null;
        assert URI.create("http://192.168.178.1") != null;
        assert URI.create("http://0.0.0.0:25565") != null;
    }
    
}
