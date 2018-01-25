package io.opensphere.wms.state;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the map layer state controller.
 *
 */
public class MapLayerStateControllerTest
{
    /**
     * Verifies the static property.
     */
    @Test
    public void test()
    {
        assertEquals("Map Layers", MapLayerStateController.MODULE_NAME);
    }
}
