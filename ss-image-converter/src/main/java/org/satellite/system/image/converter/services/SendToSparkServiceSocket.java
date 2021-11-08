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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SendToSparkServiceSocket implements SatelliteServiceSocket{

    private final WatcherFileArchive watcherFileArchive;


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
            convertAndSend(images,manifest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convertAndSend(final Set<Path> images,final String manifest){
        final var postfixPaths = ((OptionsImageConverters)watcherFileArchive
                                            .getWatcherOptions()).getBandsByValue(manifest);
        final var mapDs = new HashMap<String, Dataset>();

        final var x = gdal.Open(images.stream().findFirst().get().toAbsolutePath().toString()).getRasterXSize();
        final var y = gdal.Open(images.stream().findFirst().get().toAbsolutePath().toString()).getRasterYSize();
        try(final Socket sender = new Socket("127.0.0.1",9999);) {
            try {
                IntStream.range(1,y).filter(f->f == 1 || (f - 1) % 3 == 0).forEach(rowId->{
                    IntStream.range(1,x).filter(f->f == 1 || (f - 1) % 3 == 0).forEach(colId->{
                        final var dto = new DtoSparkImagePart();
                        dto.setColId(colId);
                        dto.setRowId(rowId);
                        images.forEach(image->{
                            final var postfixSearchArray =
                                    postfixPaths.stream().filter(pf->image.toString().contains(pf)).toArray();
                            if (postfixSearchArray.length>0){
                                try {
                                    final var methodName = ((OptionsImageConverters)watcherFileArchive
                                            .getWatcherOptions()).getBandsMethodByBandsPostfix(postfixSearchArray[0].toString())
                                            .stream().findFirst().get();
                                    final var ds = gdal.Open(image.toAbsolutePath().toString());
                                    double[] tempArray = new double[9];
                                    ds.GetRasterBand(1).ReadRaster(colId-1,rowId-1,3,3,tempArray);

                                    final var result =
                                            Arrays.stream(tempArray, 0, 9)
                                                    .boxed().collect(Collectors.toList());

                                    Method method = DtoSparkImagePart.class.getDeclaredMethod(methodName, List.class);
                                    method.invoke(dto,result);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            };
                        });
                        send(dto, sender);
                    });
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        final var end = new DtoSparkImagePart();
        end.setRowId(0);
        send(end, sender);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
