package io.opensphere.geopackage.mantle;

/**
 * Interface to an object wanting to be notified if a geo package has been
 * removed from the system.
 */
public interface PackageDeleteListener
{
    /**
     * Called when the geo package has been removed from the system.
     */
    void packageDeleted();
}
