package io.opensphere.core.model;

import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.math.Vector3d;

/**
 * Bound an objected in model coordinates in the plane defined by this box.
 */
public class ModelBoundingBox implements BoundingBox<ModelPosition>
{
    /** Lower Left corner. */
    private final ModelPosition myLowerLeft;

    /** Lower Right corner. */
    private final ModelPosition myLowerRight;

    /** Upper Left corner. */
    private final ModelPosition myUpperLeft;

    /** Upper Right corner. */
    private final ModelPosition myUpperRight;

    /**
     * Construct a ModelBoundingBox.
     *
     * @param upperLeft Upper Left corner.
     * @param lowerLeft Lower Left corner.
     * @param lowerRight Lower Right corner.
     * @param upperRight Upper Right corner.
     *
     */
    public ModelBoundingBox(ModelPosition upperLeft, ModelPosition lowerLeft, ModelPosition lowerRight, ModelPosition upperRight)
    {
        myUpperLeft = upperLeft;
        myLowerLeft = lowerLeft;
        myLowerRight = lowerRight;
        myUpperRight = upperRight;
    }

    @Override
    public boolean contains(BoundingBox<ModelPosition> otherBox)
    {
        return BoundingBoxes.contains(this, otherBox);
    }

    @Override
    public boolean contains(Position point, double radius)
    {
        if (!(point instanceof ModelPosition))
        {
            return false;
        }
        // true when the position is within the bounding cube.
        Vector3d pos = ((ModelPosition)point).getPosition();
        return myUpperLeft.getX() <= pos.getX() && pos.getX() <= myLowerRight.getX() && myUpperLeft.getY() <= pos.getY()
                && pos.getY() <= myLowerRight.getY() && myUpperLeft.getZ() <= pos.getZ() && pos.getZ() <= myLowerRight.getZ();
    }

    @Override
    public Position getCenter()
    {
        return new ModelPosition(new Vector3d((myUpperLeft.getX() + myLowerRight.getX()) / 2,
                (myUpperLeft.getY() + myLowerRight.getY()) / 2, (myUpperLeft.getZ() + myLowerRight.getZ()) / 2));
    }

    @Override
    public double getDepth()
    {
        return myLowerRight.getZ() - myUpperLeft.getZ();
    }

    @Override
    public double getHeight()
    {
        return myLowerRight.getY() - myUpperLeft.getY();
    }

    @Override
    public ModelPosition getLowerLeft()
    {
        return myLowerLeft;
    }

    @Override
    public ModelPosition getLowerRight()
    {
        return myLowerRight;
    }

    @Override
    public Vector3d getOffset(BoundingBox<ModelPosition> outerBox)
    {
        ModelBoundingBox ob = (ModelBoundingBox)outerBox;
        double x = myUpperLeft.getX() - ob.getUpperLeft().getX();
        double y = myUpperLeft.getY() - ob.getUpperLeft().getY();
        double z = myUpperLeft.getZ() - ob.getUpperLeft().getZ();
        return new Vector3d(x, y, z);
    }

    @Override
    public Vector3d getOffsetPercent(Position position)
    {
        if (!(position instanceof ModelPosition))
        {
            return null;
        }

        ModelPosition modelPos = (ModelPosition)position;
        double widthPct = (modelPos.getX() - myUpperLeft.getX()) / getWidth();
        double heightPct = (modelPos.getY() - myUpperLeft.getY()) / getHeight();

        return new Vector3d(widthPct, heightPct, 0);
    }

    @Override
    public Class<ModelPosition> getPositionType()
    {
        return ModelPosition.class;
    }

    @Override
    public ModelPosition getUpperLeft()
    {
        return myUpperLeft;
    }

    @Override
    public ModelPosition getUpperRight()
    {
        return myUpperRight;
    }

    @Override
    public List<? extends ModelPosition> getVertices()
    {
        return Arrays.asList(myUpperLeft, myLowerLeft, myLowerRight, myUpperRight);
    }

    @Override
    public double getWidth()
    {
        return myLowerRight.getX() - myUpperLeft.getX();
    }

    @Override
    public BoundingBox<ModelPosition> intersection(BoundingBox<ModelPosition> otherBox)
    {
        ModelBoundingBox mbb = null;
        Geometry gc = BoundingBoxes.intersectionEnvelope(this, otherBox);
        if (gc != null && gc.getCoordinates().length > 0)
        {
            Coordinate[] cds = gc.getCoordinates();
            mbb = new ModelBoundingBox(new ModelPosition(cds[3].x, cds[3].y, cds[3].z),
                    new ModelPosition(cds[0].x, cds[0].y, cds[0].z), new ModelPosition(cds[1].x, cds[1].y, cds[1].z),
                    new ModelPosition(cds[2].x, cds[2].y, cds[2].z));
        }
        return mbb;
    }

    @Override
    public boolean intersects(BoundingBox<ModelPosition> otherBox)
    {
        return BoundingBoxes.intersects(this, otherBox);
    }

    @Override
    public boolean overlaps(Quadrilateral<? extends ModelPosition> other, double tolerance)
    {
        /* TODO this needs some work. First determine the plane which this box
         * is in, then project both boxes using the projection which puts this
         * box onto the x-y plane. After projection set the z coords of the
         * other quad to 0 and use regular overlap checks. */
        return other.contains(myLowerRight, tolerance) || other.contains(myUpperLeft, tolerance);
    }

    @Override
    public String toSimpleString()
    {
        StringBuilder sb = new StringBuilder(64);

        sb.append("[(").append(myUpperLeft.getX()).append(',');
        sb.append(myUpperLeft.getY()).append(',');
        sb.append(myUpperLeft.getZ()).append("),(");

        sb.append(myLowerLeft.getX()).append(',');
        sb.append(myLowerLeft.getY()).append(',');
        sb.append(myLowerLeft.getZ()).append("),(");

        sb.append(myLowerRight.getX()).append(',');
        sb.append(myLowerRight.getY()).append(',');
        sb.append(myLowerRight.getZ()).append("),(");

        sb.append(myUpperRight.getX()).append(',');
        sb.append(myUpperRight.getY()).append(',');
        sb.append(myUpperRight.getZ()).append(")]");

        return sb.toString();
    }

    @Override
    public BoundingBox<ModelPosition> union(BoundingBox<ModelPosition> otherBox)
    {
        ModelBoundingBox mbb = null;
        Geometry gc = BoundingBoxes.unionEnvelope(this, otherBox);
        if (gc != null && gc.getCoordinates().length > 0)
        {
            Coordinate[] cds = gc.getCoordinates();
            mbb = new ModelBoundingBox(new ModelPosition(cds[3].x, cds[3].y, cds[3].z),
                    new ModelPosition(cds[0].x, cds[0].y, cds[0].z), new ModelPosition(cds[1].x, cds[1].y, cds[1].z),
                    new ModelPosition(cds[2].x, cds[2].y, cds[2].z));
        }
        return mbb;
    }
}
