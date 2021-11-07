package org.satellite.system.image.converter.core;


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
            final var client = new Socket("127.0.0.1",9999);
            final var testData = "testData";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(testData);
            oos.flush();
            oos.close();

            // Get the size of the file
            long length = testData.length();
            byte[] bytes = new byte[16 * 1024];
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            OutputStream out = client.getOutputStream();
            int count;
            while ((count = is.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            out.close();
            is.close();
            client.close();
        }catch (ConnectException e){
            e.printStackTrace();
        }

    }

    @Test
    public void testServerSocket() throws IOException {
        final var listener = new ServerSocket(9999);
        while (true){
            final var server = listener.accept();
            final var output = server.getOutputStream();
            final var writer = new PrintWriter(output,true);
            writer.println("This is a message sent to ther server");
//            Files.copy(server.getInputStream(), Path.of("temp.txt"));
//            BufferedReader fromSoc = new BufferedReader(new InputStreamReader(server.getInputStream()));

        }
    }
}
