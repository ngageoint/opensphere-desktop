package io.opensphere.myplaces.specific.factory;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.myplaces.specific.MyPlacesMigrator;
import io.opensphere.myplaces.specific.points.PointMigrator;
import io.opensphere.myplaces.specific.regions.RegionMigrator;
import io.opensphere.myplaces.specific.tracks.TrackMigrator;

/**
 * Creates all the necessary migrators.
 *
 */
public final class MigratorFactory
{
    /**
     * The instance of this class.
     */
    private static MigratorFactory ourInstance = new MigratorFactory();

    /**
     * Gets the instance of this class.
     *
     * @return The instance.
     */
    public static MigratorFactory getInstance()
    {
        return ourInstance;
    }

    /**
     * Private constructor helps make it a singleton.
     */
    private MigratorFactory()
    {
    }

    /**
     * Gets the available migrators to migrate data into my places data.
     *
     * @return The list of migrators.
     */
    public List<MyPlacesMigrator> getMigrators()
    {
        List<MyPlacesMigrator> migrators = New.list();

        migrators.add(new PointMigrator());
        migrators.add(new RegionMigrator());
        migrators.add(new TrackMigrator());

        return migrators;
    }
}
