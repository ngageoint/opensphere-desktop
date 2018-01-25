package io.opensphere.kml.tree.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.LookAt;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.kml.common.util.KMLToolboxUtils;
import io.opensphere.mantle.data.util.impl.DataTypeActionUtils;

/**
 * Helps with tree fly-to events.
 */
public final class KMLFlyToHelper
{
    /**
     * Requests the map go to ( re-center ) on the given features.
     *
     * @param features the list of features to go to.
     * @return Whether or not there were locations to fly to
     */
    public static boolean gotoFeatures(Collection<KMLFeature> features)
    {
        boolean flew = false;

        if (!features.isEmpty())
        {
            // Find the first LookAt, if there is one
            LookAt lookAt = null;
            for (KMLFeature feature : features)
            {
                if (feature.getAbstractView() instanceof LookAt)
                {
                    lookAt = (LookAt)feature.getAbstractView();
                    break;
                }
            }

            // Create a list of locations from the features
            DynamicViewer viewer = KMLToolboxUtils.getToolbox().getMapManager().getStandardViewer();
            if (lookAt != null)
            {
                LatLonAlt location = KMLSpatialTemporalUtils.convertLookAt(lookAt);
                new ViewerAnimator(viewer, new GeographicPosition(location)).start();
                flew = true;
            }
            else
            {
                List<LatLonAlt> locations = new ArrayList<>(features.size());
                for (KMLFeature feature : features)
                {
                    LatLonAlt location = featureToLatLonAlt(feature);
                    if (location != null)
                    {
                        locations.add(location);
                    }
                }

                if (!locations.isEmpty())
                {
                    DataTypeActionUtils.gotoLocations(locations, viewer);
                    flew = true;
                }
            }
        }

        return flew;
    }

    /**
     * Converts a Feature to a LatLonAlt.
     *
     * @param feature The feature
     * @return The equivalent LatLonAlt
     */
    private static LatLonAlt featureToLatLonAlt(KMLFeature feature)
    {
        return feature.getGeoBoundingBox() != null ? feature.getGeoBoundingBox().getCenter().getLatLonAlt() : null;
    }

    /**
     * Private constructor.
     */
    private KMLFlyToHelper()
    {
    }
}
