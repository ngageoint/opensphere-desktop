package io.opensphere.wms.state.model;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.wms.envoy.WMSEnvoy;
import io.opensphere.wms.layer.WMSDataType;

/**
 * Tests the WMSEnvoyAndState class.
 *
 */
public class WMSEnvoyAndStateTest
{
    /**
     * Tests the getters.
     */
    @Test
    public void test()
    {
        WMSEnvoy envoy = EasyMock.createMock(WMSEnvoy.class);
        WMSDataType dataType = EasyMock.createMock(WMSDataType.class);
        WMSLayerState state = new WMSLayerState();

        WMSEnvoyAndState envoyAndState = new WMSEnvoyAndState(envoy, dataType, state, null);

        assertEquals(envoy, envoyAndState.getEnvoy());
        assertEquals(state, envoyAndState.getState());
        assertEquals(dataType, envoyAndState.getTypeInfo());
    }
}
