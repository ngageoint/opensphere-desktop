package io.opensphere.osh.aerialimagery.transformer.modelproviders;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.QueryException;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.model.PlatformMetadataAndImage;
import io.opensphere.osh.util.OSHImageQuerier;

/**
 * Unit test for {@link AerialImageProvider} class.
 */
public class AerialImageProviderTest
{
    /**
     * The test query time.
     */
    private static final long ourQueryTime = System.currentTimeMillis();

    /**
     * The type key.
     */
    private static final String ourTypeKey = "iamtypekey";

    /**
     * Tests getting the image.
     *
     * @throws QueryException Bad query.
     */
    @Test
    public void testGetModel() throws QueryException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo videoLayer = createDataType(support);
        OSHImageQuerier querier = createQuerier(support, videoLayer);
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        PlatformMetadata previousModel = createMetadata();

        support.replayAll();

        AerialImageProvider provider = new AerialImageProvider(querier);
        PlatformMetadataAndImage metadataAndImage = (PlatformMetadataAndImage)provider.getModel(dataType, videoLayer,
                ourQueryTime, previousModel);

        assertEquals(10.5, metadataAndImage.getCameraPitchAngle(), 0d);
        assertEquals(11.8, metadataAndImage.getCameraRollAngle(), 0d);
        assertEquals(43.2, metadataAndImage.getCameraYawAngle(), 0d);
        assertEquals(-17.3, metadataAndImage.getPitchAngle(), 0d);
        assertEquals(-43d, metadataAndImage.getRollAngle(), 0d);
        assertEquals(90.3, metadataAndImage.getYawAngle(), 0d);
        assertEquals(ourQueryTime, metadataAndImage.getTime().getTime());
        GeographicConvexQuadrilateral footprint = new GeographicConvexQuadrilateral(
                new GeographicPosition(LatLonAlt.createFromDegrees(-10, -10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(-10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, -10)));
        assertEquals(footprint.getVertices(), metadataAndImage.getFootprint().getVertices());
        LatLonAlt location = LatLonAlt.createFromDegreesMeters(5, 5, Altitude.createFromMeters(101, ReferenceLevel.ELLIPSOID));
        assertEquals(location, metadataAndImage.getLocation());
        assertArrayEquals(new byte[] { 1, 2, 3 }, metadataAndImage.getImageBytes().array());

        support.verifyAll();
    }

    /**
     * Tests getting the image but it returns null.
     *
     * @throws QueryException Bad query.
     */
    @Test
    public void testGetModelNullImage() throws QueryException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo videoLayer = createDataType(support);
        OSHImageQuerier querier = createQuerierNullImage(support, videoLayer);
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        PlatformMetadata previousModel = createMetadata();

        support.replayAll();

        AerialImageProvider provider = new AerialImageProvider(querier);
        PlatformMetadata metadataAndImage = provider.getModel(dataType, videoLayer, ourQueryTime, previousModel);

        assertEquals(previousModel, metadataAndImage);

        support.verifyAll();
    }

    /**
     * Tests getting the image but the passed in metadata is null.
     *
     * @throws QueryException Bad query.
     */
    @Test
    public void testGetModelNullMetadata() throws QueryException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo videoLayer = support.createMock(DataTypeInfo.class);
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        PlatformMetadata previousModel = null;

        support.replayAll();

        AerialImageProvider provider = new AerialImageProvider(querier);
        PlatformMetadata metadataAndImage = provider.getModel(dataType, videoLayer, ourQueryTime, previousModel);

        assertNull(metadataAndImage);

        support.verifyAll();
    }

    /**
     * Tests getting the image but the video layer hasn't been linked yet.
     *
     * @throws QueryException Bad query.
     */
    @Test
    public void testGetModelNullVideoLayer() throws QueryException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo videoLayer = null;
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        PlatformMetadata previousModel = createMetadata();

        support.replayAll();

        AerialImageProvider provider = new AerialImageProvider(querier);
        PlatformMetadata metadataAndImage = provider.getModel(dataType, videoLayer, ourQueryTime, previousModel);

        assertEquals(previousModel, metadataAndImage);

        support.verifyAll();
    }

    /**
     * Creates the {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @return The mocked video layer.
     */
    private DataTypeInfo createDataType(EasyMockSupport support)
    {
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        EasyMock.expect(dataType.getTypeKey()).andReturn(ourTypeKey);

        return dataType;
    }

    /**
     * Creates the test metadata to use.
     *
     * @return The test metadata.
     */
    private PlatformMetadata createMetadata()
    {
        PlatformMetadata metadata = new PlatformMetadata();

        metadata.setCameraPitchAngle(10.5);
        metadata.setCameraRollAngle(11.8);
        metadata.setCameraYawAngle(43.2);
        GeographicConvexQuadrilateral footprint = new GeographicConvexQuadrilateral(
                new GeographicPosition(LatLonAlt.createFromDegrees(-10, -10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(-10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, -10)));
        metadata.setFootprint(footprint);
        LatLonAlt location = LatLonAlt.createFromDegreesMeters(5, 5, Altitude.createFromMeters(101, ReferenceLevel.ELLIPSOID));
        metadata.setLocation(location);
        metadata.setPitchAngle(-17.3);
        metadata.setRollAngle(-43);
        metadata.setTime(new Date(ourQueryTime));
        metadata.setYawAngle(90.3);

        return metadata;
    }

    /**
     * Creates a mocked querier.
     *
     * @param support Used to create the mock.
     * @param videoLayer The mocked video layer.
     * @return The mocked querier.
     * @throws QueryException Bad query.
     */
    private OSHImageQuerier createQuerier(EasyMockSupport support, DataTypeInfo videoLayer) throws QueryException
    {
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);

        EasyMock.expect(
                querier.queryImage(EasyMock.cmpEq(ourTypeKey), EasyMock.cmpEq(TimeSpan.get(ourQueryTime, Milliseconds.ONE))))
                .andReturn(new byte[] { 1, 2, 3 });

        return querier;
    }

    /**
     * Creates a mocked querier that returns a null image.
     *
     * @param support Used to create the mock.
     * @param videoLayer The mocked video layer.
     * @return The image querier.
     * @throws QueryException Bad query.
     */
    private OSHImageQuerier createQuerierNullImage(EasyMockSupport support, DataTypeInfo videoLayer) throws QueryException
    {
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);

        EasyMock.expect(
                querier.queryImage(EasyMock.cmpEq(ourTypeKey), EasyMock.cmpEq(TimeSpan.get(ourQueryTime, Milliseconds.ONE))))
                .andReturn(null);

        return querier;
    }
}
