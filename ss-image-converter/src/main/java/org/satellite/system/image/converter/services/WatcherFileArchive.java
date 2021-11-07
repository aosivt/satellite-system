package org.satellite.system.image.converter.services;

import lombok.Getter;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.satellite.system.image.converter.core.WatcherOptions;
import org.satellite.system.image.converter.exceptions.InitWatcherException;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WatcherFileArchive {

    private final WatchService watcher = FileSystems.getDefault().newWatchService();
    private final Set<SatelliteServiceSocket> services = new HashSet<>();
    @Getter
    private final LinkedBlockingQueue<Path> paths = new LinkedBlockingQueue<>();
    @Getter
    private final WatcherOptions watcherOptions;
    private final WatchKey key;
    private final Integer BUFFER_SIZE = 2048;
    private final String TEMPLATE_ZIP_EXTENSION = ".zip";

    private final Runnable unzipAndReplaceFunction;
    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);


    private WatcherFileArchive(final WatcherOptions watcherOptions) throws IOException {
        this.watcherOptions = watcherOptions;
        key = initWatcherArchive();
        unzipAndReplaceFunction = ()->{
            key.pollEvents().forEach(f->{
                System.out.println("event");
                try {
                    unArch(f.context().toString());
                    delete(f.context().toString());
                    services.parallelStream().forEach(SatelliteServiceSocket::send);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        };
        watch();
    }

    public static void start(final WatcherOptions watcherOptions) throws IOException {
        final var instance = new WatcherFileArchive(watcherOptions);
        instance.services.add(new SendToDataBaseService(instance.paths));
        instance.services.add(new SendToSparkServiceSocket(instance));
    }

    private WatchKey initWatcherArchive(){
        try {
            return Paths.get(watcherOptions.getPathToArchive()).register(watcher,
                            StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException x) {
            throw new InitWatcherException(watcherOptions.getPathToArchive());
        }
    }
    private void watch(){
        service.scheduleAtFixedRate(unzipAndReplaceFunction,5, 10,TimeUnit.SECONDS);
    }
    private void unArch(final String nameFile) throws IOException {
        final var inputPath = Paths.get(watcherOptions.getPathToArchive(), File.separator, nameFile);
        if (Files.notExists(inputPath)) return;

        final var stringRootPath = String.format("%s%s",watcherOptions.getPathLocalImage(),
                nameFile.substring(0,nameFile.length()-TEMPLATE_ZIP_EXTENSION.length()));

        final var rootOutputPath = Paths.get(stringRootPath);
        Files.createDirectory(rootOutputPath);
        try (InputStream fin = Files.newInputStream(inputPath)) {
            final var in = new ZipInputStream(new BufferedInputStream(fin));
            final var buffer = new byte[BUFFER_SIZE];
            ZipEntry entry = null;
            OutputStream out = null;
            while((entry = in.getNextEntry())!=null) {
                try {
                    if (!checkAllowed(entry.getName().toLowerCase())) continue;
                    final var outputPath =
                            Paths.get(stringRootPath,File.separator, eraseDirectoryFromStringPath(entry.getName()));
                    out = Files.newOutputStream(outputPath);
                    int len = 0;
                    while ((len = in.read(buffer)) > 0) {
                        assert out != null;
                        out.write(buffer, 0, len);
                    }
                }
                finally {
                    if(out!=null) out.close();
                }
            }
        }
        paths.add(rootOutputPath);
    }

    private void delete(final String nameFile) throws IOException {
        final var inputPath = Paths.get(watcherOptions.getPathToArchive(), File.separator, nameFile);
        Files.delete(inputPath);
    }

    private Boolean checkAllowed(final String nameFile){
        if (watcherOptions.allowedMetaData().stream().filter(nameFile::contains).toArray().length > 0) return true;
        return checkAllowedExtension(nameFile);
    }
    private Boolean checkAllowedExtension(final String nameFile){
        final var extension = getExtensionFile(nameFile);
        return watcherOptions.allowedExtensions().contains(extension);
    }
    private String getExtensionFile(final String nameFile){
        final var splitFileName = nameFile.split("\\.");
        return splitFileName[splitFileName.length-1].toLowerCase(Locale.ROOT);
    }
    private String eraseDirectoryFromStringPath(final String absolutePath){
        return Paths.get(absolutePath).getFileName().toString();
    }

}
