package io.opensphere.mantle.data.impl;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.mantle.data.GeographicExtents;

/**
 * Stores one or more {@link GeographicBoundingBox}'s.
 */
public class DefaultGeographicExtents implements GeographicExtents
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The bounding boxes. */
    private final List<GeographicBoundingBox> myBoundingBoxes;

    /**
     * CTOR.
     */
    public DefaultGeographicExtents()
    {
        myBoundingBoxes = new ArrayList<>();
    }

    /**
     * CTOR with single geographic bounding box.
     *
     * @param bb - the bounding box.
     */
    public DefaultGeographicExtents(GeographicBoundingBox bb)
    {
        myBoundingBoxes = new ArrayList<>();
        myBoundingBoxes.add(bb);
    }

    /**
     * Gets the bounding boxes.
     *
     * @return the bounding box list
     */
    @Override
    public List<GeographicBoundingBox> getBoundingBoxes()
    {
        return myBoundingBoxes;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append(getClass().getSimpleName()).append(": NumRegions: ")
                .append(myBoundingBoxes == null ? 0 : myBoundingBoxes.size()).append('\n');
        if (myBoundingBoxes != null)
        {
            for (GeographicBoundingBox bb : myBoundingBoxes)
            {
                sb.append("  ").append(bb.toString()).append('\n');
            }
        }
        return sb.toString();
    }
}
