package io.opensphere.kml;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.Region;
import de.micromata.opengis.kml.v_2_2_0.ViewRefreshMode;
import io.opensphere.core.Toolbox;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.kml.common.model.KMLController;
import io.opensphere.kml.common.model.KMLDataEvent;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.KMLMapController;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLDataSourceUtils;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;

/**
 * Region Controller.
 */
@ThreadSafe
public class KMLRegionController implements KMLController
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The mantle controller. */
    private final KMLMapController myMantleController;

    /** The set of features being managed by the controller. */
    @GuardedBy("this")
    private final Set<KMLFeature> myFeatures;

    /** The current visible bounding box. */
    @GuardedBy("this")
    private GeographicBoundingBox myVisibleBoundingBox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param mantleController The mantle controller
     */
    public KMLRegionController(Toolbox toolbox, KMLMapController mantleController)
    {
        myToolbox = toolbox;
        myMantleController = mantleController;
        myFeatures = new HashSet<>();
    }

    @Override
    public synchronized void addData(KMLDataEvent dataEvent, boolean reload)
    {
        for (KMLFeature feature : dataEvent.getDataSource().getAllFeatures())
        {
            if (isValidRegion(feature))
            {
                myFeatures.add(feature);
            }
        }
        handleViewChangedInternal();
    }

    @Override
    public synchronized void removeData(KMLDataSource dataSource)
    {
        for (KMLFeature feature : dataSource.getAllFeatures())
        {
            if (isValidRegion(feature))
            {
                myFeatures.remove(feature);
            }
        }
    }

    /**
     * Handle view changed.
     */
    public synchronized void handleViewChanged()
    {
        myVisibleBoundingBox = null;
        handleViewChangedInternal();
    }

    /**
     * Handle view changed.
     */
    private void handleViewChangedInternal()
    {
        if (myFeatures.isEmpty())
        {
            return;
        }

        Map<Boolean, List<KMLFeature>> featureTypeMap = myFeatures.stream()
                .collect(Collectors.<KMLFeature>partitioningBy(f -> f.getFeature() instanceof NetworkLink));
        List<KMLFeature> networkLinkFeatures = featureTypeMap.get(Boolean.TRUE);
        List<KMLFeature> otherFeatures = featureTypeMap.get(Boolean.FALSE);

        // Network Links: Load non-loaded data source if the region is active
        if (CollectionUtilities.hasContent(networkLinkFeatures))
        {
            for (KMLFeature feature : networkLinkFeatures)
            {
                KMLDataSource dataSource = feature.getResultingDataSource();
                if (!dataSource.isLoaded())
                {
                    boolean isRegionActive = isRegionActive(feature.getRegion());
                    feature.setRegionActive(isRegionActive);

                    ViewRefreshMode viewRefreshMode = KMLDataSourceUtils.getViewRefreshMode(dataSource);
                    if (isRegionActive && (viewRefreshMode == ViewRefreshMode.ON_REGION || viewRefreshMode == null))
                    {
                        KMLDataRegistryHelper.queryAndActivate(myToolbox.getDataRegistry(), dataSource, dataSource.getPath(),
                                Nulls.STRING);
                    }
                }
            }
        }

        // Overlays and Placemarks: update the visibility of the features
        if (CollectionUtilities.hasContent(otherFeatures))
        {
            setRegionActive(otherFeatures);
        }
    }

    /**
     * Sets the region active flag and updates the feature visibility as
     * necessary.
     *
     * @param features The features
     */
    private void setRegionActive(Collection<? extends KMLFeature> features)
    {
        for (KMLFeature feature : features)
        {
            boolean isRegionActive = isRegionActive(feature.getRegion());
            if (isRegionActive != feature.isRegionActive())
            {
                feature.setRegionActive(isRegionActive);
            }
        }
        myMantleController.updateFeatureVisibility(features);
    }

    /**
     * Determines if the region is active for the given data source.
     *
     * @param region The region
     * @return Whether the region is active
     */
    private boolean isRegionActive(Region region)
    {
        boolean isRegionActive = false;

        GeographicBoundingBox regionBbox = KMLSpatialTemporalUtils.caclulateGeographicBoundingBox(region.getLatLonAltBox());

        // Determine if the level of detail is satisfied
        double regionLod = getLod(regionBbox);
        double minLod = region.getLod().getMinLodPixels();
        double maxLod = region.getLod().getMaxLodPixels();
        boolean isLodSatisfied = minLod < regionLod && (regionLod < maxLod || maxLod == -1 || maxLod == 0);

        if (isLodSatisfied)
        {
            // Determine if the region is in view
            boolean isRegionInView = getVisibleBoundingBox().intersects(regionBbox);

            isRegionActive = isRegionInView;
        }

        return isRegionActive;
    }

    /**
     * Determines the level of detail for for the given bounding box.
     *
     * @param bbox The geographic bounding box
     * @return The level of detail
     */
    private double getLod(GeographicBoundingBox bbox)
    {
        Vector2i lowerLeft = myToolbox.getMapManager().convertToPoint(bbox.getLowerLeft());
        Vector2i lowerRight = myToolbox.getMapManager().convertToPoint(bbox.getLowerRight());
        Vector2i upperRight = myToolbox.getMapManager().convertToPoint(bbox.getUpperRight());
        Vector2i upperLeft = myToolbox.getMapManager().convertToPoint(bbox.getUpperLeft());
        double topBottomAve = MathUtil.average(lowerRight.distance(lowerLeft), upperRight.distance(upperLeft));
        double leftRightAve = MathUtil.average(upperLeft.distance(lowerLeft), upperRight.distance(lowerRight));
        return Math.max(topBottomAve, leftRightAve);
    }

    /**
     * Gets the current visible bounding box.
     *
     * @return The current visible bounding box
     */
    private GeographicBoundingBox getVisibleBoundingBox()
    {
        if (myVisibleBoundingBox == null)
        {
            myVisibleBoundingBox = myToolbox.getMapManager().getVisibleBoundingBox();
        }
        return myVisibleBoundingBox;
    }

    /**
     * Determines if the feature has a valid region.
     *
     * @param feature the feature
     * @return whether the region is valid
     */
    private static boolean isValidRegion(KMLFeature feature)
    {
        Region region = feature.getRegion();
        return region != null && region.getLatLonAltBox() != null && region.getLod() != null;
    }
}
