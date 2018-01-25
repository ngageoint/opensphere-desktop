package io.opensphere.wms.state.model;

import static org.junit.Assert.assertSame;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.wms.layer.WMSLayerValueProvider;

/**
 * Basic test for the WMSLayerAndSate model.
 *
 */
public class WMSLayerAndStateTest
{
    /**
     * Tests the getters.
     */
    @Test
    public void test()
    {
        WMSLayerValueProvider layer = EasyMock.createMock(WMSLayerValueProvider.class);
        WMSLayerState state = new WMSLayerState();

        WMSLayerAndState layerState = new WMSLayerAndState(layer, state);

        assertSame(layer, layerState.getLayer());
        assertSame(state, layerState.getState());
    }
}
