package io.opensphere.geopackage.export.feature;

import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.model.DoubleRange;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.geopackage.model.ProgressModel;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.schema.TableColumnKey;
import mil.nga.sf.GeometryType;
import mil.nga.sf.proj.ProjectionConstants;

/**
 * Takes a {@link DataTypeInfo} and exports its features to a geopackage table.
 */
public class FeatureLayerExporter
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(FeatureLayerExporter.class);

    /**
     * Gets the data elements for a data type.
     */
    private final DataElementLookupUtils myDataElements;

    /**
     * Used to export a single {@link DataElement} to the geopackage.
     */
    private final FeatureRowExporter myRowExporter = new FeatureRowExporter();

    /**
     * Constructs a new {@link FeatureLayerExporter}.
     *
     * @param dataElements Used to get {@link DataElement}s for a specified
     *            {@link DataTypeInfo}.
     */
    public FeatureLayerExporter(DataElementLookupUtils dataElements)
    {
        myDataElements = dataElements;
    }

    /**
     * Exports the features of the specified dataType to the geopackage file.
     * This will create a table in the geopackage file with the same name as the
     * dataType. This table will then be populated with the data contained in
     * the {@link DataElement}s of the dataType.
     *
     * @param dataType The data type to export.
     * @param geopackage The geopackage to export to.
     * @param model Used to report progess to user.
     * @param ta Used to monitor if user has cancelled the export process.
     */
    public void exportFeatures(DataTypeInfo dataType, GeoPackage geopackage, ProgressModel model, CancellableTaskActivity ta)
    {
        GeometryColumns geometryColumns = new GeometryColumns();
        String tableName = StringUtilities.replaceSpecialCharacters(dataType.getDisplayName()).replace('-', '_');

        List<Pair<FeatureColumn, String>> columns = createColumns(dataType);
        List<String> featureTables = geopackage.getFeatureTables();
        if (!featureTables.contains(tableName))
        {
            geometryColumns.setId(new TableColumnKey(tableName, GeoPackageColumns.GEOMETRY_COLUMN));
            geometryColumns.setGeometryType(GeometryType.GEOMETRY);
            geometryColumns.setZ((byte)1);
            geometryColumns.setM((byte)0);

            BoundingBox boundingBox = new BoundingBox();

            List<FeatureColumn> featureColumns = New.list();
            for (Pair<FeatureColumn, String> pair : columns)
            {
                featureColumns.add(pair.getFirstObject());
            }

            geopackage.createFeatureTableWithMetadata(geometryColumns, boundingBox,
                    ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM, featureColumns);
        }

        List<Long> elementIds = myDataElements.getDataElementCacheIds(dataType);
        if (!elementIds.isEmpty())
        {
            try
            {
                FeatureDao dao = geopackage.getFeatureDao(tableName);
                List<DataElement> elements = myDataElements.getDataElements(elementIds, dataType, null, false);
                List<Pair<FeatureColumn, String>> columnNames = New.list();
                for (Pair<FeatureColumn, String> pair : columns)
                {
                    if (!GeoPackageColumns.GEOMETRY_COLUMN.equals(pair.getSecondObject())
                            && !GeoPackageColumns.ID_COLUMN.equals(pair.getSecondObject()))
                    {
                        columnNames.add(pair);
                    }
                }

                for (DataElement element : elements)
                {
                    if (element.getVisualizationState().isVisible())
                    {
                        myRowExporter.exportRow(element, dao, columnNames);
                        model.setCompletedCount(model.getCompletedCount() + 1);

                        if (ta.isCancelled())
                        {
                            break;
                        }
                    }
                }
                System.err.println(model.getCompletedCount());
            }
            catch (DataElementLookupException e)
            {
                LOGGER.error(e, e);
            }
        }
    }

    /**
     * Creates the geopackage columns that reflect the columns within the data
     * type.
     *
     * @param dataType The data type we are exporting.
     * @return The list of geopackage columns that match the columns in the
     *         dataType.
     */
    private List<Pair<FeatureColumn, String>> createColumns(DataTypeInfo dataType)
    {
        List<Pair<FeatureColumn, String>> columns = New.list();

        MetaDataInfo metaInfo = dataType.getMetaDataInfo();
        List<String> keys = metaInfo.getKeyNames();

        for (String columnName : keys)
        {
            if (!columnName.equals(metaInfo.getGeometryColumn()) && !"ID".equals(columnName.toUpperCase()))
            {
                GeoPackageDataType type = getDataType(columnName, metaInfo.getKeyClassType(columnName), metaInfo);
                FeatureColumn column = FeatureColumn.createColumn(columns.size(),
                        StringUtilities.replaceSpecialCharacters(columnName), type, false, null);
                columns.add(new Pair<>(column, columnName));
            }
        }

        columns.add(new Pair<>(FeatureColumn.createPrimaryKeyColumn(columns.size(), GeoPackageColumns.ID_COLUMN),
                GeoPackageColumns.ID_COLUMN));
        columns.add(new Pair<>(FeatureColumn.createGeometryColumn(columns.size(), GeoPackageColumns.GEOMETRY_COLUMN,
                GeometryType.GEOMETRY, false, null), GeoPackageColumns.GEOMETRY_COLUMN));

        return columns;
    }

    /**
     * Gets the geopackage data type for the specified column and its class
     * type.
     *
     * @param columnName The column we are getting the geopackage type for.
     * @param classType The class type of the column.
     * @param metaInfo Used to check for the time column.
     * @return The {@link GeoPackageDataType}.
     */
    private GeoPackageDataType getDataType(String columnName, Class<?> classType, MetaDataInfo metaInfo)
    {
        GeoPackageDataType dataType = GeoPackageDataType.TEXT;

        if (columnName.equals(metaInfo.getKeyForSpecialType(TimeKey.DEFAULT)))
        {
            dataType = GeoPackageDataType.DATETIME;
        }
        else if (classType.equals(Double.class) || classType.equals(DoubleRange.class))
        {
            dataType = GeoPackageDataType.DOUBLE;
        }
        else if (classType.equals(Float.class))
        {
            dataType = GeoPackageDataType.FLOAT;
        }
        else if (classType.equals(Integer.class))
        {
            dataType = GeoPackageDataType.MEDIUMINT;
        }
        else if (classType.equals(Long.class))
        {
            dataType = GeoPackageDataType.INTEGER;
        }
        else if (classType.equals(Boolean.class))
        {
            dataType = GeoPackageDataType.BOOLEAN;
        }
        else if (!classType.equals(String.class) && !classType.isAssignableFrom(DynamicEnumerationKey.class))
        {
            LOGGER.warn("Unknown data type for class " + classType + " and column " + columnName);
        }

        return dataType;
    }
}
