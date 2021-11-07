package org.satellite.system.image.converter.parsers.santinel.dto;

import java.util.List;

public class ContentUnit {
    public DataObjectPointer dataObjectPointer;
    public String ID;
    public String unitType;
    public List<ContentUnit> contentUnit;
    public String textInfo;
    public String pdiID;
    public String dmdID;
}
