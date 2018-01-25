package io.opensphere.csvcommon.detect.location.algorithm.decider;

import java.util.List;

import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;

/**
 * The Interface LocationDecider.
 */
public interface LocationDecider
{
    /**
     * Types of string comparisons for colum names.
     */
    enum CompareType
    {
        /** String that are equal. */
        EQUALS,

        /** Strings that start with another string. */
        STARTS_WITH,

        /** Strings that end with another string. */
        ENDS_WITH,

        /** Strings that contain another string. */
        CONTAINS
    }

    /**
     * Determines if location columns are recognized in a header.
     *
     * @param sampler the sampler
     * @return the location results
     */
    LocationResults determineLocationColumns(CellSampler sampler);

    /**
     * Creates a potential column based on several parameters.
     *
     * @param cType the string comparison type.
     * @param cells the set of header names
     * @param colName the name of a particular column
     * @param knownName one of the known lat/lon names
     * @return the potential location column
     */
    PotentialLocationColumn createPotentialColumn(CompareType cType, List<String> cells, String colName, String knownName);
}
