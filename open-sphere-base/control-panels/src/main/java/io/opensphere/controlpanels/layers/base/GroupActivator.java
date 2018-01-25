package io.opensphere.controlpanels.layers.base;

import java.util.Collection;

import org.apache.log4j.Logger;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DataGroupActivator;

/**
 * Activates or deactivates data groups.
 *
 */
public class GroupActivator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GroupActivator.class);

    /** The data group activator. */
    private final DataGroupActivator myDataGroupActivator;

    /**
     * Constructs a new group activator.
     *
     * @param activator The activator.
     */
    public GroupActivator(DataGroupActivator activator)
    {
        myDataGroupActivator = activator;
    }

    /**
     * Activate deactivates a group and its descendants and ancestors.
     *
     * @param activate True if the group and its descendants should be
     *            activated, false if it should be deactivated.
     * @param dataGroupInfo The data group to activate or deactivate.
     * @param confirmer Interface to an object that asks the user yes no
     *            questions.
     */
    public void activateDeactivateGroup(boolean activate, DataGroupInfo dataGroupInfo, UserConfirmer confirmer)
    {
        if (confirmWithUser(activate, dataGroupInfo, confirmer))
        {
            try
            {
                // Activate self and children
                myDataGroupActivator.setGroupsActive(dataGroupInfo.groupStream().parallel(), activate);

                // Activate (grand)parents
                for (DataGroupInfo parent = dataGroupInfo.getParent(); parent != null; parent = parent.getParent())
                {
                    if (activate || canDeactivate(parent))
                    {
                        myDataGroupActivator.setGroupActive(parent, activate);
                    }
                }
            }
            catch (InterruptedException e)
            {
                LOGGER.error(e, e);
            }
        }
    }

    /**
     * If activating a lot of data types, asks the user if they are sure they
     * want to do that.
     *
     * @param activate True if the group and its descendants should be
     *            activated, false if it should be deactivated.
     * @param dataGroupInfo The data group to activate or deactivate.
     * @param confirmer Interface to an object that asks the user yes no
     *            questions.
     * @return True if activation should proceed, false otherwise.
     */
    private boolean confirmWithUser(boolean activate, DataGroupInfo dataGroupInfo, UserConfirmer confirmer)
    {
        boolean proceed = true;
        if (activate && confirmer != null)
        {
            Collection<DataTypeInfo> members = dataGroupInfo.getMembers(true);

            if (members.size() > Constants.SELECT_WARN_THRESHOLD)
            {
                proceed = confirmer.askUser("Are you sure you want to activate " + members.size() + " items?",
                        "Confirm Activation");
            }
        }

        return proceed;
    }

    /**
     * Determines if the group can be deactivated.
     *
     * @param group the data group
     * @return whether the group can be deactivated
     */
    private boolean canDeactivate(DataGroupInfo group)
    {
        return group.getChildren().stream()
                .noneMatch(g -> g.activationProperty().isActivatingOrDeactivating() || g.activationProperty().isActive());
    }
}
