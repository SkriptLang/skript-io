package org.skriptlang.skript_io.utility;

import org.junit.Test;

import static org.skriptlang.skript_io.utility.FileController.*;

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
    
}
