package io.opensphere.core.common.geospatial.model.interfaces;

import java.util.List;

/**
 * This interface was designed to help merge the DataObject hierarchy of classes
 * between the ones that exist in common and the ones that exist in the
 * jwwViewer project.
 *
 * This interface should contain: - Any common method signatures that exist in
 * both: com.bitsys.common.geospatial.model.DataPoint AND
 * com.bitsys.common.geospatial.model.DataPoint
 *
 * This interface should not contain: - Any methods related to rendering a
 * DataPoint - Methods for converting between the different object types
 *
 */
public interface IDataPointCollection extends IDataObject
{

    /**
     *
     * @return
     */
    public List<? extends IDataPoint> getPoints();

    /**
     *
     * @param point
     */
    public void addPoint(IDataPoint point);

    /**
     *
     * @param points
     */
    public void addPoints(List<? extends IDataPoint> points);

    /**
     *
     * @return Four points {min lon, min lat, max lon, max lat}
     */
    public IDataPoint[] getMinimumBoundingRectangle();

}
