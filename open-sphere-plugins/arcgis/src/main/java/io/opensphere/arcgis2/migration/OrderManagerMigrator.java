package io.opensphere.arcgis2.migration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.arcgis.config.v1.ArcGISServerSource;
import io.opensphere.core.order.impl.config.v1.OrderManagerConfig;
import io.opensphere.core.order.impl.config.v1.OrderManagerParticipant;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.datasources.impl.UrlDataSource;

/**
 * Migrates the old order manager entries to match that of the new id format.
 */
public class OrderManagerMigrator implements MicroMigrator
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
    public OrderManagerMigrator(PreferencesRegistry prefsRegistry)
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
        Preferences prefs = myPrefsRegistry.getPreferences("io.opensphere.core.order.impl.OrderManagerRegistryImpl");
        OrderManagerConfig config = prefs.getJAXBObject(OrderManagerConfig.class, "Image Layer Family::Reference Layers",
                (OrderManagerConfig)null);

        if (config != null)
        {
            boolean hasChanged = false;
            Set<String> existingIds = New.set();
            List<Pair<OrderManagerParticipant, ArcGISServerSource>> toMigrate = New.list();
            if (config.getParticipants() != null)
            {
                for (OrderManagerParticipant participant : config.getParticipants())
                {
                    for (ArcGISServerSource oldServer : oldServerToNewServer.keySet())
                    {
                        if (participant.getId().startsWith(oldServer.getURL(null)))
                        {
                            toMigrate.add(new Pair<>(participant, oldServer));
                        }
                    }

                    existingIds.add(participant.getId());
                }
            }

            for (Pair<OrderManagerParticipant, ArcGISServerSource> migrate : toMigrate)
            {
                ArcGISServerSource oldServer = migrate.getSecondObject();
                UrlDataSource newServer = oldServerToNewServer.get(oldServer);
                String newId = migrate.getFirstObject().getId().replace(oldServer.getURL(null), newServer.getURL());
                StringBuffer buffer = new StringBuffer(newServer.getURL());
                buffer.append(newId);
                newId = buffer.toString();
                if (!existingIds.contains(newId))
                {
                    migrate.getFirstObject().setId(newId);
                    hasChanged = true;
                }
            }

            if (hasChanged)
            {
                prefs.putJAXBObject("Image Layer Family::Reference Layers", config, false, this);
            }
        }
    }
}
