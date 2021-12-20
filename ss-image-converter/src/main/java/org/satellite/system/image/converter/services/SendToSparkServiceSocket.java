package org.satellite.system.image.converter.services;

import lombok.RequiredArgsConstructor;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.satellite.system.image.converter.core.DtoSparkImagePart;
import org.satellite.system.image.converter.core.Md5StringNameFile;
import org.satellite.system.image.converter.core.OptionsImageConverters;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SendToSparkServiceSocket implements SatelliteServiceSocket, Md5StringNameFile {

    private final WatcherFileArchive watcherFileArchive;

    private final ScheduledExecutorService sender = Executors.newScheduledThreadPool(12);

    private final DtoSparkImagePart END_QUEUE = new DtoSparkImagePart();

    private final LinkedBlockingQueue<Runnable> jobs = new LinkedBlockingQueue<>(12);


    public SendToSparkServiceSocket(WatcherFileArchive watcherFileArchive) {
        this.watcherFileArchive = watcherFileArchive;

    }

    static {
        gdal.AllRegister();
    }
    @Override
    public void send(final Path path) {
            iterate(path);
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
            final var placeData = watcherFileArchive.getWatcherOptions().getPathLocalImageTemp();
            final var rootImagePath = rootPath.toFile().getName();
//            convertAndSend(images,manifest,String.format("%s%s%s",placeData,getStringPathForStringNameFile(rootImagePath),rootImagePath));
            convertAndSend(images,manifest,rootImagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convertAndSendWithKite(final Set<Path> images,final String manifest, final String path) {

        final var start = new Date();

        final var postfixPaths = ((OptionsImageConverters) watcherFileArchive
                .getWatcherOptions()).getBandsByValue(manifest);

        final var map = new HashMap<Method, Dataset>();
        images.forEach(image -> {
            final var postfixSearchArray =
                    postfixPaths.stream().filter(pf -> image.toString().toLowerCase().contains(pf)).toArray();
            if (postfixSearchArray.length > 0) {
                final var methodName = ((OptionsImageConverters) watcherFileArchive
                        .getWatcherOptions()).getBandsMethodByBandsPostfix(postfixSearchArray[0].toString())
                        .stream().findFirst().get();
                final var ds = gdal.Open(image.toAbsolutePath().toString());
                try {
                    Method method = DtoSparkImagePart.class.getDeclaredMethod(methodName, Double[].class);
                    map.put(method, ds);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });

        try(final var server = new ServerSocket(9999);) {
            final var outputStream = server.accept().getOutputStream();
            final var printer = new PrintWriter(outputStream);
//            final var printer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
//            OutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            if (map.isEmpty()) return;
            final var metaDataDs = map.entrySet().stream().findAny().orElseThrow().getValue();
            final var x = metaDataDs.getRasterXSize();
            final var y = metaDataDs.getRasterYSize();
            final var proj = metaDataDs.GetProjection();
            final var geoTransform =
                    Arrays.stream(metaDataDs.GetGeoTransform())
                            .boxed().toArray(Double[]::new);

            IntStream.range(1, y).filter(f -> f == 1 || (f - 1) % 3 == 0).forEach(rowId -> {

                IntStream.range(1, x).filter(f -> f == 1 || (f - 1) % 3 == 0).forEach(colId -> {
                    final var dto = new DtoSparkImagePart();
                    dto.setColId(colId);
                    dto.setRowId(rowId);
                    dto.setName(path);
                    dto.setWidth(x);
                    dto.setHeight(y);
                    dto.setProjection(proj);
                    dto.setGeoTransform(geoTransform);
                    map.forEach((key, value) -> {
                        double[] tempArray = new double[9];
                        if (colId + 3 < value.getRasterXSize() && rowId + 3 < value.getRasterYSize())
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
                    final var json = dto.value();
                    printer.println(json);
                });
                if ((rowId - 1) % 300 == 0){
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.printf("Дата начала: %s ||| Дата завершения: %s", start, new Date());

    }


    private void convertAndSend(final Set<Path> images,final String manifest, final String path) {
        final var start = new Date();

        final var postfixPaths = ((OptionsImageConverters) watcherFileArchive
                .getWatcherOptions()).getBandsByValue(manifest);

        final var map = new HashMap<Method, Dataset>();
        images.forEach(image -> {
            final var postfixSearchArray =
                    postfixPaths.stream().filter(pf -> image.toString().toLowerCase().contains(pf)).toArray();
            if (postfixSearchArray.length > 0) {
                final var methodName = ((OptionsImageConverters) watcherFileArchive
                        .getWatcherOptions()).getBandsMethodByBandsPostfix(postfixSearchArray[0].toString())
                        .stream().findFirst().get();
                final var ds = gdal.Open(image.toAbsolutePath().toString());
                try {
                    Method method = DtoSparkImagePart.class.getDeclaredMethod(methodName, Double[].class);
                    map.put(method, ds);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });

        try(final Socket sender = new Socket("localhost", 9999);) {
            final var outputStream = sender.getOutputStream();
            final var printer = new PrintWriter(outputStream);
//            final var printer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
//            OutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            if (map.isEmpty()) return;
            final var metaDataDs = map.entrySet().stream().findAny().orElseThrow().getValue();
            final var x = metaDataDs.getRasterXSize();
            final var y = metaDataDs.getRasterYSize();
            final var proj = metaDataDs.GetProjection();
            final var geoTransform =
                    Arrays.stream(metaDataDs.GetGeoTransform())
                            .boxed().toArray(Double[]::new);
//            final var jobsIsFulled = new AtomicBoolean(jobs.size()==12);
//            final var set = new LinkedList<DtoSparkImagePart>();
//            sender.scheduleAtFixedRate(()->{
//                final var runJobs = jobs.poll();
//                if (Objects.nonNull(runJobs)) runJobs.run();
//                }
//                if (jobsIsFulled.get()){
//                    jobs.poll().run();
//                }

//                if (jobs.size() == 12){
//                    new Thread(()->
//                        IntStream.rangeClosed(0,12).forEach(f-> Objects.requireNonNull(jobs.poll()).run());
//                    ).start();
//                }
//            ,0,5,TimeUnit.MICROSECONDS);

            IntStream.range(1, y).filter(f -> f == 1 || (f - 1) % 3 == 0).forEach(rowId -> {

                IntStream.range(1, x).filter(f -> f == 1 || (f - 1) % 3 == 0).forEach(colId -> {
                    final var dto = new DtoSparkImagePart();
                    dto.setColId(colId);
                    dto.setRowId(rowId);
                    dto.setName(path);
                    dto.setWidth(x);
                    dto.setHeight(y);
                    dto.setProjection(proj);
                    dto.setGeoTransform(geoTransform);
                    map.forEach((key, value) -> {
                        double[] tempArray = new double[9];
                        if (colId + 3 < value.getRasterXSize() && rowId + 3 < value.getRasterYSize())
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
//                    try  {
//                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                        ObjectOutputStream oos = new ObjectOutputStream(bos);
//                        oos.writeObject(dto);
//                        objectOutputStream.writeObject(dto);
//                        objectOutputStream.reset();
                        final var json = dto.value();
                        printer.println(json);
//                        printer.write(json,0, json.length());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                        set.add(dto);
                });
                if ((rowId - 1) % 600 == 0){
                    try {
                        final var sleep = 18000;
                        System.out.printf("> rowId: %s\n", rowId);
                        System.out.printf("> sleep: %s\n", sleep);
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//                    jobsIsFulled.set(jobs.size()==12);
//                    while (jobsIsFulled.get()){
//                        jobsIsFulled.set(jobs.size()!=0);
//                    }
//                    if (set.size()>5){
//                        final var sendObjects = set.toArray(DtoSparkImagePart[]::new);
//                            jobs.add(()->{
//                                try(final Socket sender = new Socket("localhost",9999);) {
//                                    OutputStream outputStream = sender.getOutputStream();
//                                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
//                                    objectOutputStream.writeObject(sendObjects);
//                                    outputStream.close();
//                                    objectOutputStream.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            });
//                        set.clear();
//                    }
            });

//            jobs.add(()->{
//                try(final Socket sender = new Socket("127.0.0.1",9999);) {
//                    OutputStream outputStream = sender.getOutputStream();
//                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
//                    objectOutputStream.writeObject(END_QUEUE);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            set.clear();
//            objectOutputStream.writeObject(END_QUEUE);
            outputStream.flush();
            outputStream.close();
//            objectOutputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.printf("Дата начала: %s ||| Дата завершения: %s", start, new Date());

    }

    private void sendDto(final DtoSparkImagePart[] dto){
        final var result = sender.submit(()->{
            try(final Socket sender = new Socket("127.0.0.1",9999);) {
                OutputStream outputStream = sender.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(dto);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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
