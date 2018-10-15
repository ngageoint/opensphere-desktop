package io.opensphere.mantle.controller.util;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.model.time.ExtentAccumulator;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DataGroupInfoUtilities.
 */
public final class DataGroupInfoUtilities
{
    /**
     * Find time extent for the provided list of DataGroupInfo.
     *
     * @param dgiCollection the {@link DataGroupInfo} collection.
     * @return the time span
     */
    public static TimeSpan findTimeExtentFromDGICollection(Collection<DataGroupInfo> dgiCollection)
    {
        ExtentAccumulator extentAccumulator = new ExtentAccumulator();

        for (DataGroupInfo dgi : dgiCollection)
        {
            if (dgi.hasMembers(false))
            {
                for (DataTypeInfo dti : dgi.getMembers(false))
                {
                    if (dti.getTimeExtents() != null)
                    {
                        List<TimeSpan> dtiExtents = dti.getTimeExtents().getTimespans();
                        if (dtiExtents != null && !dtiExtents.isEmpty())
                        {
                            dtiExtents.stream().filter(ts -> !ts.isTimeless()).forEach(extentAccumulator::add);
                        }
                    }
                }
            }
        }
        return extentAccumulator.getExtent();
    }

    /**
     * Instantiates a new data group info utilities.
     */
    private DataGroupInfoUtilities()
    {
        // Don't allow instantiation.
    }
}
