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
//        gdal.AllRegister();
//        init(args.length > 0 ? args[0] : null);
        testServerSocket();
    }

    private static void init(final String path) throws IOException {
        final var prop = new OptionsImageConverters(path);
        WatcherFileArchive.start(prop);
    }
    public static void testServerSocket() throws IOException {
        final var listener = new ServerSocket(9999);
//        final var t = new Thread(() -> {
            while (true) {
                // get the input stream from the connected socket
                final InputStream inputStream;
                try {
                    final var server = listener.accept();
                    inputStream = server.getInputStream();
                    // create a DataInputStream so we can read data from it.
                    final var objectInputStream = new ObjectInputStream(inputStream);

                    DtoSparkImagePart listOfMessages;
                    while((listOfMessages = (DtoSparkImagePart) objectInputStream.readObject())!=null){
                        System.out.println(listOfMessages.getRowId());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
//        });
//        t.setDaemon(true);
//        t.start();
    }
}
