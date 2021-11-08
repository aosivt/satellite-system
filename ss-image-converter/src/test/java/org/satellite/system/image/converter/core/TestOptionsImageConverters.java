package org.satellite.system.image.converter.core;


import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.satellite.system.image.converter.Main;
import org.satellite.system.image.converter.exceptions.EmptyMainProperties;

import static org.junit.Assert.*;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestOptionsImageConverters {

    private final OptionsImageConverters options = new OptionsImageConverters();
    private final String TEST_NAME_SATELLITE = "landSat5";
    private final String TEST_NAME_BAND = "B3.";

    public TestOptionsImageConverters() throws IOException {
    }

    @Before
    public void initTest(){

    }

    @Test
    public void testGetSatelliteNames(){
        final var prefixes = options.getExistsPrefix();
        final var existsBands = options.getExistsBands();
        final var satelliteNames = options.getSatelliteNames();
        final var allPostfixExistsBands = options.getAllPostfixExistsBands();
        final var existsBandsBySatelliteName = options.getExistsBandsBySatelliteName(TEST_NAME_SATELLITE);
        assertTrue(allPostfixExistsBands.contains(TEST_NAME_BAND));
        assertTrue(satelliteNames.contains(TEST_NAME_SATELLITE));
        assertTrue(existsBandsBySatelliteName.contains(TEST_NAME_BAND));
    }
    @Test
    public void testEmptyException() {
        boolean thrown = false;
        try {
            OptionsImageConverters options = new OptionsImageConverters(Objects.requireNonNull(Main.class.getClassLoader()
                    .getResource("empty.properties")).getPath());
        } catch (EmptyMainProperties e){
            thrown = true;
        }
        assertTrue(true);
    }

    @Test
    public void testConnectToSparkSocket() throws IOException {
        try {
            final var dto = new DtoSparkImagePart();
            final var dto2 = new DtoSparkImagePart();
            final var end = new DtoSparkImagePart();
            dto.setRowId(1);

            final var client = new Socket("localhost",9999);
            OutputStream outputStream = client.getOutputStream();
            final var template = new ObjectOutputStream(outputStream);

            template.writeObject(dto);
            dto2.setRowId(2);
            template.writeObject(dto2);
            end.setRowId(0);
            template.writeObject(end);
        }catch (ConnectException e){
            e.printStackTrace();
        }

    }

    @Test
    public void testServerSocket() throws IOException {
        final var listener = new ServerSocket(9999);
        final var t = new Thread(() -> {
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
          });
        t.setDaemon(true);
        t.start();
    }

}
