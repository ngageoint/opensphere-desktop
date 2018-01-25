package io.opensphere.csvcommon.detect.location.algorithm;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.csvcommon.common.WktType;
import io.opensphere.csvcommon.detect.location.algorithm.LocationMatchMaker;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.importer.config.ColumnType;

/**
 * The Class WktMatchMaker will attempt to identify a column that contains WKT
 * geometry data by matching one of the available WktTypes below.
 */
public class WktMatchMaker extends LocationMatchMaker
{
    @Override
    protected void validateValue(int index, String str, LocationResults results)
    {
        for (WktType type : WktType.values())
        {
            String toParse = str.toLowerCase();
            if (toParse.startsWith(type.toString()))
            {
                String[] wktTok = toParse.split(type.toString());
                if (wktTok.length > 1)
                {
                    toParse = wktTok[1].trim();
                    if ("(".equals(String.valueOf(toParse.charAt(0))) && toParse.endsWith(")"))
                    {
                        PotentialLocationColumn plc = createPotentialColumn(index, results, 1.0f, ColumnType.WKT_GEOMETRY);
                        if (plc != null)
                        {
                            plc.setLocationFormat(CoordFormat.WKT_GEOMETRY);
                        }
                    }
                }
            }
        }
    }
}
