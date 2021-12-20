package org.satellite.system.image.converter.core;

import java.io.Serializable;
import java.util.List;

 interface SparkImagePart extends Serializable {
    Integer getRowId();
    Integer getColId();
    Double[] getGeoTransform();
    String getProjection();

    Double[] getDataDeepBlue();
    void setDataDeepBlue(Double[] value);
    Double[] getDataBlue();
    void setDataBlue(Double[] value);
    Double[] getDataGreen();
    void setDataGreen(Double[] value);
    Double[] getDataRed();
    void setDataRed(Double[] value);
    Double[] getDataNIR();
    void setDataNIR(Double[] value);
    Double[] getDataSWIR2();
    void setDataSWIR2(Double[] value);
    Double[] getDataSWIR3();
    void setDataSWIR3(Double[] value);
    Double[] getDataSWIR1();
    void setDataSWIR1(Double[] value);
    Double[] getDataCirrus();
    void setDataCirrus(Double[] value);
    Double[] getDataTer();
    void setDataTer(Double[] value);
    Double[] getDataTIRS1();
    void setDataTIRS1(Double[] value);
    Double[] getDataTIRS2();
    void setDataTIRS2(Double[] value);
    Double[] getDataVCID1();
    void setDataVCID1(Double[] value);
    Double[] getDataVCID2();
    void setDataVCID2(Double[] value);

    String getName();

    void setName(String name);



}
