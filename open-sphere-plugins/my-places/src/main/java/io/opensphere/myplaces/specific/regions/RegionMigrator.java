package io.opensphere.myplaces.specific.regions;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import io.opensphere.controlpanels.roipanel.config.v1.ROIConfig;
import io.opensphere.controlpanels.roipanel.config.v1.ROIGroup;
import io.opensphere.controlpanels.roipanel.config.v1.RegionOfInterest;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.Position;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.New;
import io.opensphere.myplaces.specific.MyPlacesMigrator;
import io.opensphere.myplaces.specific.regions.utils.RegionUtils;

/**
 * Migrates the ROI data into my places data.
 *
 */
public class RegionMigrator implements MyPlacesMigrator
{
    /** Key for the preferences. */
    private static final String PREFERENCE_KEY = "config";

    @Override
    public void individualMigrationDone(Preferences preferences)
    {
    }

    @Override
    public void migrate(Folder rootFolder, Toolbox toolbox)
    {
        ROIConfig config = toolbox.getPreferencesRegistry().getPreferences("io.opensphere.controlpanels.roipanel.ROIPanel")
                .getJAXBObject(ROIConfig.class, PREFERENCE_KEY, null);
        if (config != null)
        {
            Map<String, RegionOfInterest> map = New.map();
            Set<RegionOfInterest> orphanRegions = New.set();
            for (RegionOfInterest roi : config.getROI())
            {
                map.put(roi.getName(), roi);
                orphanRegions.add(roi);
            }

            for (ROIGroup group : config.getROIGroups())
            {
                Collection<RegionOfInterest> regionsForThisGroup = New.collection();
                for (String name : group.getRegionsOfInterest())
                {
                    RegionOfInterest roi = map.get(name);
                    if (roi != null)
                    {
                        regionsForThisGroup.add(roi);
                        orphanRegions.remove(roi);
                    }
                }

                if (!regionsForThisGroup.isEmpty())
                {
                    Folder groupFolder = rootFolder.createAndAddFolder();
                    groupFolder.setName(group.getName());
                    groupFolder.setId(UUID.randomUUID().toString());
                    groupFolder.setVisibility(Boolean.TRUE);

                    for (RegionOfInterest region : regionsForThisGroup)
                    {
                        List<? extends Position> positions = New.list(region.getGeoPoints());
                        RegionUtils.createRegionFromPositions(groupFolder, region.getName(), positions, null);
                    }
                }
            }

            for (RegionOfInterest orphan : orphanRegions)
            {
                List<? extends Position> positions = New.list(orphan.getGeoPoints());
                RegionUtils.createRegionFromPositions(rootFolder, orphan.getName(), positions, null);
            }
        }
    }

    @Override
    public boolean needsIndividualMigration(Preferences preferences)
    {
        return false;
    }
}
