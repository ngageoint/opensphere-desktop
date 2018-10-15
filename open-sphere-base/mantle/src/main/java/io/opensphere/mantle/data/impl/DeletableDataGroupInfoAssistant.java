package io.opensphere.mantle.data.impl;

import java.util.function.Consumer;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoAssistant;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoOrderManager;

/**
 * A {@link DataGroupInfoAssistant} that supports deleting the data group.
 */
public class DeletableDataGroupInfoAssistant extends DefaultDataGroupInfoAssistant
{
    /** The data type order manager. */
    private final DataTypeInfoOrderManager myDataTypeOrderManager;

    /**
     * The object wanting notification when this data group gets deleted.
     */
    private final Consumer<DataGroupInfo> myDeleteListener;

    /** The Mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Constructor.
     *
     * @param mantleToolbox The mantle toolbox.
     * @param prefsRegistry The system preferences registry.
     * @param dataTypeOrderManager The data type order manager.
     */
    public DeletableDataGroupInfoAssistant(MantleToolbox mantleToolbox, PreferencesRegistry prefsRegistry,
            DataTypeInfoOrderManager dataTypeOrderManager)
    {
        this(mantleToolbox, prefsRegistry, dataTypeOrderManager, null);
    }

    /**
     * Constructor.
     *
     * @param mantleToolbox The mantle toolbox.
     * @param prefsRegistry The system preferences registry.
     * @param dataTypeOrderManager The data type order manager.
     * @param deleteListener Object wanting notification of the delete, or null
     *            if no such thing is desired.
     */
    public DeletableDataGroupInfoAssistant(MantleToolbox mantleToolbox, PreferencesRegistry prefsRegistry,
            DataTypeInfoOrderManager dataTypeOrderManager, Consumer<DataGroupInfo> deleteListener)
    {
        myMantleToolbox = mantleToolbox;
        myPreferencesRegistry = prefsRegistry;
        myDataTypeOrderManager = dataTypeOrderManager;
        myDeleteListener = deleteListener;
    }

    @Override
    public boolean canDeleteGroup(DataGroupInfo dgi)
    {
        return true;
    }

    @Override
    public void deleteGroup(DataGroupInfo dgi, Object source)
    {
        dgi.getChildren().forEach(g -> deleteGroup(g, source));
        for (DataTypeInfo type : dgi.getMembers(false))
        {
            if (myDataTypeOrderManager != null)
            {
                myDataTypeOrderManager.expungeDataType(type);
            }
            if (myPreferencesRegistry != null)
            {
                DefaultMetaDataInfo.clearPreferencesRegistryEntryForNumericCache(myPreferencesRegistry, type.getTypeKey(), this);
            }
            myMantleToolbox.getDataTypeInfoPreferenceAssistant().removePreferences(type.getTypeKey());
            myMantleToolbox.getVisualizationStyleController().removeStyle(dgi, type);
            myMantleToolbox.getDataTypeController().removeDataType(type, this);
        }

        DataGroupInfo parent = dgi.getParent();
        if (parent != null)
        {
            parent.removeChild(dgi, this);
        }
        myMantleToolbox.getDataGroupController().cleanUpGroup(dgi);

        if (myDeleteListener != null)
        {
            myDeleteListener.accept(dgi);
        }
    }
}
