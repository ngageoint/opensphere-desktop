package io.opensphere.wms.state.save.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultPropertyValueReceiver;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.util.collections.New;
import io.opensphere.wms.layer.WMSLayer;

/**
 * Tests getting the WMSLayerProvider class and verifies that it can properly
 * retrieve WMSLayers.
 *
 */
public class WMSLayerProviderTest
{
    /**
     * Verifies that the WMSLayerProvider can properly retrieve the WMSLayers.
     */
    @Test
    public void testGetLayers()
    {
        DataRegistry dataRegistry = createDataRegistry();

        EasyMock.replay(dataRegistry);

        WMSLayerProvider provider = new WMSLayerProvider(dataRegistry);
        List<WMSLayer> layers = provider.getLayers();

        assertEquals(2, layers.size());
        EasyMock.verify(dataRegistry);
    }

    /**
     * Creates the easy mocked data registry.
     *
     * @return The data registry.
     */
    private DataRegistry createDataRegistry()
    {
        DataRegistry registry = EasyMock.createMock(DataRegistry.class);
        registry.performLocalQuery(EasyMock.isA(Query.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @SuppressWarnings("unchecked")
            @Override
            public Void answer()
            {
                Query query = (Query)EasyMock.getCurrentArguments()[0];
                DataModelCategory category = query.getDataModelCategory();
                assertNull(category.getCategory());
                assertNull(category.getSource());
                assertEquals(WMSLayer.class.getName(), category.getFamily());

                DefaultPropertyValueReceiver<WMSLayer> valueReceiver = (DefaultPropertyValueReceiver<WMSLayer>)query
                        .getPropertyValueReceivers().iterator().next();
                PropertyDescriptor<?> descriptor = valueReceiver.getPropertyDescriptor();
                assertEquals("value", descriptor.getPropertyName());
                assertEquals(WMSLayer.class, descriptor.getType());

                List<WMSLayer> values = New.list();
                values.add(null);
                values.add(null);
                valueReceiver.receive(values);

                return null;
            }
        });

        return registry;
    }
}
