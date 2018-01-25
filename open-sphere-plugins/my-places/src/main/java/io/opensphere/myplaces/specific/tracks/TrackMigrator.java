package io.opensphere.myplaces.specific.tracks;

import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.myplaces.specific.MyPlacesMigrator;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.impl.DefaultTrackConverter;
import io.opensphere.tracktool.model.persist.v1.JAXBTrackConfig;
import io.opensphere.tracktool.registry.TrackRegistry;
import io.opensphere.tracktool.util.TrackUtils;

/**
 * Migrates the old track data to the my places data.
 *
 */
public class TrackMigrator implements MyPlacesMigrator
{
    /**
     * The migrated property.
     */
    private static final String MIGRATED_PROP = "tracksMigrated";

    @Override
    public void individualMigrationDone(Preferences preferences)
    {
        preferences.putBoolean(MIGRATED_PROP, true, this);
    }

    @Override
    public void migrate(Folder myPlacesFolder, Toolbox toolbox)
    {
        Preferences prefs = toolbox.getPreferencesRegistry().getPreferences("io.opensphere.tracktool.TrackToolPlugin");
        JAXBTrackConfig config = prefs.getJAXBObject(JAXBTrackConfig.class, TrackRegistry.TRACK_CONFIG_PREFERENCES_KEY, null);
        if (config != null)
        {
            List<Track> tracks = config.getTrackList(new DefaultTrackConverter());
            if (tracks != null)
            {
                for (Track track : tracks)
                {
                    toKml(myPlacesFolder, track);
                }
            }
        }
    }

    @Override
    public boolean needsIndividualMigration(Preferences preferences)
    {
        boolean isMigrated = preferences.getBoolean(MIGRATED_PROP, false);
        return !isMigrated;
    }

    /**
     * Converts the track to a kml that is stored within the myPlacesFolder.
     *
     * @param myPlacesFolder The my places folder.
     * @param track The track.
     */
    private void toKml(Folder myPlacesFolder, Track track)
    {
        TrackUtils.toKml(myPlacesFolder, track);
    }
}
