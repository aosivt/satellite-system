package org.satellite.system.image.converter.core;

import lombok.extern.log4j.Log4j2;
import org.satellite.system.image.converter.Main;
import org.satellite.system.image.converter.exceptions.EmptyPropertiesPath;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptionsImageConverters {
    private final Properties properties = new Properties();

    public OptionsImageConverters() throws IOException {
        properties.load(Main.class.getClassLoader().getResourceAsStream("image-converter.properties"));
    }

    public OptionsImageConverters(final String pathProperties) throws IOException {
        if (Objects.nonNull(pathProperties)){
            properties.load(new FileInputStream(pathProperties));
        }
        throw new EmptyPropertiesPath(pathProperties);
    }

    public String[] getSatelliteNames(){
        return properties.getProperty("satellites.name").split(",");
    }
    public String[] getExistsBands(){
        return properties.getProperty("satellites.exists.bands").split(",");
    }
    public String[] getPrefixes(){
        return Stream.of(getSatelliteNames())
                .map(n->properties.getProperty(String.format("satellites.%s.prefix",n))).toArray(String[]::new);
    }
    public Set<String> getAllPostfixExistsBands(){
        return Stream.of(getSatelliteNames())
                .flatMap(n-> Stream.of(getExistsBands()).map(b->properties.getProperty(String.format("satellites.%s.bands.%s",n,b)))).collect(Collectors.toSet());
    }
    public String[] getExistsBandsBySatelliteName(final String name){
        return Stream.of(getSatelliteNames()).filter(n->n.equals(name))
                .flatMap(n-> Stream.of(getExistsBands()).map(b->properties.getProperty(String.format("satellites.%s.bands.%s",n,b))))
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

}
