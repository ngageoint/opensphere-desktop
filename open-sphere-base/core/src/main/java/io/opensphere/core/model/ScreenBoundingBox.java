package io.opensphere.core.model;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.math.Vector3d;

/**
 * Bound an object on the screen. Screen Coordinates move from UpperLeft to
 * LowerRight.
 */
public class ScreenBoundingBox implements BoundingBox<ScreenPosition>
{
    /** Lower Right corner. */
    private final ScreenPosition myLowerRight;

    /** Upper Left corner. */
    private final ScreenPosition myUpperLeft;

    /**
     * Create a bounding box from a {@link Rectangle}.
     *
     * @param rect The rectangle from which to create a bounding box.
     * @return the newly created bounding box.
     */
    public static ScreenBoundingBox fromRectangle(Rectangle rect)
    {
        ScreenPosition ul = new ScreenPosition(rect.x, rect.y);
        ScreenPosition lr = new ScreenPosition(rect.getMaxX(), rect.getMaxY());
        return new ScreenBoundingBox(ul, lr);
    }

    /**
     * Get the smallest bounding box which contains all of the positions.
     *
     * @param positions The positions which must be contained in the box.
     * @return The smallest bounding box which contains all of the positions.
     */
    public static ScreenBoundingBox getMinimumBoundingBox(Collection<? extends ScreenPosition> positions)
    {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (ScreenPosition pos : positions)
        {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());

            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
        }

