package io.opensphere.wms.state;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the map layer state controller.
 *
 */
public class DataLayerStateControllerTest
{
    /**
     * Verifies the static property.
     */
    @Test
    public void test()
    {
        assertEquals("Layers", DataLayerStateController.MODULE_NAME);
    }
}
