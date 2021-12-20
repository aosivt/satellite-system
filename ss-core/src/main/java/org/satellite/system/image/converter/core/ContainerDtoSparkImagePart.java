package org.satellite.system.image.converter.core;

import java.io.Serializable;

public class ContainerDtoSparkImagePart implements Serializable {
    private String placePath;

    private DtoSparkImagePart[] dto;

    public DtoSparkImagePart[] getDto() {
        return dto;
    }

    public void setDto(DtoSparkImagePart[] dto) {
        this.dto = dto;
    }

    public String getPlacePath() {
        return placePath;
    }

    public void setPlacePath(String placePath) {
        this.placePath = placePath;
    }
}
