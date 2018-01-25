package io.opensphere.mantle.data.geom.style;

import java.util.Iterator;

/**
 * The Interface FeatureCombinedGeometryBuilderData.
 *
 * Provides data for individual geometries that are built by
 * {@link FeatureCombinedGeometryBuilderData}s.
 */
public interface FeatureCombinedGeometryBuilderData extends Iterable<FeatureIndividualGeometryBuilderData>
{
    /**
     * Gets the feature count.
     *
     * @return the feature count
     */
    int getFeatureCount();

    /**
     * Iterator. Gets an iterator to the underlying builder data list.
     *
     * @return the iterator
     */
    @Override
    Iterator<FeatureIndividualGeometryBuilderData> iterator();
}
