package io.opensphere.controlpanels.component.map.boundingbox;

import java.util.Collection;
import java.util.List;
import java.util.Observable;

import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.util.collections.New;

/**
 * Contains the data used by the BoundingBoxOverlay.
 */
public class BoundingBoxModel extends Observable
{
    /**
     * The bounding box property.
     */
    public static final String BOUNDING_BOX_PROP = "boundingBox";

    /**
     * The bounding box to draw.
     */
    private final List<LineSegment2d> myBoundingBox = New.list();

    /**
     * Gets the bounding box to draw.
     *
     * @return The bounding box.
     */
    public Collection<LineSegment2d> getBoundingBox()
    {
        return New.unmodifiableList(myBoundingBox);
    }

    /**
     * Sets the bounding box to draw.
     *
     * @param boundingBox the bounding box.
     */
    public void setBoundingBox(Collection<LineSegment2d> boundingBox)
    {
        myBoundingBox.clear();
        myBoundingBox.addAll(boundingBox);
        setChanged();
        notifyObservers(BOUNDING_BOX_PROP);
    }
}
