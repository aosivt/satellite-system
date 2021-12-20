package org.satellite.system.image.converter.services;

import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;

public interface SatelliteServiceSocket {
    void send(Path path);
}
