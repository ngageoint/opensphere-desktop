package io.opensphere.server.control;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.control.AbstractServerSourceController.ReloadListener;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerListManager;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.util.ServerConstants;

/**
 * The Class OGCServerHandler.
 */
public class OGCServerHandler
{
    /** Static logging reference. */
    private static final Logger LOGGER = Logger.getLogger(OGCServerHandler.class);

    /** Map of managers that handle activating/deactivating a given source. */
    private final Map<OGCServerSource, OGCServerActivationManager> myActivationMgrMap = new IdentityHashMap<>();

    /** Manage the label that appears when servers are activating. */
    private final ActivationTaskActivity myActivationTaskActivity = new ActivationTaskActivity();

    /** Executor for dispatching state events. */
    private final Executor myEventExecutor;

    /** Manager used to retrieve/update the list of active servers. */
    private final ServerListManager myListManager;

    /** The load listeners. */
    private final WeakChangeSupport<ServerHandlerLoadListener> myLoadListeners = new WeakChangeSupport<>();

    private Map<IDataSource, ReloadListener> myReloadListenerMap = new HashMap<IDataSource, ReloadListener>();

    /** The Core Event Manager. */
    private final Toolbox myToolbox;

    /** The map of sources to activation threads. */
    private final Map<IDataSource, Thread> myActivationThreads = Collections.synchronizedMap(new HashMap<IDataSource, Thread>());

    /**
     * Instantiates a new <code>OGCServerSource</code> handler.
     *
     * @param toolbox The toolbox used to access utilities in Core
     */
    public OGCServerHandler(Toolbox toolbox)
    {
        myToolbox = toolbox;
        ServerToolbox serverToolbox = ServerToolboxUtils.getServerToolbox(myToolbox);
        myEventExecutor = serverToolbox.getPluginExecutor();
        myListManager = serverToolbox.getServerLayerListManager();
        myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(myActivationTaskActivity);
    }

    /**
     * Activate a data source.
     *
     * @param source the source to activate
     */
    public void activateSource(final IDataSource source)
    {
        if (!(source instanceof OGCServerSource))
        {
            throw new IllegalArgumentException(StringUtilities.concat(getClass().getSimpleName(),
                    " can only add sources of type ", OGCServerSource.class.getSimpleName()));
        }
        final OGCServerSource src = (OGCServerSource)source;
        src.setBusy(true, this);

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                myActivationThreads.put(src, Thread.currentThread());
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("OGCServerHandler::addDataSource::run: START : " + src.getName());
                }
                // Fire start event now. End event will be fired when plugins
                // respond and a composite status is built.
                notifyLoadStarted(src);

                // Get max time to wait for response (default = 120 seconds)
                int timeoutMilliseconds = ServerConstants
                        .getDefaultServerActivateTimeoutFromPrefs(myToolbox.getPreferencesRegistry());
                if (src.getActivateTimeoutMillis() > 0)
                {
                    timeoutMilliseconds = src.getActivateTimeoutMillis();
                }

                final PausingTimeBudget timeBudget = PausingTimeBudget.startMillisecondsPaused(timeoutMilliseconds);
                ServerConnectionParams payload = new ServerConnectionParamsImpl(src,
                        myToolbox.getUIRegistry().getMainFrameProvider(), myToolbox, timeBudget);

