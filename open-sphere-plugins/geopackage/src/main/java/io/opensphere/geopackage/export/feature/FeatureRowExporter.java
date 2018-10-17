package io.opensphere.geopackage.export.feature;

import java.util.Date;
import java.util.List;

import io.opensphere.core.model.DoubleRange;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.sf.Geometry;

/**
 * Exports a {@link DataElement}s data and puts it into geopackage row.
 */
public class FeatureRowExporter
{
    /**
     * Used to convert the geometry to a geopackage geomtry.
     */
    private final GeometryExporter myGeometryExporter = new GeometryExporter();

    /**
     * Exports the specified element to the given dao.
     *
     * @param element The element to export.
     * @param dao The dao to put the new geopackage row into.
     * @param columns The columns to export.
     */
    public void exportRow(DataElement element, FeatureDao dao, List<Pair<FeatureColumn, String>> columns)
    {
        FeatureRow row = dao.newRow();

        exportGeometry(element, dao, row);

        for (Pair<FeatureColumn, String> pair : columns)
        {
            String elementColumnName = pair.getSecondObject();
            String geopackageColumnName = pair.getFirstObject().getName();
            if (pair.getFirstObject().getDataType() == GeoPackageDataType.DATETIME)
            {
                Date theDate = null;
                if (!element.getTimeSpan().isUnboundedStart())
                {
                    theDate = element.getTimeSpan().getStartDate();
                }
                else if (!element.getTimeSpan().isUnboundedEnd())
                {
                    theDate = element.getTimeSpan().getEndDate();
                }

                if (theDate != null)
                {
                    row.setValue(geopackageColumnName, DateTimeUtilities.generateISO8601DateString(theDate));
                }
            }
            else
            {
                Object value = element.getMetaData().getValue(elementColumnName);
                if (value instanceof DoubleRange)
                {
                    value = Double.valueOf(((DoubleRange)value).doubleValue());
                }
                else if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }

                row.setValue(geopackageColumnName, value);
            }
        }

        dao.insert(row);
    }

    /**
     * Exports the geometry of the element to the given row.
     *
     * @param element The element to that may or may not contain a geometry.
     * @param dao The dao the row will be inserted into.
     * @param row The row to put the geometry into.
     */
    private void exportGeometry(DataElement element, FeatureDao dao, FeatureRow row)
    {
        if (element instanceof MapDataElement)
        {
            Geometry geometry = myGeometryExporter.convertGeometry((MapDataElement)element);
            if (geometry != null)
            {
                GeoPackageGeometryData geomData = new GeoPackageGeometryData(dao.getGeometryColumns().getSrsId());
                geomData.setGeometry(geometry);
                row.setGeometry(geomData);
            }
        }
    }
}
