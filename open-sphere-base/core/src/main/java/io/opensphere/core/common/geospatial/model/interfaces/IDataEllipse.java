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
 */

public interface IDataEllipse extends IDataPoint
{
    /**
     * Set the semi-major axis
     *
     * @param val, nm
     */
    public void setSemiMajorAxis(double val);

    /**
     * Get the semi-major axis.
     *
     * @return nm
     */
    public double getSemiMajorAxis();

    /**
     * Set the semi-minor axis
     *
     * @param val, nm
     */
    public void setSemiMinorAxis(double val);

    /**
     * Get the semi-minor axis.
     *
     * @return nm
     */
    public double getSemiMinorAxis();

    /**
     * Set the orientation angle in degrees from North
     *
     * @param deg
     */
    public void setOrientation(double deg);

    /**
     * Get the orientation angle in degrees from North
     *
     * @return deg
     */
    public double getOrientation();

    /**
     *
     * @return
     */
    @Override
    public IDataEllipse clone();
}
