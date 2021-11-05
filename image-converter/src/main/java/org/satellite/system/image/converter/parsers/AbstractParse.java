package org.satellite.system.image.converter.parsers;

//import aos.Entities.CatalogSatelliteImage;
//import aos.Entities.FileFormat;
//import aos.Entities.Repository.DirectoriesInterfaces.Directories;
//import aos.Entities.Repository.Util.HibernateUtil;
//import aos.Entities.Repository.Util.UpdaterDirectoriesTables;
//import aos.Entities.Satellite;
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.GeometryFactory;
//import com.vividsolutions.jts.geom.Polygon;
//
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbstractParse implements Parser {
//    protected CatalogSatelliteImage catalogSatelliteImage = new CatalogSatelliteImage();
//
//    protected final static List<Directories> fileFormats = UpdaterDirectoriesTables.select("FileFormat");
//    protected final static List<Directories> satellites = UpdaterDirectoriesTables.select("Satellite");
    protected Map<String, String> nameBands = new HashMap<>();

//    protected Coordinate[] pGpoints = new Coordinate[5];
//    {
//        pGpoints[0] = new Coordinate();
//        pGpoints[1] = new Coordinate();
//        pGpoints[2] = new Coordinate();
//        pGpoints[3] = new Coordinate();
//        pGpoints[4] = pGpoints[0];
//    }

    public void save(){
//        setPolygonSatellite();
        setDescription();
//        catalogSatelliteImage.setBandNumber(Integer.toUnsignedLong(nameBands.size()));
//        UpdaterDirectoriesTables.update(catalogSatelliteImage);
    }

    public static List<String> readFileInList(String fileName) {
        List<String> lines = Collections.emptyList();
        try{
            lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }


//    public void setPolygonSatellite(){
//
//         Create a GeometryFactory if you don't have one already
//        GeometryFactory geometryFactory = new GeometryFactory();
//         Simply pass an array of Coordinate or a CoordinateSequence to its method
//        Polygon polygonFromCoordinates = geometryFactory.createPolygon(pGpoints);
//        catalogSatelliteImage.setGeom(polygonFromCoordinates);
//    }

    public DateFormat getFormatDate(String format){
        return new SimpleDateFormat(format, Locale.ENGLISH);
    }

    public abstract Date getDateFromString(String stringDate);

    public Date getDateFromString(String format, String stringDate) throws ParseException {
        return getFormatDate(format).parse(stringDate);
    }

    public abstract void setDescription();
    public abstract void setFile(String nameFile);

//    public static Satellite getSatellite(String title){
//        return (Satellite)satellites.parallelStream()
//                .filter(f->title.toLowerCase().equals(f.getDirName().toLowerCase()))
//                .reduce((r1,r2)->r1)
//                .orElse(new Satellite());
//    }
//    public static FileFormat getFileFormatByTittle(String title){
//        FileFormat fileFormat = (FileFormat) fileFormats.parallelStream()
//                .filter(f->title.toLowerCase().equals(f.getDirName().toLowerCase()))
//                    .findFirst()
//                    .orElse(new FileFormat(title,title.toLowerCase()));
//                .reduce((r1,r2)-> r1)
//                .orElse(new FileFormat(title,title.toLowerCase()));
//        return fileFormat;
//    }
//    public CatalogSatelliteImage getCatalogSatelliteImage(){
//        return catalogSatelliteImage;
//    }
}
