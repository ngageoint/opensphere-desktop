package io.opensphere.core.datafilter.columns;

import java.util.stream.Collectors;

import io.opensphere.state.v2.ColumnMappingType;
import io.opensphere.state.v2.ColumnMappings;
import io.opensphere.state.v2.ColumnType;

/** Utilities for the ColumnMappings JAXB classes. */
final class ColumnMappingUtils
{
    /** Private constructor. */
    private ColumnMappingUtils()
    {
    }

    /**
     * Copies a ColumnMappings.
     *
     * @param source the source
     * @return the copy
     */
    public static ColumnMappings copy(ColumnMappings source)
    {
        ColumnMappings copy = new ColumnMappings();
        copy.getColumnMapping()
        .addAll(source.getColumnMapping().stream().map(ColumnMappingUtils::copy).collect(Collectors.toList()));
        return copy;
    }

    /**
     * Copies a ColumnMappingType.
     *
     * @param source the source
     * @return the copy
     */
    private static ColumnMappingType copy(ColumnMappingType source)
    {
        ColumnMappingType copy = new ColumnMappingType();
        copy.setName(source.getName());
        copy.setType(source.getType());
        copy.setDescription(source.getDescription());
        copy.getColumn().addAll(source.getColumn().stream().map(ColumnMappingUtils::copy).collect(Collectors.toList()));
        return copy;
    }

    /**
     * Copies a ColumnType.
     *
     * @param source the source
     * @return the copy
     */
    private static ColumnType copy(ColumnType source)
    {
        ColumnType copy = new ColumnType();
        copy.setLayer(source.getLayer());
        copy.setValue(source.getValue());
        return copy;
    }
}
