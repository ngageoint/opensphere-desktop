package io.opensphere.server.control;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.mantle.datasources.impl.UrlSourceConfig;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.display.ServerSourceEditor;
import io.opensphere.server.display.ServiceValidator;
import io.opensphere.server.display.UrlSourceEditor;
import io.opensphere.server.display.model.UrlSourceModel;
import io.opensphere.server.toolbox.ServerSourceController;

/** UrlDataSource {@link ServerSourceController}. */
public abstract class UrlServerSourceController extends AbstractServerSourceController
{
    /** The server customization. */
    private volatile ServerCustomization myServerCustomization;

    /** The map of sources to activation threads. */
    private final Map<IDataSource, Thread> myActivationThreads = Collections.synchronizedMap(new HashMap<IDataSource, Thread>());

    /** The set of sources whose activations were canceled by the user, not because of an error. */
    private final Set<IDataSource> myUserCancellations = Collections.synchronizedSet(new HashSet<IDataSource>());

    private Map<IDataSource, ReloadListener> myReloadListenerMap = new HashMap<IDataSource, ReloadListener>();

    @Override
    public void open(Toolbox toolbox, Class<?> prefsTopic)
    {
        super.open(toolbox, prefsTopic);

        myServerCustomization = getServerCustomization();

        addServerTypes();

        // Load the config and activate sources
        Preferences preferences = toolbox.getPreferencesRegistry().getPreferences(prefsTopic);
        setConfig(readConfig(preferences));

        initialize();
    }

    @Override
    public IDataSource createNewSource(String typeName)
    {
        return new UrlDataSource();
    }

    @Override
    public void activateSource(IDataSource source)
    {
        ThreadUtilities.runBackground(() ->
        {
            source.setBusy(true, this);
            myActivationThreads.put(source, Thread.currentThread());

            boolean success = false;
            try
            {
                success = handleActivateSource(source);
            }
            finally
            {
                source.setActive(success);
                source.setLoadError(!success && !myUserCancellations.contains(source), this);
                source.setBusy(false, this);

                // Persist the configuration
                updateSource(source);

                myActivationThreads.remove(source);
                myUserCancellations.remove(source);
            }

            System.out.println("Activated " + source.getName() + " at " + new Date());
        });
    }

    @Override
    public void deactivateSource(IDataSource source)
    {
        ThreadUtilities.runBackground(() ->
        {
            source.setBusy(true, this);
            // if activation has started for this source, cancel activation
            synchronized (myActivationThreads)
            {
                if (myActivationThreads.containsKey(source))
                {
                    myActivationThreads.get(source).interrupt();
                    myUserCancellations.add(source);
                }
            }

            try
            {
                handleDeactivateSource(source);
            }
            finally
            {
                source.setActive(false);
                source.setLoadError(false, this);
                source.setBusy(false, this);

                // Persist the configuration
                updateSource(source);
                System.out.println("Deactivated " + source.getName() + " at " + new Date());
                if (myReloadListenerMap.containsKey(source))
                {
                    myReloadListenerMap.get(source).finishReload(source);
                }
            }
        });
    }

    @Override
    public String getSourceDescription(IDataSource source)
    {
        StringBuilder description = new StringBuilder("<html>").append(myServerCustomization.getServerType()).append(": ")
                .append(source.getName());
        if (source instanceof UrlDataSource)
        {
            UrlDataSource serverSource = (UrlDataSource)source;
            description.append("<br>&nbsp URL: ").append(serverSource.getBaseUrl());
        }
        description.append("</html>");
        return description.toString();
    }

    @Override
    public String getTypeName(IDataSource source)
    {
        return myServerCustomization.getServerType();
    }

    @Override
    public int getOrdinal()
    {
        return -1;
    }

    @Override
    protected void reloadActiveSources()
    {
        getSourceList().stream().filter(source -> source.isActive()).forEach(source ->
        {
            System.out.println("Reloading: " + source.getName() + " at " + new Date());
            myReloadListenerMap.put(source, new ReloadListener()
            {
                @Override
                public void finishReload(IDataSource source)
                {
                    activateSource(source);
                    myReloadListenerMap.remove(source);
                }
            });
            deactivateSource(source);
        });
    }

    /**
     * Reads the config from the preferences.
     *
     * @param preferences the preferences
     * @return the config
     */
    protected IDataSourceConfig readConfig(Preferences preferences)
    {
        return preferences.getJAXBObject(UrlSourceConfig.class, getPrefsKey(), new UrlSourceConfig());
    }

    /**
     * Adds the server types to this controller.
     */
    protected void addServerTypes()
    {
        addServerType(myServerCustomization, new SingleCallable<ServerSourceEditor>()
        {
            @Override
            protected ServerSourceEditor callOnce()
            {
                ServiceValidator<UrlDataSource> validator = getValidator(getToolbox().getServerProviderRegistry());
                return new UrlSourceEditor(new UrlSourceModel("e.g. " + getExampleUrl()), validator);
            }
        });
    }

    /**
     * Gets the server customization.
     *
     * @return the server customization
     */
    protected abstract ServerCustomization getServerCustomization();

    /**
     * Gets the validator.
     *
     * @param registry the server provider registry
     * @return the validator
     */
    protected abstract ServiceValidator<UrlDataSource> getValidator(ServerProviderRegistry registry);

    /**
     * Gets the example URL.
     *
     * @return the example URL
     */
    protected abstract String getExampleUrl();

    /**
     * Handles activating the source.
     *
     * @param source the source
     * @return whether activation was successful
     */
    protected abstract boolean handleActivateSource(IDataSource source);

    /**
     * Handles deactivating the source.
     *
     * @param source the source
     */
    protected abstract void handleDeactivateSource(IDataSource source);
}
