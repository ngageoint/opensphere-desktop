package io.opensphere.osh.aerialimagery.results;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Combines the different sensor data into one {@link PlatformMetadata} looking
 * at the times to figure out which one go with which.
 */
public class MetadataCombiner implements Comparator<PlatformMetadata>, Serializable
{
    /**
     * Serialization version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Combines the different sensor datas and puts them into
     * {@link PlatformMetadata}s containing all the data. It looks at the data's
     * times to figure out which data gets combined with which.
     *
     * @param locations The location sensor data.
     * @param platformOrientations The platform's orientation sensor data.
     * @param gimbalOrientation The camera's orientation sensor data.
     * @return The combined list of metadata.
     */
    public List<PlatformMetadata> combineData(List<PlatformMetadata> locations, List<PlatformMetadata> platformOrientations,
            List<PlatformMetadata> gimbalOrientation)
    {
        locations.sort(this);
        platformOrientations.sort(this);
        gimbalOrientation.sort(this);

        List<PlatformMetadata> combined = New.list();

        int index = 0;
        for (PlatformMetadata gimbal : gimbalOrientation)
        {
            PlatformMetadata location = findNearest(gimbal, locations);
            PlatformMetadata platformOrientation = findNearest(gimbal, platformOrientations);

            PlatformMetadata aCombined = new PlatformMetadata();
            aCombined.setCameraPitchAngle(gimbal.getCameraPitchAngle());
            aCombined.setCameraRollAngle(gimbal.getCameraRollAngle());
            aCombined.setCameraYawAngle(gimbal.getCameraYawAngle());

            aCombined.setPitchAngle(platformOrientation.getPitchAngle());
            aCombined.setRollAngle(platformOrientation.getRollAngle());
            aCombined.setYawAngle(platformOrientation.getYawAngle());

            double groundAltitude = locations.get(0).getLocation().getAltM();
            if (index >= gimbalOrientation.size() / 2)
            {
                groundAltitude = locations.get(locations.size() - 1).getLocation().getAltM();
            }

            double normalizedAltitude = location.getLocation().getAltM() - groundAltitude;
            LatLonAlt normalizedLocation = LatLonAlt.createFromDegreesMeters(location.getLocation().getLatD(),
                    location.getLocation().getLonD(), normalizedAltitude, ReferenceLevel.TERRAIN);
            aCombined.setLocation(normalizedLocation);
            aCombined.setTime(gimbal.getTime());

            combined.add(aCombined);

            index++;
        }

        return combined;
    }

    @Override
    public int compare(PlatformMetadata o1, PlatformMetadata o2)
    {
        return o1.getTime().compareTo(o2.getTime());
    }

    /**
     * Finds the nearest metadata that correlates with the passed in gimbal
     * data.
     *
     * @param gimbal The camera data.
     * @param locationsOrOrientations The list of locations or platform
     *            orientations.
     * @return The nearest location or platform orientation relative to the
     *         gimbal's time.
     */
    private PlatformMetadata findNearest(PlatformMetadata gimbal, List<PlatformMetadata> locationsOrOrientations)
    {
        long gimbalTime = gimbal.getTime().getTime();

        int index = Collections.binarySearch(locationsOrOrientations, gimbal, this);
        PlatformMetadata locationOrOrientation = null;
        if (index < 0)
        {
            index = -(index + 1);

            PlatformMetadata sooner = locationsOrOrientations.get(index - 1);
            if (index < locationsOrOrientations.size())
            {
                PlatformMetadata later = locationsOrOrientations.get(index);

                long laterDelta = later.getTime().getTime() - gimbalTime;
                long soonerDelta = gimbalTime - sooner.getTime().getTime();

                if (laterDelta < soonerDelta)
                {
                    locationOrOrientation = later;
                }
                else
                {
                    locationOrOrientation = sooner;
                }
            }
            else
            {
                locationOrOrientation = sooner;
            }
        }
        else
        {
            locationOrOrientation = locationsOrOrientations.get(index);
        }

        return locationOrOrientation;
    }
}
