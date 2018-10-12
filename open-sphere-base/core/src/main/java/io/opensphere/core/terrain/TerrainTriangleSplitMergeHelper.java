package io.opensphere.core.terrain;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.viewer.Viewer;

/**
 * A helper to contain the data and methods necessary for handling splitting and
 * merging. When a triangle is petrified, it no longer requires a helper.
 */
@SuppressWarnings("PMD.GodClass")
public class TerrainTriangleSplitMergeHelper
{
    /**
     * To prevent excessive triangles at the poles, triangles beyond this
     * latitude (North or South) are not split.
     */
    private static final double LATITUDE_LIMIT = 75.;

    /** Longitude beyond which a point is considered to be at 180. */
    private static final double LONGITUDE_VERTEX_SHARE_LIMIT = 179.9;

    /**
     * The amount which the merge size is greater than the split size. This
     * number should always be greater than 1.
     */
    private static final double MERGE_SPLIT_FACTOR = 1.5;

    /** Pixel size at which to merge triangles. */
    private static double ourMergeViewSize;

    /** Pixel size at which to split triangles. */
    private static double ourSplitViewSize;

    /** The size of this triangle on the celestial body's model. */
    private final double myArcSize;

    /** the generation at which I reside. */
    private final int myGeneration;

    /**
     * True when this triangle intersects the view volume and faces the viewer.
     */
    private boolean myInView;

    /**
     * The minimum amount of variance between the arc size of the triangle and
     * the distance of the split point to the triangle's plane required for
     * splitting triangles at the lowest level.
     */
    private double myMinVariance;

    /** The triangle for which this helper is an agent. */
    private final TerrainTriangle myOwner;

    /**
     * Hint for how dense the resolution of the terrain should be. This is used
     * to determine splitting and merging.
     */
    private double myResolutionHintM;

    /** The estimated current pixel size of this triangle. */
    private double myViewSize;

    static
    {
        // Initialize the split/merge settings to reasonable values.
        final double defMergeSize = 80.;
        ourMergeViewSize = defMergeSize;
        ourSplitViewSize = ourMergeViewSize * MERGE_SPLIT_FACTOR;
    }

    /**
     * Set the pixel size at which to merge triangles. This method also sets the
     * split size to ensure consistency.
     *
     * @param mergeViewSize the mergeViewSize to set
     */
    public static void setMergeViewSize(double mergeViewSize)
    {
        ourMergeViewSize = mergeViewSize;
        ourSplitViewSize = ourMergeViewSize * MERGE_SPLIT_FACTOR;
    }

    /**
     * Constructor.
     *
     * @param owner The triangle for which this helper is an agent.
     */
    public TerrainTriangleSplitMergeHelper(TerrainTriangle owner)
    {
        myOwner = owner;
        myArcSize = calculateArcSize();
        myGeneration = myOwner.getParent() == null ? 0 : myOwner.getParent().getSplitMergeHelper().getGeneration() + 1;
    }

    /**
     * Check to see whether I should be merged.
     *
     * @param bounds The region over which merging should occur.
     */
    public void checkMerge(GeographicBoundingBox bounds)
    {
        if (bounds != null && !bounds.overlaps(myOwner.getGeographicPolygon().getBoundingBox(), 0.))
        {
            return;
        }

        if (myOwner.getLeftChild() != null)
        {
            myOwner.getLeftChild().checkMerge(bounds);

            if (myOwner.getRightChild() != null)
            {
                myOwner.getRightChild().checkMerge(bounds);
            }
        }
        else if (myOwner.getParent() != null && myOwner.getParent().getSplitMergeHelper().mergeConditionsSatisfied())
        {
            myOwner.getParent().getSplitMergeHelper().merge(false, bounds);
        }
    }

    /**
     * Check to see if this triangle or any children should be merged because of
     * variance and perform the merge if necessary.
     *
     * @param bounds The region over which merging should occur.
     */
    public void checkMergeForVariance(GeographicBoundingBox bounds)
    {
        if (bounds != null && !bounds.overlaps(myOwner.getGeographicPolygon().getBoundingBox(), 0.))
        {
            return;
        }

        if (myOwner.getLeftChild() != null)
        {
            myOwner.getLeftChild().checkMergeForVariance(bounds);
            if (myOwner.getRightChild() != null)
            {
                myOwner.getRightChild().checkMergeForVariance(bounds);
            }
        }
        else if (myOwner.getParent() != null && myOwner.getParent().getSplitMergeHelper().mergeForVarianceConditionsSatisfied())
        {
            myOwner.getParent().getSplitMergeHelper().merge(true, bounds);
        }
    }

