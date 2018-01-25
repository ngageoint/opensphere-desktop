package io.opensphere.server.migration;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.impl.DataGroupActivator;
import io.opensphere.mantle.data.impl.DefaultDataGroupActivator;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.filter.config.v1.ServerFilter;
import io.opensphere.server.filter.config.v1.ServerFilter.FilterType;
import io.opensphere.server.filter.config.v1.ServerSourceFilterConfig;

/**
 * Migrator from version 4 to version 5 of OpenSphere.
 *
 * Activates layers that are in the legacy server filters, then marks the
 * filters as migrated in the config file.
 */
public class Migrator4to5
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(Migrator4to5.class);

    /** Key used to store the filter config in the preferences. */
    private static final String PREFERENCES_KEY = "serverSourceFilter";

    /** The data group activator. */
    private final DataGroupActivator myDataGroupActivator;

    /** Reference to the Mantle DataTypeInfoPreferenceAssistant. */
    private final DataTypeInfoPreferenceAssistant myDataTypeInfoPreferenceAssistant;

    /** The preferences. */
    private final Preferences myPreferences;

    /** The server source config. */
    private final ServerSourceFilterConfig myServerSourceFilterConfig;

    /**
     * Recursively adds all child groups including the given group into the
     * collection.
     *
     * @param group the group
     * @param allGroups the collection to build up
     */
    private static void getAll(DataGroupInfo group, Collection<DataGroupInfo> allGroups)
    {
        allGroups.add(group);
        for (DataGroupInfo child : group.getChildren())
        {
            getAll(child, allGroups);
        }
    }

    /**
     * Returns a collection of all child groups including the given group.
     *
     * @param group the group
     * @return the collection of all groups
     */
    private static Collection<DataGroupInfo> getAllGroups(DataGroupInfo group)
    {
        Collection<DataGroupInfo> allGroups = New.list();
        getAll(group, allGroups);
        return allGroups;
    }

    /**
     * Determines if this group's members load to timeline.
     *
     * @param group the data group
     * @return whether this group's members load to timeline
     */
    private static boolean isTimeline(DataGroupInfo group)
    {
        boolean isTimeline = false;
        for (DataTypeInfo dataType : group.getMembers(false))
        {
            if (dataType.getBasicVisualizationInfo().getLoadsTo() == LoadsTo.TIMELINE)
            {
                isTimeline = true;
                break;
            }
        }
        return isTimeline;
    }

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public Migrator4to5(Toolbox toolbox)
    {
        myPreferences = toolbox.getPreferencesRegistry().getPreferences("io.opensphere.server.OGCServerPlugin");
        myServerSourceFilterConfig = myPreferences.getJAXBObject(ServerSourceFilterConfig.class, PREFERENCES_KEY,
                new ServerSourceFilterConfig());
        myDataTypeInfoPreferenceAssistant = MantleToolboxUtils.getMantleToolbox(toolbox).getDataTypeInfoPreferenceAssistant();
        myDataGroupActivator = new DefaultDataGroupActivator(toolbox.getEventManager());
    }

    /**
     * Migrates a data group.
     *
     * @param group the data group
     */
    public void migrate(DataGroupInfo group)
    {
        // Get the active server filter layer ids
        final Collection<String> activeLayerIds = getActiveLayerIds(group);

        if (!activeLayerIds.isEmpty())
        {
            // Get the list of active, non-timeline groups from the filters
            Collection<DataGroupInfo> activeGroups = StreamUtilities.filter(getAllGroups(group),
                g -> activeLayerIds.contains(g.getId()) && !isTimeline(g));

            // Set the data type visibilities before activating
            for (Iterator<DataGroupInfo> iter = activeGroups.iterator(); iter.hasNext();)
            {
                DataGroupInfo activeGroup = iter.next();
                boolean anyVisible = false;
                for (DataTypeInfo dataType : activeGroup.getMembers(false))
                {
                    boolean isVisible = myDataTypeInfoPreferenceAssistant.isVisiblePreference(dataType.getTypeKey(), false);
                    if (isVisible)
                    {
                        anyVisible = true;
                        break;
                    }
                }
                if (!anyVisible)
                {
                    iter.remove();
                }
            }

            // Activate the groups
            try
            {
                myDataGroupActivator.setGroupsActive(activeGroups, true);
            }
            catch (InterruptedException e)
            {
                LOGGER.error(e, e);
            }

            // Mark server filters as migrated
            updateConfig(group);
        }
    }

    /**
     * Gets the active server filter layer ids for the given group.
     *
     * @param group the data group
     * @return the active layer ids
     */
    private Collection<String> getActiveLayerIds(DataGroupInfo group)
    {
        Collection<String> activeLayerIds;
        if (myServerSourceFilterConfig.isEmpty())
        {
            activeLayerIds = Collections.emptySet();
        }
        else
        {
            activeLayerIds = New.set();
            for (FilterType filterType : FilterType.values())
            {
                ServerFilter serverFilter = myServerSourceFilterConfig.getActiveFilter(group.getId(), filterType);
                if (serverFilter != null && !serverFilter.isMigrated())
                {
                    activeLayerIds.addAll(serverFilter.getLayerPathsAsStrings());
                }
            }
        }
        return activeLayerIds;
    }

    /**
     * Marks server filters as migrated for the given group.
     *
     * @param group the data group
     */
    private void updateConfig(DataGroupInfo group)
    {
        for (ServerFilter serverFilter : myServerSourceFilterConfig.getServerFilters(group.getId()))
        {
            serverFilter.setMigrated(true);
        }
        myPreferences.putJAXBObject(PREFERENCES_KEY, myServerSourceFilterConfig, false, this);
    }
}
