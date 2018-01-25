package io.opensphere.overlay.scalebar;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.Utilities;

/** The bar portion of the scale bar. */
public class ScalebarLine extends Renderable
{
    /** The currently drawn line. */
    private PolylineGeometry myLine;

    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /** The pick geometry. */
    private TileGeometry myPickTile;

    /** The current width of the scale bar in pixels. */
    private double myPixelWidth;

    /**
     * Construct me.
     *
     * @param parent parent component.
     */
    public ScalebarLine(Component parent)
    {
        super(parent);
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
    }

    /**
     * Draw the actual scale bar.
     *
     * @param pixelWidth Length of the scale line.
     */
    public void drawScaleBarLines(double pixelWidth)
    {
        myPixelWidth = pixelWidth;
        Set<Geometry> removes = new HashSet<>();
        if (myLine != null)
        {
            removes.add(myLine);
            getGeometries().remove(myLine);
        }

        ScreenBoundingBox bbox = getDrawBounds();
        ScreenPosition center = bbox.getCenter();

        double leftX = center.getX() - pixelWidth / 2.;
        double rightX = center.getX() + pixelWidth / 2.;

        ScreenPosition start = bbox.getUpperLeft().add(new Vector3d(leftX, bbox.getHeight() / 2, 0d));
        ScreenPosition end = bbox.getUpperLeft().add(new Vector3d(rightX, bbox.getHeight() / 2, 0));

        ScreenPosition leftBottom = bbox.getUpperLeft().add(new Vector3d(leftX, bbox.getHeight() / 2 + 10, 0d));
        ScreenPosition leftTop = bbox.getUpperLeft().add(new Vector3d(leftX, bbox.getHeight() / 2 - 10, 0d));

        ScreenPosition rightBottom = bbox.getUpperLeft().add(new Vector3d(rightX, bbox.getHeight() / 2 + 10, 0d));
        ScreenPosition rightTop = bbox.getUpperLeft().add(new Vector3d(rightX, bbox.getHeight() / 2 - 10, 0d));

        List<ScreenPosition> positions = new ArrayList<>();
        positions.add(leftBottom);
        positions.add(leftTop);
        // This is going to overlap a couple small sections
        positions.add(start);
        positions.add(end);
        positions.add(rightBottom);
        positions.add(rightTop);
        PolylineGeometry.Builder<ScreenPosition> polyBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(getBaseZOrder() + 2, true, false);
        props.setColor(Color.GREEN);
        props.setWidth(2f);
        polyBuilder.setVertices(positions);
        myLine = new PolylineGeometry(polyBuilder, props, null);

        getGeometries().add(myLine);

        Set<PolylineGeometry> adds = Collections.singleton(myLine);
        updateGeometries(adds, removes);
    }

    /**
     * Get the The current width of the scale bar in pixels.
     *
     * @return the The current width of the scale bar in pixels
     */
    public double getPixelWidth()
    {
        return myPixelWidth;
    }

    @Override
    public void handleCleanupListeners()
    {
        myMouseSupport.cleanupListeners();
    }

    @Override
    public void init()
    {
        myPickTile = getPickTile(getDrawBounds(), 20);
        getGeometries().add(myPickTile);
        myMouseSupport.setActionGeometry(myPickTile);
    }

    @Override
    public void mouseDragged(Geometry geom, Point dragStart, MouseEvent evt)
    {
        if (Utilities.sameInstance(geom, myPickTile))
        {
            Point end = evt.getPoint();
            moveWindow(new ScreenPosition((int)(end.getX() - dragStart.getX()), (int)(end.getY() - dragStart.getY())));
        }
    }
}
