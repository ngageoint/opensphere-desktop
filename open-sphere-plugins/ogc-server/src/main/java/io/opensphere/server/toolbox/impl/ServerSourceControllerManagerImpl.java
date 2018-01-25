package io.opensphere.server.toolbox.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.concurrent.GuardedBy;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.ApplicationLifecycleEvent.Stage;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;

/**
 * Implementation of {@link ServerSourceControllerManager} interface. Builds a
 * mapping of available {@link ServerSourceController}s from any that are
 * declared for the ServiceLoader. Also provides methods to add/access/remove
 * controllers.
 */
public class ServerSourceControllerManagerImpl implements ServerSourceControllerManager
{
    /** The change support. */
    private final ChangeSupport<LoadListener> myChangeSupport = new WeakChangeSupport<>();

    /** The controllers list. */
    @GuardedBy("myListLock")
    private final List<ServerSourceController> myControllerList;

    /** The controllers map. */
    @GuardedBy("myListLock")
    private final Map<String, ServerSourceController> myControllerMap;

    /** Lifecycle event listener that triggers the Controller to initialize. */
    private final EventListener<ApplicationLifecycleEvent> myLifecycleListener;

    /** Lock that prevents concurrent modification of controller list. */
    private final ReadWriteLock myListLock = new ReentrantReadWriteLock();

    /** The plugin executor. */
    private final Executor myPluginExecutor;

    /**
     * The map of controller class to preferences topic, for overriding the
     * default.
     */
    private final Map<Class<? extends ServerSourceController>, Class<?>> myPreferencesTopicMap = New.concurrentMap();

    /** The default preferences topic that gets sent to each controller. */
    private final Class<?> myPrefsTopic;

    /** The core toolbox. */
    private final Toolbox myToolbox;

    /**
     * Compares two controllers.
     *
     * @param controller1 the first controller
     * @param controller2 the second controller
     * @return the comparison result
     */
    private static int compare(ServerSourceController controller1, ServerSourceController controller2)
    {
        int result;
        if (controller1.getOrdinal() != -1 && controller2.getOrdinal() != -1)
        {
            result = Integer.compare(controller1.getOrdinal(), controller2.getOrdinal());
        }
        else if (controller1.getOrdinal() == -1 && controller2.getOrdinal() == -1)
        {
            result = controller1.getTypeName(null).compareTo(controller2.getTypeName(null));
        }
        else
        {
            result = Integer.compare(controller2.getOrdinal(), controller1.getOrdinal());
        }
        return result;
    }

    /**
     * Implementation of the {@link ServerSourceControllerManager}.
     *
     * @param toolbox the Core toolbox
     * @param preferencesTopic the string used to retrieve the top-level server
     *            preferences
     * @param pluginExecutor the plugin executor
     */
    public ServerSourceControllerManagerImpl(Toolbox toolbox, Class<?> preferencesTopic, Executor pluginExecutor)
    {
        myToolbox = toolbox;
        myPrefsTopic = preferencesTopic;
        myPluginExecutor = pluginExecutor;
        myControllerMap = New.map();
        myControllerList = New.list();
        myLifecycleListener = this::handleApplicationLifecycleEvent;
        toolbox.getEventManager().subscribe(ApplicationLifecycleEvent.class, myLifecycleListener);
    }

    @Override
    public void addLoadListener(LoadListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public Collection<ServerSourceController> getControllers()
    {
        myListLock.readLock().lock();
        try
        {
            return Collections.unmodifiableCollection(myControllerList);
        }
        finally
        {
            myListLock.readLock().unlock();
        }
    }

    @Override
    public ServerSourceController getServerSourceController(String typeName)
    {
        ServerSourceController controller = null;
        myListLock.readLock().lock();
        try
        {
            for (Entry<String, ServerSourceController> entry : myControllerMap.entrySet())
            {
                if (entry.getKey().equalsIgnoreCase(typeName))
                {
                    controller = entry.getValue();
                    break;
                }
            }
        }
        finally
        {
            myListLock.readLock().unlock();
        }
        return controller;
    }

    @Override
    public void removeLoadListener(LoadListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void setPreferencesTopic(Class<? extends ServerSourceController> controllerClass, Class<?> preferencesTopic)
    {
        myPreferencesTopicMap.put(controllerClass, preferencesTopic);
    }

    /**
     * Opens only the server controllers that aren't overridable by other
     * controllers.
     *
     * @param potentials The controllers currently installed in the system.
     */
    protected void openControllers(List<ServerSourceController> potentials)
    {
        List<ServerSourceController> controllers = New.list();

        for (ServerSourceController controller : potentials)
        {
            boolean add = true;
            for (ServerSourceController override : potentials)
            {
                if (override.overridesController(controller))
                {
                    add = false;
                    break;
                }
            }

            if (add)
            {
                controllers.add(controller);
            }
        }

        // Open the controllers
        for (ServerSourceController controller : controllers)
        {
            Class<?> prefsTopic = myPreferencesTopicMap.get(controller.getClass());
            if (prefsTopic == null)
            {
                prefsTopic = myPrefsTopic;
            }
            controller.open(myToolbox, prefsTopic);
        }

        // Keep track of the controllers
        myListLock.writeLock().lock();
        try
        {
            for (ServerSourceController controller : controllers)
            {
                for (String typeName : controller.getTypeNames())
                {
                    myControllerMap.put(typeName, controller);
                }
                myControllerList.add(controller);
            }

            Collections.sort(myControllerList, ServerSourceControllerManagerImpl::compare);
        }
        finally
        {
            myListLock.writeLock().unlock();
        }

        myChangeSupport.notifyListeners(listener -> listener.loadComplete());
    }

    /**
     * Handles lifecycle events so that the controller can be initialized after
     * the plugins are all initialized.
     *
     * @param event the event
     */
    private void handleApplicationLifecycleEvent(ApplicationLifecycleEvent event)
    {
        if (event.getStage() == Stage.PLUGINS_INITIALIZED)
        {
            myPluginExecutor.execute(this::initializeControllers);
        }
    }

    /**
     * Initializes the controllers.
     */
    private void initializeControllers()
    {
        List<ServerSourceController> potentials = New.list();
        for (ServerSourceController controller : ServiceLoader.load(ServerSourceController.class))
        {
            potentials.add(controller);
        }

        openControllers(potentials);
    }
}
