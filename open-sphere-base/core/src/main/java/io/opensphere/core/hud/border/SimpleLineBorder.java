package io.opensphere.core.hud.border;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.hud.framework.Border;
import io.opensphere.core.hud.framework.Panel;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/**
 * A border with no size and no geometries.
 */
public class SimpleLineBorder implements Border
{
    /** Geometries which will be drawn on the border. */
    private final Set<Geometry> myGeometries = new HashSet<>();

    /** Border width. */
    private final int myHeight;

    /** Color of the border line. */
    private final Color myLineColor;

    /** Width of the line drawn on the border. */
    private final int myLineWidth;

    /** Parent panel. */
    private final Panel<?, ?> myParent;

    /** Border width. */
    private final int myWidth;

    /**
     * Construct a HUDSimpleLineBorder.
     *
     * @param parent Parent panel.
     * @param builder Builder for the border.
     */
    public SimpleLineBorder(Panel<?, ?> parent, Builder builder)
    {
        myParent = parent;
        myWidth = builder.getWidth();
        myHeight = builder.getHeight();
        myLineWidth = builder.getLineWidth();
        myLineColor = builder.getLineColor();
    }

    @Override
    public int getBottomInset()
    {
        return myHeight;
    }

    @Override
    public Set<Geometry> getGeometries()
    {
        return myGeometries;
    }

    @Override
    public int getLeftInset()
    {
        return myWidth;
    }

    /**
     * Get the lineColor.
     *
     * @return the lineColor
     */
    public Color getLineColor()
    {
        return myLineColor;
    }

    @Override
    public int getRightInset()
    {
        return myWidth;
    }

    @Override
    public int getTopInset()
    {
        return myHeight;
    }

    @Override
    public final void init()
    {
        ScreenBoundingBox frameLoc = myParent.getDrawBounds();
        ScreenPosition frameUL = frameLoc.getUpperLeft();
        ScreenPosition frameLR = frameLoc.getLowerRight();

        double left = frameUL.getX() + (myWidth + 1) / 2d;
        double top = frameUL.getY() + (myHeight + 1) / 2d;
        double right = frameLR.getX() - (myWidth + 1) / 2d;
        double bottom = frameLR.getY() - (myWidth + 1) / 2d;
        ScreenPosition upperLeft = new ScreenPosition(left, top);
        ScreenPosition upperRight = new ScreenPosition(right, top);
        ScreenPosition lowerLeft = new ScreenPosition(left, bottom);
        ScreenPosition lowerRight = new ScreenPosition(right, bottom);

        List<ScreenPosition> positions = new ArrayList<>();
        positions.add(upperLeft);
        positions.add(lowerLeft);
        positions.add(lowerRight);
        positions.add(upperRight);
        positions.add(upperLeft);
        PolylineGeometry.Builder<ScreenPosition> polyBuilder = new PolylineGeometry.Builder<ScreenPosition>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(0, true, false);
        props.setColor(myLineColor);
        props.setWidth(myLineWidth);
        polyBuilder.setVertices(positions);
        PolylineGeometry line = new PolylineGeometry(polyBuilder, props, null);

        myGeometries.add(line);
    }

    /** Builder class for the simple line border. */
    public static final class Builder
    {
        /** Border width. */
        private int myHeight;

        /** Color of the border line. */
        private Color myLineColor = Color.LIGHT_GRAY;

        /** Width of the line drawn on the border. */
        private int myLineWidth;

        /** Border width. */
        private int myWidth;

        /**
         * Get the height.
         *
         * @return the height
         */
        public int getHeight()
        {
            return myHeight;
        }

        /**
         * Get the lineColor.
         *
         * @return the lineColor
         */
        public Color getLineColor()
        {
            return myLineColor;
        }

        /**
         * Get the lineWidth.
         *
         * @return the lineWidth
         */
        public int getLineWidth()
        {
            return myLineWidth;
        }

        /**
         * Get the width.
         *
         * @return the width
         */
        public int getWidth()
        {
            return myWidth;
        }

        /**
         * Set the height.
         *
         * @param height the height to set
         */
        public void setHeight(int height)
        {
            myHeight = height;
        }

        /**
         * Set the lineColor.
         *
         * @param lineColor the lineColor to set
         */
        public void setLineColor(Color lineColor)
        {
            myLineColor = lineColor;
        }

        /**
         * Set the lineWidth.
         *
         * @param lineWidth the lineWidth to set
         */
        public void setLineWidth(int lineWidth)
        {
            myLineWidth = lineWidth;
        }

        /**
         * Set the width.
         *
         * @param width the width to set
         */
        public void setWidth(int width)
        {
            myWidth = width;
        }
    }
}
