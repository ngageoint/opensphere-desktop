package io.opensphere.city.transformer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.city.envoy.CityEnvoy;
import io.opensphere.city.model.json.Result;
import io.opensphere.city.transformer.sun.LightModel;
import io.opensphere.city.transformer.sun.SunRenderer;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.collada.ColladaParser;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.RenderProperties;
import io.opensphere.core.geometry.renderproperties.TransformRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.core.util.math.VectorUtilities;
import io.opensphere.mantle.crust.MiniMantle;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionListener;
import io.opensphere.xyztile.envoy.XYZTileEnvoy;

/**
 * Intercepts xyz tile image queries, converts them to collada model queries and
 * publishes the collada models to the map.
 */
public class ColladaToGeometries extends XYZTileEnvoy implements QueryRegionListener, Observer
{
    /** Temporary - type key. */
    private static final String DATA_TYPE_KEY = "http://http://api.cc3dnow.com/cybercity3dbuildings";

    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(ColladaToGeometries.class);

    /**
     * The zoom level before buildings will start to show up.
     */
    private static final int ourBuildsStartToShowLevel = 15;

    /** The latest camera position. */
    private volatile GeographicPosition myCameraPosition;

    /** A procrastinating executor. */
    private final ProcrastinatingExecutor myExecutor = new ProcrastinatingExecutor("ColladaToGeometries", 500, 1000);

    /** Connects core geometries to mantle layer. */
    private final MiniMantle myMiniMantle;

    /** Map of result to geometries. */
    private final Map<Result, Collection<Geometry>> myResultToGeomsMap = New.map();

    /**
     * Updates the lighting model based on location and timeline.
     */
    private final SunRenderer mySunlight;

    /**
     * The transformer to publish geometries.
     */
    private final Transformer myTransformer;

    /**
     * Constructor.
     *
     * @param toolbox the system toolbox.
     * @param transformer The city transformer.
     */
    public ColladaToGeometries(Toolbox toolbox, Transformer transformer)
    {
        super(toolbox);
        myTransformer = transformer;
        mySunlight = new SunRenderer(toolbox.getMapManager(), toolbox.getTimeManager(), toolbox.getAnimationManager());
        myMiniMantle = new MiniMantle(toolbox.getEventManager());
        myMiniMantle.open();
        mySunlight.getModel().addObserver(this);
    }

    @Override
    public void allQueriesRemoved(boolean animationPlanCancelled)
    {
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.xyztile.envoy.XYZTileEnvoy#providesDataFor(io.opensphere.core.data.util.DataModelCategory)
     */
    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return super.providesDataFor(category) && "cybercity3dbuildings".equals(category.getCategory());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.xyztile.envoy.XYZTileEnvoy#query(io.opensphere.core.
     *      data.util.DataModelCategory, java.util.Collection, java.util.List,
     *      java.util.List, int, java.util.Collection,
     *      io.opensphere.core.data.CacheDepositReceiver)
     */
    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws InterruptedException, QueryException
    {
        if (parameters.size() != 1 || !(parameters.get(0) instanceof ZYXKeyPropertyMatcher))
        {
            throw new IllegalArgumentException(ZYXKeyPropertyMatcher.class.getSimpleName() + " was not found in parameters.");
        }

        final ZYXKeyPropertyMatcher param = (ZYXKeyPropertyMatcher)parameters.get(0);
        final ZYXImageKey key = param.getImageKey();

        if (key.getZ() >= ourBuildsStartToShowLevel)
        {
            query(key.getBounds());
        }

        myCameraPosition = getToolbox().getMapManager().getProjection().convertToPosition(
                getToolbox().getMapManager().getStandardViewer().getPosition().getLocation(), Altitude.ReferenceLevel.ELLIPSOID);
        myExecutor.execute(this::manageGeometries);
    }

    @Override
    public void queryRegionAdded(QueryRegion region)
    {
        for (final Polygon poly : JTSCoreGeometryUtilities.convertToJTSPolygonsAndSplit(region.getGeometries()))
        {
            final GeographicBoundingBox bounds = GeographicBoundingBox
                    .getMinimumBoundingBox(JTSUtilities.convertToGeographicPositions(poly).getFirstObject());
            try
            {
                query(bounds);
            }
            catch (final QueryException e)
            {
                LOGGER.error(e, e);
            }
        }
    }

    @Override
    public void queryRegionRemoved(QueryRegion region)
    {
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (LightModel.LIGHT_PROP.equals(arg))
        {
            updateLighting();
        }
    }

    @Override
    protected String buildImageUrlString(DataModelCategory category, ZYXImageKey key)
    {
        return null;
    }

    /**
     * Gets the matrix4d transform.
     *
     * @param object The 3d building object.
     * @return The transform.
     */
    private Matrix4d getTransform(Result object)
    {
        final LatLonAlt location = LatLonAlt.createFromDegrees(object.getLatitude(), object.getLongitude());
        final Vector3d scaleVector = new Vector3d(1, 1, 1);

        final Vector3d modelVector = getToolbox().getMapManager().getProjection().convertToModel(new GeographicPosition(location),
                Vector3d.ORIGIN);

        return VectorUtilities.getModelTransform(modelVector, 0, 0, 0, scaleVector);
    }

