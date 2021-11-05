package org.satellite.system.image.converter.core;


import org.junit.Before;
import org.junit.Test;
import org.satellite.system.image.converter.Main;
import org.satellite.system.image.converter.exceptions.EmptyMainProperties;

import static org.junit.Assert.*;

import java.io.IOException;
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
}
