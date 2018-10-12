package io.opensphere.core.util.jts.core;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LineType;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.JTSUtilities;

/**
 * Utilities which utilize both JTS geometries and core geometries for operating
 * on lines.
 */
public class JTSLineStringUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(JTSLineStringUtilities.class);

    /** The default Z order. */
    private static final int DEFAULT_ZORDER = ZOrderRenderProperties.TOP_Z - 1000;

    /**
     * Builds a set of core geometries for the supplied line sets. Defaults to
     * using other utilities if the supplied region is not a line.
     *
     * @param region the region to convert to a line set.
     * @param color the color to apply to the converted line.
     * @return a {@link List} of {@link Geometry} objects generated from the
     *         supplied region.
     */
    public static List<? extends Geometry> buildLineSet(com.vividsolutions.jts.geom.Geometry region, Color color)
    {
        if (region == null)
        {
            return Collections.<PolylineGeometry>emptyList();
        }

        if (region instanceof LineString)
        {
            return Collections
                    .singletonList(convertToPolylineGeometry((LineString)region, createPolylineProperties(color, 3, false)));
        }
        else if (region instanceof MultiLineString)
        {
            PolylineRenderProperties properties = createPolylineProperties(color, 3, false);
            MultiLineString multiLineString = (MultiLineString)region;

            List<PolylineGeometry> returnValue = New.list();

            for (int i = 0; i < multiLineString.getNumGeometries(); i++)
            {
                returnValue.add(convertToPolylineGeometry((LineString)multiLineString.getGeometryN(i), properties));
            }
            return returnValue;
        }
        else
        {
            LOGGER.warn("Attempted to use line string utilities with a non-line geometry.");
            return JTSCoreGeometryUtilities.buildSelectionPolygonSet(region, color);
        }
    }

    /**
     * Creates the polygon geometry from JTS polygon.
     *
     * @param line the line string to convert to a OpenSphere geometry.
     * @param props The render properties for the resulting polygon.
     * @return the polygon geometry
     */
    public static PolylineGeometry convertToPolylineGeometry(LineString line, PolylineRenderProperties props)
    {
        if (line == null)
        {
            return null;
        }

        PolylineGeometry.Builder<GeographicPosition> builder = new PolylineGeometry.Builder<>();
        builder.setVertices(Arrays.stream(line.getCoordinates()).map(JTSUtilities::createPosition).collect(Collectors.toList()));
        builder.setLineType(LineType.STRAIGHT_LINE);

        return new PolylineGeometry(builder, props, null);
    }

    /**
     * Creates an instance of the {@link PolylineRenderProperties} class for use
     * in generating new {@link PolylineGeometry} instances.
     *
     * @param color the color to include in the properties object.
     * @param width the width to include in the properties object.
     * @param pickable the 'pickable' state to include in the properties object.
     * @return a new properties object to use in creating
     *         {@link PolylineGeometry} objects.
     */
    private static PolylineRenderProperties createPolylineProperties(Color color, int width, boolean pickable)
    {
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(DEFAULT_ZORDER, true, pickable);
        props.setColor(color);
        props.setWidth(width);
        return props;
    }

}
