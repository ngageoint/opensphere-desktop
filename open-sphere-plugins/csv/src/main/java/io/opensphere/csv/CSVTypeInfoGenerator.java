package io.opensphere.csv;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.collections.New;
import io.opensphere.csv.config.v2.CSVDataSource;
import io.opensphere.csvcommon.ColumnInfo;
import io.opensphere.csvcommon.common.Constants;
import io.opensphere.csvcommon.common.Utilities;
import io.opensphere.csvcommon.config.v1.CSVColumnInfo;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.AltitudeKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseOrientationKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMajorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMinorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LineOfBearingKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.RadiusKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class CSVTypeInfoGenerator.
 */
@SuppressWarnings("PMD.GodClass")
public final class CSVTypeInfoGenerator
{
    /**
     * Generate type info.
     *
     * @param tb the {@link Toolbox}
     * @param fileSource the {@link CSVDataSource}
     * @param useDeterminedDataTypes the use determined data types
     * @param useDynamicEnumerations the use dynamic enumerations
     * @return the data type info
     */
    public static CSVDataTypeInfo generateTypeInfo(Toolbox tb, CSVDataSource fileSource, boolean useDeterminedDataTypes,
            boolean useDynamicEnumerations)
    {
        CSVDataTypeInfo typeInfo = new CSVDataTypeInfo(tb, fileSource, "CSV", fileSource.generateTypeKey(), "CSV",
                fileSource.getName(), false);

        // Set the metadata info
        DefaultMetaDataInfo metaDataInfo = new DefaultMetaDataInfo();
        metaDataInfo.setSpecialKeyDetector(MantleToolboxUtils.getMantleToolbox(tb).getColumnTypeDetector());
        Map<String, Class<?>> columnNameToClassMap = determineColumnClasses(tb, typeInfo.getTypeKey(), fileSource,
                fileSource.getParseParameters().getColumnNames(), useDeterminedDataTypes, useDynamicEnumerations);
        fillOutMetaDataInfo(tb, columnNameToClassMap, fileSource, typeInfo, metaDataInfo);
        typeInfo.setMetaDataInfo(metaDataInfo);

        // Set the basic visualization info
        Set<LoadsTo> supportedLoadsToTypes = metaDataInfo.hasTypeForSpecialKey(TimeKey.DEFAULT)
                ? DefaultBasicVisualizationInfo.LOADS_TO_ALL_TYPES : DefaultBasicVisualizationInfo.LOADS_TO_BASE_AND_STATIC;
        typeInfo.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(fileSource.getLayerSettings().getLoadsTo(),
                supportedLoadsToTypes, fileSource.getLayerSettings().getColor(), true));

        // Set map visualization info
        if (fileSource.getParseParameters().hasCategory(ColumnType.Category.SPATIAL))
        {
            DefaultMapFeatureVisualizationInfo mapVisInfo = new DefaultMapFeatureVisualizationInfo(
                    getVisualizationType(fileSource));
            typeInfo.setMapVisualizationInfo(mapVisInfo);
        }

        // Set the URL string
        typeInfo.setUrl(CSVDataSource.toString(fileSource.getSourceUri()));

