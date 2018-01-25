package io.opensphere.core.terrain.util;

import java.util.Collection;

import io.opensphere.core.model.GeographicPolygon;

/** An event containing the details of a change to the elevation. */
public class ElevationChangedEvent
{
    /** The participants which have been changed. */
    private final Collection<AbsoluteElevationProvider> myChangedProviders;

    /**
     * Affected regions when the affected regions are different than the
     * providers' full coverage regions. When this is null, it is assumed that
     * the providers' coverage region are the affected regions.
     */
    private final Collection<GeographicPolygon> myChangedRegions;

    /** The type of change which has occurred. */
    private final ProviderChangeType myChangeType;

    /**
     * Constructor.
     *
     * @param changedProviders The participants which have been changed.
     * @param changedRegions Affected regions when the affected regions are
     *            different than the providers' full coverage regions. When this
     *            is null, it is assumed that the providers' coverage region are
     *            the affected regions.
     * @param type The type of change which has occurred.
     */
    public ElevationChangedEvent(Collection<AbsoluteElevationProvider> changedProviders,
            Collection<GeographicPolygon> changedRegions, ProviderChangeType type)
    {
        myChangedProviders = changedProviders;
        myChangedRegions = changedRegions;
        myChangeType = type;
    }

    /**
     * Get the changedProviders.
     *
     * @return the changedProviders
     */
    public Collection<AbsoluteElevationProvider> getChangedProviders()
    {
        return myChangedProviders;
    }

    /**
     * Get the changedRegions.
     *
     * @return the changedRegions
     */
    public Collection<GeographicPolygon> getChangedRegions()
    {
        return myChangedRegions;
    }

    /**
     * Get the changeType.
     *
     * @return the changeType
     */
    public ProviderChangeType getChangeType()
    {
        return myChangeType;
    }

    /** Types of changes that can occur for providers. */
    public enum ProviderChangeType
    {
        /** A new provider has been added. */
        PROVIDER_ADDED,

        /** The priority of the providers has been changed. */
        PROVIDER_PRIORITY_CHANGED,

        /** A provider has been removed. */
        PROVIDER_REMOVED,

        /** The terrain has been modified for the existing provider. */
        TERRAIN_MODIFIED,

        ;
    }
}
