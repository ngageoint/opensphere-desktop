package io.opensphere.shapefile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord.ShapeType;
import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.util.gdal.GDALGenericUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.analysis.ColumnAnalysis;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.EllipseOrientationKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMajorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMinorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LineOfBearingKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/** Utilities for building data types. */
public final class ShapeMantleUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ShapeMantleUtilities.class);

    /**
     * Generate data type info.
     *
     * @param tb the {@link Toolbox}
     * @param source the source
     * @return the data type info
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static DataTypeInfo generateDataTypeInfo(Toolbox tb, ShapeFileSource source) throws FileNotFoundException, IOException
    {
        if (!GDALGenericUtilities.loadGDAL())
        {
            throw new IOException("Failed to load GDAL.");
        }

        String shapeFile = source.getShapeFileAbsolutePath();
        ESRIShapefile esf = ShapeFileReadUtilities.readFile(shapeFile,
                tb.getServerProviderRegistry().getProvider(HttpServer.class));

        // Create the data type info
        ShapeFileDataTypeInfo typeInfo = new ShapeFileDataTypeInfo(tb, source, "SHP", source.generateTypeKey(), "SHP",
                source.getName(), false);
        typeInfo.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(source.getLoadsTo(), source.getShapeColor(), true));
        typeInfo.setUrl(source.getPath());

        MapVisualizationType visType = MapVisualizationType.UNKNOWN;
        DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
        metaData.setSpecialKeyDetector(MantleToolboxUtils.getMantleToolbox(tb).getColumnTypeDetector());

        if (esf != null)
        {
            esf.getDbf().setFormat(ESRIShapefile.MetadataFormat.ACTUAL);

            ShapeType shapeType = ShapeType.getInstance(esf.getShapeType());
            visType = getVisualizationType(shapeType);

            List<DBFColumnInfo> metadataHeader = null;
            try
            {
                metadataHeader = esf.getMetadataHeader();
                ShapeFileReadUtilities.openPermissions(shapeFile.replace(".shp", ".dbf"));
            }
            catch (RuntimeException e)
            {
                LOGGER.error("loadShapeFile: Failed to read dbf for " + shapeFile, e);
            }

            if (esf.size() > 0)
            {
                buildMetaDataInfo(tb, source, metadataHeader, metaData, shapeType);
            }
        }

        // Update the data type info fields based off visualization type and the
        // meta data
        typeInfo.setMapVisualizationInfo(new DefaultMapFeatureVisualizationInfo(visType));
        typeInfo.setMetaDataInfo(metaData);
        typeInfo.getBasicVisualizationInfo().setSupportedLoadsToTypes(metaData.hasTypeForSpecialKey(TimeKey.DEFAULT)
                ? DefaultBasicVisualizationInfo.LOADS_TO_ALL_TYPES : DefaultBasicVisualizationInfo.LOADS_TO_BASE_AND_STATIC);

        return typeInfo;
    }

    /**
     * Adds the columns to properties.
     *
     * @param tb the toolbox
     * @param source the source
     * @param metadataHeader the metadata header
     * @param metaData the meta data
     * @param shapeType the shape type
     */
    private static void buildMetaDataInfo(Toolbox tb, ShapeFileSource source, List<DBFColumnInfo> metadataHeader,
            DefaultMetaDataInfo metaData, ShapeType shapeType)
    {
        MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(tb);
        DynamicEnumerationRegistry deReg = mtb.getDynamicEnumerationRegistry();
        String typeKey = source.generateTypeKey();

        switch (shapeType)
        {
            case POINT:
            case POINTM:
            case POINTZ:
                metaData.addKey("LAT", Double.class, null);
                metaData.setSpecialKey("LAT", LatitudeKey.DEFAULT, source);
                metaData.addKey("LON", Double.class, null);
                metaData.setSpecialKey("LON", LongitudeKey.DEFAULT, source);
                break;
            default:
                break;
        }

        if (metadataHeader != null && !metadataHeader.isEmpty())
        {
            // int i = 0;
            Collection<String> filterColumns = source.getColumnFilter();
            List<String> columnNamesFromSource = source.getColumnNames();
            Iterator<String> colNameItr = columnNamesFromSource.iterator();
            String curColName = null;
            for (DBFColumnInfo tf : metadataHeader)
            {
                curColName = colNameItr.next();
                if (tf != null && !filterColumns.contains(curColName))
                {
                    Class<?> columnClass = getColumnClass(mtb, deReg, typeKey, curColName, tf);

                    metaData.addKey(curColName, columnClass, null);
                }
            }
        }

        List<String> columnNames = source.getColumnNames();
        setSpecialKey(metaData, columnNames, source.getLobColumn(), LineOfBearingKey.DEFAULT);
        setSpecialKey(metaData, columnNames, source.getSmajColumn(), EllipseSemiMajorAxisKey.DEFAULT);
        setSpecialKey(metaData, columnNames, source.getSminColumn(), EllipseSemiMinorAxisKey.DEFAULT);
        setSpecialKey(metaData, columnNames, source.getOrientColumn(), EllipseOrientationKey.DEFAULT);

        if (source.usesTimestamp())
        {
            metaData.setSpecialKey(columnNames.get(source.getTimeColumn()), TimeKey.DEFAULT, null);
        }
        else
        {
            if (source.getDateColumn() == -1 && source.getTimeColumn() != -1)
            {
                metaData.setSpecialKey(columnNames.get(source.getTimeColumn()), TimeKey.DEFAULT, null);
            }
            else if (source.getDateColumn() != -1 && source.getTimeColumn() == -1)
            {
                metaData.setSpecialKey(columnNames.get(source.getDateColumn()), TimeKey.DEFAULT, null);
            }
        }

        metaData.copyKeysToOriginalKeys();
    }

    /**
     * Get the class for a column.
     *
     * @param mtb The mantle toolbox.
     * @param deReg The dynamic enumeration registry.
     * @param typeKey The type key.
     * @param curColName The column name.
     * @param tf The column info.
     * @return The class.
     */
    private static Class<?> getColumnClass(MantleToolbox mtb, DynamicEnumerationRegistry deReg, String typeKey, String curColName,
            DBFColumnInfo tf)
    {
        Class<?> columnClass = String.class;
        switch (tf.getType())
        {
            case DATE:
                columnClass = Date.class;
                break;
            case FLOAT:
                columnClass = Double.class;
                break;
            case LOGICAL:
                columnClass = Boolean.class;
                break;
            case NUMBER:
                columnClass = Number.class;
                break;
            case DOUBLE:
                columnClass = Double.class;
                break;
            case INTEGER:
                columnClass = Integer.class;
                break;
            default:
                break;
        }

        if (mtb.getDataAnalysisReporter().isColumnDataAnalysisEnabled())
        {
            ColumnAnalysis ca = mtb.getDataAnalysisReporter().getColumnAnalysis(typeKey, curColName);
            if (ca != null && ca.getColumnAnalyzerData() != null && ca.getColumnAnalyzerData().getTotalValuesProcessed() > 0)
            {
                if (ca.getColumnAnalyzerData().getColumnClass().getRepresentativeClass() != String.class)
                {
                    columnClass = ca.getDeterminedClass();
                }
                if (ca.isEnumCandidate())
                {
                    deReg.createEnumeration(typeKey, curColName, columnClass);
                    columnClass = DynamicEnumerationKey.class;
                }
            }
        }
        return columnClass;
    }

    /**
     * Gets the visualization type.
     *
     * @param shapeType the shape type
     * @return the visualization type
     */
    private static MapVisualizationType getVisualizationType(ShapeType shapeType)
    {
        MapVisualizationType vt = MapVisualizationType.POINT_ELEMENTS;
        switch (shapeType)
        {
            case POLYGON:
            case POLYGONM:
            case POLYGONZ:
                vt = MapVisualizationType.POLYGON_ELEMENTS;
                break;
            case POLYLINE:
            case POLYLINEM:
            case POLYLINEZ:
                vt = MapVisualizationType.POLYLINE_ELEMENTS;
                break;
            default:
                break;
        }
        return vt;
    }

    /**
     * Set the special key in the metadata if the column is positive.
     *
     * @param metaData The metadata.
     * @param columnNames The column names.
     * @param col The index into the column names.
     * @param keyType The special key type
     */
    private static void setSpecialKey(DefaultMetaDataInfo metaData, List<String> columnNames, int col, SpecialKey keyType)
    {
        if (col != -1)
        {
            metaData.setSpecialKey(columnNames.get(col), keyType, null);
        }
    }

    /** Disallow instantiation. */
    private ShapeMantleUtilities()
    {
    }
}
