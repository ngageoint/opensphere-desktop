package io.opensphere.mantle.data.geom.factory;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * Registry that maps MapGeometrySupportToGeometryConverter used for
 * transforming MapGeometrySupport classes into {@link Geometry}.
 */
public interface MapGeometrySupportConverterRegistry
{
    /**
     * Gets the converter for the specified {@link MapGeometrySupport} or null
     * if no converter is registered.
     *
     * @param mgs the map geometry support to use to get the converter
     * @return the converter the converter for that mgs.
     */
    MapGeometrySupportToGeometryConverter getConverter(MapGeometrySupport mgs);

    /**
     * Installs a converter to work with a particular class that extends
     * {@link MapGeometrySupport}. If there was previously a converter installed
     * this new converter will replace the old converter.
     *
     * @param aClass - the class to convert
     * @param converter - the converter to use for the class.
     * @return the old converter if there was one previously associated with
     *         this class.
     */
    MapGeometrySupportToGeometryConverter installConverter(Class<? extends MapGeometrySupport> aClass,
            MapGeometrySupportToGeometryConverter converter);

    /**
     * Un-install a {@link MapGeometrySupportToGeometryConverter} for the
     * specified class.
     *
     * @param aClass the a class to unregister
     * @return the map geometry support to geometry converter that was
     *         previously registered or null if none.
     */
    MapGeometrySupportToGeometryConverter uninstallConverter(Class<? extends MapGeometrySupport> aClass);
}
