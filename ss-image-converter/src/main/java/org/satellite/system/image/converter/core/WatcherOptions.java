package org.satellite.system.image.converter.core;

import java.util.Set;

public interface WatcherOptions {
    String getPathLocalImageTemp();
    String getPathLocalImage();
    String getPathToArchive();
    Set<String> allowedExtensions();
    Set<String> allowedMetaData();
}
