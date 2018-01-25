package io.opensphere.wms.state.deactivate;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DataGroupActivator;
import io.opensphere.wms.envoy.WMSEnvoy;
import io.opensphere.wms.envoy.WMSLayerEnvoy;
import io.opensphere.wms.layer.WMSLayerValueProvider;
import io.opensphere.wms.state.StateUtils;
import io.opensphere.wms.state.model.StateGroup;

/**
 * Tests the layer activator.
 *
 */
public class StateDeactivatorTest
{
    /**
     * The test state id.
     */
    private static final String ourTestStateId = "stateId";

    /**
     * Tests the layer activator.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void test() throws InterruptedException
    {
        WMSEnvoy envoy = StateUtils.createEnvoy();
        DataGroupInfo parent = createParent();
        DataGroupController controller = createDataController(parent);
        DataTypeInfo dataType = createDataType(parent);
        WMSLayerValueProvider layer = createLayer(dataType);
        WMSLayerEnvoy layerEnvoy = StateUtils.createLayerEnvoy(layer);
        StateGroup group = createStateGroup(parent);
        DataGroupActivator groupActivator = createDataGroupActivator(parent);

        EasyMock.replay(parent, controller, envoy, dataType, layer, layerEnvoy);

        StateDeactivator deactivator = new StateDeactivator(groupActivator, controller);
        deactivator.deactivateState(group);

        EasyMock.verify(parent, controller, envoy, dataType, layer, layerEnvoy);
    }

    /**
     * Creates the data group controller.
     *
     * @param expectedGroup The expected group.
     * @return The data group controller.
     */
    private DataGroupController createDataController(final DataGroupInfo expectedGroup)
    {
        DataGroupController dataController = EasyMock.createMock(DataGroupController.class);
        dataController.removeDataGroupInfo(EasyMock.eq(expectedGroup), EasyMock.isA(StateDeactivator.class));
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        return dataController;
    }

    /**
     * Creates the data group activator.
     *
     * @param parent The parent.
     * @return the data group activator
     * @throws InterruptedException If the thread is interrupted.
     */
    private DataGroupActivator createDataGroupActivator(DataGroupInfo parent) throws InterruptedException
    {
        DataGroupActivator activator = EasyMock.createMock(DataGroupActivator.class);
        EasyMock.expect(Boolean.valueOf(activator.setGroupsActive(New.list(parent), false))).andReturn(Boolean.TRUE);
        return activator;
    }

    /**
     * The data type to activate.
     *
     * @param parent The parent of the type.
     * @return The data type.
     */
    private DataTypeInfo createDataType(DataGroupInfo parent)
    {
        DataTypeInfo dataType = EasyMock.createMock(DataTypeInfo.class);

        return dataType;
    }

    /**
     * Creates the layer.
     *
     * @param dataType The data type.
     * @return The layer.
     */
    private WMSLayerValueProvider createLayer(DataTypeInfo dataType)
    {
        WMSLayerValueProvider layer = EasyMock.createMock(WMSLayerValueProvider.class);

        return layer;
    }

    /**
     * Creates the parent data group.
     *
     * @return The parent.
     */
    private DataGroupInfo createParent()
    {
        DataGroupInfo parent = EasyMock.createMock(DataGroupInfo.class);

        return parent;
    }

    /**
     * Creates the test state group.
     *
     * @param envoyAndLayer The envoy and layer being deactivated.
     * @return The test state group.
     */
    private StateGroup createStateGroup(DataGroupInfo envoyAndLayer)
    {
        StateGroup group = new StateGroup(ourTestStateId, New.list(envoyAndLayer));

        return group;
    }
}
