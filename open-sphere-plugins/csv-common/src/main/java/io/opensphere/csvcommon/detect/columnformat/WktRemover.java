package io.opensphere.csvcommon.detect.columnformat;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.WktType;

/**
 * Removes the Wkt data from a csv line, because the comma's within the wkt
 * geometry will scew our column delimiter results.
 *
 */
public class WktRemover
{
    /**
     * Removes the Wkt data from the line, if any exists.
     *
     * @param line The line to remove Wkt geometries from, if any.
     * @return The line with wkt geomtries removed if there were any to begin
     *         with.
     */
    public String removeWktData(String line)
    {
        String modifiedLine = line;

        List<String> wktTypes = New.list(WktType.POLYGON.toString().toUpperCase(), WktType.LINESTRING.toString().toUpperCase(),
                WktType.LINEARRING.toString().toUpperCase(), WktType.MULTIPOINT.toString().toUpperCase(),
                WktType.MULTILINESTRING.toString().toUpperCase(), WktType.MULTIPOLYGON.toString().toUpperCase(),
                WktType.GEOMETRYCOLLECTION.toString().toUpperCase());

        for (String wktType : wktTypes)
        {
            if (modifiedLine.toUpperCase().contains(wktType))
            {
                modifiedLine = modifiedLine.toUpperCase();

                StringBuilder regex = new StringBuilder();
                regex.append(wktType);
                regex.append("\\s*+\\(.*\\)");

                modifiedLine = modifiedLine.replaceAll(regex.toString(), wktType);
            }
        }

        return modifiedLine;
    }
}
