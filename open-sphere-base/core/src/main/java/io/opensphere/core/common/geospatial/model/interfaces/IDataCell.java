package io.opensphere.core.common.geospatial.model.interfaces;

import java.util.Date;
import java.util.List;

public interface IDataCell extends IDataObjectCollection
{

    public void setMaxLat(double l);

    public double getMaxLat();

    public void setMaxLon(double l);

    public double getMaxLon();

    public void setMinLat(double l);

    public double getMinLat();

    public void setMinLon(double l);

    public double getMinLon();

    /**
     * Determines of data point is with cell based on the bounding box of the
     * cell
     *
     * @param point
     * @return true if within cell/false otherwise
     */
    public boolean isWithinCell(IDataPoint point);

    public boolean isWithinCell(IDataObject gemoetry);

    public String getActivityLevel();

    public void setActivityLevel(String activityLevel);

    public String getActivityDescription();

    public void setActivityDescription(String activityDescription);

    public List<IDataPoint> getPointsBetween(Date start, Date end);

    public List<? extends IDataObject> getGeometriesBetween(Date start, Date end);

    public double[] getHeadingScoringFunction();

    public void setHeadingScoringFunction(double[] headingScoringFunction);

    public void setDataType(String dataType);

    @Override
    public String getDataType();

}
