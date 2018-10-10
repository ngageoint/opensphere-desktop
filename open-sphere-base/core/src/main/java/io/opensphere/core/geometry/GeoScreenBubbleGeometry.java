package io.opensphere.core.geometry;

import java.util.List;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A polyline which surrounds a given bounding box and shows the geographic
 * point to which it is anchored. This is meant to look like a cartoon speech
 * bubble.
 */
public class GeoScreenBubbleGeometry extends GeoScreenPolygonGeometry
{
    /**
     * The buffer in pixels between the edge of the bubble and the edge of the
     * box the bubble surrounds.
     */
    private final double myBorderBuffer;

    /** The box the bubble will surround. */
    private final GeoScreenBoundingBox myBoundingBox;

    /** The radius of the quart circle at the corners of the bubble. */
    private final double myCornerRadius;

    /**
     * The number of vertices to use for each quarter circle at the corners.
     */
    private final int myCornerVertexCount;

    /**
     * The percentage of the line left out of a line to show the geographic
     * anchor.
     */
    private final double myGapPercent;

    /**
     * The amount which the height will be adjusted to allow the corners to be
     * properly rendered.
     */
    private final double myHeightAdjustment;

    /**
     * The amount which the width will be adjusted to allow the corners to be
     * properly rendered.
     */
    private final double myWidthAdjustment;

    /**
     * Constructor.
     *
     * @param builder the builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public GeoScreenBubbleGeometry(Builder builder, PolygonRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints, builder.isLineSmoothing(), builder.getLineType());
        myBoundingBox = Utilities.checkNull(builder.getBoundingBox(), "builder.getBoundingBox()");
        myBorderBuffer = builder.getBorderBuffer();
        myCornerRadius = builder.getCornerRadius();
        myCornerVertexCount = builder.getCornerVertexCount();
        myGapPercent = builder.getGapPercent();

        double minSpan = 2. * myCornerRadius + 1.;
        myHeightAdjustment = minSpan - Math.min(minSpan, myBoundingBox.getHeight());
        myWidthAdjustment = minSpan - Math.min(minSpan, myBoundingBox.getWidth());

        Vector2d offset = null;
        if (myBoundingBox.getAnchor().getAnchorOffset() != null)
        {
            offset = new Vector2d(myBoundingBox.getAnchor().getAnchorOffset());
        }
        else
        {
            offset = Vector2d.ORIGIN;
        }

        AttachmentSide attach = findAttachmentSide(offset);
        createBubble(offset, attach);
    }

    @Override
    public GeoScreenBubbleGeometry clone()
    {
        return (GeoScreenBubbleGeometry)super.clone();
    }

    @Override
    public Builder createBuilder()
    {
        Builder builder = (Builder)super.createBuilder();
        builder.setBoundingBox(myBoundingBox);
        builder.setBorderBuffer(myBorderBuffer);
        builder.setCornerRadius(myCornerRadius);
        builder.setCornerVertexCount(myCornerVertexCount);
        builder.setGapPercent(myGapPercent);
        return builder;
    }

    @Override
    public GeoScreenBubbleGeometry derive(BaseRenderProperties renderProperties, Constraints constraints)
            throws ClassCastException
    {
        return new GeoScreenBubbleGeometry(createBuilder(), (PolygonRenderProperties)renderProperties, constraints);
    }

    /**
     * Get the borderBuffer.
     *
     * @return The borderBuffer.
     */
    public final double getBorderBuffer()
    {
        return myBorderBuffer;
    }

    /**
     * Get the boundingBox.
     *
     * @return The boundingBox.
     */
    public final GeoScreenBoundingBox getBoundingBox()
    {
        return myBoundingBox;
    }

    /**
     * Get the cornerRadius.
     *
     * @return The cornerRadius.
     */
    public final double getCornerRadius()
    {
        return myCornerRadius;
    }

    /**
     * Get the cornerVertexCount.
     *
     * @return The cornerVertexCount.
     */
    public final int getCornerVertexCount()
    {
        return myCornerVertexCount;
    }

    /**
     * Get the gapPercent.
     *
     * @return The gapPercent.
     */
    public final double getGapPercent()
    {
        return myGapPercent;
    }

    @Override
    protected Builder createRawBuilder()
    {
        return new Builder();
    }

    /**
     * Add the vertices for a corner of the bubble.
     *
     * @param vertices The vertices for the polyline to which I will add this
     *            corner.
     * @param startAngle The starting angle for the quarter circle at the
     *            corner.
     * @param endAngle The ending angle for the quarter circle at the corner.
     * @param center The center position of the quarter circle at the corner.
     */
    private void addBubbleCorner(List<ScreenPosition> vertices, double startAngle, double endAngle, Vector2d center)
    {
        double angleStep = (endAngle - startAngle) / myCornerVertexCount;
        for (int i = 0; i < myCornerVertexCount; ++i)
        {
            double theta = i * angleStep + startAngle;
            double sin = Math.sin(theta);
            double cos = Math.cos(theta);
            double x = cos * myCornerRadius;
            double y = sin * myCornerRadius;
            Vector2d pt = center.add(new Vector2d(x, -y));
            vertices.add(new ScreenPosition(pt));
        }
    }

