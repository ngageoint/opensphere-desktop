package io.opensphere.wms.state.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Tests the StateGroup getters.
 *
 */
public class StateGroupTest
{
    /**
     * Tests the getters.
     */
    @Test
    public void test()
    {
        String expectedId = "stateId";
        List<DataGroupInfo> expectedLayers = New.list();

        StateGroup group = new StateGroup(expectedId, expectedLayers);
        assertEquals(expectedId, group.getStateId());
        assertEquals(expectedLayers, group.getStateLayers());
    }
}
