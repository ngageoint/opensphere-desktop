package io.opensphere.myplaces.specific;

import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Provides an interface to an object that will go to the point on the map
 * defied my a data type.
 *
 */
@FunctionalInterface
public interface PointGoer
{
    /**
     * Goes to the location defined by the data type.
     *
     * @param dataType The data type containing the location.
     */
    void gotoPoint(DataTypeInfo dataType);
}
