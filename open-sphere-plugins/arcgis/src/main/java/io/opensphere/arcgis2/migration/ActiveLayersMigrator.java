package io.opensphere.arcgis2.migration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.arcgis.config.v1.ArcGISServerSource;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.ActiveGroupEntry;
import io.opensphere.mantle.data.DataGroupInfoActiveSet;
import io.opensphere.mantle.data.impl.dgset.v1.JAXBActiveGroupEntry;
import io.opensphere.mantle.data.impl.dgset.v1.JAXBDataGroupInfoActiveSet;
import io.opensphere.mantle.data.impl.dgset.v1.JAXBDataGroupInfoActiveSetConfig;
import io.opensphere.mantle.datasources.impl.UrlDataSource;

/**
 * Migrates all the old active ArcGIS XYZ layer id's to their new id format.
 */
public class ActiveLayersMigrator implements MicroMigrator
{
    /**
     * Used to get and save preferences.
     */
    private final PreferencesRegistry myPrefsRegistry;

    /**
     * Constructor.
     *
     * @param prefsRegistry Used to get and save preferences.
     */
    public ActiveLayersMigrator(PreferencesRegistry prefsRegistry)
    {
        myPrefsRegistry = prefsRegistry;
    }

    /**
     * Migrates the order manager registry values to match that of the new id
     * formats.
     *
     * @param oldServerToNewServer The map of old server url's, to the new
     *            server url's.
     */
    @Override
    public void migrate(Map<ArcGISServerSource, UrlDataSource> oldServerToNewServer)
    {
        Preferences prefs = myPrefsRegistry.getPreferences("io.opensphere.mantle.controller.DataGroupController");
        JAXBDataGroupInfoActiveSetConfig config = prefs.getJAXBObject(JAXBDataGroupInfoActiveSetConfig.class,
                "DataGroupActiveSetConfig", (JAXBDataGroupInfoActiveSetConfig)null);

        if (config != null)
        {
            boolean hasChanged = false;
            Set<String> existingIds = New.set();
            List<Pair<JAXBActiveGroupEntry, ArcGISServerSource>> toMigrate = New.list();
            Map<String, JAXBDataGroupInfoActiveSet> entryToSet = New.map();
            for (DataGroupInfoActiveSet activeSet : config.getSets())
            {
                for (ActiveGroupEntry participant : activeSet.getGroupEntries())
                {
                    for (ArcGISServerSource oldServer : oldServerToNewServer.keySet())
                    {
                        if (participant.getId().startsWith(oldServer.getURL(null)))
                        {
                            toMigrate.add(new Pair<>((JAXBActiveGroupEntry)participant, oldServer));
                            entryToSet.put(participant.getId(), (JAXBDataGroupInfoActiveSet)activeSet);
                        }
                    }

                    existingIds.add(participant.getId());
                }
            }

            for (Pair<JAXBActiveGroupEntry, ArcGISServerSource> migrate : toMigrate)
            {
                ArcGISServerSource oldServer = migrate.getSecondObject();
                UrlDataSource newServer = oldServerToNewServer.get(oldServer);
                String newId = migrate.getFirstObject().getId().replace(oldServer.getURL(null), newServer.getURL());
                newId = newId.replace("/MapServer", "");

                JAXBActiveGroupEntry oldActiveEntry = migrate.getFirstObject();
                JAXBDataGroupInfoActiveSet activeSet = entryToSet.get(oldActiveEntry.getId());

                if (activeSet != null && !existingIds.contains(newId))
                {
                    String oldName = oldActiveEntry.getName();

                    String[] splitId = newId.split("/");
                    String newName = splitId[splitId.length - 1];
                    StringBuffer buffer = new StringBuffer(newName);
                    buffer.append(" (ArcGIS)");
                    newName = buffer.toString();
                    oldActiveEntry.setId(newId);
                    oldActiveEntry.setName(newName);

                    newName = oldName;
                    newName = newName.replace("(ArcGIS XYZ)", "(ArcGIS)");
                    JAXBActiveGroupEntry newEntry = new JAXBActiveGroupEntry(newName, newId + "/0");
                    activeSet.addActiveGroupEntry(newEntry);

                    hasChanged = true;
                }
            }

            if (hasChanged)
            {
                prefs.putJAXBObject("DataGroupActiveSetConfig", config, false, this);
            }
        }
    }
}
