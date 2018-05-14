package io.opensphere.server.services;

import java.awt.Color;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;

/**
 * The Class ServerBasicVisualizationInfo.
 */
public class ServerBasicVisualizationInfo extends DefaultBasicVisualizationInfo
{
    /**
     * Instantiates a new server basic visualization info.
     *
     * @param loadsTo the loads to
     * @param defaultTypeColor the default type color
     * @param usesDataElements the uses data elements
     */
    public ServerBasicVisualizationInfo(LoadsTo loadsTo, Color defaultTypeColor, boolean usesDataElements)
    {
        super(loadsTo, defaultTypeColor, usesDataElements);
    }

    /**
     * Expose the super class mutator for defaultTypeColor and set the type
     * color if it is the same as the old default.
     *
     * @param c the new default type color
     */
    @Override
    public void setDefaultTypeColor(Color c)
    {
        if (EqualsHelper.equalsAny(getTypeColor(), null, DEFAULT_DEFAULT_COLOR))
        {
            super.setTypeColor(c, this);
        }
        super.setDefaultTypeColor(c);
    }
}
