package io.opensphere.wms.state.activate.controllers;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DataGroupActivator;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.wms.state.model.StateGroup;

/**
 * Tests the layer activator.
 *
 */
public class LayerActivatorTest
{
    /**
     * The state id.
     */
    private static final String ourStateId = "stateId";

    /**
     * Tests the layer activator with an active group.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testActive() throws InterruptedException
    {
        test(true);
    }

    /**
     * Tests the layer activator with an inactive group.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testInactive() throws InterruptedException
    {
        test(false);
    }

    /**
     * Runs a test.
     *
     * @param isGroupActive Whether the group is to be active.
     * @throws InterruptedException If the thread is interrupted.
     */
    public void test(boolean isGroupActive) throws InterruptedException
    {
        DataGroupInfo parent = createParent();
        parent.activationProperty().setActive(isGroupActive);
        DataGroupActivator dataGroupActivator = createDataGroupActivator(parent, isGroupActive);

        EasyMock.replay(dataGroupActivator);

        LayerActivator activator = new LayerActivator(dataGroupActivator);
        StateGroup stateGroup = activator.activateLayers(ourStateId, New.list(parent), false);

        assertEquals(ourStateId, stateGroup.getStateId());
        assertEquals(1, stateGroup.getStateLayers().size());
        assertEquals(parent, stateGroup.getStateLayers().get(0));

        EasyMock.verify(dataGroupActivator);
    }

    /**
     * Creates the data group activator.
     *
     * @param parent The parent.
     * @param isGroupActive If the group is active.
     * @return the data group activator
     * @throws InterruptedException If the thread is interrupted.
     */
    private DataGroupActivator createDataGroupActivator(DataGroupInfo parent, boolean isGroupActive) throws InterruptedException
    {
        DataGroupActivator activator = EasyMock.createMock(DataGroupActivator.class);
        if (isGroupActive)
        {
            EasyMock.expect(Boolean.valueOf(activator.setGroupsActive(New.list(), true))).andReturn(Boolean.TRUE);
            EasyMock.expect(Boolean.valueOf(activator.reactivateGroups(New.list(parent)))).andReturn(Boolean.TRUE);
        }
        else
        {
            EasyMock.expect(Boolean.valueOf(activator.setGroupsActive(New.list(parent), true))).andReturn(Boolean.TRUE);
            EasyMock.expect(Boolean.valueOf(activator.reactivateGroups(New.list()))).andReturn(Boolean.TRUE);
        }
        return activator;
    }

    /**
     * Creates the parent data group.
     *
     * @return The parent.
     */
    private DataGroupInfo createParent()
    {
        DefaultDataGroupInfo parent = new DefaultDataGroupInfo(false, null, "providerType", "id");
        parent.addMember(new DefaultDataTypeInfo(null, "sourcePrefix", "typeKey1", "typeName", "displayName", false), this);
        parent.addMember(new DefaultDataTypeInfo(null, "sourcePrefix", "typeKey2", "typeName", "displayName", false), this);
        return parent;
    }
}
