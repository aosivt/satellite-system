package org.satellite.system.image.converter.services;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;

@RequiredArgsConstructor
public class SendToDataBaseService implements SatelliteServiceSocket{

    private final LinkedBlockingQueue<Path> paths;

    @Override
    public void send() {

    }

}
