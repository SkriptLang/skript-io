package org.skriptlang.skript_io.utility.web;

import java.net.URI;

public interface Request {
    
    URI getPath();
    
    default void setPath(URI path) {
    
    }
    
    String getSource();
    
    String getMethod();
    
    default void setMethod(String mode) {
    
    }
    
    String getContentType();
    
    default void setContentType(String type) {
    
    }
    
    String getHeader(String header);
    
    void setHeader(String header, String type);
    
}
