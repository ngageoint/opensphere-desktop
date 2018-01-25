package io.opensphere.osh.aerialimagery.transformer.modelproviders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.osh.util.OSHImageQuerier;

/**
 * Unit test for the {@link ModelProviderFactory} class.
 */
public class ModelProviderFactoryTest
{
    /**
     * Tests creating model providers.
     */
    @Test
    public void testCreateProviders()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);

        support.replayAll();

        ModelProviderFactory factory = new ModelProviderFactory(dataRegistry, querier);

        List<ModelProvider> providers = factory.createProviders();

        assertEquals(2, providers.size());
        assertTrue(providers.get(0) instanceof PlatformMetadataProvider);
        assertTrue(providers.get(1) instanceof AerialImageProvider);

        support.verifyAll();
    }
}