        return typeInfo;
    }

    /**
     * Determine column classes.
     *
     * @param tb the {@link Toolbox}
     * @param dtiKey the dti key
     * @param fileSource the file source
     * @param columnNames the column names
     * @param useDeterminedDataTypes the use determined data types
     * @param useDynamicEnumerations the use dynamic enumerations
     * @return the map
     */
    private static Map<String, Class<?>> determineColumnClasses(Toolbox tb, String dtiKey, CSVDataSource fileSource,
            List<? extends String> columnNames, boolean useDeterminedDataTypes, boolean useDynamicEnumerations)
    {
        Map<String, Class<?>> columnNameToClassMap = New.map();

        List<CSVColumnInfo> colInfoList = fileSource.getParseParameters().getColumnClasses();
        TIntObjectHashMap<SpecialColumn> specialColumnMap = Utilities
                .createSpecialColumnMap(fileSource.getParseParameters().getSpecialColumns());
        String columnName = null;
        for (int i = 0; i < columnNames.size(); i++)
        {
            columnName = columnNames.get(i);
            if (colInfoList == null || colInfoList.isEmpty())
            {
                columnNameToClassMap.put(columnName, String.class);
            }
            else
            {
                Class<?> cClass;
                if (i < colInfoList.size())
                {
                    CSVColumnInfo colInfo = colInfoList.get(i);
                    try
                    {
                        cClass = Class.forName(colInfo.getClassName());
                    }
                    catch (ClassNotFoundException e)
                    {
                        cClass = String.class;
                    }
                    if (!isSpecialColumn(specialColumnMap, i) && useDynamicEnumerations && colInfo.isIsEnumCandidate()
                            && colInfo.getNumSamplesConsidered() > 1000 && colInfo.getUniqueValueCount() > 0
                            && colInfo.getUniqueValueCount() < 128)
                    {
                        getDynamicEnumerationRegistry(tb).createEnumeration(dtiKey, columnName, cClass);
                        cClass = DynamicEnumerationKey.class;
                    }
                }
                else
                {
                    cClass = String.class;
                }
                columnNameToClassMap.put(columnName, cClass == null || !useDeterminedDataTypes ? String.class : cClass);
            }
        }
        return columnNameToClassMap;
    }

    /**
     * Fills out the MetaDataInfo for the file based on the CSVDataSource.
     *
     * @param tb the {@link Toolbox}
     * @param columnNameToClassMap the column name to class map
     * @param fileSource the my file source
     * @param typeInfo the type info
     * @param metaDataInfo - the MetaDataInfo to fill out.
     */
    private static void fillOutMetaDataInfo(Toolbox tb, Map<String, Class<?>> columnNameToClassMap, CSVDataSource fileSource,
            DefaultDataTypeInfo typeInfo, DefaultMetaDataInfo metaDataInfo)
    {
        List<ColumnInfo> columnInfos = New.list();

        // Add additional columns
        if (fileSource.getParseParameters().hasType(ColumnType.DATE, ColumnType.TIME, ColumnType.TIMESTAMP))
        {
            columnInfos.add(new ColumnInfo(Constants.TIME, Date.class, TimeKey.DEFAULT));
        }
        if (fileSource.getParseParameters().hasType(ColumnType.LAT, ColumnType.LON, ColumnType.POSITION, ColumnType.MGRS))
        {
            columnInfos.add(new ColumnInfo(Constants.LAT, Double.class, LatitudeKey.DEFAULT));
            columnInfos.add(new ColumnInfo(Constants.LON, Double.class, LongitudeKey.DEFAULT));
        }

        // Add the columns in the file
        List<? extends String> columnNames = fileSource.getParseParameters().getColumnNames();
        TIntObjectHashMap<SpecialColumn> specialColumnMap = Utilities
                .createSpecialColumnMap(fileSource.getParseParameters().getSpecialColumns());
        Set<String> filteredColumns = fileSource.getColumnFilter();
        for (int i = 0; i < columnNames.size(); i++)
        {
            String columnName = columnNames.get(i);
            ColumnInfo columnInfo = getColumnInfo(columnName, columnNameToClassMap.get(columnName), specialColumnMap.get(i),
                    filteredColumns);
            if (columnInfo != null)
            {
                columnInfos.add(columnInfo);
            }
        }

        // Create the columns in the meta data
        for (ColumnInfo columnInfo : columnInfos)
        {
            metaDataInfo.addKey(columnInfo.getColumnName(), columnInfo.getColumnClass(), null);
            if (columnInfo.getSpecialType() != null)
            {
                metaDataInfo.setSpecialKey(columnInfo.getColumnName(), columnInfo.getSpecialType(), null);
            }
        }

        // Do the most important thing ever
        metaDataInfo.copyKeysToOriginalKeys();
    }

    /**
     * Creates a column info object.
     *
     * @param columnName the column name
     * @param columnClass the column class
     * @param specialColumn the special column
     * @param filteredColumns the filtered columns
     * @return The column info
     */
    private static ColumnInfo getColumnInfo(final String columnName, Class<?> columnClass, SpecialColumn specialColumn,
            Set<String> filteredColumns)
    {
        ColumnInfo columnInfo = null;
        if (!filteredColumns.contains(columnName))
        {
            columnInfo = new ColumnInfo(columnName, columnClass);
            if (specialColumn != null && specialColumn.getColumnType() != null)
            {
                switch (specialColumn.getColumnType())
                {
                    case DATE:
                    case TIME:
                    case TIMESTAMP:
                    case LAT:
                    case LON:
                        // Exclude these types because they will be included in
                        // extra columns
                        columnInfo = null;
                        break;
                    case DOWN_TIMESTAMP:
                        columnInfo.setColumnClass(Date.class);
                        break;
                    case DOWN_DATE:
                    case DOWN_TIME:
                        columnInfo.setColumnClass(String.class);
                        break;
                    case MGRS:
                        columnInfo.setColumnClass(String.class);
                        columnInfo.setColumnName(Constants.MGRS);
                        break;
                    case ALT:
                        columnInfo.setColumnClass(Double.class);
                        columnInfo.setSpecialType(AltitudeKey.DEFAULT);
                        break;
                    case SEMIMAJOR:
                    {
                        columnInfo.setColumnClass(Double.class);
                        Class<? extends Length> unit = EllipseSemiMajorAxisKey.detectUnit(columnName);
                        EllipseSemiMajorAxisKey specialType = unit != null ? new EllipseSemiMajorAxisKey(unit)
                                : EllipseSemiMajorAxisKey.DEFAULT;
                        columnInfo.setSpecialType(specialType);
                    }
                        break;
                    case SEMIMINOR:
                    {
                        columnInfo.setColumnClass(Double.class);
                        Class<? extends Length> unit = EllipseSemiMinorAxisKey.detectUnit(columnName);
                        EllipseSemiMinorAxisKey specialType = unit != null ? new EllipseSemiMinorAxisKey(unit)
                                : EllipseSemiMinorAxisKey.DEFAULT;
                        columnInfo.setSpecialType(specialType);
                    }
                        break;
                    case ORIENTATION:
                        columnInfo.setColumnClass(Double.class);
                        columnInfo.setSpecialType(EllipseOrientationKey.DEFAULT);
                        break;
                    case RADIUS:
                        columnInfo.setColumnClass(Double.class);
                        columnInfo.setSpecialType(RadiusKey.DEFAULT);
                        break;
                    case LOB:
                        columnInfo.setColumnClass(Double.class);
                        columnInfo.setSpecialType(LineOfBearingKey.DEFAULT);
                        break;
                    default:
                        break;
                }
            }
        }
        return columnInfo;
    }

    /**
     * Gets the MapVisualizationType from the data source.
     *
     * @param fileSource the data source
     * @return the MapVisualizationType
     */
    private static MapVisualizationType getVisualizationType(CSVDataSource fileSource)
    {
        MapVisualizationType geomType;
        if (fileSource.getParseParameters().hasType(ColumnType.WKT_GEOMETRY))
        {
            geomType = MapVisualizationType.MIXED_ELEMENTS;
        }
        else if (fileSource.getParseParameters().hasType(ColumnType.SEMIMAJOR, ColumnType.SEMIMINOR, ColumnType.ORIENTATION))
        {
            geomType = MapVisualizationType.ELLIPSE_ELEMENTS;
        }
        else if (fileSource.getParseParameters().hasType(ColumnType.RADIUS))
        {
            geomType = MapVisualizationType.CIRCLE_ELEMENTS;
        }
        else if (fileSource.getParseParameters().hasType(ColumnType.LOB))
        {
            geomType = MapVisualizationType.LOB_ELEMENTS;
        }
        else
        {
            geomType = MapVisualizationType.POINT_ELEMENTS;
        }
        return geomType;
    }

    /**
     * Gets the dynamic enumeration registry.
     *
     * @param tb the tb
     * @return the dynamic enumeration registry
     */
    private static DynamicEnumerationRegistry getDynamicEnumerationRegistry(Toolbox tb)
    {
        return MantleToolboxUtils.getMantleToolbox(tb).getDynamicEnumerationRegistry();
    }

    /**
     * Checks if is special column.
     *
     * @param specialColumnMap the special column map
     * @param colIndex the col index
     * @return true, if is special column
     */
    private static boolean isSpecialColumn(TIntObjectHashMap<SpecialColumn> specialColumnMap, int colIndex)
    {
        return specialColumnMap.contains(colIndex);
    }

    /** Disallow instantiation. */
    private CSVTypeInfoGenerator()
    {
    }
}
