package org.satellite.system.image.converter;

import org.gdal.gdal.gdal;
import org.satellite.system.image.converter.core.OptionsImageConverters;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        gdal.AllRegister();
        try {
            init();
            final var ds = gdal.Open("/media/alex/058CFFE45C3C7827/maiskoe/2016/S2A_MSIL1C_20160106T053222_N0201_R105_T45UVA_20160106T053218.SAFE/GRANULE/L1C_T45UVA_A002819_20160106T053218/IMG_DATA/T45UVA_20160106T053222_B01.jp2");
            ds.GetGeoTransform();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void init() throws IOException{
        final var prop = new OptionsImageConverters();
        System.out.println(prop);
    }

}
