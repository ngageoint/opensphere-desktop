package io.opensphere.myplaces.migration;

import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.dataaccess.MyPlacesDataAccessor;
import io.opensphere.myplaces.specific.MyPlacesMigrator;
import io.opensphere.myplaces.specific.factory.MigratorFactory;

/**
 * Migrates all data that needs to be migrated into the MyPlaces data.
 */
public class Migrator
{
    /**
     * The migrated property.
     */
    private static final String MIGRATED_PROP = "migrated";

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The data accessor.
     */
    private final MyPlacesDataAccessor myDataAccessor;

    /**
     * Constructs a new migrator.
     *
     * @param toolbox The toolbox.
     * @param dataAccessor The dataaccessor.
     */
    public Migrator(Toolbox toolbox, MyPlacesDataAccessor dataAccessor)
    {
        myToolbox = toolbox;
        myDataAccessor = dataAccessor;
    }

    /**
     * Migrates all data that needs to be migrated into the MyPlaces data.
     */
    public void migrateAllIfNeeded()
    {
        PreferencesRegistry registry = myToolbox.getPreferencesRegistry();
        Preferences preferences = registry.getPreferences(Migrator.class);
        boolean isMigrated = preferences.getBoolean(MIGRATED_PROP, false);

        if (!isMigrated)
        {
            Kml kml = new Kml();
            Document document = kml.createAndSetDocument();
            document.setName(Constants.MY_PLACES_LABEL);
            Folder rootFolder = document.createAndAddFolder();
            rootFolder.setName(Constants.MY_PLACES_LABEL);
            rootFolder.setId(Constants.MY_PLACES_ID);
            rootFolder.setVisibility(Boolean.TRUE);

            List<MyPlacesMigrator> migrators = MigratorFactory.getInstance().getMigrators();
            for (MyPlacesMigrator migrator : migrators)
            {
                migrator.migrate(rootFolder, myToolbox);
            }

            myDataAccessor.saveMyPlacesSynchronized(kml);

            preferences.putBoolean(MIGRATED_PROP, true, this);

            for (MyPlacesMigrator migrator : migrators)
            {
                if (migrator.needsIndividualMigration(preferences))
                {
                    migrator.individualMigrationDone(preferences);
                }
            }
        }

        List<MyPlacesMigrator> migrators = MigratorFactory.getInstance().getMigrators();
        for (MyPlacesMigrator migrator : migrators)
        {
            if (migrator.needsIndividualMigration(preferences))
            {
                Kml kml = myDataAccessor.loadMyPlaces();
                Feature feature = kml.getFeature();
                if (feature instanceof Document)
                {
                    List<Feature> features = ((Document)feature).getFeature();
                    if (!features.isEmpty() && features.get(0) instanceof Folder)
                    {
                        Folder placesFolder = (Folder)features.get(0);
                        migrator.migrate(placesFolder, myToolbox);

                        myDataAccessor.saveMyPlacesSynchronized(kml);

                        migrator.individualMigrationDone(preferences);
                    }
                }
            }
        }
    }
}
