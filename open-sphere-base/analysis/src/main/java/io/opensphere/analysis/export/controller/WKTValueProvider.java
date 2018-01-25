package io.opensphere.analysis.export.controller;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTWriter;

import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.analysis.listtool.model.ListToolTableModel;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.util.jts.GeometrySupportToJTSGeometryFactory;

/**
 * Gets the WKT value for a given row within {@link ListToolTableModel} if the
 * user has indicated to add the WKT geometry column.
 */
public class WKTValueProvider
{
    /**
     * Contains the user's inputs.
     */
    private final ExportOptionsModel myExportModel;

    /**
     * Used to create the JTS geometry to pass to the wkt writer.
     */
    private final GeometryFactory myGeometryFactory = new GeometryFactory();

    /**
     * Writes a JTS geometry to wkt format.
     */
    private final WKTWriter myWriter = new WKTWriter();

    /**
     * Constructs a new WKT value provider.
     *
     * @param exportModel Contains the user's inputs.
     */
    public WKTValueProvider(ExportOptionsModel exportModel)
    {
        myExportModel = exportModel;
    }

    /**
     * Gets the WKT geometry column if necessary.
     *
     * @param element The data element to get the WKT geometry for.
     * @return the WKT geometry string
     */
    public String getWKTValue(DataElement element)
    {
        String wktGeometry = null;
        if (myExportModel.isAddWkt() && element instanceof MapDataElement)
        {
            MapGeometrySupport support = ((MapDataElement)element).getMapGeometrySupport();
            if (support != null)
            {
                Geometry g = GeometrySupportToJTSGeometryFactory.convertToJTSGeometry(support, myGeometryFactory);
                wktGeometry = myWriter.write(g);
            }
        }
        return wktGeometry;
    }
}
