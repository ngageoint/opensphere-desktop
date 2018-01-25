package io.opensphere.geopackage.export.model;

import java.util.Collection;

import io.opensphere.core.export.ExportException;
import io.opensphere.mantle.data.DataTypeInfo;

/** Interface for a portion of a geo-package export. */
public interface GeoPackageSubExporter
{
    /**
     * Gets the exportable data types.
     *
     * @return the exportable data types
     */
    Collection<DataTypeInfo> getExportableTypes();

    /**
     * Gets the number of records that will be exported from the data types.
     *
     * @return the record count
     */
    int getRecordCount();

    /**
     * Exports all relevant things from the passed in data types to the
     * specified geopackage file.
     *
     * @param model the export model
     * @throws ExportException if a problem occurs while exporting
     */
    void export(ExportModel model) throws ExportException;
}
