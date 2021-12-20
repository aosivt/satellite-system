package org.satellite.system.image.converter.core;

import java.util.Arrays;
import java.util.List;

public class DtoSparkImagePart implements SparkImagePart{
    private Integer lengthObject = 0;
    private Integer rowId = -1;
    private Integer colId = -1;
    private Integer width = -1;
    private Integer height = -1;
    private String projection = "";
    private Double[] geoTransform = new Double[]{};
    private Double[] dataDeepBlue= new Double[]{};
    private Double[] dataBlue= new Double[]{};
    private Double[] dataGreen= new Double[]{};
    private Double[] dataRed= new Double[]{};
    private Double[] dataNIR= new Double[]{};
    private Double[] dataSWIR2= new Double[]{};
    private Double[] dataSWIR3= new Double[]{};
    private Double[] dataSWIR1= new Double[]{};
    private Double[] dataCirrus= new Double[]{};
    private Double[] dataTer= new Double[]{};
    private Double[] dataTIRS1= new Double[]{};
    private Double[] dataTIRS2= new Double[]{};
    private Double[] dataVCID1= new Double[]{};
    private Double[] dataVCID2= new Double[]{};
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String value(){
        return String.format("{" +
                "\"rowId\":%s," +
                "\"colId\":%s," +
                "\"width\":%s," +
                "\"height\":%s," +
                "\"projection\":\"%s\"," +
                "\"geoTransform\":%s," +
                "\"dataDeepBlue\":%s," +
                "\"dataBlue\":%s," +
                "\"dataGreen\":%s," +
                "\"dataRed\":%s," +
                "\"dataNIR\":%s," +
                "\"dataSWIR2\":%s," +
                "\"dataSWIR3\":%s," +
                "\"dataSWIR1\":%s," +
                "\"dataCirrus\":%s," +
                "\"dataTer\":%s," +
                "\"dataTIRS1\":%s," +
                "\"dataTIRS2\":%s," +
                "\"dataVCID1\":%s," +
                "\"dataVCID2\":%s," +
                "\"name\":\"%s\"" +
                "}",
                rowId,
                colId,
                width,
                height,
                projection.replace("\"","'"),
                Arrays.toString(geoTransform),
                Arrays.toString(dataDeepBlue),
                Arrays.toString(dataBlue),
                Arrays.toString(dataGreen),
                Arrays.toString(dataRed),
                Arrays.toString(dataNIR),
                Arrays.toString(dataSWIR2),
                Arrays.toString(dataSWIR3),
                Arrays.toString(dataSWIR1),
                Arrays.toString(dataCirrus),
                Arrays.toString(dataTer),
                Arrays.toString(dataTIRS1),
                Arrays.toString(dataTIRS2),
                Arrays.toString(dataVCID1),
                Arrays.toString(dataVCID2),
                name
                );
    }

}
