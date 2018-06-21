package io.opensphere.core.appl;

import java.awt.SplashScreen;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.opensphere.core.Plugin;
import io.opensphere.core.PluginConfigLoader;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.orwell.ApplicationStatistics;
import io.opensphere.core.orwell.PluginStatistics;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.predicate.InPredicate;
import net.jcip.annotations.GuardedBy;

/**
 * Initializer for plug-ins.
 */
@SuppressWarnings("PMD.GodClass")
class PluginInit
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PluginInit.class);

    /** The Constant UNABLE_TO_INITIALIZE_PLUGIN. */
    private static final String UNABLE_TO_INITIALIZE_PLUGIN = "Unable to initialize plugin [";

    /** The collection of plug-ins. */
    @GuardedBy("myPluginInstances")
    private final Map<Class<?>, List<MetaAndInstance>> myPluginInstances = new LinkedHashMap<>();

    /** The toolbox, containing various application control interfaces. */
    private final ToolboxImpl myToolbox;

    /**
     * Checks a plugin id against the required tree to see if it or any other of
     * its requirements or their requirements are circular (Recursive).
     *
     * @param searchId the to level plugin id to search for
     * @param curId the current plugin id to recurse
     * @param pluginIdToPluginDepdencencySetMap map of plugin id to its set of
     *            required plugins
     * @param searchPath a list in which the path of the search is documented
     * @return true, if is circular
     */
    private static boolean isCircular(String searchId, String curId, Map<String, Set<String>> pluginIdToPluginDepdencencySetMap,
            List<String> searchPath)
    {
        Utilities.checkNull(searchPath, "searchPath");
        boolean circular = false;
        searchPath.add(curId);

        final Set<String> curDepSet = pluginIdToPluginDepdencencySetMap.get(curId);
        if (curDepSet != null && !curDepSet.isEmpty())
        {
            if (curDepSet.contains(searchId))
            {
                circular = true;
                searchPath.add(searchId);
            }
            else
            {
                for (final String depId : curDepSet)
                {
                    if (searchPath.contains(depId))
                    {
                        circular = true;
                        searchPath.add(depId);
                    }
                    else
                    {
                        circular = isCircular(searchId, depId, pluginIdToPluginDepdencencySetMap, searchPath);
                    }
                    if (circular)
                    {
                        break;
                    }
                }
            }
        }

        if (!circular)
        {
            searchPath.remove(curId);
        }
        return circular;
    }

    /**
     * Construct the initializer.
     *
     * @param toolbox The toolbox.
     */
    public PluginInit(ToolboxImpl toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Initializes the plug-ins and starts the registered envoys.
     *
     * @return The plug-ins.
     */
    public Collection<? extends Plugin> initializePlugins()
    {
        registerAndInitClasses();
        final ApplicationStatistics applicationStatistics = myToolbox.getStatisticsManager().getApplicationStatistics();

        final Collection<List<MetaAndInstance>> values;
        synchronized (myPluginInstances)
        {
            values = myPluginInstances.values();
        }
        final Collection<Plugin> result = new ArrayList<>(values.size());
        for (final List<MetaAndInstance> list : values)
        {
            for (final MetaAndInstance metaAndInstance : list)
            {
                applicationStatistics.getAvailablePlugins().add(gatherPluginStatistics(metaAndInstance.getMetadata()));
                result.add(metaAndInstance.getPluginInstance());
            }
        }
        return result;
    }

    /**
     * Extracts plugin statistics from the supplied metadata, generates a
     * {@link PluginStatistics} object populated with the supplied data, and
     * returns it.
     *
     * @param pMetadata the plugin metadata object from which statistics are
     *            gathered.
     * @return a statistics object populated with relevant data.
     */
    protected PluginStatistics gatherPluginStatistics(PluginLoaderData pMetadata)
    {
        final PluginStatistics pluginStatistics = new PluginStatistics();
        pluginStatistics.setName(pMetadata.getId());
        pluginStatistics.setVersion(pMetadata.getPluginVersion());
        pluginStatistics.setAuthor(pMetadata.getAuthor());
        pluginStatistics.setPluginClass(pMetadata.getClazz());
        pluginStatistics.setDescription(pMetadata.getDescription());
        pluginStatistics.setLanguage(pMetadata.getLanguage());
        pluginStatistics.setSummary(pMetadata.getSummary());
        pluginStatistics.setRequiredPluginDependencies(pMetadata.getRequiredPluginDependency());
        pluginStatistics.setOptionalPluginDependencies(pMetadata.getOptionalPluginDependency());
        pluginStatistics.setPluginProperties(pMetadata.getPluginProperty());
        return pluginStatistics;
    }

    /**
     * Builds the circular path string.
     *
     * @param path the path
     * @return the string
     */
    private String buildCircularPathString(List<String> path)
    {
        final StringBuilder sb = new StringBuilder();
        for (final String val : path)
        {
            sb.append(val);
            sb.append("->");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Check the dependencies for the plug-ins in the workingList. If all the
     * dependencies are in the accepted plug-in set, add the plug-in being
     * checked to the accepted plug-in set as well as the ordered list. If any
     * dependencies have not been checked yet, leave the plug-in alone.
     *
     * @param workingList The list of plug-ins to check.
     * @param acceptedPluginSet The set of already-accepted plug-ins.
     * @param orderedList The list of plug-ins ordered by interdependency.
     * @param pluginIdToPluginDependencySetMap The map of plug-in ids to plug-in
     *            ids depended upon.
     */
    private void checkDependencies(List<PluginLoaderData> workingList, Set<String> acceptedPluginSet,
            List<PluginLoaderData> orderedList, Map<String, Set<String>> pluginIdToPluginDependencySetMap)
    {
        final Iterator<PluginLoaderData> plDatItr = workingList.iterator();
        while (plDatItr.hasNext())
        {
            final PluginLoaderData plDat = plDatItr.next();
            final Set<String> reqDep = pluginIdToPluginDependencySetMap.get(plDat.getId());
            if (acceptedPluginSet.containsAll(reqDep))
            {
                acceptedPluginSet.add(plDat.getId());
                orderedList.add(plDat);
                plDatItr.remove();
            }
            else
            {
                // Check for self-requirement
                if (reqDep.contains(plDat.getId()))
                {
                    LOGGER.error(UNABLE_TO_INITIALIZE_PLUGIN + plDat.getId() + "]: Because it lists itself as a dependency.");
                    plDatItr.remove();
                    pluginIdToPluginDependencySetMap.remove(plDat.getId());
                }
                else
                {
                    // Check for missing plugins.
                    boolean foundAllPluginsInList = true;
                    String missingPlugin = "";
                    for (final String depId : reqDep)
                    {
                        if (!pluginIdToPluginDependencySetMap.containsKey(depId))
                        {
                            foundAllPluginsInList = false;
                            missingPlugin = depId;
                            break;
                        }
                    }
                    if (!foundAllPluginsInList)
                    {
                        LOGGER.error(UNABLE_TO_INITIALIZE_PLUGIN + plDat.getId() + "]: Because it requires plugin ["
                                + missingPlugin + "] which is not available.");
                        plDatItr.remove();
                        pluginIdToPluginDependencySetMap.remove(plDat.getId());
                    }
                }
            }
        }
    }

    /**
     * Check the dependencies of a plugin to see if any of them failed to
     * initialize.
     *
     * @param dependencyToPluginsMap The map of plugins to the plugins that
     *            depend on them.
     * @param failedPluginIds The ids of the plugins that have failed.
     * @param workQueue The work queue.
     * @param data The data for the current plugin.
     * @return {@code true} if any of this plugin's dependencies are int he
     *         failed set.
     */
    private boolean checkFailedPlugins(final Map<String, Collection<PluginLoaderData>> dependencyToPluginsMap,
            final Set<String> failedPluginIds, final BlockingQueue<PluginLoaderData> workQueue, PluginLoaderData data)
    {
        boolean processed;
        if (data.getRequiredPluginDependency().stream().anyMatch(new InPredicate(failedPluginIds)))
        {
            processed = true;
            failedPluginIds.add(data.getId());
            final Collection<? extends PluginLoaderData> dependents = dependencyToPluginsMap.get(data.getId());
            if (dependents != null)
            {
                workQueue.addAll(dependents);
            }
        }
        else
        {
            processed = false;
        }
        return processed;
    }

    /**
     * Create a map with keys that are the ids of plugins that are depended upon
     * and values that are collections of the dependent plugins.
     *
     * @param classesToLoad The collection of data for the plugins to be loaded.
     * @return The map.
     */
    private Map<String, Collection<PluginLoaderData>> createDependencyToPluginsMap(List<PluginLoaderData> classesToLoad)
    {
        final Map<String, Collection<PluginLoaderData>> map = LazyMap.create(New.<String, Collection<PluginLoaderData>>map(),
                String.class, New.<PluginLoaderData>listFactory());
        for (final PluginLoaderData data : classesToLoad)
        {
            if (!data.getRequiredPluginDependency().isEmpty())
            {
                for (final String dependency : data.getRequiredPluginDependency())
                {
                    map.get(dependency).add(data);
                }
            }
        }
        return New.map(map);
    }

    /**
     * Create the plugin instance.
     *
     * @param plugindata The plugin data.
     * @return The instance, or {@code null} if it could not be created.
     */
    private Plugin createPluginInstance(PluginLoaderData plugindata)
    {
        Class<?> c = null;
        try
        {
            c = Class.forName(plugindata.getClazz());
        }
        catch (final ClassNotFoundException e)
        {
            LOGGER.error("Unable to find class " + plugindata.getClazz() + ".  Skipping, This plugin will be unavailable.", e);
            return null;
        }

        Plugin pluginInstance;
        try
        {
            final long t0 = System.nanoTime();
            Constructor<?> pluginConstructor = c.getConstructor();
            pluginInstance = (Plugin)pluginConstructor.newInstance();
            final long t1 = System.nanoTime();
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                        StringUtilities.formatTimingMessage("Time to instantiate plugin " + plugindata.getId() + ": ", t1 - t0));
            }
        }
        catch (ReflectiveOperationException | RuntimeException e)
        {
            LOGGER.error("Unable to instantiate class " + c.getName(), e);
            pluginInstance = null;
        }

        return pluginInstance;
    }

    /**
     * Creates the plugin to dependency set map.
     *
     * @param workingList the working list
     * @return the map
     */
    private Map<String, Set<String>> createPluginToDependencySetMap(List<PluginLoaderData> workingList)
    {
        final Map<String, Set<String>> pluginIdToPluginDependencySetMap = New.map();
        // Build a map between the plugin ID and the set of its dependencies for
        // later reference.
        for (final PluginLoaderData plDat : workingList)
        {
            Set<String> dependSet = null;
            if (!plDat.getRequiredPluginDependency().isEmpty())
            {
                dependSet = New.set(plDat.getRequiredPluginDependency());
            }
            else
            {
                dependSet = Collections.emptySet();
            }
            pluginIdToPluginDependencySetMap.put(plDat.getId(), dependSet);
        }
        return pluginIdToPluginDependencySetMap;
    }

    /**
     * Initialize a plugin using an executor service. If initialization if
     * successful, add the plugin id to the {@code initializedPluginIds}
     * collection; otherwise add it to the {@code failedPluginIds} collection.
     * Add plugins to the {@code workQueue} that are waiting on this plugin.
     *
     * @param data The plugin data.
     * @param executor The executor.
     * @param dependencyToPluginsMap A map of plugins to the plugins that depend
     *            on them.
     * @param initializedPluginIds The output collection of initialized plugins.
     * @param failedPluginIds The output collection of failed plugins.
     * @param workQueue The work queue.
     * @return A future tied to the initialization.
     */
    private PluginInitFuture forkInit(final PluginLoaderData data, ExecutorService executor,
            final Map<String, Collection<PluginLoaderData>> dependencyToPluginsMap, final Set<String> initializedPluginIds,
            final Set<String> failedPluginIds, final BlockingQueue<PluginLoaderData> workQueue)
    {
        return new PluginInitFuture(data.getId(), executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                if (initPlugin(data))
                {
                    initializedPluginIds.add(data.getId());
                }
                else
                {
                    failedPluginIds.add(data.getId());
                }
                final Collection<? extends PluginLoaderData> dependents = dependencyToPluginsMap.get(data.getId());
                if (dependents != null)
                {
                    workQueue.addAll(dependents);
                }
            }
        }));
    }

    /**
     * Get the time budget for loading plug-ins.
     *
     * @return The budget.
     */
    private TimeBudget getBudget()
    {
        final Long timeout = Long.getLong("opensphere.pluginInit.timeoutMs");
        return timeout == null ? TimeBudget.INDEFINITE : TimeBudget.startMilliseconds(timeout.longValue());
    }

    /**
     * Initialize the plugin.
     *
     * @param plugindata The plugin data.
     * @return {@code true} iff successful.
     */
    private boolean initPlugin(PluginLoaderData plugindata)
    {
        final String initMsg = "Loading Plugin: " + plugindata.getId();
        myToolbox.getSystemToolbox().getSplashScreenManager().setInitMessage(initMsg);
        if (SplashScreen.getSplashScreen() != null)
        {
            LOGGER.info(initMsg);
        }

        final Plugin pluginInstance = createPluginInstance(plugindata);
        if (pluginInstance == null)
        {
            return false;
        }

        final Class<? extends Plugin> c = pluginInstance.getClass();
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                final Package pkg = c.getPackage();
                LOGGER.debug("Initializing plug-in: " + pkg.getImplementationTitle() + " version: "
                        + pkg.getImplementationVersion() + " vendor: " + pkg.getImplementationVendor());
            }
            final long t0 = System.nanoTime();
            pluginInstance.initialize(plugindata, myToolbox);
            final long initTime = System.nanoTime() - t0;
            final String initTimeMessage = StringUtilities
                    .formatTimingMessage("Time to initialize plugin " + plugindata.getId() + ": ", initTime);
            LOGGER.log(initTime > 2_000_000_000 ? Level.INFO : Level.DEBUG, initTimeMessage);
            final Collection<? extends Envoy> envoys = pluginInstance.getEnvoys();
            if (envoys != null)
            {
                final Set<Envoy> envoySet = new HashSet<>(envoys);
                envoySet.remove(null);
                myToolbox.getEnvoyRegistry().addObjectsForSource(pluginInstance, envoySet);
            }
            final Collection<? extends Transformer> transformers = pluginInstance.getTransformers();
            if (transformers != null)
            {
                final Set<Transformer> transformerSet = new HashSet<>(transformers);
                transformerSet.remove(null);
                myToolbox.getTransformerRegistry().addObjectsForSource(pluginInstance, transformerSet);
            }
        }
        // Catch any errors or exceptions that might occur while
        // initializing the plug-in. Attempt to skip plug-ins that fail to
        // initialize, and keep running.
        catch (final Throwable t)
        {
            LOGGER.error(UNABLE_TO_INITIALIZE_PLUGIN + plugindata.getId() + "]: " + t, t);
            myToolbox.removeObjectsForSource(pluginInstance);
            return false;
        }

        MetaAndInstance instance = new MetaAndInstance(plugindata, pluginInstance);
        synchronized (myPluginInstances)
        {
            myPluginInstances.computeIfAbsent(c, k -> new ArrayList<>(1)).add(instance);
        }

        return true;
    }

    /**
     * Orders the list of {@link PluginLoaderData} so that plugins with
     * dependencies are loaded after the plugins on which they are dependent.
     *
     * @param classesToLoad the classes to load
     * @return the list
     */
    private List<PluginLoaderData> orderByDependencies(List<PluginLoaderData> classesToLoad)
    {
        if (classesToLoad.isEmpty())
        {
            return classesToLoad;
        }

        final Set<String> acceptedPluginSet = New.set();
        final List<PluginLoaderData> orderedList = New.list(classesToLoad.size());
        final List<PluginLoaderData> workingList = New.list(classesToLoad);

        final Map<String, Set<String>> pluginIdToPluginDependencySetMap = createPluginToDependencySetMap(workingList);

        // First go through the list and grab any plugins that have no
        // dependencies.
        final Iterator<PluginLoaderData> plDatItr = workingList.iterator();
        PluginLoaderData plDat = null;
        while (plDatItr.hasNext())
        {
            plDat = plDatItr.next();
            if (plDat.getRequiredPluginDependency() == null || plDat.getRequiredPluginDependency().isEmpty())
            {
                acceptedPluginSet.add(plDat.getId());
                orderedList.add(plDat);
                plDatItr.remove();
            }
        }

        // Now do any plugins that have dependencies.
        int lastCycleWorkingCount = workingList.size();
        boolean keepWorking = true;
        while (!workingList.isEmpty() && keepWorking)
        {
            checkDependencies(workingList, acceptedPluginSet, orderedList, pluginIdToPluginDependencySetMap);

            // The list isn't getting any smaller, check for circular
            // dependencies.
            if (workingList.size() == lastCycleWorkingCount)
            {
                for (final PluginLoaderData dat : workingList)
                {
                    final List<String> path = New.list();
                    if (isCircular(dat.getId(), dat.getId(), pluginIdToPluginDependencySetMap, path))
                    {
                        LOGGER.error(UNABLE_TO_INITIALIZE_PLUGIN + dat.getId() + "]: Because it has circular dependencies: "
                                + buildCircularPathString(path));
                    }
                    else
                    {
                        // Unknown reason why they won't work, so add them and
                        // see if they load anyway.
                        orderedList.add(dat);
                    }
                }
                keepWorking = false;
            }
            lastCycleWorkingCount = workingList.size();
        }
        return orderedList;
    }

    /**
     * Reads the plug-in configuration file, and attempts to initialize all the
     * classes named therein. If there is an error, it skips that class and goes
     * on, after printing a message.
     */
    private void registerAndInitClasses()
    {
        final List<PluginLoaderData> classesToLoad = orderByDependencies(new PluginConfigLoader().getPluginConfigurations());
        final Map<String, Collection<PluginLoaderData>> dependencyToPluginsMap = createDependencyToPluginsMap(classesToLoad);

        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                new NamedThreadFactory("PluginInit"));
        final Set<String> forkedPluginIds = New.<String>set();
        final Set<String> initializedPluginIds = Collections.synchronizedSet(New.<String>set());
        final Set<String> failedPluginIds = Collections.synchronizedSet(New.<String>set());
        final BlockingQueue<PluginLoaderData> workQueue = new LinkedBlockingQueue<>(classesToLoad);
        int count = 0;
        final Queue<PluginInitFuture> futures = New.queue();

        final TimeBudget budget = getBudget();
        while (count < classesToLoad.size())
        {
            PluginLoaderData data;
            try
            {
                data = workQueue.poll(budget.getRemainingNanoseconds(), TimeUnit.NANOSECONDS);
            }
            catch (final InterruptedException e)
            {
                LOGGER.error("Interrupted waiting for queue: " + e, e);
                continue;
            }
            if (data == null)
            {
                reportTimedoutPlugins(futures);
                break;
            }
            do
            {
                if (forkedPluginIds.contains(data.getId()) || failedPluginIds.contains(data.getId()))
                {
                    continue;
                }
                if (!data.isEnabled())
                {
                    LOGGER.info("Skipping disabled plugin: " + data.getId());
                    failedPluginIds.add(data.getId());
                    ++count;
                }
                else if (initializedPluginIds.containsAll(data.getRequiredPluginDependency()))
                {
                    futures.add(
                            forkInit(data, executor, dependencyToPluginsMap, initializedPluginIds, failedPluginIds, workQueue));
                    ++count;
                    forkedPluginIds.add(data.getId());
                }
                else if (!failedPluginIds.isEmpty()
                        && checkFailedPlugins(dependencyToPluginsMap, failedPluginIds, workQueue, data))
                {
                    ++count;
                }
            }
            while ((data = workQueue.poll()) != null);
        }
        executor.shutdown();

        try
        {
            if (!executor.awaitTermination(budget.getRemainingNanoseconds(), TimeUnit.NANOSECONDS))
            {
                LOGGER.warn("Timed out waiting for plugins to initialize.");
            }
        }
        catch (final InterruptedException e)
        {
            LOGGER.warn("Interrupted waiting for plugins to initialize: " + e, e);
        }
    }

    /**
     * Check to see if any of the plugins have been initializing too long, and
     * log it if so.
     *
     * @param futures The futures.
     */
    private void reportTimedoutPlugins(Queue<PluginInitFuture> futures)
    {
        for (PluginInitFuture future; (future = futures.poll()) != null;)
        {
            if (future.isTimedOut())
            {
                LOGGER.warn("Timed out waiting for " + future.getPluginId() + " to initialize.");
            }
        }
    }

    /**
     * This class is simply for conveniently mapping plug-in metadata the proper
     * Plug-in instances.
     */
    private static class MetaAndInstance
    {
        /** The plug-in configuration information. */
        private final PluginLoaderData myMetadata;

        /** The plug-in instance. */
        private final Plugin myPluginInstance;

        /**
         * Construct the tuple.
         *
         * @param data the metadata
         * @param instance the plug-in instance
         */
        public MetaAndInstance(PluginLoaderData data, Plugin instance)
        {
            myMetadata = data;
            myPluginInstance = instance;
        }

        /**
         * Accessor for the metadata.
         *
         * @return the metadata
         */
        public PluginLoaderData getMetadata()
        {
            return myMetadata;
        }

        /**
         * Accessor for the pluginInstance.
         *
         * @return the pluginInstance
         */
        public Plugin getPluginInstance()
        {
            return myPluginInstance;
        }
    }

    /**
     * A holder for the future associated with the initialization of a plug-in.
     */
    private static class PluginInitFuture
    {
        /** The future associated with the initialization. */
        private final Future<?> myFuture;

        /** The plugin id. */
        private final String myPluginId;

        /** The system time when the future was created. */
        private final long myStartTimeMillis;

        /**
         * Constructor.
         *
         * @param id The plug-in id.
         * @param future The future associated with the initialization.
         */
        public PluginInitFuture(String id, Future<?> future)
        {
            myPluginId = id;
            myFuture = future;
            myStartTimeMillis = System.currentTimeMillis();
        }

        /**
         * Get the plugin id.
         *
         * @return The plugin id.
         */
        public String getPluginId()
        {
            return myPluginId;
        }

        /**
         * Check if the future has been running too long.
         *
         * @return If the future has been running too long.
         */
        public boolean isTimedOut()
        {
            return !myFuture.isDone() && System.currentTimeMillis() - myStartTimeMillis > 30000;
        }
    }
}
