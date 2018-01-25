package io.opensphere.mantle.data.filter;

import java.util.Collection;
import java.util.function.Predicate;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * DataLayerFilter.
 */
public class DataLayerFilter implements Predicate<DataGroupInfo>
{
    /** Predicate that determines if a data type is filterable. */
    public static final Predicate<DataTypeInfo> DATA_TYPE_FILTERABLE = new Predicate<DataTypeInfo>()
    {
        @Override
        public boolean test(DataTypeInfo dataType)
        {
            boolean isTimelineOrStaticMapElement = dataType.getBasicVisualizationInfo() != null
                    && dataType.getBasicVisualizationInfo().getLoadsTo().isAnalysisEnabled()
                    && dataType.getMapVisualizationInfo() != null
                    && dataType.getMapVisualizationInfo().getVisualizationType().isMapDataElementType();
            boolean usesDataElements = dataType.getBasicVisualizationInfo() != null
                    && dataType.getBasicVisualizationInfo().usesDataElements();
            return dataType.isFilterable() && (isTimelineOrStaticMapElement || usesDataElements);
        }
    };

    /**
     * Gets the filterable data types from the given data group.
     *
     * @param dataGroup the data group
     * @return the filterable data types
     */
    public static Collection<DataTypeInfo> getFilterableDataTypes(DataGroupInfo dataGroup)
    {
        return dataGroup.findMembers(DATA_TYPE_FILTERABLE, false, false);
    }

    /**
     * Checks to see if this DataGroupInfo has any active load filters
     * (non-recursive).
     *
     * @param tb the {@link Toolbox}
     * @param dataGroup the DataGroupInfo to check.
     * @return true, if there is an active filter
     */
    public static boolean hasActiveLoadFilter(final Toolbox tb, DataGroupInfo dataGroup)
    {
        Predicate<DataTypeInfo> activePredicate = dataType -> hasActiveLoadFilter(tb, dataType);
        return !dataGroup.findMembers(activePredicate, false, true).isEmpty();
    }

    /**
     * Checks to see if this {@link DataTypeInfo} has an active load filter.
     *
     * @param tb the {@link Toolbox}
     * @param dataType the DataTypeInfo to check.
     * @return true, if there is an active filter
     */
    public static boolean hasActiveLoadFilter(Toolbox tb, DataTypeInfo dataType)
    {
        return DATA_TYPE_FILTERABLE.test(dataType) && (tb.getDataFilterRegistry().hasLoadFilter(dataType.getTypeKey())
                || tb.getDataFilterRegistry().hasSpatialLoadFilter(dataType.getTypeKey()));
    }

    @Override
    public boolean test(DataGroupInfo dataGroup)
    {
        return !dataGroup.findMembers(DATA_TYPE_FILTERABLE, false, true).isEmpty();
    }
}