        return new ScreenBoundingBox(new ScreenPosition(maxX, maxY), new ScreenPosition(minX, minY));
    }

    /**
     * Construct me.
     *
     * @param upperLeft upper left corner.
     * @param lowerRight lower right corner.
     */
    public ScreenBoundingBox(ScreenPosition upperLeft, ScreenPosition lowerRight)
    {
        myUpperLeft = upperLeft;
        myLowerRight = lowerRight;
    }

    /**
     * Create a {@link Rectangle} from this bounding box.
     *
     * @return the newly created rectangle.
     */
    public Rectangle asRectangle()
    {
        return new Rectangle((int)myUpperLeft.getX(), (int)myUpperLeft.getY(), (int)getWidth(), (int)getHeight());
    }

    @Override
    public boolean contains(BoundingBox<ScreenPosition> otherBox)
    {
        return BoundingBoxes.contains(this, otherBox);
    }

    @Override
    public boolean contains(Position point, double radius)
    {
        if (!(point instanceof ScreenPosition))
        {
            return false;
        }
        ScreenPosition pt = (ScreenPosition)point;
        double minY = getUpperRight().getY() - radius;
        double maxY = getLowerLeft().getY() + radius;
        if (minY > pt.getY() || maxY < pt.getY())
        {
            return false;
        }

        double minX = getLowerLeft().getX() - radius;
        double maxX = getUpperRight().getX() + radius;
        return minX <= maxX ? minX <= pt.getX() && maxX >= pt.getX() : minX <= pt.getX() || maxX >= pt.getX();
    }

    @Override
    public ScreenPosition getCenter()
    {
        double centerWidthPosition = getWidth() / 2d + myUpperLeft.getX();
        double centerHeightPosition = getHeight() / 2d + myUpperLeft.getY();

        return new ScreenPosition(centerWidthPosition, centerHeightPosition);
    }

    @Override
    public double getDepth()
    {
        return 0;
    }

    @Override
    public double getHeight()
    {
        return myLowerRight.getY() - myUpperLeft.getY();
    }

    @Override
    public ScreenPosition getLowerLeft()
    {
        return new ScreenPosition(myUpperLeft.getX(), myLowerRight.getY());
    }

    @Override
    public ScreenPosition getLowerRight()
    {
        return myLowerRight;
    }

    @Override
    public Vector3d getOffset(BoundingBox<ScreenPosition> outerBox)
    {
        ScreenBoundingBox ob = (ScreenBoundingBox)outerBox;
        double x = myUpperLeft.getX() - ob.getUpperLeft().getX();
        double y = myUpperLeft.getY() - ob.getUpperLeft().getY();
        return new Vector3d(x, y, 0);
    }

    @Override
    public Vector3d getOffsetPercent(Position position)
    {
        if (!(position instanceof ScreenPosition))
        {
            return null;
        }

        ScreenPosition scrPos = (ScreenPosition)position;
        double widthPct = (scrPos.getX() - myUpperLeft.getX()) / getWidth();
        double heightPct = (scrPos.getY() - myUpperLeft.getY()) / getHeight();

        return new Vector3d(widthPct, heightPct, 0);
    }

    @Override
    public Class<ScreenPosition> getPositionType()
    {
        return ScreenPosition.class;
    }

    @Override
    public ScreenPosition getUpperLeft()
    {
        return myUpperLeft;
    }

    @Override
    public ScreenPosition getUpperRight()
    {
        return new ScreenPosition(myLowerRight.getX(), myUpperLeft.getY());
    }

    @Override
    public List<? extends ScreenPosition> getVertices()
    {
        return Arrays.asList(getLowerLeft(), getLowerRight(), getUpperRight(), getUpperLeft());
    }

    @Override
    public double getWidth()
    {
        return myLowerRight.getX() - myUpperLeft.getX();
    }

    @Override
    public BoundingBox<ScreenPosition> intersection(BoundingBox<ScreenPosition> otherBox)
    {
        ScreenBoundingBox sbb = null;
        Geometry gc = BoundingBoxes.intersectionEnvelope(this, otherBox);
        if (gc != null && gc.getCoordinates().length > 0)
        {
            Coordinate[] cds = gc.getCoordinates();
            sbb = new ScreenBoundingBox(new ScreenPosition(cds[3].x, cds[3].y), new ScreenPosition(cds[1].x, cds[1].y));
        }
        return sbb;
    }

    @Override
    public boolean intersects(BoundingBox<ScreenPosition> otherBox)
    {
        return BoundingBoxes.intersects(this, otherBox);
    }

    @Override
    public boolean overlaps(Quadrilateral<? extends ScreenPosition> other, double tolerance)
    {
        if (!(other instanceof ScreenBoundingBox))
        {
            if (other instanceof ScreenQuadrilateral)
            {
                return ((ScreenQuadrilateral)other).overlaps(this, tolerance);
            }
            return false;
        }

        ScreenBoundingBox otherScreen = (ScreenBoundingBox)other;
        if (contains(otherScreen.getLowerLeft(), tolerance) || contains(otherScreen.getUpperRight(), tolerance)
                || contains(otherScreen.getLowerRight(), tolerance) || contains(otherScreen.getUpperLeft(), tolerance))
        {
            return true;
        }

        return other.contains(getLowerLeft(), tolerance) || other.contains(getUpperRight(), tolerance)
                || other.contains(getLowerRight(), tolerance) || other.contains(getUpperLeft(), tolerance);
    }

    @Override
    public String toSimpleString()
    {
        return getLowerLeft().toSimpleString() + " | " + getUpperRight().toSimpleString();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(30);
        sb.append(getClass().getSimpleName()).append(" [").append(myUpperLeft).append('/').append(myLowerRight).append(']');
        return sb.toString();
    }

    @Override
    public BoundingBox<ScreenPosition> union(BoundingBox<ScreenPosition> otherBox)
    {
        ScreenBoundingBox sbb = null;
        Geometry gc = BoundingBoxes.unionEnvelope(this, otherBox);
        if (gc != null && gc.getCoordinates().length > 0)
        {
            Coordinate[] cds = gc.getCoordinates();
            sbb = new ScreenBoundingBox(new ScreenPosition(cds[3].x, cds[3].y), new ScreenPosition(cds[1].x, cds[1].y));
        }
        return sbb;
    }
}