                // Create a LayerBuilder that will activate the WMS, WFS,
                // and WPS plugins, then build the composite layer tree.
                // Expect activationComplete(...) to get called when
                // activation completes or timeout expires.
                OGCServerActivationManager activationMgr = new OGCServerActivationManager(src, payload, myToolbox);
                myActivationMgrMap.put(src, activationMgr);
                activationMgr.activate(new OGCServerActivationManager.Callback()
                {
                    @Override
                    public void activationComplete(OGCServerSource source, DataGroupInfo groupInfo, boolean success,
                            String errorMessage)
                    {
                        completeActivation(source, groupInfo, success, errorMessage);
                    }
                });
                myActivationTaskActivity.addServer(src);
//                catch (ServerException e)
//                {
//                    StringBuilder evtsb = new StringBuilder("<html>Error authenticating Server <b>" + src.getName() + "</b>");
//                    StringBuilder logsb = new StringBuilder("Error authenticating Server [" + src.getName() + "]");
//                    if (StringUtils.isNotEmpty(e.getMessage()))
//                    {
//                        evtsb.append("<br><br>").append(e.getMessage());
//                        logsb.append(": ").append(e.getMessage());
//                    }
//                    evtsb.append("</html>");
//                    LOGGER.warn(logsb.toString());
//                    src.setActive(false);
//                    src.setBusy(false, this);
//                    src.setLoadError(true, OGCServerHandler.this);
//                    notifyLoadEnded(src, false, evtsb.toString());
//                }
            }
        };
        executeRunnable(r);
    }

    /**
     * Adds a load listener.
     *
     * @param listener the listener to add
     */
    public void addLoadListener(ServerHandlerLoadListener listener)
    {
        myLoadListeners.addListener(listener);
    }

    /**
     * De-activate a data source.
     *
     * @param source the source to de-activate
     */
    public void deactivateSource(IDataSource source)
    {
        final OGCServerSource src = (OGCServerSource)source;
        src.setBusy(true, this);

        synchronized (myActivationThreads)
        {
            if (myActivationThreads.containsKey(src))
            {
                myActivationThreads.get(src).interrupt();
                myActivationThreads.remove(src);
            }
        }

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                notifyLoadStarted(src);

                // Remove ResponseManager for this server, then deactivate
                // server
                OGCServerActivationManager activationMgr = myActivationMgrMap.remove(src);
                if (activationMgr != null)
                {
                    activationMgr.deactivate();
                }

                // Remove layers from Mantle
                if (myListManager != null)
                {
                    myListManager.removeServer(src.getName());
                }

                myActivationTaskActivity.removeServer(src);
                src.setBusy(false, this);
                src.setActive(false);
                notifyLoadEnded(src, true, null);
                if (myReloadListenerMap.containsKey(source))
                {
                    myReloadListenerMap.get(source).finishReload(source);
                }
            }
        };
        executeRunnable(r);
    }

    /**
     * Removes a load listener.
     *
     * @param listener the listener to remove
     */
    public void removeLoadListener(ServerHandlerLoadListener listener)
    {
        myLoadListeners.removeListener(listener);
    }

    /**
     * Notify listeners that a source load has finished.
     *
     * @param source the source that has finished loading
     * @param isSuccess true, if source loaded successfully
     * @param error the error that occurred if source did not load successfully
     */
    protected void notifyLoadEnded(final IDataSource source, final boolean isSuccess, final String error)
    {
        myLoadListeners.notifyListeners(new Callback<OGCServerHandler.ServerHandlerLoadListener>()
        {
            @Override
            public void notify(ServerHandlerLoadListener listener)
            {
                listener.loadEnded(source, isSuccess, error);
            }
        });
    }

    /**
     * Notify listeners that a source has started loading.
     *
     * @param source the source that is loading
     */
    protected void notifyLoadStarted(final IDataSource source)
    {
        myLoadListeners.notifyListeners(new Callback<OGCServerHandler.ServerHandlerLoadListener>()
        {
            @Override
            public void notify(ServerHandlerLoadListener listener)
            {
                listener.loadStarted(source);
            }
        });
    }

    /**
     * Finish up once source activation has completed.
     *
     * @param source the server source that was activated
     * @param groupInfo the group info for the activated server
     * @param success true, if server activated successfully
     * @param errorMessage an error message
     */
    private void completeActivation(OGCServerSource source, DataGroupInfo groupInfo, boolean success, String errorMessage)
    {
        if (!success)
        {
            UserMessageEvent.error(myToolbox.getEventManager(), errorMessage, false, true);
            source.setActive(false);
            source.setLoadError(true, this);

            // Make sure the individual OGC service plugins are deactivated
            OGCServerActivationManager activationMgr = myActivationMgrMap.get(source);
            if (activationMgr != null)
            {
                activationMgr.deactivate();
            }
        }
        else
        {
            source.setActive(true);
            source.setLoadError(false, this);
            ServerConnectionParams payload = new ServerConnectionParamsImpl(source,
                    myToolbox.getUIRegistry().getMainFrameProvider(), myToolbox, null);
            myListManager.addServer(payload, groupInfo);
        }
        myActivationTaskActivity.removeServer(source);
        myActivationThreads.remove(source);
        source.setBusy(false, this);
        notifyLoadEnded(source, success, errorMessage);
    }

    protected void reloadSource(IDataSource source)
    {
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
    }

    /**
     * Execute a runnable. If this class has an executor set, use it, else just run it on the current thread. This is basically
     * equivalent to a null check on this class's executor.
     *
     * @param task the task to run
     */
    private void executeRunnable(Runnable task)
    {
        if (myEventExecutor == null)
        {
            task.run();
        }
        else
        {
            myEventExecutor.execute(task);
        }
    }

    /**
     * Listener interface for clients that need to know when sources have started and finished their load sequences.
     */
    public interface ServerHandlerLoadListener
    {
        /**
         * Notify listener that a source load has ended.
         *
         * @param source the source that has finished loading
         * @param success true, if the load was successful
         * @param error the error that occurred if load was not successful
         */
        void loadEnded(IDataSource source, boolean success, String error);

        /**
         * Notify listener that a source load has started.
         *
         * @param source the source that has started loading
         */
        void loadStarted(IDataSource source);
    }

    /**
     * {@link TaskActivity} class that updates the spinner indicating servers are activating.
     */
    private static class ActivationTaskActivity extends TaskActivity
    {
        /** The set of servers that are still activating. */
        private final List<OGCServerSource> myActivatingSources = New.list();

        /**
         * Constructor.
         */
        public ActivationTaskActivity()
        {
            super();
            setActive(false);
            setLabelValue("Loading Servers...");
        }

        /**
         * Adds a server that is activating.
         *
         * @param source the source for the server that has been started.
         */
        public void addServer(OGCServerSource source)
        {
            if (myActivatingSources.add(source))
            {
                update();
            }
        }

        /**
         * Removes a server that has finished activating for any reason.
         *
         * @param source the source for the server whose activation completed.
         */
        public void removeServer(OGCServerSource source)
        {
            if (myActivatingSources.remove(source))
            {
                update();
            }
        }

        /**
         * Update the TaskActivity with the latest status.
         */
        private void update()
        {
            EventQueueUtilities.runOnEDT(() -> setActive(!myActivatingSources.isEmpty()));
        }
    }
}
