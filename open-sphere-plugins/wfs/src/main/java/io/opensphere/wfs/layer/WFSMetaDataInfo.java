package io.opensphere.wfs.layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.GuardedBy;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.wfs.layer.SingleLayerRequeryEvent.RequeryType;

/**
 * This class will keep track of meta info associated with a layer and handles
 * loading and saving the set of column filters for all layers.
 */
public final class WFSMetaDataInfo extends DefaultMetaDataInfo
{
    /** The WFS layer controller. */
    private final WFSLayerColumnManager myWFSLayerController;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The Original key info. */
    private final DefaultMetaDataInfo myOriginalKeyInfo;

    /** List of columns that are DateTime types. */
    @GuardedBy("this")
    private Set<String> myDateColumns;

    /**
     * Flag that indicates whether the time column is dynamic. This affects how
     * the time column gets populated in requests to certain servers.
     */
    private volatile boolean myIsDynamicTime;

    /**
     * Instantiates a new WFS meta data info.
     *
     * @param toolbox the core toolbox
     * @param controller manager for storing/retrieving saved WFS column
     *            configurations
     */
    public WFSMetaDataInfo(Toolbox toolbox, WFSLayerColumnManager controller)
    {
        super();
        setSpecialKeyDetector(MantleToolboxUtils.getMantleToolbox(toolbox).getColumnTypeDetector());
        myWFSLayerController = controller;
        myToolbox = toolbox;
        myOriginalKeyInfo = new DefaultMetaDataInfo();
        myOriginalKeyInfo.setSpecialKeyDetector(MantleToolboxUtils.getMantleToolbox(toolbox).getColumnTypeDetector());
    }

    /**
     * Adds a column name to the list of keys that are Date types.
     *
     * @param columnName the name of the date column
     */
    public synchronized void addDateKey(String columnName)
    {
        if (myDateColumns == null)
        {
            myDateColumns = New.set();
        }
        myDateColumns.add(columnName);
    }

    /**
     * Adds a deselected column.
     *
     * @param column the column
     */
    public void addDeselectedColumn(String column)
    {
        WFSLayerConfig layerConfig = myWFSLayerController.getLayersConfig().findLayer(getDataTypeInfo().getTypeKey());
        if (layerConfig != null)
        {
            layerConfig.getDeselectedColumns().add(column);
        }
    }

    /**
     * Adds the wfs key.
     *
     * @param key the key
     * @param keyClass the key class
     * @param source the source
     * @return true, if successful
     */
    public boolean addWFSKey(String key, Class<?> keyClass, Object source)
    {
        myOriginalKeyInfo.addKey(key, keyClass, source);
        return (getCurrentColumnSet().isEmpty() || getCurrentColumnSet().contains(key)) && addKey(key, keyClass, source);
    }

    /**
     * Gets whether to automatically disable empty columns.
     *
     * @return true, if successful
     */
    public boolean isAutomaticallyDisableEmptyColumns()
    {
        boolean automaticallyDisable = true;
        WFSLayerConfig layerConfig = myWFSLayerController.getLayersConfig().findLayer(getDataTypeInfo().getTypeKey());
        if (layerConfig != null)
        {
            automaticallyDisable = layerConfig.automaticallyDisableEmptyColumns();
        }
        return automaticallyDisable;
    }

    /**
     * Sets whether to automatically disable empty columns.
     *
     * @param automaticallyDisable the new automatically disable empty columns
     */
    public void setAutomaticallyDisableEmptyColumns(boolean automaticallyDisable)
    {
        WFSLayerConfig layerConfig = myWFSLayerController.getLayersConfig().findLayer(getDataTypeInfo().getTypeKey());
        if (layerConfig != null)
        {
            layerConfig.setAutomaticallyDisableEmptyColumns(automaticallyDisable);
        }
    }

    /**
     * Gets the current column set for a data layer.
     *
     * @return the current column set
     */
    public Set<String> getCurrentColumnSet()
    {
        WFSLayerConfig layerConfig = myWFSLayerController.getLayersConfig().findLayer(getDataTypeInfo().getTypeKey());
        Set<String> filteredColumns = new HashSet<>(myOriginalKeyInfo.getKeyNames());
        if (layerConfig != null)
        {
            filteredColumns.removeAll(layerConfig.getDeselectedColumns());
        }
        else
        {
            filteredColumns.remove("FILE_NAME");
            filteredColumns.remove("FILENAME");
            filteredColumns.remove("filename");
            filteredColumns.remove("file_name");
        }

        return filteredColumns;
    }

