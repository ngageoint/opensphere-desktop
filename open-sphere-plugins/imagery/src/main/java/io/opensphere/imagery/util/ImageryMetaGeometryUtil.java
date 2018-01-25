package io.opensphere.imagery.util;

import java.awt.Color;
import java.util.List;

import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * The Class ImageryMetaGeometryUtil.
 */
public final class ImageryMetaGeometryUtil
{
    /**
     * Creates a {@link PolygonGeometry} from a geographic bounding box.
     *
     * @param gbb the {@link GeographicBoundingBox}
     * @param color the {@link Color}
     * @param width the lien width.
     * @param constraints the {@link Constraints}
     * @return the polygon geometry
     */
    public static PolygonGeometry createGeometry(GeographicBoundingBox gbb, Color color, int width, Constraints constraints)
    {
        Utilities.checkNull(gbb, "gbb");
        PolygonGeometry geom;
        PolygonGeometry.Builder<GeographicPosition> polyBuilder = new PolygonGeometry.Builder<GeographicPosition>();
        List<GeographicPosition> posList = New.list();
        posList.addAll(gbb.getVertices());
        posList.add(posList.get(0));
        polyBuilder.setVertices(posList);

        PolygonRenderProperties props = new DefaultPolygonRenderProperties(0, true, false);
        props.setColor(color == null ? Color.white : color);
        props.setWidth(width <= 0 ? 1 : width);

        geom = new PolygonGeometry(polyBuilder, props, constraints);

        return geom;
    }

    /**
     * IDo not allow instantiation.
     */
    private ImageryMetaGeometryUtil()
    {
    }
}
