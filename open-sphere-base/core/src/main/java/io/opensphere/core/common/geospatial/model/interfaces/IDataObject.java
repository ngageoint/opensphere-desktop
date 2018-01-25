package io.opensphere.core.common.geospatial.model.interfaces;

import java.awt.Color;
import java.util.Date;
import java.util.List;

/**
 * This interface was designed to help merge the DataObject hierarchy of classes
 * between the ones that exist in common and the ones that exist in the
 * jwwViewer project.
 *
 * This interface should contain: - Any common method signatures that exist in
 * both: com.bitsys.common.geospatial.model.DataObject AND
 * com.bitsys.common.geospatial.model.DataObject
 *
 * This interface should not contain: - Any methods related to rendering a
 * DataObject - Methods for converting between the different object types
 */

public interface IDataObject extends TimeRange
{

    /**
     * Returns a list of values for all of the keys
     *
     * @return
     */
    public List<?> getProperties();

    /**
     * Returns the entire list of keys
     *
     * @return
     */
    public List<String> getPropertyKeys();

    /**
     * Checks for the existence of a property
     *
     * @param key
     * @return true if it has the property, false otherwise
     */
    public boolean hasProperty(String key);

    /**
     *
     * @param key
     * @return
     */
    public Object getProperty(String key);

    /**
     *
     * @param property
     * @param value
     */
    public void setProperty(String property, Object value);

    /**
     *
     * @param property
     */
    public void removeProperty(String property);

    /**
     *
     * @return
     */
    public long getId();

    /**
     *
     * @param id
     */
    public void setId(long id);

    /**
     *
     * @return
     */
    public Color getColor();

    /**
     * Sets the color for the data object
     *
     * @param color
     */
    public void setColor(Color color);

    /**
     * Set the start date for the data object
     *
     * @param startDate
     */
    public void setStartDate(Date startDate);

    /**
     * Set the start date, same as setStartDate
     *
     * @param startDate
     */
    public void setDate(Date startDate);

    /**
     *
     * @param endDate
     */
    @Override
    public void setEndDate(Date endDate);

    /**
     *
     * @return
     */
    @Override
    public String toString();

    /**
     *
     * @return
     */
    public String getFeatureId();

    /**
     *
     * @param featureId
     */
    public void setFeatureId(String featureId);

    /**
     * Returns just the data type portion of the feature id
     *
     * @return data type
     */
    public String getDataType();

}
