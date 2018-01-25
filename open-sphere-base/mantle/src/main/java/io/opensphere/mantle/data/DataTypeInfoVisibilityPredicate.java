package io.opensphere.mantle.data;

import java.util.function.Predicate;

/**
 * Predicate that selects data types that are visible.
 */
public class DataTypeInfoVisibilityPredicate implements Predicate<DataTypeInfo>
{
    @Override
    public boolean test(DataTypeInfo t)
    {
        return t.isVisible();
    }
}
