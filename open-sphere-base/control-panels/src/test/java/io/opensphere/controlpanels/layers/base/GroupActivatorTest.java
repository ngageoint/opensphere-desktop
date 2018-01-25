package io.opensphere.controlpanels.layers.base;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DataGroupActivator;

/**
 * Tests the Group Activator class.
 *
 */
@SuppressWarnings("PMD.GodClass")
public class GroupActivatorTest
{
    /**
     * Tests activating group and descendants.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testActivateGroupAndDescendants() throws InterruptedException
    {
        UserConfirmer confirmer = createConfirmer(false, false);
        DataGroupInfo parent = createParent(null, false);
        List<DataGroupInfo> dataGroups = createDataGroups(2, 1, parent, confirmer);
        EasyMock.expect(dataGroups.get(0).groupStream()).andReturn(dataGroups.stream());

        DataGroupActivator activator = getDataGroupActivator(parent, dataGroups.stream(), true);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.replay(dataGroup);
        }

        EasyMock.replay(confirmer, parent, activator);

        GroupActivator groupActivator = new GroupActivator(activator);
        groupActivator.activateDeactivateGroup(true, dataGroups.get(0), confirmer);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.verify(dataGroup);
        }

        EasyMock.verify(confirmer, parent, activator);
    }

    /**
     * Tests activating over ten descendants.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testActivateOverTenDescendants() throws InterruptedException
    {
        UserConfirmer confirmer = createConfirmer(true, true);
        DataGroupInfo parent = createParent(null, false);
        List<DataGroupInfo> dataGroups = createDataGroups(2, 11, parent, confirmer);
        EasyMock.expect(dataGroups.get(0).groupStream()).andReturn(dataGroups.stream());

        DataGroupActivator dataGroupActivator = getDataGroupActivator(parent, dataGroups.stream(), true);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.replay(dataGroup);
        }

        EasyMock.replay(confirmer, parent, dataGroupActivator);

        GroupActivator activator = new GroupActivator(dataGroupActivator);
        activator.activateDeactivateGroup(true, dataGroups.get(0), confirmer);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.verify(dataGroup);
        }

        EasyMock.verify(confirmer, parent, dataGroupActivator);
    }

    /**
     * Tests activating over ten descendants without a confirmer.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testActivateOverTenDescendantsNoConfirmer() throws InterruptedException
    {
        DataGroupInfo parent = createParent(null, false);
        List<DataGroupInfo> dataGroups = createDataGroups(2, 11, parent, null);
        EasyMock.expect(dataGroups.get(0).groupStream()).andReturn(dataGroups.stream());

        DataGroupActivator dataGroupActivator = getDataGroupActivator(parent, dataGroups.stream(), true);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.replay(dataGroup);
        }

        EasyMock.replay(dataGroupActivator, parent);

        GroupActivator activator = new GroupActivator(dataGroupActivator);
        activator.activateDeactivateGroup(true, dataGroups.get(0), null);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.verify(dataGroup);
        }

        EasyMock.verify(dataGroupActivator, parent);
    }

    /**
     * Get the data group activator.
     *
     * @param parent The parent group.
     * @param stream The stream.
     * @param active The active.
     * @return The data group activator.
     * @throws InterruptedException If the thread is interrupted.
     */
    private DataGroupActivator getDataGroupActivator(DataGroupInfo parent, Stream<DataGroupInfo> stream, boolean active)
        throws InterruptedException
    {
        DataGroupActivator dataGroupActivator = EasyMock.createMock(DataGroupActivator.class);
        EasyMock.reportMatcher(new IArgumentMatcher()
        {
            @Override
            public boolean matches(Object arg0)
            {
                if (arg0 instanceof Stream)
                {
                    @SuppressWarnings("unchecked")
                    List<DataGroupInfo> list = ((Stream<DataGroupInfo>)arg0).collect(Collectors.toList());
                    return list.equals(stream.collect(Collectors.toList()));
                }
                else
                {
                    return false;
                }
            }

            @Override
            public void appendTo(StringBuffer buf)
            {
                buf.append("matches(").append(stream).append(')');
            }
        });
        EasyMock.expect(Boolean.valueOf(dataGroupActivator.setGroupsActive((Stream<DataGroupInfo>)null, EasyMock.eq(active))))
                .andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(dataGroupActivator.setGroupActive(parent, active))).andReturn(Boolean.TRUE);
        return dataGroupActivator;
    }

    /**
     * Tests activating over ten descendants with a cancel response.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testActivateOverTenDescendantsResponseNo() throws InterruptedException
    {
        DataGroupInfo dataGroup = EasyMock.createMock(DataGroupInfo.class);

        Set<DataTypeInfo> members = New.set();
        for (int i = 0; i < 12; i++)
        {
            DataTypeInfo dataType = EasyMock.createMock(DataTypeInfo.class);
            members.add(dataType);
        }

        dataGroup.getMembers(EasyMock.eq(true));
        EasyMock.expectLastCall().andReturn(members);

        DataGroupActivator dataGroupActivator = EasyMock.createMock(DataGroupActivator.class);

        UserConfirmer confirmer = createConfirmer(true, false);

        EasyMock.replay(dataGroupActivator, confirmer, dataGroup);

        GroupActivator activator = new GroupActivator(dataGroupActivator);
        activator.activateDeactivateGroup(true, dataGroup, confirmer);

        EasyMock.verify(dataGroupActivator, confirmer, dataGroup);
    }

    /**
     * Tests activating a single group.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testActivateSingleGroup() throws InterruptedException
    {
        UserConfirmer confirmer = createConfirmer(false, false);
        DataGroupInfo parent = createParent(null, false);
        List<DataGroupInfo> dataGroups = createDataGroups(0, 1, parent, confirmer);
        EasyMock.expect(dataGroups.get(0).groupStream()).andReturn(dataGroups.stream());

        DataGroupActivator dataGroupActivator = getDataGroupActivator(parent, dataGroups.stream(), true);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.replay(dataGroup);
        }

        EasyMock.replay(dataGroupActivator, confirmer, parent);

        GroupActivator activator = new GroupActivator(dataGroupActivator);
        activator.activateDeactivateGroup(true, dataGroups.get(0), confirmer);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.verify(dataGroup);
        }

        EasyMock.verify(dataGroupActivator, confirmer, parent);
    }

    /**
     * Tests deactivating groups with parent members active.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testDeactivateGroupParentsWithActive() throws InterruptedException
    {
        UserConfirmer confirmer = createConfirmer(false, false);
        DataGroupInfo parent = createParent(null, true);
        List<DataGroupInfo> dataGroups = createDataGroups(2, 0, parent, confirmer);
        EasyMock.expect(parent.getChildren()).andReturn(New.list(dataGroups.get(0)));
        EasyMock.expect(dataGroups.get(0).groupStream()).andReturn(dataGroups.stream());

        DataGroupActivator dataGroupActivator = getDataGroupActivator(parent, dataGroups.stream(), false);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.replay(dataGroup);
        }

        EasyMock.replay(dataGroupActivator, confirmer, parent);

        GroupActivator activator = new GroupActivator(dataGroupActivator);
        activator.activateDeactivateGroup(false, dataGroups.get(0), confirmer);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.verify(dataGroup);
        }

        EasyMock.verify(dataGroupActivator, confirmer, parent);
    }

    /**
     * Tests deactivating groups and parents.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testDeactivateGroupsAndParents() throws InterruptedException
    {
        UserConfirmer confirmer = createConfirmer(false, false);
        DataGroupInfo parent = createParent(null, false);
        List<DataGroupInfo> dataGroups = createDataGroups(2, 0, parent, confirmer);
        EasyMock.expect(parent.getChildren()).andReturn(New.list(dataGroups.get(0)));
        EasyMock.expect(dataGroups.get(0).groupStream()).andReturn(dataGroups.stream());

        DataGroupActivator dataGroupActivator = getDataGroupActivator(parent, dataGroups.stream(), false);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.replay(dataGroup);
        }

        EasyMock.replay(dataGroupActivator, confirmer, parent);

        GroupActivator activator = new GroupActivator(dataGroupActivator);
        activator.activateDeactivateGroup(false, dataGroups.get(0), confirmer);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.verify(dataGroup);
        }

        EasyMock.verify(dataGroupActivator, confirmer, parent);
    }

    /**
     * Tests deactivating over ten descendants.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testDeactivateOverTenDescendants() throws InterruptedException
    {
        UserConfirmer confirmer = createConfirmer(false, false);
        DataGroupInfo parent = createParent(null, false);
        List<DataGroupInfo> dataGroups = createDataGroups(2, 11, parent, null);
        EasyMock.expect(parent.getChildren()).andReturn(New.list(dataGroups.get(0)));
        EasyMock.expect(dataGroups.get(0).groupStream()).andReturn(dataGroups.stream());

        DataGroupActivator dataGroupActivator = getDataGroupActivator(parent, dataGroups.stream(), false);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.replay(dataGroup);
        }

        EasyMock.replay(dataGroupActivator, confirmer, parent);

        GroupActivator activator = new GroupActivator(dataGroupActivator);
        activator.activateDeactivateGroup(false, dataGroups.get(0), confirmer);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.verify(dataGroup);
        }

        EasyMock.verify(dataGroupActivator, confirmer, parent);
    }

    /**
     * Tests deactivating single group and parent.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testDeactivateSingleGroupAndParents() throws InterruptedException
    {
        UserConfirmer confirmer = createConfirmer(false, false);
        DataGroupInfo parent = createParent(null, false);
        List<DataGroupInfo> dataGroups = createDataGroups(0, 0, parent, confirmer);
        EasyMock.expect(parent.getChildren()).andReturn(New.list(dataGroups.get(0)));
        EasyMock.expect(dataGroups.get(0).groupStream()).andReturn(dataGroups.stream());

        DataGroupActivator dataGroupActivator = getDataGroupActivator(parent, dataGroups.stream(), false);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.replay(dataGroup);
        }

        EasyMock.replay(dataGroupActivator, confirmer, parent);

        GroupActivator activator = new GroupActivator(dataGroupActivator);
        activator.activateDeactivateGroup(false, dataGroups.get(0), confirmer);

        for (DataGroupInfo dataGroup : dataGroups)
        {
            EasyMock.verify(dataGroup);
        }

        EasyMock.verify(dataGroupActivator, confirmer, parent);
    }

    /**
     * Creates the confirmer.
     *
     * @param expectCall True if expected to be called.
     * @param responseYes True if it should return a yes response.
     * @return The confirmer.
     */
    private UserConfirmer createConfirmer(boolean expectCall, boolean responseYes)
    {
        UserConfirmer confirmer = EasyMock.createMock(UserConfirmer.class);

        if (expectCall)
        {
            confirmer.askUser(EasyMock.isA(String.class), EasyMock.isA(String.class));
            EasyMock.expectLastCall().andReturn(Boolean.valueOf(responseYes));
        }

        return confirmer;
    }

    /**
     * Creates data groups.
     *
     * @param numberOfChildren The number of children for the testing data
     *            group.
     * @param numberOfMembers The number of members for the testing data group.
     * @param parent The parent.
     * @param confirmer The confirmer.
     * @return The list of data groups.
     */
    private List<DataGroupInfo> createDataGroups(int numberOfChildren, int numberOfMembers, DataGroupInfo parent,
            UserConfirmer confirmer)
    {
        List<DataGroupInfo> dataGroups = New.list();

        Set<DataGroupInfo> children = New.set();

        for (int i = 0; i < numberOfChildren; i++)
        {
            DataGroupInfo child = EasyMock.createMock(DataGroupInfo.class);
            children.add(child);
        }

        DataGroupInfo dataGroup = EasyMock.createMock(DataGroupInfo.class);
        DataGroupActivationProperty activationProperty = new DataGroupActivationProperty(dataGroup);
        EasyMock.expect(dataGroup.activationProperty()).andReturn(activationProperty).anyTimes();
        EasyMock.expect(dataGroup.getParent()).andReturn(parent);

        if (numberOfMembers > 0 && confirmer != null)
        {
            Set<DataTypeInfo> members = New.set();
            for (int i = 0; i < numberOfMembers; i++)
            {
                DataTypeInfo dataType = EasyMock.createMock(DataTypeInfo.class);
                members.add(dataType);
            }

            EasyMock.expect(dataGroup.getMembers(EasyMock.eq(true))).andReturn(members);
        }

        dataGroups.add(dataGroup);
        dataGroups.addAll(children);

        return dataGroups;
    }

    /**
     * Create the parent.
     *
     * @param members The members of the parent.
     * @param membersActive True if a member should be active.
     * @return The parent.
     */
    private DataGroupInfo createParent(Set<DataTypeInfo> members, boolean membersActive)
    {
        DataGroupInfo parent = EasyMock.createMock(DataGroupInfo.class);
        EasyMock.expect(parent.getParent()).andReturn(null);
        if (members != null)
        {
            EasyMock.expect(parent.getMembers(EasyMock.eq(true))).andReturn(members);
        }

        return parent;
    }
}
