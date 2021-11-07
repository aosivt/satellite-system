package org.satellite.system.image.converter.parsers.landsat;

public enum OptionsMTL {

    ACQUISITION_DATE("ACQUISITION_DATE"),
    DATE_ACQUIRED("DATE_ACQUIRED"),
    CLOUD_COVER("CLOUD_COVER"),
    ROLL_ANGLE("ROLL_ANGLE"),
    DATA_TYPE("DATA_TYPE"),
    OUTPUT_FORMAT("OUTPUT_FORMAT"),
    CORNER_UL_LAT_PRODUCT("CORNER_UL_LAT_PRODUCT"),
    CORNER_UL_LON_PRODUCT("CORNER_UL_LON_PRODUCT"),
    CORNER_UR_LAT_PRODUCT("CORNER_UR_LAT_PRODUCT"),
    CORNER_UR_LON_PRODUCT("CORNER_UR_LON_PRODUCT"),
    CORNER_LL_LAT_PRODUCT("CORNER_LL_LAT_PRODUCT"),
    CORNER_LL_LON_PRODUCT("CORNER_LL_LON_PRODUCT"),
    CORNER_LR_LAT_PRODUCT("CORNER_LR_LAT_PRODUCT"),
    CORNER_LR_LON_PRODUCT("CORNER_LR_LON_PRODUCT"),

    FILE_NAME_BAND_1("FILE_NAME_BAND_1"),
    FILE_NAME_BAND_2("FILE_NAME_BAND_2"),
    FILE_NAME_BAND_3("FILE_NAME_BAND_3"),
    FILE_NAME_BAND_4("FILE_NAME_BAND_4"),
    FILE_NAME_BAND_5("FILE_NAME_BAND_5"),
    FILE_NAME_BAND_6("FILE_NAME_BAND_6"),
    FILE_NAME_BAND_7("FILE_NAME_BAND_7"),
    FILE_NAME_BAND_8("FILE_NAME_BAND_8"),
    FILE_NAME_BAND_9("FILE_NAME_BAND_9"),

    SPACECRAFT_ID("SPACECRAFT_ID");


    private final String option;

    OptionsMTL(String option) {
        this.option = option;
    }
    public String getOption(String rowFromMtlFile){
        if (!rowFromMtlFile.contains(option)) return "";

        String temp  = rowFromMtlFile.replaceAll("[-+=^:,\\s\"]","");
        temp = temp.replaceAll(option,"");
        return temp;
    }
}
