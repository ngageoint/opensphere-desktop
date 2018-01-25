package io.opensphere.csvcommon.detect.location.format;

import java.util.List;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.model.LatLonAltParser;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.importer.config.ColumnType;

/**
 * The LocationFormatDetector will attempt to parse sample values for location
 * data and try to determine if the existing formats are valid. It will keep
 * track of the number of valid formats in the sample data and use this to
 * adjust the overall column confidence. If the column is valid but there is no
 * valid data, the confidence should be scored as a 0 and removed from the
 * result set.
 */
public class LocationFormatDetector
{
    /**
     * Detect data formats.
     *
     * @param locationResults the location results to check
     * @param rows the sample data set to check formats for
     */
    public void detectLocationColumnFormats(LocationResults locationResults, List<? extends List<? extends String>> rows)
    {
        checkLatLonFormats(locationResults, rows, locationResults.getLatLonResults());
        checkLocationFormats(locationResults, rows, locationResults.getLocationResults());
        updateLocationFormats(locationResults);
    }

    /**
     * Check location formats.
     *
     * @param locationResults the location results
     * @param rows the rows
     * @param locResults the loc results
     */
    private void checkLocationFormats(LocationResults locationResults, List<? extends List<? extends String>> rows,
            List<PotentialLocationColumn> locResults)
    {
        for (int i = locResults.size() - 1; i >= 0; i--)
        {
            int emptyCells = 0;
            int successCount = 0;
            PotentialLocationColumn potential = locResults.get(i);

            for (List<? extends String> row : rows)
            {
                String cellValue = row.get(potential.getColumnIndex());
                if (cellValue.length() > 0)
                {
                    switch (potential.getType())
                    {
                        case POSITION:
                            if ("(".equals(String.valueOf(cellValue.charAt(0))) && cellValue.endsWith(")"))
                            {
                                cellValue = cellValue.substring(1, cellValue.length() - 1).trim();
                            }
                            Pair<CoordFormat, CoordFormat> formatType = LatLonAltParser.getLatLonFormat(cellValue);
                            if (formatType != null)
                            {
                                if (formatType.getFirstObject() != null && formatType.getSecondObject() != null)
                                {
                                    successCount++;
                                }
                                potential.setLatFormat(formatType.getFirstObject());
                                potential.setLonFormat(formatType.getSecondObject());
                            }

                            break;

                        case MGRS:

                            if (LocationFormatter.validateMGRS(cellValue))
                            {
                                potential.setLocationFormat(CoordFormat.MGRS);
                                successCount++;
                            }
                            break;

                        case WKT_GEOMETRY:

                            if (LocationFormatter.validateWKTGeometry(cellValue))
                            {
                                potential.setLocationFormat(CoordFormat.WKT_GEOMETRY);
                                successCount++;
                            }
                            break;

                        case LAT:
                        case LON:
                            successCount++;
                            break;

                        default:
                            break;
                    }
                }
                else
                {
                    emptyCells++;
                }
            }

            float confidence = 0;
            float rowCount = rows.size();
            if (rowCount - emptyCells > 0)
            {
                confidence = successCount / (rowCount - emptyCells) * potential.getConfidence();
            }

            if (confidence > 0)
            {
                potential.setConfidence(confidence);
            }
            else
            {
                locationResults.removeLocationColumn(potential);
            }
        }
    }

    /**
     * Check lat lon formats.
     *
     * @param locationResults the location results
     * @param rows the rows
     * @param latLonResults the lat lon results
     */
    private void checkLatLonFormats(LocationResults locationResults, List<? extends List<? extends String>> rows,
            List<LatLonColumnResults> latLonResults)
    {
        int rowCount = rows.size();
        for (int i = latLonResults.size() - 1; i >= 0; i--)
        {
            int emptyCells = 0;
            int successCount1 = 0;
            int successCount2 = 0;
            LatLonColumnResults potential = latLonResults.get(i);

            for (List<? extends String> row : rows)
            {
                String lat = row.get(potential.getLatColumn().getColumnIndex());
                String lon = row.get(potential.getLonColumn().getColumnIndex());

                if (lat.length() > 0 && lon.length() > 0)
                {
                    Pair<CoordFormat, CoordFormat> formatType = LatLonAltParser.getLatLonFormat(lat + " " + lon);
                    if (formatType != null)
                    {
                        if (formatType.getFirstObject() != null)
                        {
                            successCount1++;
                        }
                        if (formatType.getSecondObject() != null)
                        {
                            successCount2++;
                        }

                        potential.getLatColumn().setLatFormat(formatType.getFirstObject());
                        potential.getLonColumn().setLonFormat(formatType.getSecondObject());
                    }
                }
                else
                {
                    emptyCells++;
                }
            }

            setConfidences(locationResults, rowCount, emptyCells, successCount1, successCount2, potential);
        }
    }

    /**
     * Sets the confidences.
     *
     * @param locationResults the location results
     * @param rows the number of rows
     * @param emptyCells the empty cells
     * @param successCount1 the success count1
     * @param successCount2 the success count2
     * @param potential the potential
     */
    private void setConfidences(LocationResults locationResults, int rows, int emptyCells,
            int successCount1, int successCount2, LatLonColumnResults potential)
    {
        float rowCount = rows;
        float confidence1 = (successCount1 / rowCount + potential.getConfidence()) / 2.0f;
        float confidence2 = (successCount2 / rowCount + potential.getConfidence()) / 2.0f;
        if (confidence1 > 0 && confidence2 > 0 && !Float.isInfinite(confidence1) && !Float.isInfinite(confidence2))
        {
            if (Math.abs(confidence1 - confidence2) == 0.)
            {
                potential.setConfidence(confidence1);
            }
            else
            {
                potential.setConfidence(confidence1 + confidence2 / 2.0f);
                potential.getLatColumn().setConfidence(confidence1);
                potential.getLonColumn().setConfidence(confidence2);
            }
        }
        else
        {
            locationResults.removeLatLonResult(potential);
        }
    }

    /**
     * If there are results whose formats were not detected, set them to
     * unknown. This could happen in files where all sample rows have no data.
     *
     * @param locationResults the location results
     */
    private void updateLocationFormats(LocationResults locationResults)
    {
        for (LatLonColumnResults llcr : locationResults.getLatLonResults())
        {
            if (llcr.getLatColumn().getLatFormat() == null)
            {
                llcr.getLatColumn().setLatFormat(CoordFormat.UNKNOWN);
            }
            if (llcr.getLonColumn().getLonFormat() == null)
            {
                llcr.getLonColumn().setLonFormat(CoordFormat.UNKNOWN);
            }
        }

        for (PotentialLocationColumn plc : locationResults.getLocationResults())
        {
            if (plc.getLatFormat() == null && plc.getType().equals(ColumnType.LAT))
            {
                plc.setLatFormat(CoordFormat.UNKNOWN);
            }
            if (plc.getLonFormat() == null && plc.getType().equals(ColumnType.LON))
            {
                plc.setLonFormat(CoordFormat.UNKNOWN);
            }
            if (plc.getLocationFormat() == null && (plc.getType().equals(ColumnType.LAT) || plc.getType().equals(ColumnType.LON)))
            {
                plc.setLocationFormat(CoordFormat.UNKNOWN);
            }
        }
    }
}
