package io.opensphere.wms.sld.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.wms.sld.SldRegistry;
import io.opensphere.wms.sld.config.SldConfigUtilities;
import io.opensphere.wms.sld.config.v1.SldConfiguration;
import io.opensphere.wms.sld.event.SldChangeEvent;
import io.opensphere.wms.sld.event.SldChangeListener;
import net.opengis.sld._100.StyledLayerDescriptor;

/**
 * The Class SldRegistryImpl.
 */
public class SldRegistryImpl implements SldRegistry
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(SldRegistryImpl.class);

    /** Lock to prevent different threads from corrupting file I/O or maps. */
    private final ReentrantLock myChangeLock = new ReentrantLock();

    /** The map of layer ids to Styled Layer Descriptors. */
    private Map<String, List<StyledLayerDescriptor>> myDataTypeToSldMap;

    /** Map used internally to keep track of SLD names that are in use. */
    private final Map<String, List<String>> myDataTypeToSldNameMap = New.map();

    /** The Change support. */
    private final WeakChangeSupport<SldChangeListener> mySldChangeListeners;

    /** Map used internally to track SLD names to their corresponding SLD. */
    private final Map<String, StyledLayerDescriptor> mySldNameToSldMap = New.map();

    /** The core toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new default implementation of the SLD registry.
     *
     * @param toolbox the toolbox
     */
    public SldRegistryImpl(Toolbox toolbox)
    {
        myToolbox = toolbox;
        mySldChangeListeners = new WeakChangeSupport<>();
        try
        {
            SldConfiguration cfgFile = new SldConfiguration();
            myChangeLock.lock();
            cfgFile.loadConfig(myToolbox);
            myDataTypeToSldMap = cfgFile.getLayerMap();
            if (myDataTypeToSldMap == null)
            {
                myDataTypeToSldMap = New.map();
            }
            else
            {
                buildTrackingMaps();
            }
        }
        finally
        {
            myChangeLock.unlock();
        }

//        logDebugConfig();
    }

    @Override
    public void addNewSld(final String layerKey, final StyledLayerDescriptor sldConfig)
    {
        final String sldName = SldConfigUtilities.getNameFromSld(sldConfig);
        try
        {
            myChangeLock.lock();
            if (!myDataTypeToSldNameMap.containsKey(layerKey) || !myDataTypeToSldNameMap.get(layerKey).contains(sldName))
            {
                CollectionUtilities.multiMapAdd(myDataTypeToSldMap, layerKey, sldConfig, false);
                CollectionUtilities.multiMapAdd(myDataTypeToSldNameMap, layerKey, sldName, false);
                mySldNameToSldMap.put(buildNameKey(layerKey, sldName), sldConfig);
                logDebugConfig();
                writeFile();
                // Notify sld listeners that a new sld has been added
                mySldChangeListeners.notifyListeners(new Callback<SldChangeListener>()
                {
                    @Override
                    public void notify(SldChangeListener listener)
                    {
                        listener.sldCreated(new SldChangeEvent(this, layerKey, sldName, sldConfig));
                    }
                });
            }
        }
        finally
        {
            myChangeLock.unlock();
        }
    }

    @Override
    public void addSldChangeListener(SldChangeListener listener)
    {
        mySldChangeListeners.addListener(listener);
    }

    @Override
    public StyledLayerDescriptor getSldByLayerAndName(String layerKey, String sldName)
    {
        return mySldNameToSldMap.get(buildNameKey(layerKey, sldName));
    }

    @Override
    public List<String> getSldNamesForLayer(String layerKey)
    {
        if (!myDataTypeToSldNameMap.containsKey(layerKey))
        {
            return Collections.emptyList();
        }
        return New.list(myDataTypeToSldNameMap.get(layerKey));
    }

    @Override
    public List<StyledLayerDescriptor> getSldsForLayer(String layerKey)
    {
        if (!myDataTypeToSldMap.containsKey(layerKey))
        {
            return Collections.emptyList();
        }
        return New.list(myDataTypeToSldMap.get(layerKey));
    }

    @Override
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public void removeSld(final String layerKey, final String sldName)
    {
        try
        {
            myChangeLock.lock();
            if (myDataTypeToSldNameMap.containsKey(layerKey) && myDataTypeToSldNameMap.get(layerKey).contains(sldName))
            {
                CollectionUtilities.multiMapRemove(myDataTypeToSldNameMap, layerKey, sldName);
                final StyledLayerDescriptor removedSld = mySldNameToSldMap.remove(buildNameKey(layerKey, sldName));
                if (removedSld != null)
                {
                    CollectionUtilities.multiMapRemove(myDataTypeToSldMap, layerKey, removedSld);
                }
                logDebugConfig();
                writeFile();
                // Notify Change listeners that an SLD has been removed
                mySldChangeListeners.notifyListeners(new Callback<SldChangeListener>()
                {
                    @Override
                    public void notify(SldChangeListener listener)
                    {
                        listener.sldDeleted(new SldChangeEvent(this, layerKey, sldName, removedSld));
                    }
                });
            }
        }
        finally
        {
            myChangeLock.unlock();
        }
    }

    @Override
    public void removeSldChangeListener(SldChangeListener listener)
    {
        mySldChangeListeners.removeListener(listener);
    }

    @Override
    public void replaceSld(final String layerKey, final StyledLayerDescriptor sldConfig)
    {
        String sldName = SldConfigUtilities.getNameFromSld(sldConfig);

        // if SLD does not alread exist, just add it
        if (!myDataTypeToSldNameMap.containsKey(layerKey) || !myDataTypeToSldNameMap.get(layerKey).contains(sldName))
        {
            addNewSld(layerKey, sldConfig);
        }
        else
        {
            try
            {
                myChangeLock.lock();
                String sldKey = buildNameKey(layerKey, sldName);
                StyledLayerDescriptor oldSld = mySldNameToSldMap.remove(sldKey);
                if (oldSld != null)
                {
                    CollectionUtilities.multiMapRemove(myDataTypeToSldMap, sldKey, oldSld);
                }
                mySldNameToSldMap.put(sldKey, sldConfig);
                CollectionUtilities.multiMapAdd(myDataTypeToSldMap, layerKey, sldConfig, false);
                logDebugConfig();
                writeFile();

                // Does the change need to be evented out??
            }
            finally
            {
                myChangeLock.unlock();
            }
        }
    }

    /**
     * Builds the name key for use in the name-&gt;SLD map.
     *
     * @param layerKey the layer key
     * @param sldName the SLD name
     * @return the key that uniquely identifies the SLD internally
     */
    private String buildNameKey(String layerKey, String sldName)
    {
        return StringUtilities.concat(layerKey, ":", sldName);
    }

    /**
     * Internal method that builds the maps of layers and SLD names. NOTE: This
     * method is not thread save and calling methods need to synchronize
     * appropriately.
     */
    private void buildTrackingMaps()
    {
        try
        {
            myChangeLock.lock();
            for (Map.Entry<String, List<StyledLayerDescriptor>> entry : myDataTypeToSldMap.entrySet())
            {
                String layerKey = entry.getKey();
                List<String> nameList = New.list();
                for (StyledLayerDescriptor sld : entry.getValue())
                {
                    String sldName = SldConfigUtilities.getNameFromSld(sld);
                    nameList.add(sldName);
                    mySldNameToSldMap.put(buildNameKey(layerKey, sldName), sld);
                }
                myDataTypeToSldNameMap.put(layerKey, nameList);
            }
        }
        finally
        {
            myChangeLock.unlock();
        }
    }

    /**
     * Output the layer/sld configuration to the debug at "info" level.
     */
    private void logDebugConfig()
    {
        if (LOGGER.isInfoEnabled())
        {
            StringBuilder sb = new StringBuilder("SLD Configuration:\n");
            for (Map.Entry<String, List<StyledLayerDescriptor>> entry : myDataTypeToSldMap.entrySet())
            {
                String layer = entry.getKey();
                sb.append("Layer[").append(layer).append("] SLDs:\n");
                for (StyledLayerDescriptor sld : entry.getValue())
                {
                    sb.append("   ").append(SldConfigUtilities.printSld(sld)).append('\n');
                }
            }
            LOGGER.info(sb.toString());
        }
    }

    /**
     * Write the contents of the SLD map to file. NOTE: This method is not
     * thread save and calling methods need to synchronize appropriately.
     */
    private void writeFile()
    {
        if (!myDataTypeToSldMap.isEmpty())
        {
            SldConfiguration cfgFile = new SldConfiguration(myDataTypeToSldMap);
            try
            {
                cfgFile.writeSldConfig(myToolbox);
            }
            catch (JAXBException e)
            {
                LOGGER.warn("Failed to save SLD configuration.", e);
            }
        }
    }
}