    /**
     * Shows and hides geometries based on visible size of the model.
     */
    private synchronized void manageGeometries()
    {
        final List<Result> resultsToHide = New.list();
        for (final Map.Entry<Result, Collection<Geometry>> entry : myResultToGeomsMap.entrySet())
        {
            final Result result = entry.getKey();
            final Collection<Geometry> geometries = entry.getValue();
            final boolean shouldHide = shouldHide(result, geometries);
            if (shouldHide)
            {
                resultsToHide.add(result);
            }
        }

        final List<Geometry> geometriesToRemove = New.list();
        for (final Result toHide : resultsToHide)
        {
            final Collection<Geometry> geometries = myResultToGeomsMap.remove(toHide);
            geometriesToRemove.addAll(geometries);
        }

        myMiniMantle.removeGeometries(DATA_TYPE_KEY, geometriesToRemove);
        myTransformer.publishGeometries(New.collection(), geometriesToRemove);
        removeFromRegistry(resultsToHide);
    }

    /**
     * Parses the collada model and publishes the geometries.
     *
     * @param object The building object to publish.
     */
    private synchronized void parseColladaAndPublish(Result object)
    {
        if (!myResultToGeomsMap.containsKey(object))
        {
            final List<Geometry> results = New.list();

            try
            {
                if (object.getColladaContent() != null && object.getColladaContent().available() > 0)
                {
                    final DefaultPolylineRenderProperties lineProps = new DefaultPolylineRenderProperties(
                            ZOrderRenderProperties.TOP_Z, true, true);
                    final DefaultPolygonMeshRenderProperties meshProps = new DefaultPolygonMeshRenderProperties(
                            ZOrderRenderProperties.TOP_Z - 101, true, false, true);
                    meshProps.setLighting(mySunlight.getModel().getLight());
                    final LatLonAlt location = LatLonAlt.createFromDegrees(object.getLatitude(), object.getLongitude(),
                            ReferenceLevel.ELLIPSOID);
                    new ColladaParser(lineProps, meshProps, Constraints.createLocationOnlyConstraint(location))
                            .parseModels(object.getColladaContent(), results);
                    final Matrix4d transform = getTransform(object);
                    for (final Geometry geom : results)
                    {
                        final RenderProperties renderProperties = geom.getRenderProperties();
                        if (renderProperties instanceof TransformRenderProperties)
                        {
                            ((TransformRenderProperties)renderProperties).setTransform(transform);
                        }
                    }

                    final boolean shouldHide = shouldHide(object, results);
                    if (!shouldHide)
                    {
                        myResultToGeomsMap.put(object, results);
                        myMiniMantle.addGeometries(DATA_TYPE_KEY, results);
                        myTransformer.publishGeometries(results, Collections.emptyList());
                    }
                    else
                    {
                        removeFromRegistry(New.list(object));
                    }
                }
            }
            catch (IOException | JAXBException e)
            {
                LOGGER.error(e);
            }
        }
    }

    /**
     * Query for the 3d buildings.
     *
     * @param bounds The geographic bounds.
     * @throws QueryException Bad query.
     */
    private void query(GeographicBoundingBox bounds) throws QueryException
    {
        final String baseUrl = "http://api.cc3dnow.com/objects?key=a5014122-a04a-52ba-be0d-8a06c85b5fab&token=63fcdf1fdc4800e8d423eedf8e0cfb57e3fd1b85";
        final Collection<Result> results = CityEnvoy.query(getToolbox().getDataRegistry(), baseUrl, bounds);
        for (final Result result : results)
        {
            parseColladaAndPublish(result);
        }
    }

    /**
     * Removes the result from the registry to help with memory.
     *
     * @param results The results to remove.
     */
    private void removeFromRegistry(List<Result> results)
    {
        final long[] ids = new long[results.size()];
        int index = 0;
        for (final Result result : results)
        {
            ids[index] = result.getDataRegistryId();
            index++;
        }
        getDataRegistry().removeModels(ids);
    }

    /**
     * Updates the visibility of the geometries.
     *
     * @param result the result
     * @param geometries the geometries
     * @return True if the geometry should be hidden, false if it should be
     *         shown.
     */
    private boolean shouldHide(Result result, Collection<Geometry> geometries)
    {
        final LatLonAlt objectPos = LatLonAlt.createFromDegrees(result.getLatitude(), result.getLongitude());
        final double groundDistance = GeographicBody3D.greatCircleDistanceM(objectPos, myCameraPosition.getLatLonAlt(),
                WGS84EarthConstants.RADIUS_EQUATORIAL_M);
        final double cameraAltitude = myCameraPosition.getAlt().getMeters();
        final double straightLineDistanceSquared = Math.pow(groundDistance, 2) + Math.pow(cameraAltitude, 2);
        final double ratio = straightLineDistanceSquared / result.getArea();
        final boolean isHidden = ratio > 20000;

        return isHidden;
    }

    /**
     * Updates the lighting on the geometries.
     */
    private synchronized void updateLighting()
    {
        for (final Collection<Geometry> geoms : myResultToGeomsMap.values())
        {
            for (final Geometry geom : geoms)
            {
                if (geom instanceof PolygonMeshGeometry)
                {
                    final PolygonMeshGeometry poly = (PolygonMeshGeometry)geom;
                    poly.getRenderProperties().setLighting(mySunlight.getModel().getLight());
                }
            }
        }
    }
}
