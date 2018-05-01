package io.opensphere.wps.layer;

import java.awt.Color;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.crust.DataTypeInfoUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.filter.DataLayerFilter;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DeletableDataGroupInfoAssistant;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.util.WPSConstants;

/**
 * Configures a WPS layer properly so that data is shown on the globe correctly.
 */
public class LayerConfigurer
{
    /**
     * The mantle toolbox.
     */
    private final MantleToolbox myMantlebox;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new layer configurer.
     *
     * @param toolbox The system toolbox.
     */
    public LayerConfigurer(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myMantlebox = MantleToolboxUtils.getMantleToolbox(myToolbox);
    }

    /**
     * Configures the layer so data can be displayed properly on the globe.
     *
     * @param configuration Contains user's inputs on how the data should look.
     * @param resultType The layer to configure.
     * @return The group to add this layer to, once data is received.
     */
    public DataGroupInfo configureLayer(WpsProcessConfiguration configuration, WFSDataType resultType)
    {
        DataTypeInfo correspondingType = getDataType(resultType.getTypeName());
        DataGroupInfo group = resultType.getParent();
        if (correspondingType != null)
        {
            DataTypeInfoUtilities.copyDataTypeInfo(correspondingType, resultType, this);
            setLayerColor(configuration, resultType);

            if (group == null)
            {
                DataGroupInfo parent = correspondingType.getParent();
                while (!parent.isRootNode())
                {
                    parent = parent.getParent();
                }

                String instanceName = configuration.getInputs().get(WPSConstants.PROCESS_INSTANCE_NAME);
                group = new DefaultDataGroupInfo(false, myToolbox, "WPS", resultType.getTypeKey(), instanceName);
                ((DefaultDataGroupInfo)group).setAssistant(
                        new DeletableDataGroupInfoAssistant(MantleToolboxUtils.getMantleToolbox(myToolbox), null, null, g ->
                        {
                        }));
                parent.addChild(group, this);
            }
        }

        myMantlebox.getDataTypeController().addDataType("WPS", "WPS", resultType, this);

        return group;
    }

    /**
     * Gets the set of available data types currently loaded in the application.
     *
     * @param typeName the name of the type to fetch.
     * @return the set of available data types currently loaded in the
     *         application.
     */
    private DataTypeInfo getDataType(String typeName)
    {
        DataTypeInfo returnValue = null;

        List<DataGroupInfo> dataGroups = myMantlebox.getDataGroupController().createGroupList(null, new DataLayerFilter());
        for (DataGroupInfo dgi : dataGroups)
        {
            for (DataTypeInfo type : dgi.getMembers(false))
            {
                if (StringUtils.equals(typeName, type.getTypeName()) && type.getMetaDataInfo() != null)
                {
                    returnValue = type;
                    break;
                }
            }
        }
        return returnValue;
    }

    /**
     * Sets the color of the layer to the color selected by the user.
     *
     * @param configuration Contains the color selected by the user.
     * @param resultType The layer to set the color for.
     */
    public void setLayerColor(WpsProcessConfiguration configuration, WFSDataType resultType)
    {
        String colorProp = configuration.getInputs().get(WPSConstants.COLOR_PROP);
        if (StringUtils.isNotEmpty(colorProp))
        {
            Color color = ColorUtilities.convertFromHexString(colorProp.replaceFirst("0x", ""), 0, 1, 2, 3);
            resultType.setDefaultTypeColor(color);
            resultType.getBasicVisualizationInfo().setTypeColor(color, this);
        }
    }
}
