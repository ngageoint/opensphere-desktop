package io.opensphere.core.common.shapefile.utils;

import io.opensphere.core.common.shapefile.shapes.ShapeRecord;

public class ShapefileRecord
{
    public ShapeRecord shape;

    public Object[] metadata;

    public ShapefileRecord(ShapeRecord sr, Object[] md)
    {
        shape = sr;
        metadata = md;
    }

}
