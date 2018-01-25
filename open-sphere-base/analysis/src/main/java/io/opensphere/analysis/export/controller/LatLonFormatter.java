package io.opensphere.analysis.export.controller;

import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.analysis.export.model.LatLonFormat;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;

/**
 * Formats the latitude and longitude values to the format the user specified.
 */
public class LatLonFormatter
{
    /**
     * The model containing the user's inputs.
     */
    private final ExportOptionsModel myExportModel;

    /**
     * Constructs a new formatter.
     *
     * @param exportModel The model containing the user's inputs.
     */
    public LatLonFormatter(ExportOptionsModel exportModel)
    {
        myExportModel = exportModel;
    }

    /**
     * Formats the cell's value to a format the user has selected.
     *
     * @param cellValue The latitude or longitude value.
     * @param key Indicates if the cell is longitude or latitude.
     * @return The formatted cell value.
     */
    public Object format(Object cellValue, SpecialKey key)
    {
        Object value = cellValue;

        if (cellValue instanceof Double)
        {
            double decimalDegrees = ((Double)cellValue).doubleValue();
            if (LatLonFormat.DMS == myExportModel.getSelectedLatLonFormat())
            {
                value = key instanceof LatitudeKey ? LatLonAlt.latToDMSString(decimalDegrees, 3)
                        : LatLonAlt.lonToDMSString(decimalDegrees, 3);
            }
            else if (LatLonFormat.DMS_CUSTOM == myExportModel.getSelectedLatLonFormat())
            {
                value = key instanceof LatitudeKey ? LatLonAlt.latToDMSString(decimalDegrees, 0, '.', '.', ' ')
                        : LatLonAlt.lonToDMSString(decimalDegrees, 0, '.', '.', ' ');
            }
        }

        return value;
    }
}
