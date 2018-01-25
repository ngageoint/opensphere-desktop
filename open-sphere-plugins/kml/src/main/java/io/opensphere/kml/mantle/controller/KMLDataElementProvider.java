package io.opensphere.kml.mantle.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.kml.common.util.KMLStyleCache;
import io.opensphere.kml.common.util.KMLToolboxUtils;
import io.opensphere.kml.mantle.model.KMLMantleFeature;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.AbstractDataElementProvider;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/**
 * Data element provider for a JAK 2.2.0 object.
 */
public class KMLDataElementProvider extends AbstractDataElementProvider<KMLFeature>
{
    /** The unique type counter. */
    public static final AtomicInteger ourUniqueTypeCounter = new AtomicInteger(1000);

    /** The KML data source. */
    private final KMLDataSource myDataSource;

    /** The geometry builder. */
    private final KMLGeometryBuilder myGeometryBuilder;

    /**
     * Constructor.
     *
     * @param serverRegistry The server provider registry.
     * @param mantleToolbox The mantle toolbox
     * @param dataRegistry The data registry
     * @param dataSource The data source
     * @param dataType The DataTypeInfo
     * @param features The features
     */
    public KMLDataElementProvider(ServerProviderRegistry serverRegistry, MantleToolbox mantleToolbox, DataRegistry dataRegistry,
            KMLDataSource dataSource, DataTypeInfo dataType, Iterable<? extends KMLFeature> features)
    {
        super(dataType, features);
        myDataSource = dataSource;
        myGeometryBuilder = new KMLGeometryBuilder(dataRegistry, serverRegistry, mantleToolbox, dataSource);
    }

    @Override
    protected DataElement createDataElement(KMLFeature feature)
    {
        TimeSpan timeSpan = KMLSpatialTemporalUtils.timeSpanFromTimePrimitive(feature.getTimePrimitive());

        KMLMantleFeature mantleFeature = new KMLMantleFeature(feature, timeSpan);
        MetaDataProvider metaDataProvider = mantleFeature.newMetaDataProvider(getDataTypeInfo().getMetaDataInfo(), myDataSource);

        Geometry geometry = ((Placemark)feature.getFeature()).getGeometry();
        KMLStyleCache styleCache = KMLToolboxUtils.getKmlToolbox().getStyleCache();
        Style style = styleCache.getStyle(feature, StyleState.NORMAL);
        Style highlightStyle = styleCache.getStyle(feature, StyleState.HIGHLIGHT);
        MapGeometrySupport geomSupport = myGeometryBuilder.createMapGeometrySupport(geometry, style, highlightStyle, true);

        DataElement dataElement;
        if (geomSupport != null)
        {
            setTimeSpan(geomSupport, timeSpan);

            // Nasty side effect
            feature.setGeoBoundingBox(getBoundingBox(geomSupport));
            feature.setGeometryColor(geomSupport.getColor());

            dataElement = new DefaultMapDataElement(ourUniqueTypeCounter.incrementAndGet(), timeSpan, getDataTypeInfo(),
                    metaDataProvider, geomSupport);
        }
        else
        {
            dataElement = new DefaultDataElement(ourUniqueTypeCounter.incrementAndGet(), timeSpan, getDataTypeInfo(),
                    metaDataProvider);
        }

        return dataElement;
    }

    /**
     * Set the time span in a map geometry support and its children.
     *
     * @param geomSupport The map geometry support.
     * @param timeSpan The time span.
     */
    private static void setTimeSpan(MapGeometrySupport geomSupport, TimeSpan timeSpan)
    {
        geomSupport.setTimeSpan(timeSpan);
        List<MapGeometrySupport> children = geomSupport.getChildren();
        if (children != null)
        {
            for (int index = 0; index < children.size(); ++index)
            {
                setTimeSpan(children.get(index), timeSpan);
            }
        }
    }

    /**
     * Custom version of getBoundingBox that can handle geometries with
     * different altitude references.
     *
     * @param geomSupport The geometry support
     * @return The GeographicBoundingBox
     */
    private static GeographicBoundingBox getBoundingBox(MapGeometrySupport geomSupport)
    {
        GeographicBoundingBox bounds = null;
        if (geomSupport instanceof MapLocationGeometrySupport)
        {
            MapLocationGeometrySupport locationGeomSupport = (MapLocationGeometrySupport)geomSupport;
            bounds = new GeographicBoundingBox(locationGeomSupport.getLocation(), locationGeomSupport.getLocation());
        }
        else if (geomSupport instanceof MapPathGeometrySupport)
        {
            MapPathGeometrySupport pathGeomSupport = (MapPathGeometrySupport)geomSupport;
            bounds = MapGeometrySupportUtils.getBoundingBox(pathGeomSupport);
        }

        if (bounds != null && geomSupport.hasChildren())
        {
            GeographicBoundingBox childBB = MapGeometrySupportUtils.getMergedChildBounds(geomSupport, null,
                    Altitude.ReferenceLevel.TERRAIN);
            if (childBB != null)
            {
                bounds = GeographicBoundingBox.merge(bounds, childBB, Altitude.ReferenceLevel.TERRAIN);
            }
        }
        return bounds;
    }
}
