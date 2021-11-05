package org.satellite.system.image.converter;

import org.gdal.gdal.gdal;
import org.satellite.system.image.converter.core.OptionsImageConverters;
import org.satellite.system.image.converter.services.WatcherFileArchive;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        gdal.AllRegister();
        init();
    }

    private static void init() throws IOException {
        final var prop = new OptionsImageConverters();
        WatcherFileArchive.start(prop);
    }

}
