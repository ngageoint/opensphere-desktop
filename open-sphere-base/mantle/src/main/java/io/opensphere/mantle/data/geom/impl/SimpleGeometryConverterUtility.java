package io.opensphere.mantle.data.geom.impl;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.geom.MapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.MapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapIconGeometrySupport;
import io.opensphere.mantle.data.geom.MapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.MapPointGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;

/**
 * A utility class that assists in converting the heavy weight
 * {@link MapGeometrySupport} default types to the lighter weight simple types
 * where applicable.
 */
public final class SimpleGeometryConverterUtility
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SimpleGeometryConverterUtility.class);

    /**
     * Checks a {@link MapGeometrySupport} to see if it can be simplified by
     * checking to see if it has children, callouts, or tool tips.
     *
     * @param support the {@link MapGeometrySupport}
     * @return true, if the support can be simplified, false if it already is
     *         simple or if it has any of the listed items such that
     *         simplification would cause loss of detail.
     */
    public static boolean canSimplify(MapGeometrySupport support)
    {
        boolean canSimplify = false;
        if (!(support instanceof AbstractSimpleGeometrySupport))
        {
            canSimplify = !support.hasChildren() && support.getCallOutSupport() == null && support.getToolTip() == null;
        }
        return canSimplify;
    }

    /**
     * If possible takes a {@link MapGeometrySupport} and converts it to its
     * simple form. Any support that is already a simple type will simply be
     * returned. If it cannot be simplified the result support will be the
     * unmodified original {@link MapGeometrySupport}.
     *
     * @param support the {@link MapGeometrySupport} to convert if possible
     * @return the simple {@link MapGeometrySupport} or the original if it was
     *         already simple or cannot be simplified.
     */
    public static MapGeometrySupport convertSupportToSimpleFormIfPossible(MapGeometrySupport support)
    {
        MapGeometrySupport result = support;
        if (!(support instanceof AbstractSimpleGeometrySupport) && canSimplify(support))
        {
            if (support instanceof MapPointGeometrySupport)
            {
                result = convertToSimpleForm((MapPointGeometrySupport)support);
            }
            else if (support instanceof MapEllipseGeometrySupport)
            {
                result = convertToSimpleForm((MapEllipseGeometrySupport)support);
            }
            else if (support instanceof MapCircleGeometrySupport)
            {
                result = convertToSimpleForm((MapCircleGeometrySupport)support);
            }
            else if (support instanceof MapPolylineGeometrySupport)
            {
                result = convertToSimpleForm((MapPolylineGeometrySupport)support);
            }
            else if (support instanceof MapPolygonGeometrySupport)
            {
                result = convertToSimpleForm((MapPolygonGeometrySupport)support);
            }
            else if (support instanceof MapIconGeometrySupport)
            {
                result = convertToSimpleForm((MapIconGeometrySupport)support);
            }
            else if (support instanceof MapLineOfBearingGeometrySupport)
            {
                result = convertToSimpleForm((MapLineOfBearingGeometrySupport)support);
            }
            if (LOGGER.isTraceEnabled() && Utilities.notSameInstance(support, result))
            {
                LOGGER.trace("Converted " + support.getClass().getSimpleName() + " to " + result.getClass().getSimpleName());
            }
        }
        return result;
    }

    /**
     * If necessary converts a provided {@link MapCircleGeometrySupport} to a
     * {@link SimpleMapCircleGeometrySupport}.
     *
     * @param support the {@link MapCircleGeometrySupport} to simplify
     * @return the {@link SimpleMapCircleGeometrySupport}
     *         {@link SimpleMapCircleGeometrySupport} by discarding any children
     *         and other extra info if not provided.
     */
    public static MapCircleGeometrySupport convertToSimpleForm(MapCircleGeometrySupport support)
    {
        if (support instanceof SimpleMapEllipseGeometrySupport)
        {
            return support;
        }
        SimpleMapCircleGeometrySupport simple = new SimpleMapCircleGeometrySupport(support.getLocation(), support.getRadius());
        copyBasicSupportSettings(support, simple);
        return simple;
    }

    /**
     * If necessary converts a provided {@link MapEllipseGeometrySupport} to a
     * {@link SimpleMapEllipseGeometrySupport}.
     *
     * @param support the {@link MapEllipseGeometrySupport} to simplify
     * @return the {@link SimpleMapEllipseGeometrySupport}
     *         {@link SimpleMapEllipseGeometrySupport} by discarding any
     *         children and other extra info if not provided.
     */
    public static MapEllipseGeometrySupport convertToSimpleForm(MapEllipseGeometrySupport support)
    {
        if (support instanceof SimpleMapEllipseGeometrySupport)
        {
            return support;
        }
        SimpleMapEllipseGeometrySupport simple = new SimpleMapEllipseGeometrySupport(support.getLocation(),
                support.getSemiMajorAxis(), support.getSemiMinorAxis(), support.getOrientation());
        copyBasicSupportSettings(support, simple);
        return simple;
    }

    /**
     * If necessary converts a provided {@link MapIconGeometrySupport} to a
     * {@link SimpleMapIconGeometrySupport}.
     *
     * @param support the {@link MapIconGeometrySupport} to simplify
     * @return the {@link SimpleMapIconGeometrySupport}
     *         {@link SimpleMapIconGeometrySupport} by discarding any children
     *         and other extra info if not provided.
     */
    public static MapIconGeometrySupport convertToSimpleForm(MapIconGeometrySupport support)
    {
        if (support instanceof SimpleMapIconGeometrySupport)
        {
            return support;
        }
        SimpleMapIconGeometrySupport simple = new SimpleMapIconGeometrySupport(support.getLocation(), support.getIconURL(),
                support.getIconSize());
        copyBasicSupportSettings(support, simple);
        return simple;
    }

    /**
     * If necessary converts a provided {@link MapLineOfBearingGeometrySupport}
     * to a {@link SimpleMapLineOfBearingGeometrySupport}.
     *
     * @param support the {@link MapLineOfBearingGeometrySupport} to simplify
     * @return the {@link SimpleMapLineOfBearingGeometrySupport}
     *         {@link SimpleMapLineOfBearingGeometrySupport} by discarding any
     *         children and other extra info if not provided.
     */
    public static MapLineOfBearingGeometrySupport convertToSimpleForm(MapLineOfBearingGeometrySupport support)
    {
        if (support instanceof SimpleMapLineOfBearingGeometrySupport)
        {
            return support;
        }
        SimpleMapLineOfBearingGeometrySupport simple = new SimpleMapLineOfBearingGeometrySupport(support.getLocation(),
                support.getOrientation(), support.getLength());
        copyBasicSupportSettings(support, simple);
        return simple;
    }

    /**
     * If necessary converts a provided {@link MapPointGeometrySupport} to a
     * {@link SimpleMapIconGeometrySupport}.
     *
     * @param support the {@link MapPointGeometrySupport} to simplify
     * @return the {@link SimpleMapPointGeometrySupport}
     *         {@link SimpleMapPointGeometrySupport} by discarding any children
     *         and other extra info if not provided.
     */
    public static MapPointGeometrySupport convertToSimpleForm(MapPointGeometrySupport support)
    {
        if (support instanceof SimpleMapPointGeometrySupport)
        {
            return support;
        }
        SimpleMapPointGeometrySupport simple = new SimpleMapPointGeometrySupport(support.getLocation());
        copyBasicSupportSettings(support, simple);
        return simple;
    }

    /**
     * If necessary converts a provided {@link MapPolygonGeometrySupport} to a
     * {@link SimpleMapPolygonGeometrySupport}.
     *
     * @param support the {@link MapPolygonGeometrySupport} to simplify
     * @return the {@link SimpleMapPolygonGeometrySupport}
     *         {@link SimpleMapPolygonGeometrySupport} by discarding any
     *         children and other extra info if not provided.
     */
    public static MapPolygonGeometrySupport convertToSimpleForm(MapPolygonGeometrySupport support)
    {
        if (support instanceof SimpleMapPolygonGeometrySupport)
        {
            return support;
        }
        SimpleMapPolygonGeometrySupport simple = new SimpleMapPolygonGeometrySupport(support.getLocations(), support.getHoles());
        simple.setFillColor(support.getFillColor());
        simple.setFilled(support.isFilled());
        simple.setLineDrawn(support.isLineDrawn());
        simple.setLineType(support.getLineType());
        simple.setLineWidth(support.getLineWidth());
        copyBasicSupportSettings(support, simple);
        return simple;
    }

    /**
     * If necessary converts a provided {@link MapPolylineGeometrySupport} to a
     * {@link SimpleMapPolygonGeometrySupport}.
     *
     * @param support the {@link MapPolylineGeometrySupport} to simplify
     * @return the {@link SimpleMapPolylineGeometrySupport}
     *         {@link SimpleMapPolylineGeometrySupport} by discarding any
     *         children and other extra info if not provided.
     */
    public static MapPolylineGeometrySupport convertToSimpleForm(MapPolylineGeometrySupport support)
    {
        if (support instanceof SimpleMapPolygonGeometrySupport)
        {
            return support;
        }
        SimpleMapPolylineGeometrySupport simple = new SimpleMapPolylineGeometrySupport(support.getLocations());
        simple.setLineType(support.getLineType());
        simple.setLineWidth(support.getLineWidth());
        copyBasicSupportSettings(support, simple);
        return simple;
    }

    /**
     * Copy basic support settings from a source {@link MapGeometrySupport} to a
     * destination {@link MapGeometrySupport}.
     *
     * @param source the source
     * @param destination the destination
     */
    private static void copyBasicSupportSettings(MapGeometrySupport source, MapGeometrySupport destination)
    {
        destination.setColor(source.getColor(), null);
        destination.setTimeSpan(source.getTimeSpan());
        destination.setFollowTerrain(source.followTerrain(), null);
    }

    /**
     * Instantiates a new simple geometry converter utility.
     */
    private SimpleGeometryConverterUtility()
    {
        // Do not allow instantiation.
    }
}
