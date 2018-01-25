package io.opensphere.osh.aerialimagery.results;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Unit test for {@link MetadataCombiner}.
 */
public class MetadataCombinerTest
{
    /**
     * Tests combining data.
     *
     * @throws ParseException Bad parse.
     */
    @Test
    public void testCombineData() throws ParseException
    {
        List<PlatformMetadata> locations = createLocations();
        List<PlatformMetadata> platOrients = createPlatformOrientations();
        List<PlatformMetadata> gimbalOrients = createGimbalOrientations();

        MetadataCombiner combiner = new MetadataCombiner();
        List<PlatformMetadata> metadatas = combiner.combineData(locations, platOrients, gimbalOrients);

        assertEquals(2, metadatas.size());

        PlatformMetadata metadata = metadatas.get(0);

        assertEquals(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.274Z"), metadata.getTime());

        assertEquals(-15.443460464477539, metadata.getCameraPitchAngle(), 0d);
        assertEquals(-2.876760482788086, metadata.getCameraRollAngle(), 0d);
        assertEquals(0.10008019953966141, metadata.getCameraYawAngle(), 0d);

        assertEquals(1.3307557106018066, metadata.getPitchAngle(), 0d);
        assertEquals(1.6258397102355957, metadata.getRollAngle(), 0d);
        assertEquals(73.36104583740234, metadata.getYawAngle(), 0d);

        assertEquals(LatLonAlt.createFromDegreesMeters(34.6905037, -86.5819168, 0, ReferenceLevel.ELLIPSOID).asVec3d(),
                metadata.getLocation().asVec3d());

        metadata = metadatas.get(1);

        assertEquals(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.739Z"), metadata.getTime());

        assertEquals(-14.831459999084473, metadata.getCameraPitchAngle(), 0d);
        assertEquals(-2.8407604694366455, metadata.getCameraRollAngle(), 0d);
        assertEquals(0.17208021879196167, metadata.getCameraYawAngle(), 0d);

        assertEquals(1.3301459550857544, metadata.getPitchAngle(), 0d);
        assertEquals(1.619006872177124, metadata.getRollAngle(), 0d);
        assertEquals(73.3594970703125, metadata.getYawAngle(), 0d);

        assertEquals(LatLonAlt.createFromDegreesMeters(34.6905036, -86.5819167, 0.01, ReferenceLevel.ELLIPSOID).asVec3d(),
                metadata.getLocation().asVec3d());
    }

    /**
     * Creates the gimbal orientations.
     *
     * @return The gimbal orientations.
     * @throws ParseException Bad parse.
     */
    private List<PlatformMetadata> createGimbalOrientations() throws ParseException
    {
        /* 2015-12-19T21:01:29.274Z,0.10008019953966141,-15.443460464477539,-2.
         * 876760482788086
         * 2015-12-19T21:01:29.739Z,0.17208021879196167,-14.831459999084473,-2.
         * 8407604694366455 */
        List<PlatformMetadata> orientations = New.list();

        PlatformMetadata metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.274Z"));
        metadata.setCameraPitchAngle(-15.443460464477539);
        metadata.setCameraRollAngle(-2.876760482788086);
        metadata.setCameraYawAngle(0.10008019953966141);
        orientations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.739Z"));
        metadata.setCameraPitchAngle(-14.831459999084473);
        metadata.setCameraRollAngle(-2.8407604694366455);
        metadata.setCameraYawAngle(0.17208021879196167);
        orientations.add(0, metadata);

        return orientations;
    }

    /**
     * Creates location test data.
     *
     * @return The test location data.
     * @throws ParseException Bad parse.
     */
    private List<PlatformMetadata> createLocations() throws ParseException
    {
        /* 2015-12-19T21:01:29.231Z,34.6905037,-86.5819168,183.99
         * 2015-12-19T21:01:29.321Z,34.6905037,-86.5819167,183.98
         * 2015-12-19T21:01:29.521Z,34.6905036,-86.5819167,183.99
         * 2015-12-19T21:01:29.621Z,34.6905036,-86.5819167,183.99
         * 2015-12-19T21:01:29.733Z,34.6905036,-86.5819167,183.99
         * 2015-12-19T21:01:29.831Z,34.6905036,-86.5819167,183.98 */
        List<PlatformMetadata> locations = New.list();

        PlatformMetadata metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.231Z"));
        metadata.setLocation(LatLonAlt.createFromDegreesMeters(34.6905037, -86.5819168, 183.99, ReferenceLevel.ELLIPSOID));
        locations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.321Z"));
        metadata.setLocation(LatLonAlt.createFromDegreesMeters(34.6905037, -86.5819167, 183.98, ReferenceLevel.ELLIPSOID));
        locations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.521Z"));
        metadata.setLocation(LatLonAlt.createFromDegreesMeters(34.6905036, -86.5819167, 183.99, ReferenceLevel.ELLIPSOID));
        locations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.621Z"));
        metadata.setLocation(LatLonAlt.createFromDegreesMeters(34.6905036, -86.5819167, 183.99, ReferenceLevel.ELLIPSOID));
        locations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.733Z"));
        metadata.setLocation(LatLonAlt.createFromDegreesMeters(34.6905036, -86.5819167, 183.99, ReferenceLevel.ELLIPSOID));
        locations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.831Z"));
        metadata.setLocation(LatLonAlt.createFromDegreesMeters(34.6905036, -86.5819167, 183.98, ReferenceLevel.ELLIPSOID));
        locations.add(0, metadata);

        return locations;
    }

    /**
     * Creates the platform's orientation test data.
     *
     * @return The platform's orientations.
     * @throws ParseException Bad parse.
     */
    private List<PlatformMetadata> createPlatformOrientations() throws ParseException
    {
        /* 2015-12-19T21:01:29.270Z,73.36104583740234, 1.3307557106018066,1.
         * 6258397102355957 2015-12-19T21:01:29.324Z,73.35967254638672,
         * 1.3303154706954956,1. 6343384981155396
         * 2015-12-19T21:01:29.424Z,73.35750579833984, 1.31881844997406,1.
         * 6398361921310425 2015-12-19T21:01:29.526Z,73.35836029052734,
         * 1.3274003267288208,1. 63909113407135
         * 2015-12-19T21:01:29.623Z,73.35868072509766, 1.3329054117202759,1.
         * 6344081163406372 2015-12-19T21:01:29.734Z,73.3594970703125,
         * 1.3301459550857544,1. 619006872177124
         * 2015-12-19T21:01:29.831Z,73.35693359375,1.3350272178649902,1.
         * 61716890335083 */
        List<PlatformMetadata> orientations = New.list();

        PlatformMetadata metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.270Z"));
        metadata.setPitchAngle(1.3307557106018066);
        metadata.setRollAngle(1.6258397102355957);
        metadata.setYawAngle(73.36104583740234);
        orientations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.324Z"));
        metadata.setPitchAngle(1.3303154706954956);
        metadata.setRollAngle(1.6343384981155396);
        metadata.setYawAngle(73.35967254638672);
        orientations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.424Z"));
        metadata.setPitchAngle(1.31881844997406);
        metadata.setRollAngle(1.6398361921310425);
        metadata.setYawAngle(73.35750579833984);
        orientations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.526Z"));
        metadata.setPitchAngle(1.3274003267288208);
        metadata.setRollAngle(1.63909113407135);
        metadata.setYawAngle(73.35836029052734);
        orientations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.623Z"));
        metadata.setPitchAngle(1.3329054117202759);
        metadata.setRollAngle(1.6344081163406372);
        metadata.setYawAngle(73.35868072509766);
        orientations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.734Z"));
        metadata.setPitchAngle(1.3301459550857544);
        metadata.setRollAngle(1.619006872177124);
        metadata.setYawAngle(73.3594970703125);
        orientations.add(0, metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.831Z"));
        metadata.setPitchAngle(1.3350272178649902);
        metadata.setRollAngle(1.61716890335083);
        metadata.setYawAngle(73.35693359375);
        orientations.add(0, metadata);

        return orientations;
    }
}
