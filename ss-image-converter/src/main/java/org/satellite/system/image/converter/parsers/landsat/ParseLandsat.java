package org.satellite.system.image.converter.parsers.landsat;
import org.satellite.system.image.converter.parsers.AbstractParse;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

public class ParseLandsat extends AbstractParse {

    public static final String DATE_FORMAT = "yyyy-mm-dd";

    public ParseLandsat(String nameMTLFile){

        setFile(nameMTLFile);

        readFileInList(nameMTLFile)
                .parallelStream()
                .forEach(this::setCatalogSatelliteImage);


    }
    private void setCatalogSatelliteImage(String rowFromMtlFile){
        Arrays.asList(OptionsMTL.values())
                .parallelStream()
                .forEach(optionsMTL -> setField(optionsMTL,rowFromMtlFile));

    }
    private void setField(OptionsMTL optionsMTL, String value){
    if (optionsMTL.getOption(value).isEmpty()) return;

    switch (optionsMTL){
//            case DATA_TYPE:             catalogSatelliteImage.setProcessingLevel(optionsMTL.getOption(value));
//                                        break;
//            case ROLL_ANGLE:            catalogSatelliteImage.setAngle(Float.parseFloat(optionsMTL.getOption(value)));
//                                        break;
//            case CLOUD_COVER:           catalogSatelliteImage.setCloudCover(optionsMTL.getOption(value));
//                                        break;
//            case OUTPUT_FORMAT:         catalogSatelliteImage.setFileFormat(getFileFormatByTittle(optionsMTL.getOption(value)));
//                                        break;
//            case ACQUISITION_DATE:      catalogSatelliteImage.setSurveyDate(getDateFromString(optionsMTL.getOption(value)));
//                                        break;
//            case DATE_ACQUIRED:         catalogSatelliteImage.setSurveyDate(getDateFromString(optionsMTL.getOption(value)));
//                                        break;
//            case SPACECRAFT_ID:         catalogSatelliteImage.setSatellite(getSatellite(optionsMTL.getOption(value)));
//                                        break;
//
//            case CORNER_LR_LAT_PRODUCT: pGpoints[0].y = Double.parseDouble(optionsMTL.getOption(value));
//                break;
//            case CORNER_LR_LON_PRODUCT: pGpoints[0].x = Double.parseDouble(optionsMTL.getOption(value));
//                break;
//            case CORNER_UR_LAT_PRODUCT: pGpoints[1].y = Double.parseDouble(optionsMTL.getOption(value));
//                break;
//            case CORNER_UR_LON_PRODUCT: pGpoints[1].x = Double.parseDouble(optionsMTL.getOption(value));
//                break;
//            case CORNER_UL_LAT_PRODUCT: pGpoints[2].y = Double.parseDouble(optionsMTL.getOption(value));
//                break;
//            case CORNER_UL_LON_PRODUCT: pGpoints[2].x = Double.parseDouble(optionsMTL.getOption(value));
//                break;
//            case CORNER_LL_LAT_PRODUCT: pGpoints[3].y = Double.parseDouble(optionsMTL.getOption(value));
//                                        break;
//            case CORNER_LL_LON_PRODUCT: pGpoints[3].x = Double.parseDouble(optionsMTL.getOption(value));
//                                        break;

            case FILE_NAME_BAND_1:      nameBands.put(optionsMTL.name(),optionsMTL.getOption(value));
                                        break;
            case FILE_NAME_BAND_2:      nameBands.put(optionsMTL.name(),optionsMTL.getOption(value));
                                        break;
            case FILE_NAME_BAND_3:      nameBands.put(optionsMTL.name(),optionsMTL.getOption(value));
                                        break;
            case FILE_NAME_BAND_4:      nameBands.put(optionsMTL.name(),optionsMTL.getOption(value));
                                        break;
            case FILE_NAME_BAND_5:      nameBands.put(optionsMTL.name(),optionsMTL.getOption(value));
                                        break;
            case FILE_NAME_BAND_6:      nameBands.put(optionsMTL.name(),optionsMTL.getOption(value));
                                        break;
            case FILE_NAME_BAND_7:      nameBands.put(optionsMTL.name(),optionsMTL.getOption(value));
                                        break;
            case FILE_NAME_BAND_8:      nameBands.put(optionsMTL.name(),optionsMTL.getOption(value));
                                        break;
            case FILE_NAME_BAND_9:      nameBands.put(optionsMTL.name(),optionsMTL.getOption(value));
                                        break;
        }
    }


    @Override
    public Date getDateFromString(String stringDate) {
        try {
            return getDateFromString(this.DATE_FORMAT,stringDate);
        } catch (ParseException e ){

        }
        return new Date();
    }

    @Override
    public void setDescription() {

    }

    @Override
    public void setFile(String nameFile) {

    }

}
