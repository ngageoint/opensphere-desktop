package io.opensphere.controlpanels.layers.activedata.zorder;

import java.util.Collection;
import java.util.Comparator;

import io.opensphere.core.Toolbox;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Sorts the data groups by their z-order value or name if they don't have a
 * z-order value.
 */
public class ZOrderGroupComparator implements Comparator<DataGroupInfo>
{
    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new z order comparator.
     *
     * @param toolbox The toolbox.
     */
    public ZOrderGroupComparator(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public int compare(DataGroupInfo group1, DataGroupInfo group2)
    {
        Collection<DataTypeInfo> group1Members = group1.getMembers(false);
        Collection<DataTypeInfo> group2Members = group2.getMembers(false);

        int compareValue = 0;

        if (group1Members.isEmpty() && group2Members.isEmpty())
        {
            compareValue = group1.getDisplayName().compareTo(group2.getDisplayName());
        }
        else
        {
            OrderManagerRegistry registry = myToolbox.getOrderManagerRegistry();

            int group1WinCount = 0;
            int group1LossCount = 0;
            for (DataTypeInfo group1Type : group1Members)
            {
                float winCount = 0;

                int membersOfSameCategory = 0;

                if (group1Type.getOrderKey() != null)
                {
                    int group1Order = Integer.MAX_VALUE;
                    group1Order = registry.getOrderManager(group1Type.getOrderKey()).getOrder(group1Type.getOrderKey());

                    for (DataTypeInfo group2Type : group2Members)
                    {
                        int group2Order = Integer.MAX_VALUE;
                        if (group2Type.getOrderKey() != null
                                && group1Type.getOrderKey().getCategory().equals(group2Type.getOrderKey().getCategory()))
                        {
                            group2Order = registry.getOrderManager(group2Type.getOrderKey()).getOrder(group2Type.getOrderKey());

                            membersOfSameCategory++;

                            if (group1Order <= group2Order)
                            {
                                winCount++;
                            }
                        }
                    }
                }

                float winPercentage = winCount / membersOfSameCategory;
                if (winPercentage >= .5f)
                {
                    group1WinCount++;
                }
                else
                {
                    group1LossCount++;
                }
            }

            if (group1WinCount == group1LossCount)
            {
                compareValue = group1.getDisplayName().compareTo(group2.getDisplayName());
            }
            else if (group1WinCount < group1LossCount)
            {
                compareValue = -1;
            }
            else
            {
                compareValue = 1;
            }
        }

        return compareValue;
    }
}