    /**
     * Add the vertices for a side of the bubble including the attachment to the
     * geographic anchor if necessary.
     *
     * @param vertices The vertices for the polyline to which I will add this
     *            side.
     * @param start The start of the line.
     * @param end The end of the line.
     * @param anchor When true, include the additional vertices to connect to
     *            the anchor.
     */
    private void addBubbleSide(List<ScreenPosition> vertices, Vector2d start, Vector2d end, boolean anchor)
    {
        vertices.add(new ScreenPosition(start));
        if (anchor)
        {
            vertices.add(new ScreenPosition(start.interpolate(end, 0.5 - myGapPercent * 0.5)));
            vertices.add(new ScreenPosition(0, 0));
            vertices.add(new ScreenPosition(start.interpolate(end, 0.5 + myGapPercent * 0.5)));
        }
        vertices.add(new ScreenPosition(end));
    }

    /**
     * Create all of the vertices for the bubble.
     *
     * @param offset The offset from the geographic anchor for the box this
     *            bubble will surround.
     * @param attachSide The side on which the anchor is attached.
     * @throws IllegalArgumentException when the vertices cannot be used to
     *             build a valid polyline.
     */
    private void createBubble(Vector2d offset, AttachmentSide attachSide) throws IllegalArgumentException
    {
        double bubbleHeight = myBoundingBox.getHeight() + myHeightAdjustment;
        double bubbleWidth = myBoundingBox.getWidth() + myWidthAdjustment;
        Vector2d ul = new Vector2d(-myBorderBuffer, -bubbleHeight - myBorderBuffer).add(offset);
        Vector2d ll = new Vector2d(-myBorderBuffer, myBorderBuffer).add(offset);
        Vector2d lr = new Vector2d(bubbleWidth + myBorderBuffer, myBorderBuffer).add(offset);
        Vector2d ur = new Vector2d(bubbleWidth + myBorderBuffer, -bubbleHeight - myBorderBuffer).add(offset);

        Vector2d down = new Vector2d(0., myCornerRadius);
        Vector2d up = new Vector2d(0., -myCornerRadius);
        Vector2d left = new Vector2d(-myCornerRadius, 0.);
        Vector2d right = new Vector2d(myCornerRadius, 0.);

        double threeHalvesPi = Math.PI + MathUtil.HALF_PI;

        List<ScreenPosition> vertices = New.list();

        // Upper left corner
        addBubbleCorner(vertices, MathUtil.HALF_PI, Math.PI, ul.add(new Vector2d(myCornerRadius, myCornerRadius)));

        // Left side (from top to bottom)
        addBubbleSide(vertices, ul.add(down), ll.add(up), attachSide == AttachmentSide.LEFT);

        // Bottom left corner
        addBubbleCorner(vertices, Math.PI, threeHalvesPi, ll.add(new Vector2d(myCornerRadius, -myCornerRadius)));

        // Bottom side (from right to left)
        addBubbleSide(vertices, ll.add(right), lr.add(left), attachSide == AttachmentSide.BOTTOM);

        // Bottom right corner
        addBubbleCorner(vertices, threeHalvesPi, MathUtil.TWO_PI, lr.add(new Vector2d(-myCornerRadius, -myCornerRadius)));

        // Right side (from bottom to top)
        addBubbleSide(vertices, lr.add(up), ur.add(down), attachSide == AttachmentSide.RIGHT);

        // Upper right corner
        addBubbleCorner(vertices, 0., MathUtil.HALF_PI, ur.add(new Vector2d(-myCornerRadius, myCornerRadius)));

        // upper side (from right to left)
        addBubbleSide(vertices, ur.add(left), ul.add(right), attachSide == AttachmentSide.TOP);

        setVertices(vertices);
    }

