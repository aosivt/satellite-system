package org.satellite.system.image.converter;

import org.gdal.gdal.gdal;
import org.satellite.system.image.converter.core.DtoSparkImagePart;
import org.satellite.system.image.converter.core.OptionsImageConverters;
import org.satellite.system.image.converter.services.WatcherFileArchive;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException {
        gdal.AllRegister();
        init(args.length > 0 ? args[0] : null);
    }

    private static void init(final String path) throws IOException {
        final var prop = new OptionsImageConverters(path);
        WatcherFileArchive.start(prop);
    }
}
