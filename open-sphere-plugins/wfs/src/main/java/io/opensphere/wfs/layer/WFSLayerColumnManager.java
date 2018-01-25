package io.opensphere.wfs.layer;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class WFSLayerColumnManager. This class will handle the set of user
 * selected columns for each data layer.
 */
public class WFSLayerColumnManager
{
    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The preferences used to persist the column selections. */
    private final Preferences myPrefs;

    /** The Layers config. */
    private WFSLayerColumns myLayersConfig;

    /** The Lock. */
    private final ReentrantReadWriteLock myLock;

    /**
     * Instantiates a new wFS layer column manager.
     *
     * @param toolbox the toolbox
     */
    public WFSLayerColumnManager(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myPrefs = toolbox.getPreferencesRegistry().getPreferences(WFSLayerColumns.class);
        myLock = new ReentrantReadWriteLock();
        loadLayerColumnConfig();
    }

    /**
     * Gets the layers config.
     *
     * @return the layers config
     */
    public WFSLayerColumns getLayersConfig()
    {
        return myLayersConfig;
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Remove the associated config from the list and re-save the config file.
     *
     * @param dataType the data type to use to get the corresponding config
     */
    public void remove(DataTypeInfo dataType)
    {
        try
        {
            myLock.writeLock().lock();
            myLayersConfig.remove(dataType.getTypeKey());
            saveLayerColumnConfig(dataType, null);
        }
        finally
        {
            myLock.writeLock().unlock();
        }
    }

    /**
     * Save and write the layer column config file.
     *
     * @param dti the data type info to remove from the data type controller
     * @param config the layer config to save
     */
    public void saveLayerColumnConfig(DataTypeInfo dti, WFSLayerConfig config)
    {
        try
        {
            myLock.writeLock().lock();
            if (config != null)
            {
                boolean found = false;
                for (WFSLayerConfig wfsConfig : myLayersConfig.getLayers())
                {
                    if (wfsConfig.getLayerKey().equals(dti.getTypeKey()))
                    {
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    myLayersConfig.getLayers().add(config);
                }
            }

            if (myPrefs != null)
            {
                myPrefs.putJAXBObject(WFSLayerColumns.PREFERENCE_KEY, myLayersConfig, false, WFSLayerColumns.class);
            }
            DataTypeController dtc = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController();
            dtc.removeDataType(dti, this);
        }
        finally
        {
            myLock.writeLock().unlock();
        }
    }

    /**
     * Load layer column config.
     */
    private void loadLayerColumnConfig()
    {
        if (myPrefs != null)
        {
            try
            {
                myLock.readLock().lock();
                myLayersConfig = myPrefs.getJAXBObject(WFSLayerColumns.class, WFSLayerColumns.PREFERENCE_KEY,
                        new WFSLayerColumns());
            }
            finally
            {
                myLock.readLock().unlock();
            }
        }
    }
}
