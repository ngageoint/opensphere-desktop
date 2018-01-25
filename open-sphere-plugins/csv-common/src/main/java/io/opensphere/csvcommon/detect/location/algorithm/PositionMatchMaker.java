package io.opensphere.csvcommon.detect.location.algorithm;

import java.util.List;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.algorithm.LocationMatchMaker;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.importer.config.ColumnType;

/**
 * The Class PositionMatchMaker will attempt to identify a column that contains
 * 2 values either surrounded by parenthesis or separated by one of the
 * separator characters in the SEP field. It will then attempt to determine if
 * the values are valid latitudes and longitudes by using the applicable match
 * maker for decimal or DMS values. Results are aggregated and returned.
 */
public class PositionMatchMaker extends LocationMatchMaker
{
    /** The Aggregate results. */
    private LocationResults myAggregateResults;

    /** The Lat dms match maker. */
    private final DMSMatchMaker myDMSMatchMaker1;

    /** The Temp dms lat location results. */
    private final LocationResults myDMSResults1;

    /** The Lon dms match maker. */
    private final DMSMatchMaker myDMSMatchMaker2;

    /** The Temp dms lon location results. */
    private final LocationResults myDMSResults2;

    /** The Decimal lat match maker. */
    private final DecimalLatLonMatchMaker myDecimalMatchMaker1;

    /** The Temp lat location results. */
    private final LocationResults myDecimalResults1;

    /** The Decimal lon match maker. */
    private final DecimalLatLonMatchMaker myDecimalMatchMaker2;

    /** The Temp lon location results. */
    private final LocationResults myDecimalResults2;

    /** The Constant ourMinDecimalConfidence. */
    private static final float ourMinDecimalConfidence = .75f;

    /** The Max confidence. */
    private float myMaxConfidence;

    /** The Constant SEP. */
    private static final String SEP = "[\\s_,:]";

    /**
     * Instantiates a new position match maker.
     */
    public PositionMatchMaker()
    {
        myDMSMatchMaker1 = new DMSMatchMaker();
        myDMSResults1 = new LocationResults();

        myDMSMatchMaker2 = new DMSMatchMaker();
        myDMSResults2 = new LocationResults();

        myDecimalMatchMaker1 = new DecimalLatLonMatchMaker();
        myDecimalResults1 = new LocationResults();

        myDecimalMatchMaker2 = new DecimalLatLonMatchMaker();
        myDecimalResults2 = new LocationResults();
    }

    @Override
    public ValuesWithConfidence<LocationResults> detect(CellSampler sampler)
    {
        myAggregateResults = new LocationResults();
        for (List<? extends String> row : sampler.getBeginningSampleCells())
        {
            for (int i = 0; i < row.size(); i++)
            {
                String posStr = row.get(i).trim();
                if (posStr.length() > 0)
                {
                    if ("(".equals(String.valueOf(posStr.charAt(0))) && posStr.endsWith(")"))
                    {
                        posStr = posStr.substring(1, posStr.length() - 1).trim();
                    }

                    String[] strTok = posStr.split(SEP);
                    if (strTok.length == 2)
                    {
                        myDMSMatchMaker1.validateValue(i, strTok[0], myDMSResults1);
                        myDMSMatchMaker2.validateValue(i, strTok[1], myDMSResults2);

                        myDecimalMatchMaker1.validateValue(i, strTok[0], myDecimalResults1);
                        myDecimalMatchMaker2.validateValue(i, strTok[1], myDecimalResults2);
                    }
                }
            }
        }

        consolidateResults(myDecimalResults1, myDecimalResults2);
        consolidateResults(myDMSResults1, myDMSResults2);

        return new ValuesWithConfidence<LocationResults>(myAggregateResults, Math.max(0f, myMaxConfidence));
    }

    /**
     * Consolidates the results for position columns.
     *
     * @param pos1Results the possible set of lat/lon results
     * @param pos2Results if pos1Results are lat, pos2Results should be lon and
     *            vice versa
     */
    private void consolidateResults(LocationResults pos1Results, LocationResults pos2Results)
    {
        if (!pos1Results.getLocationResults().isEmpty() && !pos2Results.getLocationResults().isEmpty())
        {
            int isLonLat = -1;
            for (int i = 0; i < pos1Results.getLocationResults().size(); i++)
            {
                PotentialLocationColumn latCol = pos1Results.getLocationResults().get(i);
                for (int j = 0; j < pos2Results.getLocationResults().size(); j++)
                {
                    PotentialLocationColumn lonCol = pos2Results.getLocationResults().get(j);

                    if (latCol.getColumnIndex() == lonCol.getColumnIndex())
                    {
                        // Make sure the columns have a valid lat/lon or lon/lat
                        if (latCol.getLonFormat() != null && lonCol.getLatFormat() != null)
                        {
                            isLonLat = 1;
                        }
                        if (latCol.getLatFormat() != null && lonCol.getLonFormat() != null)
                        {
                            isLonLat = 0;
                        }

                        if (isLonLat > -1
                                && (checkValidLocationPair(isLonLat, CoordFormat.DECIMAL, latCol, lonCol)
                                        || checkValidLocationPair(isLonLat, CoordFormat.DMS, latCol, lonCol))
                                && latCol.getConfidence() >= ourMinDecimalConfidence
                                && lonCol.getConfidence() >= ourMinDecimalConfidence)
                        {
                            float conf = (latCol.getConfidence() + lonCol.getConfidence()) / 2.0f;
                            if (myMaxConfidence == 0)
                            {
                                myMaxConfidence = conf;
                            }
                            else if (conf < myMaxConfidence)
                            {
                                myMaxConfidence = conf;
                            }
                            LatLonColumnResults llcr = new LatLonColumnResults(latCol, lonCol);
                            llcr.setColumnType(ColumnType.POSITION);
                            llcr.setConfidence(conf);
                            myAggregateResults.addResult(llcr);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks to see if these columns could be a valid pair of columns with
     * decimal lat and lon.
     *
     * @param isLonLat = 1 then lon/lat format, isLonLat = 0 then lat/lon format
     * @param format the location format
     * @param column1 could be either lat or lon
     * @param column2 if column1 is lat, column2 will be lon
     * @return true, if successful
     */
    private boolean checkValidLocationPair(int isLonLat, CoordFormat format, PotentialLocationColumn column1,
            PotentialLocationColumn column2)
    {
        boolean isValid = false;
        switch (format)
        {
            case DECIMAL:

                if (isLonLat == 1)
                {
                    isValid = column1.getLonFormat().equals(CoordFormat.DECIMAL)
                            && column2.getLatFormat().equals(CoordFormat.DECIMAL);
                }
                else
                {
                    isValid = column1.getLatFormat().equals(CoordFormat.DECIMAL)
                            && column2.getLonFormat().equals(CoordFormat.DECIMAL);
                }
                break;

            case DMS:

                if (isLonLat == 1)
                {
                    isValid = column1.getLonFormat().equals(CoordFormat.DMS) && column2.getLatFormat().equals(CoordFormat.DMS);
                }
                else
                {
                    isValid = column1.getLatFormat().equals(CoordFormat.DMS) && column2.getLonFormat().equals(CoordFormat.DMS);
                }

                break;

            default:
                break;
        }
        return isValid;
    }

    @Override
    protected void validateValue(int index, String str, LocationResults results)
    {
    }
}
