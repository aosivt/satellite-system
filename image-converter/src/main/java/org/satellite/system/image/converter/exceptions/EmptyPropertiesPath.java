package org.satellite.system.image.converter.exceptions;

public class EmptyPropertiesPath extends RuntimeException{

    public EmptyPropertiesPath(final String path){
        super(String.format("Properties path '%s' is empty or...fuck off", path));
    }
}
