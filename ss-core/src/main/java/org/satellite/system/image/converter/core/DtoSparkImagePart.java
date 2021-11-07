package org.satellite.system.image.converter.core;

import java.util.List;

public class DtoSparkImagePart implements SparkImagePart{
    private Integer rowId;
    private Integer colId;
    private Integer width;
    private Integer height;
    private String projection;
    private List<Double> geoTransform;
    private List<Double> dataDeepBlue;
    private List<Double> dataBlue;
    private List<Double> dataGreen;
    private List<Double> dataRed;
    private List<Double> dataNIR;
    private List<Double> dataSWIR2;
    private List<Double> dataSWIR3;
    private List<Double> dataSWIR1;
    private List<Double> dataCirrus;
    private List<Double> dataTer;
    private List<Double> dataTIRS1;
    private List<Double> dataTIRS2;
    private List<Double> dataVCID1;
    private List<Double> dataVCID2;

    private String placePath;

    @Override
    public Integer getColId() {
        return colId;
    }

    public void setColId(Integer colId) {
        this.colId = colId;
    }

    @Override
    public Integer getRowId() {
        return rowId;
    }

    public void setRowId(Integer rowId) {
        this.rowId = rowId;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    @Override
    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    @Override
    public List<Double> getGeoTransform() {
        return geoTransform;
    }

    public void setGeoTransform(List<Double> geoTransform) {
        this.geoTransform = geoTransform;
    }

    @Override
    public List<Double> getDataDeepBlue() {
        return dataDeepBlue;
    }

    public void setDataDeepBlue(List<Double> dataDeepBlue) {
        this.dataDeepBlue = dataDeepBlue;
    }

    @Override
    public List<Double> getDataBlue() {
        return dataBlue;
    }

    @Override
    public void setDataBlue(List<Double> dataBlue) {
        this.dataBlue = dataBlue;
    }

    @Override
    public List<Double> getDataGreen() {
        return dataGreen;
    }

    @Override
    public void setDataGreen(List<Double> dataGreen) {
        this.dataGreen = dataGreen;
    }

    @Override
    public List<Double> getDataRed() {
        return dataRed;
    }

    @Override
    public void setDataRed(List<Double> dataRed) {
        this.dataRed = dataRed;
    }

    @Override
    public List<Double> getDataNIR() {
        return dataNIR;
    }

    @Override
    public void setDataNIR(List<Double> dataNIR) {
        this.dataNIR = dataNIR;
    }

    @Override
    public List<Double> getDataSWIR2() {
        return dataSWIR2;
    }

    @Override
    public void setDataSWIR2(List<Double> dataSWIR2) {
        this.dataSWIR2 = dataSWIR2;
    }

    @Override
    public List<Double> getDataSWIR3() {
        return dataSWIR3;
    }

    @Override
    public void setDataSWIR3(List<Double> dataSWIR3) {
        this.dataSWIR3 = dataSWIR3;
    }

    @Override
    public List<Double> getDataSWIR1() {
        return dataSWIR1;
    }

    @Override
    public void setDataSWIR1(List<Double> dataSWIR1) {
        this.dataSWIR1 = dataSWIR1;
    }

    @Override
    public List<Double> getDataCirrus() {
        return dataCirrus;
    }

    @Override
    public void setDataCirrus(List<Double> dataCirrus) {
        this.dataCirrus = dataCirrus;
    }

    @Override
    public List<Double> getDataTer() {
        return dataTer;
    }

    @Override
    public void setDataTer(List<Double> dataTer) {
        this.dataTer = dataTer;
    }

    @Override
    public List<Double> getDataTIRS1() {
        return dataTIRS1;
    }

    @Override
    public void setDataTIRS1(List<Double> dataTIRS1) {
        this.dataTIRS1 = dataTIRS1;
    }

    @Override
    public List<Double> getDataTIRS2() {
        return dataTIRS2;
    }

    @Override
    public void setDataTIRS2(List<Double> dataTIRS2) {
        this.dataTIRS2 = dataTIRS2;
    }

    @Override
    public List<Double> getDataVCID1() {
        return dataVCID1;
    }

    @Override
    public void setDataVCID1(List<Double> dataVCID1) {
        this.dataVCID1 = dataVCID1;
    }

    @Override
    public List<Double> getDataVCID2() {
        return dataVCID2;
    }

    @Override
    public void setDataVCID2(List<Double> dataVCID2) {
        this.dataVCID2 = dataVCID2;
    }

    @Override
    public String getPlacePath() {
        return placePath;
    }

    public void setPlacePath(String placePath) {
        this.placePath = placePath;
    }
}
