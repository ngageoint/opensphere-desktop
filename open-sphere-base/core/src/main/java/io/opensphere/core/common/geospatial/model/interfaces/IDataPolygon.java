package io.opensphere.core.common.geospatial.model.interfaces;

public interface IDataPolygon extends IDataPointCollection
{

    public IDataPolygon getMinimumBoundingRectangleAsPolygon();

    public IDataPolygon getSimpleConvexHull();
}
