package io.opensphere.kml.common.util;

import java.awt.EventQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.opensphere.core.PluginToolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.kml.common.model.KMLDataSourceController;
import io.opensphere.kml.common.model.KMLSettings;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The KML toolbox.
 */
public class KMLToolbox implements PluginToolbox
{
    /** The KML preferences. */
    private final Preferences myPreferences;

    /** The KML Plugin Executor. */
    private final Executor myPluginExecutor;

    /** The KML style cache. */
    private final KMLStyleCache myStyleCache;

    /** The global KML settings. */
    private final KMLSettings mySettings;

    /** The tree panel. */
    private AbstractHUDPanel myTreePanel;

    /** The master data group. */
    private volatile DataGroupInfo myMasterGroup;

    /** The data source controller. */
    private volatile KMLDataSourceController myDataSourceController;

    /**
     * Constructor.
     *
     * @param preferences The KML preferences
     */
    public KMLToolbox(Preferences preferences)
    {
        myPreferences = preferences;
        myPluginExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("KMLPlugin"));
        myStyleCache = new KMLStyleCache();
        mySettings = myPreferences.getJAXBObject(KMLSettings.class, "settings", new KMLSettings());
    }

    @Override
    public String getDescription()
    {
        return "Toolbox for the KMLPlugin";
    }

    /**
     * Gets the data source controller.
     *
     * @return the data source controller
     */
    public KMLDataSourceController getDataSourceController()
    {
        return myDataSourceController;
    }

    /**
     * Gets the master group.
     *
     * @return the master group
     */
    public DataGroupInfo getMasterGroup()
    {
        return myMasterGroup;
    }

    /**
     * Getter for pluginExecutor.
     *
     * @return the pluginExecutor
     */
    public Executor getPluginExecutor()
    {
        return myPluginExecutor;
    }

    /**
     * Getter for styleCache.
     *
     * @return the styleCache
     */
    public KMLStyleCache getStyleCache()
    {
        return myStyleCache;
    }

    /**
     * Getter for treePanel.
     *
     * @return the treePanel
     */
    public AbstractHUDPanel getTreePanel()
    {
        assert EventQueue.isDispatchThread();

        return myTreePanel;
    }

    /**
     * Gets the settings.
     *
     * @return the settings
     */
    public KMLSettings getSettings()
    {
        return mySettings;
    }

    /**
     * Saves the settings to the preferences.
     */
    public void saveSettings()
    {
        myPreferences.putJAXBObject("settings", mySettings, false, this);
    }

    /**
     * Sets the data source controller.
     *
     * @param dataSourceController the new data source controller
     */
    public void setDataSourceController(KMLDataSourceController dataSourceController)
    {
        myDataSourceController = dataSourceController;
    }

    /**
     * Sets the master group.
     *
     * @param masterGroup the new master group
     */
    public void setMasterGroup(DataGroupInfo masterGroup)
    {
        myMasterGroup = masterGroup;
    }

    /**
     * Setter for treePanel.
     *
     * @param treePanel the treePanel
     */
    public void setTreePanel(AbstractHUDPanel treePanel)
    {
        assert EventQueue.isDispatchThread();

        myTreePanel = treePanel;
    }
}
