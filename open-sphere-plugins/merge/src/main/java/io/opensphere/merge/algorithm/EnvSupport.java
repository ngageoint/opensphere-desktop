package io.opensphere.merge.algorithm;

import java.util.List;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;

/**
 * This interface encapsulates the support required for performing merge and
 * join operations. The OpenSphere environment provides these services, but this
 * abstraction facilitates creating and executing test cases without standing up
 * the entire system.
 */
public interface EnvSupport
{
    /**
     * Get a List of the DataElements for the specified type.
     *
     * @param type the layer for which records are retrieved.
     * @return the set of records that correspond with the supplied layer.
     */
    List<DataElement> getRecords(DataTypeInfo type);

    /**
     * Check whether the specified columns (usually from different types) should
     * be treated as being alike.
     *
     * @param column1 the left hand side of the comparison.
     * @param column2 the right hand side of the comparison.
     * @return The matched column name or null if they do not match.
     */
    String columnMatch(Col column1, Col column2);
}
