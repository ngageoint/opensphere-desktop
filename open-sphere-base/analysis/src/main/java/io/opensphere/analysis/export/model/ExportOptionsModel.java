package io.opensphere.analysis.export.model;

import java.util.List;
import java.util.Observable;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * The model used by the export file chooser's accessory view.
 */
public class ExportOptionsModel extends Observable
{
    /**
     * The add wkt property.
     */
    public static final String ADD_WKT_PROP = "addWkt";

    /**
     * The include meta columns property.
     */
    public static final String INCLUDE_META_COLUMNS_PROP = "includeMetaColumns";

    /**
     * The selected color format property.
     */
    public static final String SELECTED_COLOR_FORMAT_PROP = "selectedColorFormat";

    /**
     * The selected lat lon format property.
     */
    public static final String SELECTED_LAT_LON_FORMAT_PROP = "selectedLatLonFormat";

    /**
     * The selected rows only property.
     */
    public static final String SELECTED_ROWS_ONLY_PROP = "selectedRowsOnly";

    /**
     * The separate date time columns property.
     */
    public static final String SEPERATE_DATE_TIME_COLUMNS_PROP = "seperateDateTimeColumns";

    /**
     * Indicates if the user wants to add a WKT column to the export.
     */
    private boolean myAddWkt;

    /**
     * The list of available color formats for export.
     */
    private final List<ColorFormat> myColorFormats = New.unmodifiableList(ColorFormat.values());

    /**
     * Indicates if the meta columns, e.g. Color, should be included in the
     * export.
     */
    private boolean myIncludeMetaColumns;

    /**
     * The list of available latitude longitude formats for export.
     */
    private final List<LatLonFormat> myLatLonFormats = New.unmodifiableList(LatLonFormat.values());

    /**
     * The color format selected by the user.
     */
    private ColorFormat mySelectedColorFormat = ColorFormat.HEXADECIMAL;

    /**
     * The latitude longitude format selected by the user.
     */
    private LatLonFormat mySelectedLatLonFormat = LatLonFormat.DECIMAL;

    /**
     * Indicates if the user wants to export only selected rows.
     */
    private boolean mySelectedRowsOnly;

    /**
     * Indicates if the user wants to separate the Date and Time into separate
     * columns at export.
     */
    private boolean mySeparateDateTimeColumns;

    /**
     * Gets the list of available color formats for export.
     *
     * @return The list of available color formats for export.
     */
    public List<ColorFormat> getColorFormats()
    {
        return myColorFormats;
    }

    /**
     * Gets the list of available latitude longitude formats for export.
     *
     * @return The list of available latitude longitude formats for export.
     */
    public List<LatLonFormat> getLatLonFormats()
    {
        return myLatLonFormats;
    }

    /**
     * Gets the color format selected by the user.
     *
     * @return The color format selected by the user.
     */
    public ColorFormat getSelectedColorFormat()
    {
        return mySelectedColorFormat;
    }

    /**
     * Gets the latitude longitude format selected by the user.
     *
     * @return The latitude longitude format selected by the user.
     */
    public LatLonFormat getSelectedLatLonFormat()
    {
        return mySelectedLatLonFormat;
    }

    /**
     * Indicates if the user wants to add a WKT column to the export.
     *
     * @return True if the user wants to add the WKT to export, false otherwise.
     */
    public boolean isAddWkt()
    {
        return myAddWkt;
    }

    /**
     * Indicates if the meta columns, e.g. Color, should be included in the
     * export.
     *
     * @return True if the user wants to include the meta columns for export,
     *         false otherwise.
     */
    public boolean isIncludeMetaColumns()
    {
        return myIncludeMetaColumns;
    }

    /**
     * Indicates if the user wants to export only selected rows.
     *
     * @return True if we should export only selected rows, false if all rows
     *         should be exported.
     */
    public boolean isSelectedRowsOnly()
    {
        return mySelectedRowsOnly;
    }

    /**
     * Indicates if the user wants to separate the Date and Time into separate
     * columns at export.
     *
     * @return True if the users wants to separate the Date and Time into
     *         separate columns at export. false otherwise.
     */
    public boolean isSeparateDateTimeColumns()
    {
        return mySeparateDateTimeColumns;
    }

    /**
     * Sets if the user wants to add a WKT column to the export.
     *
     * @param addWkt True if it should be added, false if not.
     */
    public void setAddWkt(boolean addWkt)
    {
        boolean changed = myAddWkt != addWkt;

        if (changed)
        {
            myAddWkt = addWkt;
            setChanged();
            notifyObservers(ADD_WKT_PROP);
        }
    }

    /**
     * Sets if the user wants to include the meta columns at export. e.g. color.
     *
     * @param includeMetaColumns True if the user wants to include the meta
     *            columns, false otherwise.
     */
    public void setIncludeMetaColumns(boolean includeMetaColumns)
    {
        boolean changed = myIncludeMetaColumns != includeMetaColumns;

        if (changed)
        {
            myIncludeMetaColumns = includeMetaColumns;
            setChanged();
            notifyObservers(INCLUDE_META_COLUMNS_PROP);
        }
    }

    /**
     * Sets the color format the user wants to use at export.
     *
     * @param selectedColorFormat The selected color format.
     */
    public void setSelectedColorFormat(ColorFormat selectedColorFormat)
    {
        boolean changed = !EqualsHelper.equals(mySelectedColorFormat, selectedColorFormat);

        if (changed)
        {
            mySelectedColorFormat = selectedColorFormat;
            setChanged();
            notifyObservers(SELECTED_COLOR_FORMAT_PROP);
        }
    }

    /**
     * Sets the selected latitude/longitude format the user wants to use at
     * export.
     *
     * @param selectedLatLonFormat The selected latitude/longitude format.
     */
    public void setSelectedLatLonFormat(LatLonFormat selectedLatLonFormat)
    {
        boolean changed = !EqualsHelper.equals(mySelectedColorFormat, selectedLatLonFormat);

        if (changed)
        {
            mySelectedLatLonFormat = selectedLatLonFormat;
            setChanged();
            notifyObservers(SELECTED_LAT_LON_FORMAT_PROP);
        }
    }

    /**
     * Sets if only the selected rows should be exported.
     *
     * @param selectedRowsOnly True if we should export only selected rows,
     *            false if all rows should be exported.
     */
    public void setSelectedRowsOnly(boolean selectedRowsOnly)
    {
        boolean changed = mySelectedRowsOnly != selectedRowsOnly;

        if (changed)
        {
            mySelectedRowsOnly = selectedRowsOnly;
            setChanged();
            notifyObservers(SELECTED_ROWS_ONLY_PROP);
        }
    }

    /**
     * Sets if the user wants to seperate the Date and Time into seperate
     * columns at export.
     *
     * @param separateDateTimeColumns True if the user wants to two columns one
     *            for date and one for time, false otherwise.
     */
    public void setSeparateDateTimeColumns(boolean separateDateTimeColumns)
    {
        boolean changed = mySeparateDateTimeColumns != separateDateTimeColumns;

        if (changed)
        {
            mySeparateDateTimeColumns = separateDateTimeColumns;
            setChanged();
            notifyObservers(SEPERATE_DATE_TIME_COLUMNS_PROP);
        }
    }
}
