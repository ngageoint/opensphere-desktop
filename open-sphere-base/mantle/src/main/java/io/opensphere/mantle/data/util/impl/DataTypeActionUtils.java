package io.opensphere.mantle.data.util.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.opensphere.core.Toolbox;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Utilities for actions that can be taken with a DataTypeInfo.
 */
public final class DataTypeActionUtils
{
    /**
     * Goes to the data type's locations if they exist.
     *
     * @param dataType the data type
     * @param toolbox the toolbox through which application state is accessed.
     */
    public static void gotoDataType(DataTypeInfo dataType, Toolbox toolbox)
    {
        DataTypeActionUtils.gotoDataType(dataType, toolbox, true);
    }

    /**
     * Goes to the data type's location if they exist.
     *
     * @param dataType the data type
     * @param toolbox the toolbox through which application state is accessed
     * @param zoom zooms in on the location if true
     */
    public static void gotoDataType(DataTypeInfo dataType, Toolbox toolbox, boolean zoom)
    {
        DataTypeActionUtils.gotoDataType(dataType, toolbox.getMapManager().getStandardViewer(), toolbox, zoom);
    }

    /**
     * Goes to the data type's locations if they exist.
     *
     * @param dataType the data type
     * @param viewer the viewer
     * @param toolbox the toolbox through which application state is accessed.
     * @param zoom zooms in on the location if true
     */
    public static void gotoDataType(DataTypeInfo dataType, DynamicViewer viewer, Toolbox toolbox, boolean zoom)
    {
        GeographicBoundingBox bbox = dataType.getBoundingBox();
        if (bbox == null)
        {
            List<DataElement> dataElements = MantleToolboxUtils.getDataElementLookupUtils(toolbox).getDataElements(dataType);

            bbox = GeographicBoundingBox.merge(dataElements.stream().filter(de -> de instanceof MapDataElement).map(
                    mde -> ((MapDataElement)mde).getMapGeometrySupport().getBoundingBox(toolbox.getMapManager().getProjection()))
                    .collect(Collectors.toList()));
        }
        if (bbox != null)
        {
            gotoBoundingBox(bbox, viewer, true, zoom);
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
        gotoBoundingBox(bbox, viewer, true, true);
    }

    /**
     * Goes to the bounding box.
     *
     * @param bbox the bounding box
     * @param viewer the viewer
     * @param flyTo fly to bounding box if true, snap to if false
     * @param zoom zooms in on the location if true
     */
    public static void gotoBoundingBox(GeographicBoundingBox bbox, DynamicViewer viewer, boolean flyTo, boolean zoom)
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
        if (flyTo)
        {
            new ViewerAnimator(viewer, vertices, zoom).start();
        }
        else
        {
            new ViewerAnimator(viewer, vertices, zoom).snapToPosition();
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
