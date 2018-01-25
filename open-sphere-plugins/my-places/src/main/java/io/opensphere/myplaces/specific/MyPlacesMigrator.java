package io.opensphere.myplaces.specific;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;

/**
 * Interface to a my places migrator.
 *
 */
public interface MyPlacesMigrator
{
    /**
     * Notifies the migrator that the individual migration is complete.
     *
     * @param preferences The preferences.
     */
    void individualMigrationDone(Preferences preferences);

    /**
     * Migrates its specific data and adds the data to the passed in document.
     *
     * @param myPlacesFolder The root folder to add data to.
     * @param toolbox The toolbox.
     */
    void migrate(Folder myPlacesFolder, Toolbox toolbox);

    /**
     * Indicates if this migrator needs to be migrated without other types being
     * migrated.
     *
     * @param preferences The migration preferences.
     * @return True if it needs migration, false otherwise.
     */
    boolean needsIndividualMigration(Preferences preferences);
}