    /**
     * Check to see whether this triangle and any children should be split and
     * perform the splits as necessary.
     *
     * @param view the current viewer.
     * @param bounds The region over which splitting should occur.
     */
    public void checkSplit(Viewer view, GeographicBoundingBox bounds)
    {
        if (myGeneration > myOwner.getGlobe().getMaximumGenerations() || myGeneration > myOwner.getGlobe().getMinimumGenerations()
                && bounds != null && !bounds.overlaps(myOwner.getGeographicPolygon().getBoundingBox(), 0.))
        {
            return;
        }

        if (myOwner.getLeftChild() != null)
        {
            myOwner.getLeftChild().checkSplit(view, bounds);
            myOwner.getRightChild().checkSplit(view, bounds);
        }
        else
        {
            if (splitConditionsSatisfied() && split(view))
            {
                // No need to merge the descendant bounds since
                // they will be contained in my bounds.
                myOwner.getLeftChild().checkSplit(view, bounds);
                myOwner.getRightChild().checkSplit(view, bounds);
            }
        }
    }

    /**
     * Get the arcSize.
     *
     * @return the arcSize
     */
    public double getArcSize()
    {
        return myArcSize;
    }

    /**
     * Get the generation.
     *
     * @return the generation
     */
    public int getGeneration()
    {
        return myGeneration;
    }

    /**
     * Get the minVariance.
     *
     * @return the minVariance
     */
    public double getMinVariance()
    {
        return myMinVariance;
    }

    /**
     * Get the resolutionHintM.
     *
     * @return the resolutionHintM
     */
    public double getResolutionHintM()
    {
        return myResolutionHintM;
    }

    /**
     * Get the viewSize.
     *
     * @return the viewSize
     */
    public double getViewSize()
    {
        return myViewSize;
    }

    /**
     * Get the inView.
     *
     * @return the inView
     */
    public boolean isInView()
    {
        return myInView;
    }

    /** Determine the resolution hint for this triangle and any children. */
    public void resetElevationParameters()
    {
        if (myOwner.getElevationProvider() != null)
        {
            myResolutionHintM = myOwner.getElevationProvider().getResolutionHintM();
            myMinVariance = myOwner.getElevationProvider().getMinVariance();
        }
        else
        {
            myResolutionHintM = myOwner.getGlobe().getCelestialBody().getElevationManager()
                    .getHighestOverlappingResolution(myOwner.getGeographicPolygon());
            myMinVariance = myOwner.getGlobe().getCelestialBody().getElevationManager()
                    .getMinVariance(myOwner.getGeographicPolygon());
        }
    }

    /**
     * Reset the size of this triangle in the view based on the given viewer.
     *
     * @param view the current viewer.
     */
    public void resetViewSize(Viewer view)
    {
        myInView = myOwner.isInView(view) && myOwner.getPlane().isInFront(view.getPosition().getLocation(), 0.);
        if (myInView || getResolutionHintM() >= 0.)
        {
            double modelWidth = view.getViewVolumeWidthAt(myOwner.getModelBoundingSphere().getCenter());
            double viewportWidth = view.getViewportWidth();
            myViewSize = myArcSize * viewportWidth / modelWidth;
        }
        else
        {
            myViewSize = -1.;
        }
    }

    /**
     * Split triangles until the desired the minimum number of generations has
     * been created.
     */
    public void splitToMinGeneration()
    {
        if (myGeneration < myOwner.getGlobe().getMinimumGenerations())
        {
            split(null);
            if (myOwner.getLeftChild() != null)
            {
                myOwner.getLeftChild().getSplitMergeHelper().splitToMinGeneration();
                myOwner.getRightChild().getSplitMergeHelper().splitToMinGeneration();
            }
        }
    }

    /**
     * Remove children for this triangle and any children or neighbors as
     * required to prevent seams.
     *
     * @param forVariance When true this merge is because there is little
     *            resolution gained from the split.
     * @param bounds The region over which merging should occur.
     */
    protected void merge(boolean forVariance, GeographicBoundingBox bounds)
    {
        if (myOwner.getLeftChild() == null || myOwner.getRightChild() == null)
        {
            return;
        }

        if (myOwner.getAdjacentC() == null)
        {
            // simple merge
            myOwner.resetAdjacentsForMerge();
            myOwner.setLeftChild(null);
            myOwner.setRightChild(null);
        }
        else
        {
            // merge me and my adjacent C at the same time.
            myOwner.resetAdjacentsForMerge();
            myOwner.getAdjacentC().resetAdjacentsForMerge();

            myOwner.setLeftChild(null);
            myOwner.setRightChild(null);
            myOwner.getAdjacentC().setLeftChild(null);
            myOwner.getAdjacentC().setRightChild(null);

            // Since we skipped checking merges until we got to the bottom, try
            // back up the parents to see if they can be merged.
            if (forVariance)
            {
                checkMergeForVariance(bounds);
                myOwner.getAdjacentC().getParent().checkMergeForVariance(bounds);
                return;
            }
            checkMerge(bounds);
            myOwner.getAdjacentC().getParent().checkMerge(bounds);
        }
    }

