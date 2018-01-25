package io.opensphere.geopackage.export.model;

import java.io.File;
import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.progress.ProgressReporter;
import io.opensphere.mantle.data.DataTypeInfo;
import mil.nga.geopackage.GeoPackage;

/**
 * Contains information used by the export classes during an export, such as the
 * file to export to as well as which layers to export.
 */
public class ExportModel
{
    /**
     * The file to export to.
     */
    private final File myExportFile;

    /** The geo package. */
    private volatile GeoPackage myGeoPackage;

    /**
     * The layers to export.
     */
    private final List<DataTypeInfo> myDataTypesToExport = New.list();

    /**
     * The max tile zoom level.
     */
    private volatile int myMaxZoomLevel;

    /**
     * The total number of things to export.
     */
    private volatile int myTotalCountToExport;

    /** The progress reporter. */
    private volatile ProgressReporter myProgressReporter;

    /**
     * Constructor.
     *
     * @param exportFile The export file.
     */
    public ExportModel(File exportFile)
    {
        myExportFile = exportFile;
    }

    /**
     * Gets the geo package.
     *
     * @return the geo package
     */
    public GeoPackage getGeoPackage()
    {
        return myGeoPackage;
    }

    /**
     * Sets the geo package.
     *
     * @param geoPackage the geo package
     */
    public void setGeoPackage(GeoPackage geoPackage)
    {
        myGeoPackage = geoPackage;
    }

    /**
     * Gets the data types to export to.
     *
     * @return The list of data types.
     */
    public List<DataTypeInfo> getDataTypesToExport()
    {
        return myDataTypesToExport;
    }

    /**
     * Gets the file to export to.
     *
     * @return The file to export.
     */
    public File getExportFile()
    {
        return myExportFile;
    }

    /**
     * Gets the total number of things to export.
     *
     * @return The total number of things to export.
     */
    public int getTotalCountToExport()
    {
        return myTotalCountToExport;
    }

    /**
     * Sets the total number of things to export.
     *
     * @param totalCountToExport The total number of things to export.
     */
    public void setTotalCountToExport(int totalCountToExport)
    {
        myTotalCountToExport = totalCountToExport;
    }

    /**
     * Gets the maxZoomLevel.
     *
     * @return the maxZoomLevel
     */
    public int getMaxZoomLevel()
    {
        return myMaxZoomLevel;
    }

    /**
     * Sets the maxZoomLevel.
     *
     * @param maxZoomLevel the maxZoomLevel
     */
    public void setMaxZoomLevel(int maxZoomLevel)
    {
        myMaxZoomLevel = maxZoomLevel;
    }

    /**
     * Gets the progress reporter.
     *
     * @return the progress reporter
     */
    public ProgressReporter getProgressReporter()
    {
        return myProgressReporter;
    }

    /**
     * Sets the progress reporter.
     *
     * @param progressReporter the progress reporter
     */
    public void setProgressReporter(ProgressReporter progressReporter)
    {
        myProgressReporter = progressReporter;
    }
}
