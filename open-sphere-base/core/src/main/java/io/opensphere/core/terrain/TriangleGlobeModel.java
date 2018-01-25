package io.opensphere.core.terrain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicConvexPolygon;
import io.opensphere.core.model.GeographicPolygon;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.SimpleTesseraBlockBuilder;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.model.TesseraList.TesseraBlock;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.projection.GeographicProjectionModel;
import io.opensphere.core.terrain.util.AbsoluteElevationProvider;
import io.opensphere.core.terrain.util.ElevationChangedEvent;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.viewer.Viewer;

/**
 * A 3D model of the globe made from triangles. This model should not be used
 * for generating model positions for geometries which will appear on the model
 * as it may be changing at any given time.
 */
@SuppressWarnings("PMD.GodClass")
public class TriangleGlobeModel extends GeographicProjectionModel
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TriangleGlobeModel.class);

    /** The celestial body I model. */
    private final GeographicBody3D myCelestialBody;

    /** The viewer from the most recent view update. */
    private Viewer myCurrentViewer;

    /** The snapshot of my most recent state. */
    private volatile ImmutableTriangleGlobeModel myGlobeSnapshot;

    /** True when high accuracy processing is in use. */
    private boolean myHighAccuracy;

    /** The lock for synchronizing access to my backing model. */
    private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock();

    /** Maximum number of generations for the triangles of this globe. */
    private final int myMaximumGenerations;

    /** Minimum number of generations for the triangles of this globe. */
    private final int myMinimumGenerations;

    /** The largest triangle whose a and b vertices are on the south pole. */
    private TerrainTriangle myNorthBottom;

    /** The currently petrified terrain for this globe. */
    private final Map<AbsoluteElevationProvider, Map<GeographicPolygon, TesseraList<? extends GeographicProjectedTesseraVertex>>> myPetrifiedTerrainBlocks = New
            .map();

    /**
     * The elevation provider which petrifies terrain, if any exists. There can
     * be at most one petrified region.
     */
    private AbsoluteElevationProvider myPetrifyingElevationProvider;

    /** The largest triangle whose a and b vertices are on the south pole. */
    private TerrainTriangle mySouthBottom;

    /**
     * Construct this globe.
     *
     * @param minimumGenerations minimum number of generations for the triangles
     *            of this globe.
     * @param maximumGenerations minimum number of generations for the triangles
     *            of this globe.
     * @param body projection which I model.
     */
    public TriangleGlobeModel(int minimumGenerations, int maximumGenerations, GeographicBody3D body)
    {
        myCelestialBody = body;
        myMinimumGenerations = minimumGenerations;
        myMaximumGenerations = maximumGenerations;

        myLock.writeLock().lock();
        try
        {
            createBaseGlobe();
        }
        finally
        {
            myLock.writeLock().unlock();
        }
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertPolygonToModelMesh(Polygon polygon,
            Vector3d modelCenter)
    {
        throw new UnsupportedOperationException("Cannot get non-petrified tesserae for a mutable globe.");
    }

    @Override
    public void generateModelSnapshot()
    {
        myLock.writeLock().lock();
        try
        {
            myGlobeSnapshot = new ImmutableTriangleGlobeModel(this);
        }
        finally
        {
            myLock.writeLock().unlock();
        }
    }

    @Override
    public GeographicBody3D getCelestialBody()
    {
        return myCelestialBody;
    }

    @Override
    public Vector3d getCelestialBodyModelPosition(GeographicPosition inPos, Vector3d modelCenter)
    {
        return myCelestialBody.convertToModel(inPos, modelCenter);
    }

    /**
     * Find the triangle which contains the geographic location. If the
     * containing triangle is degenerate in the model, a nearby triangle will be
     * returned.
     *
     * @param inPos position for which the triangle is desired.
     * @param quickCheck When provided, this triangle will be checked first. If
     *            it contains the point, it will be returned.
     * @return the containing triangle or one close to it.
     */
    public TerrainTriangle getContainingTriangle(GeographicPosition inPos, TerrainTriangle quickCheck)
    {
        // TODO should this be disallowed?
        return myGlobeSnapshot.getContainingTriangle(inPos, quickCheck);
    }

    /**
     * Get the model position which is based on the celestial body, but adjusted
     * in altitude by the amount given by the elevation provider.
     *
     * @param pos The geographic position whose model position is desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return The elevation adjusted model position.
     */
    public Vector3d getElevationAdjustedModelPosition(GeographicPosition pos, Vector3d modelCenter)
    {
        LatLonAlt lla = pos.getLatLonAlt();
        double elevationM = lla.getAltM();
        elevationM += myCelestialBody.getElevationManager().getElevationM(pos, true);
        GeographicPosition posToConvert = new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(lla.getLatD(), lla.getLonD(), elevationM, Altitude.ReferenceLevel.ELLIPSOID));
        return myCelestialBody.convertToModel(posToConvert, modelCenter);
    }

    @Override
    public double getElevationOnTerrainM(GeographicPosition position)
    {
        return myGlobeSnapshot.getElevationOnTerrainM(position);
    }

    /**
     * Get the maximumGenerations.
     *
     * @return the maximumGenerations
     */
    public int getMaximumGenerations()
    {
        return myMaximumGenerations;
    }

    /**
     * Get the minimumGenerations.
     *
     * @return the minimumGenerations
     */
    public int getMinimumGenerations()
    {
        return myMinimumGenerations;
    }

    @Override
    public double getMinimumInviewDistance(Viewer view)
    {
        return myGlobeSnapshot.getMinimumInviewDistance(view);
    }

    @Override
    public TriangleGlobeModel getModelSnapshot()
    {
        if (myGlobeSnapshot == null)
        {
            generateModelSnapshot();
        }
        return myGlobeSnapshot;
    }

    @Override
    public String getName()
    {
        return "Triangle Globe Model";
    }

    @Override
    public Vector3d getNormalAtPosition(GeographicPosition inPos)
    {
        return myGlobeSnapshot.getNormalAtPosition(inPos);
    }

    /**
     * Get the petrifiedTerrainBlocks.
     *
     * @return the petrifiedTerrainBlocks
     */
    public Map<AbsoluteElevationProvider, Map<GeographicPolygon, TesseraList<? extends GeographicProjectedTesseraVertex>>> getPetrifiedTerrainBlocks()
    {
        return myPetrifiedTerrainBlocks;
    }

    /**
     * Get the tesserae for the triangles which are petrified within the given
     * region.
     *
     * @param polygon The polygon within which the tesserae is desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return The tesserae for the triangles which are petrified within the
     *         given region.
     */
    public TesseraList<? extends GeographicProjectedTesseraVertex> getPetrifiedTesserae(GeographicConvexPolygon polygon,
            Vector3d modelCenter)
    {
        SimpleTesseraBlockBuilder<TerrainVertex> triBuilder = new SimpleTesseraBlockBuilder<>(3, modelCenter);
        SimpleTesseraBlockBuilder<TerrainVertex> quadBuilder = new SimpleTesseraBlockBuilder<>(4, modelCenter);

        // When getting the tesserae, include only petrified regions.
        getNorthBottom().getTesserae(triBuilder, quadBuilder, polygon, true);
        getSouthBottom().getTesserae(triBuilder, quadBuilder, polygon, true);

        List<TesseraBlock<TerrainVertex>> tess = new ArrayList<>(2);
        if (!triBuilder.getBlockVertices().isEmpty())
        {
            tess.add(new TesseraBlock<TerrainVertex>(triBuilder, true));
        }
        if (!quadBuilder.getBlockVertices().isEmpty())
        {
            tess.add(new TesseraBlock<TerrainVertex>(quadBuilder, true));
        }
        return new TesseraList<TerrainVertex>(tess);
    }

    @Override
    public Vector3d getSurfaceIntersection(Vector3d pointA, Vector3d pointB)
    {
        return myCelestialBody.getSurfaceIntersection(pointA, pointB);
    }

    @Override
    public Vector3d getTerrainIntersection(Ray3d ray, Viewer view)
    {
        // TODO should this be disallowed?
        return myGlobeSnapshot.getTerrainIntersection(ray, view);
    }

    @Override
    public Vector3d getTerrainModelPosition(GeographicPosition pos, Vector3d modelCenter)
    {
        // TODO should this be disallowed?
        return myGlobeSnapshot.getTerrainModelPosition(pos, modelCenter);
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> getTesserae(GeographicConvexPolygon polygon,
            Vector3d modelCenter)
    {
        throw new UnsupportedOperationException("Cannot get non-petrified tesserae for a mutable globe.");
    }

    /**
     * Get the model point at sea level.
     *
     * @param pos location for which to get the model point
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return the model point.
     */
    public Vector3d getVertexIgnoreAltitude(GeographicPosition pos, Vector3d modelCenter)
    {
        return myCelestialBody.convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(pos.getLatLonAlt().getLatD(),
                pos.getLatLonAlt().getLonD(), Altitude.ReferenceLevel.ELLIPSOID)), modelCenter);
    }

    @Override
    public Collection<GeographicBoundingBox> handleElevationChange(ElevationChangedEvent event)
    {
        myLock.writeLock().lock();
        try
        {
            Collection<GeographicBoundingBox> affectedBounds = null;
            switch (event.getChangeType())
            {
                case PROVIDER_ADDED:
                case PROVIDER_PRIORITY_CHANGED:
                case PROVIDER_REMOVED:
                    // TODO there is some room for efficiency improvement here.
                    myPetrifyingElevationProvider = null;
                    myPetrifiedTerrainBlocks.clear();
                    createBaseGlobe();

                    ElevationManager manager = getCelestialBody().getElevationManager();
                    Collection<AbsoluteElevationProvider> allProviders = manager.getProviders().values();
                    for (AbsoluteElevationProvider provider : allProviders)
                    {
                        if (provider.petrifiesTerrain())
                        {
                            myPetrifyingElevationProvider = provider;
                            GeographicBoundingBox bbox = provider.getBoundingBox();
                            createAndPetrify(provider, bbox);
                            createPetrifiedBlocks(provider);
                        }
                    }

                    doSplitsAndMerges(null);
                    affectedBounds = Collections.singleton(GeographicBoundingBox.WHOLE_GLOBE);
                    break;
                case TERRAIN_MODIFIED:
                    if (event.getChangedProviders().size() != 1)
                    {
                        LOGGER.error("Only one provider may modify terrain at a time.");
                        return null;
                    }

                    AbsoluteElevationProvider provider = event.getChangedProviders().iterator().next();
                    Collection<GeographicBoundingBox> combinedBounds = New.collection();
                    boolean foundNonOccludedRegion = false;
                    Collection<? extends GeographicPolygon> modifiedLocations = event.getChangedRegions() == null
                            ? provider.getRegions() : event.getChangedRegions();

                    for (GeographicPolygon region : modifiedLocations)
                    {
                        if (!getCelestialBody().getElevationManager().isOccluded(provider, region))
                        {
                            foundNonOccludedRegion = true;
                            combinedBounds.add(region.getBoundingBox());
                            // TODO Check to make sure this doesn't do anything
                            // to petrified regions.
                            myNorthBottom.modifyElevation(region, false);
                            mySouthBottom.modifyElevation(region, false);
                        }
                    }

                    // If the the new elevations are significantly closer to or
                    // farther from the viewer, splitting and merging may be
                    // required.
                    if (foundNonOccludedRegion)
                    {
                        combinedBounds.addAll(doSplitsAndMerges(null));
                    }
                    affectedBounds = combinedBounds;
                    break;
                default:
                    throw new UnexpectedEnumException(event.getChangeType());
            }
            return affectedBounds;
        }
        finally
        {
            myLock.writeLock().unlock();
        }
    }

    @Override
    public Collection<GeographicBoundingBox> handleModelDensityChanged(int density)
    {
        TerrainTriangleSplitMergeHelper.setMergeViewSize(density);
        myLock.writeLock().lock();
        try
        {
            return doSplitsAndMerges(null);
        }
        finally
        {
            myLock.writeLock().unlock();
        }
    }

    @Override
    public void setHighAccuracy(boolean highAccuracy)
    {
        if (myHighAccuracy == highAccuracy)
        {
            return;
        }
        myHighAccuracy = highAccuracy;

        myLock.writeLock().lock();
        try
        {
            if (myPetrifyingElevationProvider != null)
            {
                myPetrifiedTerrainBlocks.clear();
                createPetrifiedBlocks(myPetrifyingElevationProvider);
            }
        }
        finally
        {
            myLock.writeLock().unlock();
        }
    }

    @Override
    public Collection<GeographicBoundingBox> updateModelForView(Viewer view)
    {
        myLock.writeLock().lock();
        try
        {
            myCurrentViewer = view;
            myNorthBottom.resetViewSize(view);
            mySouthBottom.resetViewSize(view);

            return doSplitsAndMerges(null);
        }
        finally
        {
            myLock.writeLock().unlock();
        }
    }

    /** Create the globe from scratch. */
    protected final void createBaseGlobe()
    {
        GeographicPosition northPole = new GeographicPosition(LatLonAlt.createFromDegrees(90, 0));
        GeographicPosition southPole = new GeographicPosition(LatLonAlt.createFromDegrees(-90, 0));
        GeographicPosition topLeft = new GeographicPosition(LatLonAlt.createFromDegrees(90, -180));
        GeographicPosition topRight = new GeographicPosition(LatLonAlt.createFromDegrees(90, 180));
        GeographicPosition bottomLeft = new GeographicPosition(LatLonAlt.createFromDegrees(-90, -180));
        GeographicPosition bottomRight = new GeographicPosition(LatLonAlt.createFromDegrees(-90, 180));

        myNorthBottom = new NorthTerrainTriangle(northPole, northPole, bottomRight);
        myNorthBottom.setGlobe(this);

        mySouthBottom = new TerrainTriangle(null, bottomLeft, bottomRight, northPole);
        mySouthBottom.setGlobe(this);

        TerrainTriangle northLeft = new TerrainTriangle(myNorthBottom, bottomLeft, northPole, topLeft);
        northLeft.setGlobe(this);
        myNorthBottom.setLeftChild(northLeft);
        TerrainTriangle northRight = new TerrainTriangle(myNorthBottom, northPole, bottomRight, topRight);
        northRight.setGlobe(this);
        myNorthBottom.setRightChild(northRight);

        TerrainTriangle southLeft = new TerrainTriangle(mySouthBottom, northPole, bottomLeft, southPole);
        northLeft.setGlobe(this);
        mySouthBottom.setLeftChild(southLeft);
        TerrainTriangle southRight = new TerrainTriangle(mySouthBottom, bottomRight, northPole, southPole);
        northRight.setGlobe(this);
        mySouthBottom.setRightChild(southRight);

        myNorthBottom.setAdjacentA(southRight);
        myNorthBottom.setAdjacentB(southLeft);
        mySouthBottom.setAdjacentA(northRight);
        mySouthBottom.setAdjacentB(northLeft);

        northRight.setAdjacentA(northLeft);
        northLeft.setAdjacentB(northRight);

        northRight.setAdjacentC(southRight);
        northLeft.setAdjacentC(southLeft);

        southRight.setAdjacentA(southLeft);
        southLeft.setAdjacentB(southRight);

        southRight.setAdjacentC(northRight);
        southLeft.setAdjacentC(northLeft);

        mySouthBottom.getSplitMergeHelper().splitToMinGeneration();
        myNorthBottom.getSplitMergeHelper().splitToMinGeneration();

        if (myCelestialBody != null)
        {
            myNorthBottom.resetViewSize(myCurrentViewer);
            mySouthBottom.resetViewSize(myCurrentViewer);
        }
    }

    /**
     * Get the northBottom.
     *
     * @return the northBottom
     */
    protected TerrainTriangle getNorthBottom()
    {
        return myNorthBottom;
    }

    /**
     * Get the southBottom.
     *
     * @return the southBottom
     */
    protected TerrainTriangle getSouthBottom()
    {
        return mySouthBottom;
    }

    /**
     * Set the northBottom.
     *
     * @param northBottom the northBottom to set
     */
    protected void setNorthBottom(TerrainTriangle northBottom)
    {
        myNorthBottom = northBottom;
    }

    /**
     * Set the southBottom.
     *
     * @param southBottom the southBottom to set
     */
    protected void setSouthBottom(TerrainTriangle southBottom)
    {
        mySouthBottom = southBottom;
    }

    /**
     * Generate terrain for the bounding box and petrify it.
     *
     * @param provider The provider of the terrain.
     * @param bbox The region over which to generate terrain.
     */
    private void createAndPetrify(AbsoluteElevationProvider provider, GeographicBoundingBox bbox)
    {
        GeographicPosition ll = new GeographicPosition(LatLonAlt.createFromDegrees(bbox.getLowerLeft().getLatLonAlt().getLatD(),
                bbox.getLowerLeft().getLatLonAlt().getLonD(), ReferenceLevel.ELLIPSOID));
        GeographicPosition ur = new GeographicPosition(LatLonAlt.createFromDegrees(bbox.getUpperRight().getLatLonAlt().getLatD(),
                bbox.getUpperRight().getLatLonAlt().getLonD(), ReferenceLevel.ELLIPSOID));
        double diagM = getCelestialBody().convertToModel(ll, Vector3d.ORIGIN)
                .distance(getCelestialBody().convertToModel(ur, Vector3d.ORIGIN));

        if (diagM / provider.getResolutionHintM() > 500)
        {
            GeographicBoundingBox ulBox = new GeographicBoundingBox(bbox.getLeftCenter(), bbox.getUpperCenter());
            GeographicBoundingBox llBox = new GeographicBoundingBox(bbox.getLowerLeft(), bbox.getCenter());
            GeographicBoundingBox lrBox = new GeographicBoundingBox(bbox.getLowerCenter(), bbox.getRightCenter());
            GeographicBoundingBox urBox = new GeographicBoundingBox(bbox.getCenter(), bbox.getUpperRight());
            createAndPetrify(provider, ulBox);
            createAndPetrify(provider, llBox);
            createAndPetrify(provider, lrBox);
            createAndPetrify(provider, urBox);
        }
        else
        {
            doSplitsAndMerges(bbox);
            myNorthBottom.checkPetrify(provider, bbox);
            mySouthBottom.checkPetrify(provider, bbox);
        }
    }

    /**
     * Create the petrified blocks for the provider. The terrain terrain should
     * be generated before this is called.
     *
     * @param provider The provider for which to generate petrified terrain
     *            blocks.
     */
    private void createPetrifiedBlocks(AbsoluteElevationProvider provider)
    {
        myNorthBottom.checkPetrify(provider, null);
        mySouthBottom.checkPetrify(provider, null);

        Map<GeographicPolygon, TesseraList<? extends GeographicProjectedTesseraVertex>> blocks = myPetrifiedTerrainBlocks
                .get(provider);
        if (blocks == null)
        {
            blocks = New.map();
            myPetrifiedTerrainBlocks.put(provider, blocks);
        }

        // Build the tesserae for the petrified regions
        for (GeographicPolygon polygon : provider.getRegions())
        {
            if (polygon instanceof GeographicConvexPolygon)
            {
                Vector3d modelCenter;
                if (myHighAccuracy)
                {
                    GeographicPosition center = polygon.getBoundingBox().getCenter();
                    LatLonAlt position = LatLonAlt.createFromDegrees(center.getLatLonAlt().getLatD(),
                            center.getLatLonAlt().getLonD(), ReferenceLevel.ELLIPSOID);
                    modelCenter = myCelestialBody.convertToModel(new GeographicPosition(position), null);
                }
                else
                {
                    modelCenter = Vector3d.ORIGIN;
                }

                TesseraList<? extends GeographicProjectedTesseraVertex> tesserae = getPetrifiedTesserae(
                        (GeographicConvexPolygon)polygon, modelCenter);
                blocks.put(polygon, tesserae);
            }
            else
            {
                LOGGER.error("Non-convex polygons are not supported for terrain generation");
            }
        }
    }

    /**
     * Perform the splitting and merging of triangles in the model.
     *
     * @param bounds The region over which to do splits and merges.
     * @return The box which bounds all changes in the model.
     */
    private Collection<GeographicBoundingBox> doSplitsAndMerges(GeographicBoundingBox bounds)
    {
        myNorthBottom.checkMerge(bounds);
        mySouthBottom.checkMerge(bounds);

        myNorthBottom.checkSplit(myCurrentViewer, bounds);
        mySouthBottom.checkSplit(myCurrentViewer, bounds);

        myNorthBottom.checkMergeForVariance(bounds);
        mySouthBottom.checkMergeForVariance(bounds);

        Collection<GeographicBoundingBox> changes = New.collection();
        changes.addAll(TerrainTriangleUtilities.locateDifferences(myNorthBottom, getModelSnapshot().getNorthBottom()));
        changes.addAll(TerrainTriangleUtilities.locateDifferences(mySouthBottom, getModelSnapshot().getSouthBottom()));

        return changes;
    }
}
