package org.satellite.system.image.converter.exceptions;

public class InitWatcherException extends RuntimeException{
    public InitWatcherException(final String path){
        super(String.format("Watcher don`t started watching because you not set path or this path [%s] don`t exist - asshole",path));
    }
}
