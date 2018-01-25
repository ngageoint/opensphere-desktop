package io.opensphere.core.pipeline.processor;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Immutable version of {@link ProcessorDistributionKey}.
 */
public class ImmutableProcessorDistributionKey extends ProcessorDistributionKey
{
    /**
     * Constructor.
     *
     * @param geom The geometries for which this key is valid.
     * @param constraintKey The constraint key for the geometries, which may be
     *            {@code null}.
     * @param timeSpan The time over which this key is valid.
     */
    public ImmutableProcessorDistributionKey(Geometry geom, Object constraintKey, TimeSpan timeSpan)
    {
        super(geom, constraintKey, timeSpan);
    }

    @Override
    @SuppressWarnings("PMD.UselessOverridingMethod")
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    @Override
    @SuppressWarnings("PMD.UselessOverridingMethod")
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public void set(Geometry geom, Object constraintKey, TimeSpan timeSpan)
    {
        throw new UnsupportedOperationException();
    }
}