    /**
     * Check to see whether the conditions for merging this triangle are
     * satisfied.
     *
     * @return true when the conditions for merging this triangle are satisfied.
     */
    protected boolean mergeConditionsSatisfied()
    {
        if (myOwner.getLeftChild() == null || myOwner.getLeftChild().getLeftChild() != null
                || myOwner.getRightChild().getLeftChild() != null || myGeneration < myOwner.getGlobe().getMinimumGenerations() - 1
                || splitConditionsSatisfied())
        {
            return false;
        }

        if (myOwner.getAdjacentC() != null)
        {
            if (!Utilities.sameInstance(myOwner, myOwner.getAdjacentC().getAdjacentC())
                    || myOwner.getAdjacentC().getSplitMergeHelper().splitConditionsSatisfied())
            {
                return false;
            }

            if (myOwner.getAdjacentC().getLeftChild() == null || myOwner.getAdjacentC().getLeftChild().getLeftChild() != null
                    || myOwner.getAdjacentC().getRightChild().getLeftChild() != null)
            {
                return false;
            }
        }

        if (!myInView)
        {
            return true;
        }

        if (myResolutionHintM < 0.)
        {
            return myOwner.getLeftChild().getSplitMergeHelper().getViewSize() < ourMergeViewSize
                    || myOwner.getRightChild().getSplitMergeHelper().getViewSize() < ourMergeViewSize;
        }
        return myOwner.getLeftChild().getSplitMergeHelper().getArcSize() < myResolutionHintM
                || myOwner.getRightChild().getSplitMergeHelper().getArcSize() < myResolutionHintM;
    }

    /**
     * Check whether the conditions for merging because of variance have been
     * satisfied.
     *
     * @return true if the conditions for merging because of variance have been
     *         satisfied.
     */
    protected boolean mergeForVarianceConditionsSatisfied()
    {
        if (myOwner.getLeftChild() == null || myOwner.getLeftChild().getLeftChild() != null
                || myOwner.getRightChild().getLeftChild() != null
                || myGeneration < myOwner.getGlobe().getMinimumGenerations() - 1)
        {
            return false;
        }

        if (myOwner.getAdjacentC() != null)
        {
            if (!Utilities.sameInstance(myOwner, myOwner.getAdjacentC().getAdjacentC()))
            {
                return false;
            }

            if (myOwner.getAdjacentC().getLeftChild() == null || myOwner.getAdjacentC().getLeftChild().getLeftChild() != null
                    || myOwner.getAdjacentC().getRightChild().getLeftChild() != null)
            {
                return false;
            }
        }

        return !myOwner.isDegenerateOnGlobe() && (getVarianceAtMidPoint() < myMinVariance && myOwner.getAdjacentC() == null
                || myOwner.getAdjacentC().getSplitMergeHelper().getVarianceAtMidPoint() < myOwner.getAdjacentC()
                .getSplitMergeHelper().getMinVariance());
    }

    /**
     * Get the maximum linear size of the triangle on the model.
     *
     * @return the maximum linear size of the triangle on the model.
     */
    private double calculateArcSize()
    {
        LatLonAlt ptA = myOwner.getVertexA().getCoordinates().getLatLonAlt();
        LatLonAlt ptB = myOwner.getVertexB().getCoordinates().getLatLonAlt();
        LatLonAlt ptC = myOwner.getVertexC().getCoordinates().getLatLonAlt();
        return Math.max(GeographicBody3D.greatCircleDistanceM(ptA, ptB, WGS84EarthConstants.RADIUS_MEAN_M),
                GeographicBody3D.greatCircleDistanceM(ptA, ptC, WGS84EarthConstants.RADIUS_MEAN_M));
    }

