package io.opensphere.core.common.geospatial.model.interfaces;

/**
 * This interface was designed to help merge the DataObject hierarchy of classes
 * between the ones that exist in common and the ones that exist in the
 * jwwViewer project.
 *
 * This interface should contain: - Any common method signatures that exist in
 * both: com.bitsys.common.geospatial.model.DataPoint AND
 * com.bitsys.common.geospatial.model.DataObject
 *
 * This interface should not contain: - Any methods related to rendering a
 * DataPoint - Methods for converting between the different object types
 *
 */
public interface IDataPoint extends IDataObject
{

    /**
     *
     * @return
     */
    public double getLat();

    /**
     *
     * @param deg
     */
    public void setLat(double deg);

    /**
     *
     * @return
     */
    public double getLon();

    /**
     *
     * @param deg
     */
    public void setLon(double deg);

    /**
     *
     * @return
     */
    public IDataPoint clone();

}
