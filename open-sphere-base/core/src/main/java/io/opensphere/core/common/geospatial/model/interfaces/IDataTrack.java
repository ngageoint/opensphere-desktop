package io.opensphere.core.common.geospatial.model.interfaces;

import java.util.Date;
import java.util.List;

/**
 * This interface was designed to help merge the DataObject hierarchy of classes
 * between the ones that exist in common and the ones that exist in the
 * jwwViewer project.
 *
 * This interface should contain: - Any common method signatures that exist in
 * both: com.bitsys.common.geospatial.model.DataTrack AND
 * com.bitsys.common.geospatial.model.DataTrack
 *
 * This interface should not contain: - Any methods related to rendering a
 * DataTrack - Methods for converting between the different object types
 */
public interface IDataTrack extends IDataPointCollection
{

    /**
     * Adds all the points of one track to this track
     *
     * @param track
     */
    public void addTrack(IDataTrack track);

    /**
     * Adds a point to the track
     *
     * @param point
     */
    @Override
    public void addPoint(IDataPoint point);

    /**
     * Returns the first point (by date) in the track
     *
     * @return the first point in the track
     */
    public IDataPoint getStartPoint();

    /**
     * Returns the last point (by date) in the track
     *
     * @return the last point in the track
     */
    public IDataPoint getEndPoint();

    /**
     * Get points surrounding this track of points by data. Gets points that
     * have times greater than the given date
     *
     * @param predictionDate Date by which to base what is surrounding or not
     * @return List of data points
     */
    public List<? extends IDataPoint> getSurroundingPoints(Date predictionDate);

    /**
     * Get the most recent n points
     *
     * @param n number of points to get
     * @return Collection of DataPoints
     */
    public List<? extends IDataPoint> getMostRecentPoints(int n);

    /**
     * Get points between two dates
     *
     * @param start
     * @param end
     * @return list of points between the given dates
     */
    public List<? extends IDataPoint> getPointsBetweenDates(Date start, Date end);

    /**
     * Get the least recent n points
     *
     * @param n The number of points to get
     * @return Collection of data points
     */
    public List<? extends IDataPoint> getLeastRecentPoints(int n);

}
