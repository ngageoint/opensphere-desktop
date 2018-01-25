package io.opensphere.mantle.data.dynmeta.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.dynmeta.DynamicDataElementMetadataManager;
import io.opensphere.mantle.data.dynmeta.DynamicMetadataDataTypeController;

/**
 * The Class DynamicColumnManager.
 */
public class DynamicMetadataManagerImpl implements DynamicDataElementMetadataManager
{
    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The Type key to coordinator map. */
    private final Map<String, DynamicMetadataDataTypeController> myTypeKeyToCoordinatorMap = new ConcurrentHashMap<>();

    /**
     * Instantiates a new dynamic column manager.
     *
     * @param tb the tb
     */
    public DynamicMetadataManagerImpl(Toolbox tb)
    {
        myToolbox = tb;
        // myTypeKeyToCoordinatorMap = new ConcurrentHashMap<String,
        // DynamicMetadataDataTypeController>();
    }

    /**
     * Adds the data type.
     *
     * @param dti the dti
     */
    public void addDataType(DataTypeInfo dti)
    {
        if (dti.getMetaDataInfo() == null)
        {
            myTypeKeyToCoordinatorMap.put(dti.getTypeKey(), new NoOppDynamicMetadataDataTypeController(dti));
        }
        else
        {
            myTypeKeyToCoordinatorMap.put(dti.getTypeKey(), new DynamicMetadataDataTypeControllerImpl(myToolbox, dti));
        }
    }

    @Override
    public DynamicMetadataDataTypeController getController(String dtiKey)
    {
        return dtiKey == null ? null : myTypeKeyToCoordinatorMap.get(dtiKey);
    }

    /**
     * Removes the data type.
     *
     * @param dti the dti
     */
    public void removeDataType(DataTypeInfo dti)
    {
        myTypeKeyToCoordinatorMap.remove(dti.getTypeKey());
    }
}
