package io.opensphere.csvcommon.parse;

import java.awt.geom.Point2D;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import io.opensphere.core.common.geospatial.conversion.MGRS;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.model.LatLonAltParser;
import io.opensphere.core.util.lang.enums.EnumUtilities;
import io.opensphere.csvcommon.common.Constants;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;

/** Extracts location fields from a CSV row. */
public class CSVLocationExtractor
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CSVLocationExtractor.class);

    /** The file parameters source. */
    private final CSVParseParameters params;

    /** The WKT reader. */
    private final WKTReader myWKTReader = new WKTReader();

    /**
     * Constructor.
     *
     * @param cpp Supplier of parse parameters
     */
    public CSVLocationExtractor(CSVParseParameters cpp)
    {
        params = cpp;
    }

    /**
     * Extracts location fields.
     *
     * @param specialColumn the special column
     * @param cellValue the cell value
     * @param colName the col name
     * @param ptData the pt data
     * @param metaDataProvider the meta data provider
     * @throws CSVParseException the parse exception
     */
    public void extractLocation(SpecialColumn specialColumn, String cellValue, String colName, PointExtract ptData,
            MDILinkedMetaDataProvider metaDataProvider)
        throws CSVParseException
    {
        final ColumnType columnType = specialColumn.getColumnType();
        if (columnType == ColumnType.LAT)
        {
            CoordFormat format = EnumUtilities.fromString(LatLonAlt.CoordFormat.class, specialColumn.getFormat());
            double lat = LatLonAltParser.parseLat(cellValue, format);
            if (!Double.isNaN(lat))
            {
                ptData.setLat(Double.valueOf(lat));
                metaDataProvider.setValue(Constants.LAT, ptData.getLat());
            }
        }
        else if (columnType == ColumnType.LON)
        {
            CoordFormat format = EnumUtilities.fromString(LatLonAlt.CoordFormat.class, specialColumn.getFormat());
            double lon = LatLonAltParser.parseLon(cellValue, format);
            if (!Double.isNaN(lon))
            {
                ptData.setLon(Double.valueOf(lon));
                metaDataProvider.setValue(Constants.LON, ptData.getLon());
            }
        }
        else if (columnType == ColumnType.MGRS)
        {
            if (!params.hasType(ColumnType.LAT, ColumnType.LON))
            {
                try
                {
                    Point2D point = MGRS.computeCenterLatLon(cellValue);
                    ptData.setLat(Double.valueOf(point.getX()));
                    ptData.setLon(Double.valueOf(point.getY()));
                    metaDataProvider.setValue(Constants.LAT, ptData.getLat());
                    metaDataProvider.setValue(Constants.LON, ptData.getLon());
                }
                catch (RuntimeException e)
                {
                    LOGGER.warn(e.getMessage() + ": " + cellValue);
                }
            }
            metaDataProvider.setValue(Constants.MGRS, cellValue);
        }
        else if (columnType == ColumnType.POSITION)
        {
            if (!params.hasType(ColumnType.LAT, ColumnType.LON))
            {
                LatLonAlt point = LatLonAlt.parse(cellValue);
                if (point != null)
                {
                    ptData.setLat(Double.valueOf(point.getLatD()));
                    ptData.setLon(Double.valueOf(point.getLonD()));
                    metaDataProvider.setValue(Constants.LAT, ptData.getLat());
                    metaDataProvider.setValue(Constants.LON, ptData.getLon());
                }
                else
                {
                    LOGGER.warn("Unable to parse position: " + cellValue);
                }
            }
            metaDataProvider.setValue(colName, cellValue);
        }
        else if (columnType == ColumnType.WKT_GEOMETRY)
        {
            try
            {
                Geometry g = myWKTReader.read(cellValue);
                ptData.setWKTGeometry(g);
            }
            catch (com.vividsolutions.jts.io.ParseException e)
            {
                ptData.setWKTGeometry(null);
                throw new CSVParseException("Failed to parse WKT: " + e, e);
            }
            metaDataProvider.setValue(colName, cellValue);
        }
    }
}