    /**
     * Get the vertex which should be used to split this triangle.
     *
     * @return The vertex which divides my hypotenuse.
     */
    private TerrainVertex getSplitMidVertex()
    {
        double midLat = (myOwner.getVertexA().getCoordinates().getLatLonAlt().getLatD()
                + myOwner.getVertexB().getCoordinates().getLatLonAlt().getLatD()) * .5;
        double midLon = (myOwner.getVertexA().getCoordinates().getLatLonAlt().getLonD()
                + myOwner.getVertexB().getCoordinates().getLatLonAlt().getLonD()) * .5;
        GeographicPosition mid = new GeographicPosition(LatLonAlt.createFromDegrees(midLat, midLon));
        // Use the globe's method to get the model position since it isn't
        // certain that my elevation provider can be used for the mid position.
        Vector3d midPoint = myOwner.getGlobe().getElevationAdjustedModelPosition(mid, Vector3d.ORIGIN);
        return new TerrainVertex(mid, midPoint);
    }

    /**
     * Get the relative difference in size between the arc size of the triangle
     * and the distance of the split point to the triangle's plane.
     *
     * @return the variance.
     */
    private double getVarianceAtMidPoint()
    {
        if (myOwner.getLeftChild() != null)
        {
            return myOwner.getPlane().getDistance(myOwner.getLeftChild().getVertexC().getModelCoordinates()) / myArcSize;
        }
        return myOwner.getPlane().getDistance(getSplitMidVertex().getModelCoordinates()) / myArcSize;
    }

    /**
     * Reset the view size and the elevation related information for the child
     * of the given triangle.
     *
     * @param parent The triangle whose children require resetting..
     * @param view The current view.
     */
    private void resetChildSizeAndElevation(TerrainTriangle parent, Viewer view)
    {
        parent.getLeftChild().resetElevationParameters();
        parent.getLeftChild().resetViewSize(view);
        parent.getLeftChild().resetElevationProvider();
        parent.getRightChild().resetElevationParameters();
        parent.getRightChild().resetViewSize(view);
        parent.getRightChild().resetElevationProvider();
    }

    /**
     * Divide this triangle and any neighbors as necessary to prevent seams.
     *
     * @param view the current viewer.
     * @return true if the split occurred and false otherwise.
     */
    private boolean split(Viewer view)
    {
        if (myOwner.getLeftChild() != null || myOwner.getAdjacentC() != null && myOwner.getAdjacentC().isPetrified())
        {
            return false;
        }

        if (myOwner.getAdjacentC() == null)
        {
            // If I have no adjacent C, just split me
            myOwner.createChildren(getSplitMidVertex());

            myOwner.getAdjacentA().replaceAdjacent(myOwner, myOwner.getRightChild());
            myOwner.getAdjacentB().replaceAdjacent(myOwner, myOwner.getLeftChild());

            myOwner.getLeftChild().setAdjacentB(myOwner.getRightChild());
            myOwner.getLeftChild().setAdjacentC(myOwner.getAdjacentB());

            myOwner.getRightChild().setAdjacentA(myOwner.getLeftChild());
            myOwner.getRightChild().setAdjacentC(myOwner.getAdjacentA());

            resetChildSizeAndElevation(myOwner, view);
        }
        else if (Utilities.sameInstance(myOwner.getAdjacentC().getAdjacentC(), myOwner))
        {
            // If I am my adjacent c's adjacent C, then split both of us.
            TerrainVertex splitVert = getSplitMidVertex();
            myOwner.createChildren(splitVert);

            if (Math.abs(splitVert.getCoordinates().getLatLonAlt().getLonD()) > LONGITUDE_VERTEX_SHARE_LIMIT)
            {
                splitVert = myOwner.getAdjacentC().getSplitMergeHelper().getSplitMidVertex();
            }
            myOwner.getAdjacentC().createChildren(splitVert);

            myOwner.setChildAdjacents();
            myOwner.getAdjacentC().setChildAdjacents();

            resetChildSizeAndElevation(myOwner, view);
            resetChildSizeAndElevation(myOwner.getAdjacentC(), view);
        }
        else
        {
            // my adjacent C needs to be split before I can split.
            if (myOwner.getAdjacentC().getSplitMergeHelper().split(view))
            {
                return split(view);
            }
            return false;
        }

        return true;
    }

    /**
     * Check to see whether the conditions required for splitting this triangle
     * are satisfied.
     *
     * @return True when this triangle should be split.
     */
    private boolean splitConditionsSatisfied()
    {
        if (myGeneration < myOwner.getGlobe().getMinimumGenerations() - 1)
        {
            return true;
        }

        if (!myInView && myResolutionHintM < 0.
                || Math.abs(myOwner.getVertexA().getCoordinates().getLatLonAlt().getLatD()) > LATITUDE_LIMIT)
        {
            return false;
        }

        if (myResolutionHintM < 0.)
        {
            if (myViewSize > ourSplitViewSize)
            {
                return true;
            }
        }
        else
        {
            if (myArcSize > myResolutionHintM)
            {
                return true;
            }
        }

        return false;
    }
}
