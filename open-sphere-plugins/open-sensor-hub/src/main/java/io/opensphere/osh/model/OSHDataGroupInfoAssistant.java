package io.opensphere.osh.model;

import java.awt.Component;
import java.awt.Dimension;

import io.opensphere.controlpanels.animation.event.ShowTimelineEvent;
import io.opensphere.controlpanels.animation.model.ViewPreference;
import io.opensphere.controlpanels.event.AnimationChangeExtentRequestEvent;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfoAssistant;
import io.opensphere.mantle.data.util.impl.DataTypeActionUtils;

/** OpenSensorHub Data Group Info Assistant. */
public class OSHDataGroupInfoAssistant extends DefaultDataGroupInfoAssistant
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public OSHDataGroupInfoAssistant(Toolbox toolbox)
    {
        super();
        myToolbox = toolbox;
    }

    @Override
    public Component getLayerControlUIComponent(Dimension preferredSize, DataGroupInfo dataGroup, DataTypeInfo dataType)
    {
        IconButton button = new IconButton("Show Data");
        IconUtil.setIcons(button, "/images/target.png");
        button.setToolTipText("Adjust the map and timeline to show data for the selected layer.");
        button.addActionListener(e -> showData(dataGroup));
        return button;
    }

    /**
     * Shows data for the data group.
     *
     * @param dataGroup the data group
     */
    public void showData(DataGroupInfo dataGroup)
    {
        ThreadUtilities.runCpu(() ->
        {
            gotoLocation(dataGroup);
            setActiveTime(dataGroup);
            myToolbox.getEventManager().publishEvent(new ShowTimelineEvent(ViewPreference.TIMELINE));
        });
    }

    /**
     * Sets the active time to show data for the data group.
     *
     * @param dataGroup the data group
     */
    private void setActiveTime(DataGroupInfo dataGroup)
    {
        TimeSpan extent = dataGroup.getMembers(false).iterator().next().getTimeExtents().getExtent();
        if (extent.isBounded() && !extent.isInstantaneous())
        {
            myToolbox.getEventManager().publishEvent(new AnimationChangeExtentRequestEvent(extent, this));
        }
    }

    /**
     * Goes to the data location of the data group.
     *
     * @param dataGroup the data group
     */
    public void gotoLocation(DataGroupInfo dataGroup)
    {
        DataTypeInfo dataType = dataGroup.getMembers(false).iterator().next();
        DataTypeActionUtils.gotoDataType(dataType, myToolbox.getMapManager().getStandardViewer());
    }
}
