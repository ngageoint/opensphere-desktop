package io.opensphere.mantle.data.geom.factory.impl;

import java.util.HashMap;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.mantle.data.geom.MapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.MapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapIconGeometrySupport;
import io.opensphere.mantle.data.geom.MapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.MapPointGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.factory.MapGeometrySupportConverterRegistry;
import io.opensphere.mantle.data.geom.factory.MapGeometrySupportToGeometryConverter;
import io.opensphere.mantle.data.geom.impl.DefaultMapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapIconGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapIconGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPolylineGeometrySupport;

/**
 * Registry that maps MapGeometrySupportToGeometryConverter used for
 * transforming MapGeometrySupport classes into {@link Geometry}.
 */
public class MapGeometrySupportConverterRegistryImpl implements MapGeometrySupportConverterRegistry
{
    /** The convert to class map map. */
    private final Map<Class<? extends MapGeometrySupport>, MapGeometrySupportToGeometryConverter> myMGStoConverterMap;

    /**
     * Search up the interface hierarchy for interfaces that extend from
     * MapGeometrySupport.
     *
     * @param aClass the a class to search from
     * @return the super mgs class the super interface or null if none found
     *         that extend from MapGeometrySupport.
     */
    private static Class<?> getSuperMGSClass(Class<?> aClass)
    {
        Class<?>[] interfaces = aClass.getInterfaces();
        for (Class<?> intF : interfaces)
        {
            if (MapGeometrySupport.class.isAssignableFrom(intF))
            {
                return intF;
            }
        }
        return null;
    }

    /**
     * Instantiates a new map geometry support converter registry.
     *
     * @param tb the tb
     */
    public MapGeometrySupportConverterRegistryImpl(Toolbox tb)
    {
        myMGStoConverterMap = new HashMap<>();

        // Install default converters, both for our basic interfaces and the
        // default implementations
        // to speed lookup.
        myMGStoConverterMap.put(MapEllipseGeometrySupport.class, new MapEllipseGeometryConverter(tb));
        myMGStoConverterMap.put(DefaultMapEllipseGeometrySupport.class, new MapEllipseGeometryConverter(tb));
        myMGStoConverterMap.put(SimpleMapEllipseGeometrySupport.class, new MapEllipseGeometryConverter(tb));

        myMGStoConverterMap.put(MapCircleGeometrySupport.class, new MapCircleGeometryConverter(tb));
        myMGStoConverterMap.put(DefaultMapCircleGeometrySupport.class, new MapCircleGeometryConverter(tb));
        myMGStoConverterMap.put(SimpleMapCircleGeometrySupport.class, new MapCircleGeometryConverter(tb));

        myMGStoConverterMap.put(MapLineOfBearingGeometrySupport.class, new MapLineOfBearingGeometryConverter(tb));
        myMGStoConverterMap.put(DefaultMapLineOfBearingGeometrySupport.class, new MapLineOfBearingGeometryConverter(tb));
        myMGStoConverterMap.put(SimpleMapLineOfBearingGeometrySupport.class, new MapLineOfBearingGeometryConverter(tb));
        myMGStoConverterMap.put(MapPointGeometrySupport.class, new MapPointGeometryConverter(tb));
        myMGStoConverterMap.put(DefaultMapPointGeometrySupport.class, new MapPointGeometryConverter(tb));
        myMGStoConverterMap.put(SimpleMapPointGeometrySupport.class, new MapPointGeometryConverter(tb));
        myMGStoConverterMap.put(MapPolygonGeometrySupport.class, new MapPolygonGeometryConverter(tb));
        myMGStoConverterMap.put(DefaultMapPolygonGeometrySupport.class, new MapPolygonGeometryConverter(tb));
        myMGStoConverterMap.put(SimpleMapPolygonGeometrySupport.class, new MapPolygonGeometryConverter(tb));
        myMGStoConverterMap.put(MapPolylineGeometrySupport.class, new MapPolylineGeometryConverter(tb));
        myMGStoConverterMap.put(SimpleMapPolylineGeometrySupport.class, new MapPolylineGeometryConverter(tb));
        myMGStoConverterMap.put(DefaultMapPolylineGeometrySupport.class, new MapPolylineGeometryConverter(tb));
        myMGStoConverterMap.put(MapIconGeometrySupport.class, new MapIconGeometryConverter(tb));
        myMGStoConverterMap.put(DefaultMapIconGeometrySupport.class, new MapIconGeometryConverter(tb));
        myMGStoConverterMap.put(SimpleMapIconGeometrySupport.class, new MapIconGeometryConverter(tb));
    }

    @Override
    public MapGeometrySupportToGeometryConverter getConverter(MapGeometrySupport mgs)
    {
        MapGeometrySupportToGeometryConverter converter = null;
        if (mgs != null)
        {
            synchronized (myMGStoConverterMap)
            {
                // First try a direct retrieve.
                converter = myMGStoConverterMap.get(mgs.getClass());

                // Search up the inheritance hierarchy to find the first
                // type that is supported. Stop if we reach our interface class.
                if (converter == null)
                {
                    Class<?> aClass = mgs.getClass().getSuperclass();
                    while (aClass != Object.class && converter == null)
                    {
                        converter = myMGStoConverterMap.get(mgs.getClass());
                        aClass = aClass.getSuperclass();
                    }
                }

                // Now search the interface hierarchy to see if we can
                // find an interface we support.
                if (converter == null)
                {
                    Class<?> aClass = getSuperMGSClass(mgs.getClass());
                    while (aClass != null && converter == null)
                    {
                        converter = myMGStoConverterMap.get(aClass);
                        if (converter == null)
                        {
                            aClass = getSuperMGSClass(aClass);
                        }
                    }
                }
            }
        }
        return converter;
    }

    @Override
    public MapGeometrySupportToGeometryConverter installConverter(Class<? extends MapGeometrySupport> aClass,
            MapGeometrySupportToGeometryConverter converter)
    {
        MapGeometrySupportToGeometryConverter oldConverter = null;
        synchronized (myMGStoConverterMap)
        {
            oldConverter = myMGStoConverterMap.put(aClass, converter);
        }
        return oldConverter;
    }

    @Override
    public MapGeometrySupportToGeometryConverter uninstallConverter(Class<? extends MapGeometrySupport> aClass)
    {
        MapGeometrySupportToGeometryConverter oldConverter = null;
        synchronized (myMGStoConverterMap)
        {
            oldConverter = myMGStoConverterMap.remove(aClass);
        }
        return oldConverter;
    }
}