    /**
     * Gets a list of keys that are Date types.
     *
     * @return the list of date keys
     */
    public synchronized List<String> getDateKeys()
    {
        return myDateColumns == null ? Collections.<String>emptyList() : Collections.unmodifiableList(New.list(myDateColumns));
    }

    /**
     * Gets the set of deselected columns.
     *
     * @return the deselected columns
     */
    public Set<String> getDeselectedColumns()
    {
        WFSLayerConfig layerConfig = myWFSLayerController.getLayersConfig().findLayer(getDataTypeInfo().getTypeKey());
        if (layerConfig != null)
        {
            return layerConfig.getDeselectedColumns();
        }
        return New.set();
    }

    /**
     * Gets the unfiltered meta data info.
     *
     * @return the unfiltered meta data info
     */
    public DefaultMetaDataInfo getUnfilteredMetaDataInfo()
    {
        return myOriginalKeyInfo;
    }

    /**
     * Gets the unfiltered sorted key names.
     *
     * @return the unfiltered sorted key names
     */
    public List<String> getUnfilteredSortedKeyNames()
    {
        List<String> keyList = new ArrayList<>(myOriginalKeyInfo.getKeyNames());
        Collections.sort(keyList);
        return keyList;
    }

    /**
     * Gets the unfiltered special type for key.
     *
     * @param key the key
     * @return the unfiltered special type for key
     */
    public SpecialKey getUnfilteredSpecialTypeForKey(String key)
    {
        return myOriginalKeyInfo.getSpecialTypeForKey(key);
    }

    /**
     * Checks if the time column is dynamic.
     *
     * @return true, if time column is dynamic
     */
    public boolean isDynamicTime()
    {
        return myIsDynamicTime;
    }

    /**
     * Removes the column config from the controller for this data type.
     *
     * @param fireEvent whether to fire a re-query event
     */
    public void removeColumnConfig(boolean fireEvent)
    {
        myWFSLayerController.remove(getDataTypeInfo());
        if (fireEvent)
        {
            notifyColumnSetChanged();
        }
    }

    /**
     * Save column config.
     *
     * @param layerConfig the layer config
     */
    public void saveColumnConfig(WFSLayerConfig layerConfig)
    {
        myWFSLayerController.saveLayerColumnConfig(getDataTypeInfo(), layerConfig);
        notifyColumnSetChanged();
    }

    /**
     * Sets the time column to be dynamic or not.
     *
     * @param isDynamic true, if time column should be treated as dynamic
     */
    public void setDynamicTime(boolean isDynamic)
    {
        myIsDynamicTime = isDynamic;
    }

    @Override
    public void setSpecialKey(String key, SpecialKey specialType, Object source)
    {
        myOriginalKeyInfo.setSpecialKey(key, specialType, source);
        super.setSpecialKey(key, specialType, source);
    }

    /**
     * Notifies the event manager that a column set changed after a save or
     * remove operation.
     */
    private void notifyColumnSetChanged()
    {
        resetKeys();
        SingleLayerRequeryEvent event = new SingleLayerRequeryEvent((WFSDataType)getDataTypeInfo(), RequeryType.FULL_REQUERY);
        myToolbox.getEventManager().publishEvent(event);
    }

    /**
     * Reset keys.
     */
    private void resetKeys()
    {
        Set<String> newColumnSet = getCurrentColumnSet();
        Map<String, Class<?>> keyClassMap = myOriginalKeyInfo.getKeyClassTypeMap();
        clearKeyData();
        if (newColumnSet == null || newColumnSet.isEmpty())
        {
            newColumnSet = New.set(myOriginalKeyInfo.getKeyNames());
        }

        for (String origCol : myOriginalKeyInfo.getKeyNames())
        {
            if (newColumnSet.contains(origCol))
            {
                Class<?> keyClass = keyClassMap.get(origCol);
                SpecialKey specKey = myOriginalKeyInfo.getSpecialTypeForKey(origCol);
                addKey(origCol, keyClass, this);
                if (specKey != null)
                {
                    setSpecialKey(origCol, specKey, this);
                }
            }
        }

        copyKeysToOriginalKeys();
    }
}
