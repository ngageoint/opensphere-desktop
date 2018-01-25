package io.opensphere.mantle.data.geom.style.dialog;

import java.util.function.Predicate;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class DataLayerDataGroupFilter.
 */
public class VisualizationStyleDataGroupTreeFilter implements Predicate<DataGroupInfo>
{
    /** The DGC. */
    private final DataGroupController myDGC;

    /**
     * Instantiates a new data layer data group filter.
     *
     * @param tb the tb
     */
    public VisualizationStyleDataGroupTreeFilter(Toolbox tb)
    {
        myDGC = MantleToolboxUtils.getMantleToolbox(tb).getDataGroupController();
    }

    @Override
    public boolean test(DataGroupInfo theDGI)
    {
        boolean isAcceptable = false;

        if (theDGI.hasMembers(false))
        {
            for (DataTypeInfo dtInfo : theDGI.getMembers(false))
            {
                if (dtInfo.getBasicVisualizationInfo() != null && dtInfo.getMapVisualizationInfo() != null
                        && dtInfo.getMapVisualizationInfo().usesVisualizationStyles())
                {
                    isAcceptable = myDGC.isTypeActive(dtInfo);
                    break;
                }
            }
        }

        if (!isAcceptable && theDGI.hasChildren())
        {
            for (DataGroupInfo childGroup : theDGI.getChildren())
            {
                if (test(childGroup))
                {
                    isAcceptable = true;
                }
            }
        }
        return isAcceptable;
    }
}
