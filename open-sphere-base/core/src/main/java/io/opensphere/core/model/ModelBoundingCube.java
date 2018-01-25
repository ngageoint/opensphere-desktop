package io.opensphere.core.model;

import java.util.Arrays;
import java.util.List;

import io.opensphere.core.math.Vector3d;

/**
 * Bound an objected in model coordinates.
 */
public class ModelBoundingCube implements BoundingBox<ModelPosition>
{
    /** The greatest value for each coordinate. */
    private final ModelPosition myGreatestPosition;

    /** The least value for each coordinate. */
    private final ModelPosition myLeastPosition;

    /**
     * Construct a ModelBoundingBox.
     *
     * @param least The least value for each coordinate.
     * @param greatest The greatest value for each coordinate.
     */
    public ModelBoundingCube(ModelPosition least, ModelPosition greatest)
    {
        myLeastPosition = least;
        myGreatestPosition = greatest;
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
        return myLeastPosition.getX() <= pos.getX() && pos.getX() <= myGreatestPosition.getX()
                && myLeastPosition.getY() <= pos.getY() && pos.getY() <= myGreatestPosition.getY()
                && myLeastPosition.getZ() <= pos.getZ() && pos.getZ() <= myGreatestPosition.getZ();
    }

    @Override
    public Position getCenter()
    {
        return new ModelPosition(new Vector3d((myLeastPosition.getX() + myGreatestPosition.getX()) / 2,
                (myLeastPosition.getY() + myGreatestPosition.getY()) / 2,
                (myLeastPosition.getZ() + myGreatestPosition.getZ()) / 2));
    }

    @Override
    public double getDepth()
    {
        return myGreatestPosition.getZ() - myLeastPosition.getZ();
    }

    /**
     * Get the greatestPosition.
     *
     * @return the greatestPosition
     */
    public ModelPosition getGreatestPosition()
    {
        return myGreatestPosition;
    }

    @Override
    public double getHeight()
    {
        return myGreatestPosition.getY() - myLeastPosition.getY();
    }

    /**
     * Get the leastPosition.
     *
     * @return the leastPosition
     */
    public ModelPosition getLeastPosition()
    {
        return myLeastPosition;
    }

    @Override
    public ModelPosition getLowerLeft()
    {
        return myLeastPosition;
    }

    @Override
    public ModelPosition getLowerRight()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector3d getOffset(BoundingBox<ModelPosition> outerBox)
    {
        ModelBoundingCube ob = (ModelBoundingCube)outerBox;
        double x = myLeastPosition.getX() - ob.getLeastPosition().getX();
        double y = myLeastPosition.getY() - ob.getLeastPosition().getY();
        double z = myLeastPosition.getZ() - ob.getLeastPosition().getZ();
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
        double xPct = (modelPos.getX() - myLeastPosition.getX()) / getWidth();
        double yPct = (modelPos.getY() - myLeastPosition.getY()) / getHeight();
        double zPct = (modelPos.getZ() - myLeastPosition.getZ()) / getDepth();

        return new Vector3d(xPct, yPct, zPct);
    }

    @Override
    public Class<ModelPosition> getPositionType()
    {
        return ModelPosition.class;
    }

    @Override
    public ModelPosition getUpperLeft()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ModelPosition getUpperRight()
    {
        return myGreatestPosition;
    }

    @Override
    public List<? extends ModelPosition> getVertices()
    {
        ModelPosition[] result = new ModelPosition[8];

        result[0] = myLeastPosition;
        ModelPosition pos = new ModelPosition(
                new Vector3d(myLeastPosition.getX(), myGreatestPosition.getY(), myLeastPosition.getZ()));
        result[1] = pos;
        pos = new ModelPosition(new Vector3d(myGreatestPosition.getX(), myGreatestPosition.getY(), myLeastPosition.getZ()));
        result[2] = pos;
        pos = new ModelPosition(new Vector3d(myGreatestPosition.getX(), myLeastPosition.getY(), myLeastPosition.getZ()));
        result[3] = pos;

        pos = new ModelPosition(new Vector3d(myLeastPosition.getX(), myGreatestPosition.getY(), myGreatestPosition.getZ()));
        result[4] = pos;
        pos = new ModelPosition(new Vector3d(myLeastPosition.getX(), myLeastPosition.getY(), myGreatestPosition.getZ()));
        result[5] = pos;
        pos = new ModelPosition(new Vector3d(myGreatestPosition.getX(), myLeastPosition.getY(), myGreatestPosition.getZ()));
        result[6] = pos;
        result[7] = myGreatestPosition;

        return Arrays.asList(result);
    }

    @Override
    public double getWidth()
    {
        return myGreatestPosition.getX() - myLeastPosition.getX();
    }

    @Override
    public BoundingBox<ModelPosition> intersection(BoundingBox<ModelPosition> otherBox)
    {
        throw new UnsupportedOperationException("Currently intersection of ModelBoundingCube is not supported.");
    }

    @Override
    public boolean intersects(BoundingBox<ModelPosition> otherBox)
    {
        return overlaps(otherBox, 0.0);
    }

    @Override
    public boolean overlaps(Quadrilateral<? extends ModelPosition> other, double tolerance)
    {
        // TODO This needs some work.
        return other.contains(myGreatestPosition, tolerance) || other.contains(myLeastPosition, tolerance);
    }

    @Override
    public String toSimpleString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("[(").append(myLeastPosition.getX()).append(',');
        sb.append(myLeastPosition.getY()).append(',');
        sb.append(myLeastPosition.getZ()).append("),(");
        sb.append(myGreatestPosition.getX()).append(',');
        sb.append(myGreatestPosition.getY()).append(',');
        sb.append(myGreatestPosition.getZ()).append(")]");

        return sb.toString();
    }

    @Override
    public BoundingBox<ModelPosition> union(BoundingBox<ModelPosition> otherBox)
    {
        throw new UnsupportedOperationException("Currently union of ModelBoundingCube is not supported.");
    }
}
