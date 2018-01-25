package io.opensphere.myplaces.specific.points;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.mantle.mp.MapAnnotationPointGroup;
import io.opensphere.mantle.mp.MapAnnotationPointRegistry;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.specific.MyPlacesMigrator;
import io.opensphere.myplaces.specific.points.utils.PointUtils;
import io.opensphere.myplaces.util.OptionsAccessor;

/**
 * Migrates the my points data into my places data.
 *
 */
public class PointMigrator implements MyPlacesMigrator
{
    @Override
    public void individualMigrationDone(Preferences preferences)
    {
    }

    @Override
    public void migrate(Folder rootFolder, Toolbox toolbox)
    {
        MapAnnotationPointRegistry registry = MantleToolboxUtils.getMantleToolbox(toolbox).getMapAnnotationPointRegistry();
        MapAnnotationPoint defaultPoint = registry.getUserDefaultPoint();
        OptionsAccessor accessor = new OptionsAccessor(toolbox);
        Placemark placemark = PointUtils.toKml(null, defaultPoint);
        accessor.saveDefaultPlacemark(placemark);

        Set<MutableMapAnnotationPointGroup> groupSet = registry.getGroupSet();

        List<MapAnnotationPointGroup> groups = new ArrayList<>();
        for (MutableMapAnnotationPointGroup group : groupSet)
        {
            if (groupSet.size() > 1)
            {
                groups.add(group);
            }
            else
            {
                for (MapAnnotationPoint point : group.getPoints(false))
                {
                    migratePoint(rootFolder, point);
                }

                groups.addAll(group.getChildren());
            }
        }

        migrateGroups(rootFolder, groups);
    }

    @Override
    public boolean needsIndividualMigration(Preferences preferences)
    {
        return false;
    }

    /**
     * Migrates the point groups to the my places data.
     *
     * @param folder The folder to add to.
     * @param groups The groups to migrate.
     */
    private void migrateGroups(Folder folder, Collection<MapAnnotationPointGroup> groups)
    {
        for (MapAnnotationPointGroup group : groups)
        {
            Folder groupFolder = folder.createAndAddFolder();
            groupFolder.setName(group.getName());
            groupFolder.setId(UUID.randomUUID().toString());
            groupFolder.setVisibility(true);

            for (MapAnnotationPoint point : group.getPoints(false))
            {
                migratePoint(groupFolder, point);
            }

            migrateGroups(groupFolder, group.getChildren());
        }
    }

    /**
     * Migrates a point to a kml object.
     *
     * @param folder The folder to add to.
     * @param point The point to migrate.
     */
    private void migratePoint(Folder folder, MapAnnotationPoint point)
    {
        Placemark placemark = PointUtils.toKml(folder, point);

        for (Data data : placemark.getExtendedData().getData())
        {
            if (data.getName().equals(Constants.IS_FEATURE_ON_ID))
            {
                data.setValue(String.valueOf(true));
                break;
            }
        }
    }
}
