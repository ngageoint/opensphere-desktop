package io.opensphere.osh.aerialimagery.transformer.geometrybuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.model.PlatformMetadataAndImage;

/**
 * Unit test for {@link BuilderFactory}.
 */
public class BuilderFactoryTest
{
    /**
     * Tests building geometry builders that build geometries just from the
     * metadata.
     */
    @Test
    public void testMetadata()
    {
        EasyMockSupport support = new EasyMockSupport();

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        @SuppressWarnings("unchecked")
        MapContext<? extends Viewer> mapManager = support.createMock(MapContext.class);

        PlatformMetadata metadata = new PlatformMetadata();

        support.replayAll();

        BuilderFactory factory = new BuilderFactory(orderRegistry, mapManager);
        List<GeometryBuilder> builders = factory.createBuilders(metadata);

        assertEquals(2, builders.size());

        assertTrue(builders.get(0) instanceof FootprintGeometryBuilder);
        assertTrue(builders.get(1) instanceof PlatformGeometryBuilder);

        support.verifyAll();
    }

    /**
     * Tests building geometry builders that build geometries just from the
     * metadata and image.
     */
    @Test
    public void testMetadataImage()
    {
        EasyMockSupport support = new EasyMockSupport();

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        @SuppressWarnings("unchecked")
        MapContext<? extends Viewer> mapManager = support.createMock(MapContext.class);

        PlatformMetadata justMetadata = new PlatformMetadata();
        PlatformMetadataAndImage metadata = new PlatformMetadataAndImage(justMetadata, ByteBuffer.wrap(new byte[] { 1, 2, 3 }));

        support.replayAll();

        BuilderFactory factory = new BuilderFactory(orderRegistry, mapManager);
        List<GeometryBuilder> builders = factory.createBuilders(metadata);

        assertEquals(1, builders.size());

        assertTrue(builders.get(0) instanceof ImageGeometryBuilder);

        support.verifyAll();
    }
}
