package org.satellite.system.image.converter.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.satellite.system.image.converter.Main;
import org.satellite.system.image.converter.exceptions.EmptyMainProperties;
import org.satellite.system.image.converter.exceptions.EmptyPropertiesPath;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class OptionsImageConverters implements WatcherOptions{

    private final Properties properties = new Properties();

    private final String KEY_STRING_LOCAL_IMAGE_TEMP = "local-image-temp";
    private final String KEY_STRING_LOCAL_IMAGE = "local-image";
    private final String KEY_STRING_FILE_SYSTEM_TYPE = "file-system-type";
    private final String KEY_STRING_PATH_TO_ARCH = "path-to-arch";

    private final String TEMPLATE_STRING_SEARCH_NAME = "satellites.";
    private final Short NUMBER_POSITION_SATELLITE_NAME = 1;
    private final String TEMPLATE_STRING_SEARCH_BANDS = ".bands.";
    private final Short NUMBER_POSITION_SATELLITE_BANDS = 3;
    private final String TEMPLATE_STRING_SEARCH_PREFIX = ".prefix";
    private final String TEMPLATE_STRING_ALLOWED_IMAGE_EXTENSIONS = "allowed-image-extensions";
    private final String TEMPLATE_STRING_ALLOWED_METADATA = ".allowed-file-part-name-manifest";

    @Getter
    private final Set<String> satelliteNames = new HashSet<>();
    @Getter
    private final Set<String> existsBands = new HashSet<>();
    @Getter
    private final Set<String> existsPrefix = new HashSet<>();
    @Getter
    private final Set<String> existsManifest = new HashSet<>();

    public OptionsImageConverters() {
        this(null);
    }

    public OptionsImageConverters(final String pathProperties) {
        try {
            if (Objects.nonNull(pathProperties)){
                properties.load(new FileInputStream(pathProperties));
            }else {
                properties.load(Main.class.getClassLoader().getResourceAsStream("image-converter.properties"));
            }
        }catch (IOException e){
            throw new EmptyPropertiesPath(pathProperties);
        }
        initSets();
        checkMainOptions();
    }



    public Set<String> getAllPostfixExistsBands(){
        return getSatelliteNames().stream()
                .flatMap(n-> getExistsBands().stream()
                        .map(b->properties.getProperty(
                                String.format("%s%s%s%s",TEMPLATE_STRING_SEARCH_NAME,n,TEMPLATE_STRING_SEARCH_BANDS,b))))
                .collect(Collectors.toSet());
    }
    public Set<String> getExistsBandsBySatelliteName(final String name){
        return getSatelliteNames().stream().filter(n->n.equals(name))
                .flatMap(n-> getExistsBands().stream()
                        .map(b->properties.getProperty(
                                String.format("%s%s%s%s",TEMPLATE_STRING_SEARCH_NAME,n,TEMPLATE_STRING_SEARCH_BANDS,b))))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPathLocalImageTemp(){
        return properties.getProperty(KEY_STRING_LOCAL_IMAGE_TEMP);
    }
    @Override
    public String getPathLocalImage(){
        return properties.getProperty(KEY_STRING_LOCAL_IMAGE);
    }
    @Override
    public String getPathToArchive(){
        return properties.getProperty(KEY_STRING_PATH_TO_ARCH);
    }

    public String getFileSystemType(){
        return properties.getProperty(KEY_STRING_FILE_SYSTEM_TYPE);
    }
    @Override
    public Set<String> allowedExtensions(){
        final var prop = properties.getProperty(TEMPLATE_STRING_ALLOWED_IMAGE_EXTENSIONS);
        if (Objects.isNull(prop)){
            throw new EmptyMainProperties();
        }
        return Stream.of(prop.split(",")).map(String::toLowerCase).collect(Collectors.toSet());
    }

    @Override
    public Set<String> allowedMetaData(){
        if (existsManifest.size() == 0){
            throw new EmptyMainProperties();
        }
        return existsManifest;
    }


    private Set<String> getInitOptions(final String containsString,final Short position){
        return properties.stringPropertyNames().stream()
                .filter(f->f.contains(TEMPLATE_STRING_SEARCH_NAME) && f.contains(containsString))
                .map(s->s.split("\\.")[position])
                .collect(Collectors.toSet());
    }

    private void initSets(){
        satelliteNames.addAll(getInitOptions(TEMPLATE_STRING_SEARCH_PREFIX,NUMBER_POSITION_SATELLITE_NAME));
        existsBands.addAll(getInitOptions(TEMPLATE_STRING_SEARCH_BANDS,NUMBER_POSITION_SATELLITE_BANDS));
        fieldSetBasedOnSatellite(TEMPLATE_STRING_SEARCH_PREFIX,existsPrefix);
        fieldSetBasedOnSatellite(TEMPLATE_STRING_ALLOWED_METADATA,existsManifest);
    }

    private Set<String> findAllKeyByValue(final String value){
        return properties.stringPropertyNames().stream().
                  filter(pk->properties.getProperty(pk).contains(value)).collect(Collectors.toSet());
    }

    public Set<String> getNameSatelliteByValue(final String value){
        return findAllKeyByValue(value).stream()
                .map(pk->pk.split("\\.")[NUMBER_POSITION_SATELLITE_NAME]).collect(Collectors.toSet());
    }

    public Set<String> getBandsMethodByValue(final String value){
        return findAllKeyByValue(value).stream()
                .map(pk->pk.split("\\.")[NUMBER_POSITION_SATELLITE_BANDS]).collect(Collectors.toSet());
    }
    public Set<String> getBandsMethodByBandsPostfix(final String value){
        final var names = getNameSatelliteByValue(value);
        return existsBands.stream().flatMap(b->names.stream()
                .filter(sn-> properties.getProperty(String.format("%s%s%s%s",
                                                                    TEMPLATE_STRING_SEARCH_NAME,
                                                                    sn,TEMPLATE_STRING_SEARCH_BANDS,b)).equals(value))
                .map(s->b)).collect(Collectors.toSet());
    }

    public Set<String> getBandsByValue(final String value){
        final var names = getNameSatelliteByValue(value);
        return existsBands.stream().flatMap(b->names.stream()
                .map(sn->properties.getProperty(
                        String.format("%s%s%s%s",TEMPLATE_STRING_SEARCH_NAME,sn,TEMPLATE_STRING_SEARCH_BANDS,b))))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }


    private void fieldSetBasedOnSatellite(final String templateSearch, final Set<String> set){
        satelliteNames.stream().map(sn->
                        properties.getProperty(
                                String.format("%s%s%s",TEMPLATE_STRING_SEARCH_NAME,sn,templateSearch))
                ).filter(Objects::nonNull)
                .flatMap(fm-> Arrays.stream(fm.split(",")))
                .forEach(set::add);
    }

    private void checkMainOptions(){
        if (Objects.nonNull(getPathLocalImageTemp()) &&
                Objects.nonNull(getPathLocalImage()) &&
                Objects.nonNull(getFileSystemType()) &&
                Objects.nonNull(getPathToArchive())){
            return;
        }
        throw new EmptyMainProperties();
    }

}
