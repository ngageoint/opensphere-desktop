package io.opensphere.csvcommon.detect.location.algorithm;

import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.importer.config.ColumnType;

/**
 * The DecimalLatLonMatchMaker will attempt to determine if a value is a decimal
 * latitude or longitude and assign a confidence.
 */
public class DecimalLatLonMatchMaker extends LatLonLocationMatcher
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DecimalLatLonMatchMaker.class);

    /** The Constant ourDecimal. */
    private static final String ourDecimal = "\\.";

    /** The Constant ourConf1. */
    private static final float ourConf1 = .75f;

    /** The Constant ourConf2. */
    private static final float ourConf2 = .9f;

    /** The Num columns. */
    private int myNumColumns = -1;

    @Override
    public ValuesWithConfidence<LocationResults> detect(CellSampler sampler)
    {
        LocationResults results = new LocationResults();
        for (List<? extends String> row : sampler.getBeginningSampleCells())
        {
            if (myNumColumns == -1)
            {
                myNumColumns = row.size();
            }
            for (int i = 0; i < row.size(); i++)
            {
                validateValue(i, row.get(i), results);
            }
        }

        consolidateResults(results, myNumColumns);
        return new ValuesWithConfidence<LocationResults>(getAggregateResults(), Math.max(0f, getMaxConfidence()));
    }

    @Override
    protected void validateValue(int index, String str, LocationResults results)
    {
        try
        {
            if (str.contains("."))
            {
                String[] locTok = str.split(ourDecimal);
                if (locTok.length == 2)
                {
                    double locValue = Double.parseDouble(str);
                    if (locValue > 90.0 && locValue <= 180.0 || locValue < -90.0 && locValue >= -180.0)
                    {
                        for (Entry<PotentialLocationColumn, Integer> entry : getPotentialColumns().entrySet())
                        {
                            if (entry.getKey().getColumnIndex() == index
                                    && entry.getKey().getColumnName().equals(ColumnType.LAT.toString())
                                    && entry.getKey().getLatFormat() != null)
                            {
                                // This is actually a longitude column, change
                                // applicable values
                                entry.getKey().setType(ColumnType.LON);
                                entry.getKey().setColumnName(ColumnType.LON.toString());
                                entry.getKey().setLatFormat(null);
                                entry.getKey().setLonFormat(CoordFormat.DECIMAL);
                            }
                        }

                        PotentialLocationColumn plc = createPotentialColumn(index, results, ourConf1, ColumnType.LON);
                        if (plc != null)
                        {
                            plc.setLonFormat(CoordFormat.DECIMAL);
                            plc.setConfidence(ourConf2);
                        }
                    }
                    else if (locValue >= 0.0 && locValue <= 90.0 || locValue < 0.0 && locValue >= -90.0)
                    {
                        PotentialLocationColumn plc = createPotentialColumn(index, results, ourConf1, ColumnType.LAT);
                        if (plc != null)
                        {
                            plc.setLatFormat(CoordFormat.DECIMAL);
                            plc.setConfidence(ourConf2);
                        }
                    }
                }
            }
        }
        catch (NumberFormatException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Error validating decimal lat/lon string.", e);
            }
        }
    }
}
