package org.satellite.system.image.converter.services;

import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;

public class SendToDataBaseService implements SatelliteServiceSocket{

    private final LinkedBlockingQueue<Path> paths;

    public SendToDataBaseService(final LinkedBlockingQueue<Path> paths){
        this.paths = paths;
    }

    @Override
    public void send() {

    }

}
