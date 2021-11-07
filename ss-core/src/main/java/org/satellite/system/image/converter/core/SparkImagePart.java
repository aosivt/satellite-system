package org.satellite.system.image.converter.core;

import java.io.Serializable;
import java.util.List;

 interface SparkImagePart extends Serializable {
    Integer getRowId();
    Integer getColId();
    List<Double> getGeoTransform();
    String getProjection();
    String getPlacePath();
    
    List<Double> getDataDeepBlue();
    void setDataDeepBlue(List<Double> value);
    List<Double> getDataBlue();
    void setDataBlue(List<Double> value);
    List<Double> getDataGreen();
    void setDataGreen(List<Double> value);
    List<Double> getDataRed();
    void setDataRed(List<Double> value);
    List<Double> getDataNIR();
    void setDataNIR(List<Double> value);
    List<Double> getDataSWIR2();
    void setDataSWIR2(List<Double> value);
    List<Double> getDataSWIR3();
    void setDataSWIR3(List<Double> value);
    List<Double> getDataSWIR1();
    void setDataSWIR1(List<Double> value);
    List<Double> getDataCirrus();
    void setDataCirrus(List<Double> value);
    List<Double> getDataTer();
    void setDataTer(List<Double> value);
    List<Double> getDataTIRS1();
    void setDataTIRS1(List<Double> value);
    List<Double> getDataTIRS2();
    void setDataTIRS2(List<Double> value);
    List<Double> getDataVCID1();
    void setDataVCID1(List<Double> value);
    List<Double> getDataVCID2();
    void setDataVCID2(List<Double> value);
}
