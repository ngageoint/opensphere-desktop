package io.opensphere.mantle.data.util.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Utilities for actions that can be taken with a DataTypeInfo.
 */
public final class DataTypeActionUtils
{

    /**
     * Goes to the data type's locations if they exist.
     *
     * @param dataType the data type
     * @param viewer the viewer
     */
    public static void gotoDataType(DataTypeInfo dataType, DynamicViewer viewer)
    {
        GeographicBoundingBox bbox = dataType.getBoundingBox();
        if (bbox != null)
        {
            gotoBoundingBox(bbox, viewer);
        }
    }

    /**
     * Goes to the locations, with some buffer around them.
     *
     * @param locations the locations
     * @param viewer the viewer
     */
    public static void gotoLocations(Collection<? extends LatLonAlt> locations, DynamicViewer viewer)
    {
        GeographicBoundingBox bbox = GeographicBoundingBox.getMinimumBoundingBoxLLA(locations);
        gotoBoundingBox(bbox, viewer);
    }

    /**
     * Goes to the bounding box.
     *
     * @param bbox the bounding box
     * @param viewer the viewer
     */
    public static void gotoBoundingBox(GeographicBoundingBox bbox, DynamicViewer viewer)
    {
        boolean isPoint = bbox.getWidth() == 0 && bbox.getHeight() == 0;
        GeographicBoundingBox flyToBbox;
        if (isPoint)
        {
            flyToBbox = createBbox(bbox.getLowerLeft().getLatLonAlt());
        }
        else
        {
            flyToBbox = expandBbox(bbox);
        }
        List<GeographicPosition> vertices = new ArrayList<>(2);
        vertices.add(flyToBbox.getLowerLeft());
        vertices.add(flyToBbox.getUpperRight());
        animate(viewer, vertices);
    }

    private static void animate(DynamicViewer viewer, List<GeographicPosition> vertices)
    {
        if (viewer.isFlyTo())
        {
            new ViewerAnimator(viewer, vertices, true).start();
        }
        else
        {
            new ViewerAnimator(viewer, vertices, true).snapToPosition();
        }
    }

    /**
     * Creates a bounding box for a point.
     *
     * @param point the point
     * @return the bounding box
     */
    private static GeographicBoundingBox createBbox(LatLonAlt point)
    {
        final double buffer = 0.005;
        final double altitudeScale = 1.5;
        double altitudeBuffer = point.getAltM() / altitudeScale;
        LatLonAlt lowerLeft = LatLonAlt.createFromDegreesMeters(point.getLatD() - buffer, point.getLonD() - buffer,
                point.getAltM() + altitudeBuffer, ReferenceLevel.ELLIPSOID);
        LatLonAlt upperRight = LatLonAlt.createFromDegreesMeters(point.getLatD() + buffer, point.getLonD() + buffer,
                point.getAltM() + altitudeBuffer, ReferenceLevel.ELLIPSOID);
        return new GeographicBoundingBox(lowerLeft, upperRight);
    }

    /**
     * Applies a buffer around the given bounding box.
     *
     * @param bbox the bounding box
     * @return the expanded bounding box
     */
    private static GeographicBoundingBox expandBbox(GeographicBoundingBox bbox)
    {
        final double bufferRatio = 0.2;
        double latBuffer = bbox.getDeltaLatD() * bufferRatio;
        double lonBuffer = bbox.getDeltaLonD() * bufferRatio;
        LatLonAlt lowerLeft = bbox.getLowerLeft().getLatLonAlt();
        lowerLeft = LatLonAlt.createFromDegrees(lowerLeft.getLatD() - latBuffer, lowerLeft.getLonD() - lonBuffer);
        LatLonAlt upperRight = bbox.getUpperRight().getLatLonAlt();
        upperRight = LatLonAlt.createFromDegrees(upperRight.getLatD() + latBuffer, upperRight.getLonD() + lonBuffer);
        return new GeographicBoundingBox(lowerLeft, upperRight);
    }

    /** Private constructor. */
    private DataTypeActionUtils()
    {
    }
}
