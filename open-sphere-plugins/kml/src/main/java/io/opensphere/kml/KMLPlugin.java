package io.opensphere.kml;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.AbstractPanelPlugin;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.CompositeService;
import io.opensphere.core.util.collections.New;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLToolbox;
import io.opensphere.kml.common.util.KMLToolboxUtils;
import io.opensphere.kml.envoy.KMLEnvoy;
import io.opensphere.kml.mantle.controller.KMLMantleUtilities;
import io.opensphere.kml.mantle.controller.KmlIcons;
import io.opensphere.kml.settings.KMLOptionsProvider;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.datasources.IDataSource;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * The main class for the KML plug-in.
 */
@ThreadSafe
public class KMLPlugin extends AbstractPanelPlugin
{
    /** The envoys that retrieve the KML data. */
    @GuardedBy("myEnvoys")
    private volatile Map<IDataSource, KMLEnvoy> myEnvoys;

    /** The master controller. */
    private KMLMasterController myMasterController;

    /** The plugin toolbox. */
    private volatile KMLToolbox myPluginToolbox;

    /** Listener for KML data sources in the data registry. */
    private final DataRegistryListener<KMLDataSource> myDataRegistryListener = new KMLDataRegistryListener();

    /** The service manager. */
    private volatile CompositeService myServiceManager;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);

        Preferences preferences = toolbox.getPreferencesRegistry().getPreferences(KMLPlugin.class);
        myPluginToolbox = new KMLToolbox(preferences);
        toolbox.getPluginToolboxRegistry().registerPluginToolbox(myPluginToolbox);
        KMLToolboxUtils.set(toolbox, myPluginToolbox);

        // Create and add the master data group
        KMLMantleUtilities.init(toolbox);

        myEnvoys = Collections.synchronizedMap(new HashMap<IDataSource, KMLEnvoy>());

        myServiceManager = new CompositeService(3);
        myServiceManager.addService(
                toolbox.getUIRegistry().getOptionsRegistry().getOptionsProviderService(new KMLOptionsProvider(myPluginToolbox)));
        myMasterController = myServiceManager.addService(new KMLMasterController(toolbox, preferences));
        myServiceManager.addService(toolbox.getModuleStateManager().getModuleStateControllerService(KMLStateConstants.MODULE_NAME,
                new KMLStateController(myMasterController.getDataSourceController())));
        myServiceManager.open();

        KMLDataRegistryHelper.addDataSourceChangeListener(toolbox.getDataRegistry(), myDataRegistryListener);

        KmlIcons.getKmlIconMap(toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class).getIconRegistry());
    }

    @Override
    public void close()
    {
        super.close();

        getToolbox().getDataRegistry().removeChangeListener(myDataRegistryListener);

        myServiceManager.close();

        getToolbox().getEnvoyRegistry().removeObjectsForSource(this);

        getToolbox().getPluginToolboxRegistry().removePluginToolbox(myPluginToolbox);
    }

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return Collections.<Envoy>unmodifiableCollection(myEnvoys.values());
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return Collections.singleton(myMasterController.getTransformer());
    }

    @Override
    protected void addTab(JTabbedPane tabbedPane)
    {
        tabbedPane.addTab("KML", null, myPluginToolbox.getTreePanel());
    }

    /** Listener for KML data sources in the data registry. */
    private class KMLDataRegistryListener extends DataRegistryListenerAdapter<KMLDataSource>
    {
        @Override
        public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends KMLDataSource> newValues,
                Object source)
        {
            Collection<KMLEnvoy> envoys = New.collection();
            for (KMLDataSource dataSource : newValues)
            {
                KMLEnvoy envoy = myEnvoys.get(dataSource);
                if (envoy == null)
                {
                    envoy = new KMLEnvoy(getToolbox(), dataSource);
                    envoys.add(envoy);
                    myEnvoys.put(dataSource, envoy);
                }
                else
                {
                    dataSource.associateEnvoy(envoy);
                    envoy.setDataSource(dataSource);
                }
            }
            if (!envoys.isEmpty())
            {
                getToolbox().getEnvoyRegistry().addObjectsForSource(KMLPlugin.this, envoys);
            }
        }

        @Override
        public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids,
                Iterable<? extends KMLDataSource> removedValues, Object source)
        {
            Collection<KMLEnvoy> envoys = New.collection();
            for (KMLDataSource kmlDataSource : removedValues)
            {
                KMLEnvoy envoy = myEnvoys.remove(kmlDataSource);
                if (envoy != null)
                {
                    envoys.add(envoy);
                    getToolbox().getEnvoyRegistry().removeObjectsForSource(KMLPlugin.this, Collections.singleton(envoy));
                }
            }
            getToolbox().getEnvoyRegistry().removeObjectsForSource(KMLPlugin.this, envoys);
        }

        @Override
        public void allValuesRemoved(Object source)
        {
            myEnvoys.clear();
            getToolbox().getEnvoyRegistry().removeObjectsForSource(KMLPlugin.this);
        }

        @Override
        public boolean isIdArrayNeeded()
        {
            return false;
        }
    }
}
