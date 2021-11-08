package org.satellite.system.image.converter.services;

import lombok.RequiredArgsConstructor;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.satellite.system.image.converter.core.DtoSparkImagePart;
import org.satellite.system.image.converter.core.OptionsImageConverters;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SendToSparkServiceSocket implements SatelliteServiceSocket{

    private final WatcherFileArchive watcherFileArchive;

    private final ExecutorService sender = Executors.newFixedThreadPool(12);


    public SendToSparkServiceSocket(WatcherFileArchive watcherFileArchive) {
        this.watcherFileArchive = watcherFileArchive;

    }

    static {
        gdal.AllRegister();
    }
    @Override
    public void send() {
        final var pathsIterator = watcherFileArchive.getPaths().iterator();
        while (pathsIterator.hasNext()){
            final var path  = pathsIterator.next();
            iterate(path);
        }
    }

    private void iterate(final Path rootPath){
        final var images = new HashSet<Path>();
        final var meta = new HashMap<String,Path>();
        final var allowedManifest = ((OptionsImageConverters)watcherFileArchive
                                                                                     .getWatcherOptions())
                                                                                     .allowedMetaData();
        final Consumer<Path> collector = path -> {
           final var metaMap =
                   allowedManifest.stream().filter(b->path.toString().toLowerCase()
                                                          .contains(b.toLowerCase()))
                           .collect(Collectors.toMap((v1->v1),(v2->path)));
           if (metaMap.size() > 0){
               meta.putAll(metaMap);
           } else {
               images.add(path);
           }
        };

        try {
            Files.walkFileTree(rootPath, new SatelliteSystemFileVisitor(collector));
            final var manifest = meta.entrySet().stream().findFirst().get().getKey();
            convertAndSend(images,manifest,rootPath.toFile().getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convertAndSend(final Set<Path> images,final String manifest, final String path){
        final var placeData = watcherFileArchive.getWatcherOptions().getPathLocalImageTemp();

        final var metaDataDs = gdal.Open(images.stream().findFirst().get().toAbsolutePath().toString());
        final var postfixPaths = ((OptionsImageConverters)watcherFileArchive
                                            .getWatcherOptions()).getBandsByValue(manifest);
        final var x = metaDataDs.getRasterXSize();
        final var y = metaDataDs.getRasterYSize();
        final var proj = metaDataDs.GetProjection();
        final var geoTransform =
                Arrays.stream(metaDataDs.GetGeoTransform())
                        .boxed().toArray(Double[]::new);

        final var map = new HashMap<Method, Dataset>();
        images.forEach(image->{
            final var postfixSearchArray =
                    postfixPaths.stream().filter(pf->image.toString().contains(pf)).toArray();
            if (postfixSearchArray.length > 0){
                final var methodName = ((OptionsImageConverters)watcherFileArchive
                        .getWatcherOptions()).getBandsMethodByBandsPostfix(postfixSearchArray[0].toString())
                        .stream().findFirst().get();
                final var ds = gdal.Open(image.toAbsolutePath().toString());
                try {
                    Method method = DtoSparkImagePart.class.getDeclaredMethod(methodName, Double[].class);
                    map.put(method,ds);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
                IntStream.range(1,y).filter(f->f == 1 || (f - 1) % 3 == 0).forEach(rowId->{
                    final var set = new LinkedBlockingQueue<DtoSparkImagePart>();
                    IntStream.range(1,x).filter(f->f == 1 || (f - 1) % 3 == 0).forEach(colId->{
                        final var dto = new DtoSparkImagePart();
                        dto.setColId(colId);
                        dto.setRowId(rowId);
                        dto.setPlacePath(String.format("%s%s",placeData,path));
                        dto.setWidth(x);
                        dto.setHeight(y);
                        dto.setProjection(proj);
                        dto.setGeoTransform(geoTransform);
                        map.forEach((key, value) -> {

                            double[] tempArray = new double[9];
                            value.GetRasterBand(1).ReadRaster(colId - 1, rowId - 1, 3, 3, tempArray);

                            final var result =
                                    Arrays.stream(tempArray, 0, 9)
                                            .boxed().toArray(Double[]::new);
                            try {
                                key.invoke(dto, (Object) result);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                        set.add(dto);
                    });
                    sendDto(set);
                    set.clear();
                });
            }catch (Exception e){
                e.printStackTrace();
            }
    }

    private void sendDto(final LinkedBlockingQueue<DtoSparkImagePart> dto){
//        sender.submit(()->{
            try(final Socket sender = new Socket("127.0.0.1",9999);) {
                OutputStream outputStream = sender.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(dto.toArray(DtoSparkImagePart[]::new));
            } catch (IOException e) {
                e.printStackTrace();
            }
//        });
    }

    private void send(final DtoSparkImagePart dto,final Socket sender){
        try {
            OutputStream outputStream = sender.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(dto);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SatelliteSystemFileVisitor extends SimpleFileVisitor<Path>{

        private final Consumer worker;
        SatelliteSystemFileVisitor(final Consumer worker){
            this.worker = worker;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)  {
            worker.accept(path);
            return FileVisitResult.CONTINUE;
        }
    }
}
