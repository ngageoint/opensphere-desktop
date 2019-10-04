package io.opensphere.stkterrain.envoy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.stkterrain.model.TileSet;
import io.opensphere.stkterrain.util.Constants;

/**
 * Unit test for the {@link TileSetEnvoy} class.
 */
public class TileSetEnvoyTest
{
    /**
     * The test server url.
     */
    private static final String ourTestServer = "http://somehost/terrain";

    /**
     * Tests getting the thread pool name.
     */
    @Test
    public void testGetThreadPoolName()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);
        DefaultValidatorSupport validatorSupport = new DefaultValidatorSupport(null);

        support.replayAll();

        TileSetEnvoy envoy = new TileSetEnvoy(toolbox, validatorSupport, ourTestServer);

        assertEquals(Constants.ENVOY_THREAD_POOL_NAME + ourTestServer, envoy.getThreadPoolName());

        support.verifyAll();
    }

    /**
     * Tests opening the envoy with a valid response.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testOpen() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        Toolbox toolbox = createToolbox(support, dataRegistry);

        support.replayAll();

        DefaultValidatorSupport validatorSupport = new DefaultValidatorSupport(null);
        TileSetEnvoy envoy = new TileSetEnvoy(toolbox, validatorSupport, ourTestServer);
        envoy.open();

        assertEquals(ValidationStatus.VALID, validatorSupport.getValidationStatus());

        support.verifyAll();
    }

    /**
     * Tests the provides data for.
     */
    @Test
    public void testProvidesDataFor()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);
        DefaultValidatorSupport validatorSupport = new DefaultValidatorSupport(null);

        support.replayAll();

        TileSetEnvoy envoy = new TileSetEnvoy(toolbox, validatorSupport, ourTestServer);

        DataModelCategory category = new DataModelCategory(null, TileSet.class.getName(), null);
        DataModelCategory noProvidy = new DataModelCategory(null, null, null);
        DataModelCategory provides = new DataModelCategory(ourTestServer, TileSet.class.getName(), null);
        DataModelCategory noProvides = new DataModelCategory("http://someotherserver", TileSet.class.getName(), null);

        assertTrue(envoy.providesDataFor(category));
        assertFalse(envoy.providesDataFor(noProvidy));
        assertTrue(envoy.providesDataFor(provides));
        assertFalse(envoy.providesDataFor(noProvides));

        support.verifyAll();
    }

    /**
     * The answer for the mocked addModels call.
     *
     * @return The model ids.
     */
    @SuppressWarnings("unchecked")
    private long[] addModelsAnswer()
    {
        DefaultCacheDeposit<TileSet> deposit = (DefaultCacheDeposit<TileSet>)EasyMock
                .getCurrentArguments()[0];

        DataModelCategory category = deposit.getCategory();
        DataModelCategory expected = new DataModelCategory(ourTestServer, TileSet.class.getName(), TileSet.class.getName());

        assertEquals(expected, category);
        assertEquals(Constants.TILESET_PROPERTY_DESCRIPTOR, deposit.getAccessors().iterator().next().getPropertyDescriptor());
        assertTrue(deposit.getAccessors().iterator().next() instanceof SerializableAccessor);
        List<TileSet> inputs = New.list(deposit.getInput());

        assertEquals(1, inputs.size());

        assertEquals("world", inputs.get(0).getName());
        assertEquals(1, inputs.get(0).getDataSources().size());

        return new long[] { 0, 1 };
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked data registry.
     */
    @SuppressWarnings("unchecked")
    private DataRegistry createDataRegistry(EasyMockSupport support)
    {
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        EasyMock.expect(dataRegistry.addModels(EasyMock.isA(DefaultCacheDeposit.class)))
                .andAnswer(this::addModelsAnswer);

        return dataRegistry;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param dataRegistry A mocked {@link DataRegistry} to return.
     * @return The mocked toolbox.
     * @throws MalformedURLException Bad URL.
     */
    private Toolbox createToolbox(EasyMockSupport support, DataRegistry dataRegistry)
        throws MalformedURLException
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        if (dataRegistry != null)
        {
            EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry);
        }

        return toolbox;
    }
}
