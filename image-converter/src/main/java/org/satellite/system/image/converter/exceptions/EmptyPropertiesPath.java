package org.satellite.system.image.converter.exceptions;

public class EmptyPropertiesPath extends RuntimeException{

    public EmptyPropertiesPath(final String path){
        super("Properties path is empty or...fuck off");
    }
}
