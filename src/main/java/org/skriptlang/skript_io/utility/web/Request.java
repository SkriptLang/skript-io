package org.skriptlang.skript_io.utility.web;

import java.net.URI;

public interface Request {
    
    URI getPath();
    
    default void setPath(URI path) {
    
    }
    
    String getSource();
    
    String setMethod();
    
    default void setMethod(String mode) {
    
    }
    
}