    /**
     * Helper method to find the attachment side.
     *
     * @param offset The offset from the geographic anchor for the box this
     *            bubble will surround.
     * @return The attachment side.
     */
    private AttachmentSide findAttachmentSide(Vector2d offset)
    {
        // If the offset is within the box, do not draw the tail.
        if (myBoundingBox.contains(new ScreenPosition(-offset.getX(), offset.getY()),
                myBorderBuffer + Math.max(myWidthAdjustment, myHeightAdjustment)))
        {
            return AttachmentSide.NONE;
        }

        final double safety = 3.;
        boolean allowTop = offset.getY() > myBoundingBox.getHeight() + safety || offset.getY() < -safety;
        boolean allowSide = offset.getX() > safety || offset.getX() + myBoundingBox.getWidth() < -safety - myWidthAdjustment;

        if (!allowTop && !allowSide)
        {
            return AttachmentSide.NONE;
        }
        else if (!allowSide)
        {
            return offset.getY() > 0. ? AttachmentSide.TOP : AttachmentSide.BOTTOM;
        }
        else if (!allowTop)
        {
            return offset.getX() > 0. ? AttachmentSide.LEFT : AttachmentSide.RIGHT;
        }
        else
        {
            double threeQuarterPi = MathUtil.QUARTER_PI + MathUtil.HALF_PI;
            double fiveQuarterPi = threeQuarterPi + MathUtil.HALF_PI;
            double sevenQuarterPi = fiveQuarterPi + MathUtil.HALF_PI;
            double angle = myBoundingBox.getCenter().asVector2d().add(new Vector2d(offset.getX(), -offset.getY())).multiply(-1)
                    .getAngle();
            while (angle < 0.)
            {
                angle += MathUtil.TWO_PI;
            }

            AttachmentSide attach;
            if (MathUtil.QUARTER_PI <= angle && angle < threeQuarterPi)
            {
                attach = AttachmentSide.TOP;
            }
            else if (threeQuarterPi <= angle && angle < fiveQuarterPi)
            {
                attach = AttachmentSide.LEFT;
            }
            else if (fiveQuarterPi <= angle && angle < sevenQuarterPi)
            {
                attach = AttachmentSide.BOTTOM;
            }
            else
            {
                attach = AttachmentSide.RIGHT;
            }

            return attach;
        }
    }

    /** Builder for the geometry. */
    public static class Builder extends GeoScreenPolygonGeometry.Builder
    {
        /**
         * The buffer in pixels between the edge of the bubble and the edge of
         * the box the bubble surrounds.
         */
        private double myBorderBuffer = 1.;

        /** The box the bubble will surround. */
        private GeoScreenBoundingBox myBoundingBox;

        /** The radius of the quart circle at the corners of the bubble. */
        private double myCornerRadius = 10;

        /**
         * The number of vertices to use for each quarter circle at the corners.
         */
        private int myCornerVertexCount = 5;

        /**
         * The percentage of the line left out of a line to show the geographic
         * anchor.
         */
        private double myGapPercent;

        /**
         * Constructor.
         */
        public Builder()
        {
            // set the default for the gap percent
            final double sheep = .2;
            myGapPercent = sheep;
        }

        /**
         * Get the borderBuffer.
         *
         * @return the borderBuffer
         */
        public double getBorderBuffer()
        {
            return myBorderBuffer;
        }

        /**
         * Get the boundingBox.
         *
         * @return the boundingBox
         */
        public GeoScreenBoundingBox getBoundingBox()
        {
            return myBoundingBox;
        }

        /**
         * Get the cornerRadius.
         *
         * @return the cornerRadius
         */
        public double getCornerRadius()
        {
            return myCornerRadius;
        }

        /**
         * Get the cornerVertexCount.
         *
         * @return the cornerVertexCount
         */
        public int getCornerVertexCount()
        {
            return myCornerVertexCount;
        }

        /**
         * Get the gapPercent.
         *
         * @return the gapPercent
         */
        public double getGapPercent()
        {
            return myGapPercent;
        }

        /**
         * Set the borderBuffer.
         *
         * @param borderBuffer the borderBuffer to set
         */
        public void setBorderBuffer(double borderBuffer)
        {
            myBorderBuffer = borderBuffer;
        }

        /**
         * Set the bounding box to be enclosed by the bubble. This will also set
         * the attachment point.
         *
         * @param bbox the boundingBox
         */
        public void setBoundingBox(GeoScreenBoundingBox bbox)
        {
            myBoundingBox = bbox;

            setAttachment(bbox.getAnchor().getGeographicAnchor());
        }

        /**
         * Set the cornerRadius.
         *
         * @param cornerRadius the cornerRadius to set
         */
        public void setCornerRadius(double cornerRadius)
        {
            myCornerRadius = cornerRadius;
            if (MathUtil.isZero(cornerRadius))
            {
                myCornerVertexCount = 0;
            }
        }

        /**
         * Set the cornerVertexCount.
         *
         * @param cornerVertexCount the cornerVertexCount to set
         */
        public void setCornerVertexCount(int cornerVertexCount)
        {
            myCornerVertexCount = cornerVertexCount;
        }

        /**
         * Set the gapPercent.
         *
         * @param gapPercent the gapPercent to set
         */
        public void setGapPercent(double gapPercent)
        {
            myGapPercent = gapPercent;
        }
    }

    /**
     * Available side to which the bubble can be attached to the geographic
     * anchor.
     */
    private enum AttachmentSide
    {
        /** Bottom. */
        BOTTOM,

        /** Left. */
        LEFT,

        /** No Attachment. */
        NONE,

        /** Right. */
        RIGHT,

        /** Top. */
        TOP;
    }
}
