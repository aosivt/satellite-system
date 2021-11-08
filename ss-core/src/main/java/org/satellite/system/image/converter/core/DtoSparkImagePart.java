package org.satellite.system.image.converter.core;

import java.util.List;

public class DtoSparkImagePart implements SparkImagePart{
    private Integer rowId;
    private Integer colId;
    private Integer width;
    private Integer height;
    private String projection;
    private Double[] geoTransform;
    private Double[] dataDeepBlue;
    private Double[] dataBlue;
    private Double[] dataGreen;
    private Double[] dataRed;
    private Double[] dataNIR;
    private Double[] dataSWIR2;
    private Double[] dataSWIR3;
    private Double[] dataSWIR1;
    private Double[] dataCirrus;
    private Double[] dataTer;
    private Double[] dataTIRS1;
    private Double[] dataTIRS2;
    private Double[] dataVCID1;
    private Double[] dataVCID2;

    private String placePath;

    @Override
    public Integer getRowId() {
        return rowId;
    }

    public void setRowId(Integer rowId) {
        this.rowId = rowId;
    }

    @Override
    public Integer getColId() {
        return colId;
    }

    public void setColId(Integer colId) {
        this.colId = colId;
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
    public Double[] getGeoTransform() {
        return geoTransform;
    }

    public void setGeoTransform(Double[] geoTransform) {
        this.geoTransform = geoTransform;
    }

    @Override
    public Double[] getDataDeepBlue() {
        return dataDeepBlue;
    }

    public void setDataDeepBlue(Double[] dataDeepBlue) {
        this.dataDeepBlue = dataDeepBlue;
    }

    @Override
    public Double[] getDataBlue() {
        return dataBlue;
    }

    public void setDataBlue(Double[] dataBlue) {
        this.dataBlue = dataBlue;
    }

    @Override
    public Double[] getDataGreen() {
        return dataGreen;
    }

    public void setDataGreen(Double[] dataGreen) {
        this.dataGreen = dataGreen;
    }

    @Override
    public Double[] getDataRed() {
        return dataRed;
    }

    public void setDataRed(Double[] dataRed) {
        this.dataRed = dataRed;
    }

    @Override
    public Double[] getDataNIR() {
        return dataNIR;
    }

    public void setDataNIR(Double[] dataNIR) {
        this.dataNIR = dataNIR;
    }

    @Override
    public Double[] getDataSWIR2() {
        return dataSWIR2;
    }

    public void setDataSWIR2(Double[] dataSWIR2) {
        this.dataSWIR2 = dataSWIR2;
    }

    @Override
    public Double[] getDataSWIR3() {
        return dataSWIR3;
    }

    public void setDataSWIR3(Double[] dataSWIR3) {
        this.dataSWIR3 = dataSWIR3;
    }

    @Override
    public Double[] getDataSWIR1() {
        return dataSWIR1;
    }

    public void setDataSWIR1(Double[] dataSWIR1) {
        this.dataSWIR1 = dataSWIR1;
    }

    @Override
    public Double[] getDataCirrus() {
        return dataCirrus;
    }

    public void setDataCirrus(Double[] dataCirrus) {
        this.dataCirrus = dataCirrus;
    }

    @Override
    public Double[] getDataTer() {
        return dataTer;
    }

    public void setDataTer(Double[] dataTer) {
        this.dataTer = dataTer;
    }

    @Override
    public Double[] getDataTIRS1() {
        return dataTIRS1;
    }

    public void setDataTIRS1(Double[] dataTIRS1) {
        this.dataTIRS1 = dataTIRS1;
    }

    @Override
    public Double[] getDataTIRS2() {
        return dataTIRS2;
    }

    public void setDataTIRS2(Double[] dataTIRS2) {
        this.dataTIRS2 = dataTIRS2;
    }

    @Override
    public Double[] getDataVCID1() {
        return dataVCID1;
    }

    public void setDataVCID1(Double[] dataVCID1) {
        this.dataVCID1 = dataVCID1;
    }

    @Override
    public Double[] getDataVCID2() {
        return dataVCID2;
    }

    public void setDataVCID2(Double[] dataVCID2) {
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
