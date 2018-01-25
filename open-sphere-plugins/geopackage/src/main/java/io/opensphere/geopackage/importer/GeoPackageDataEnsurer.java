package io.opensphere.geopackage.importer;

import java.io.File;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.util.collections.New;

/**
 * Ensures that we have all data within the data registry for the geopackage
 * files that we have imported. If we do not, we remove what we do have from the
 * data registry and re-import the file.
 */
public class GeoPackageDataEnsurer
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(GeoPackageDataEnsurer.class);

    /**
     * The geopackage importer used to re-import corrupte geopackages.
     */
    private final FileOrURLImporter myImporter;

    /**
     * Contains imported geopackage data.
     */
    private final DataRegistry myRegistry;

    /**
     * Constructs a new data ensurer.
     *
     * @param importer The geopackage importer used to re-import corrupte
     *            geopackages.
     * @param registry Contains imported geopackage data.
     */
    public GeoPackageDataEnsurer(FileOrURLImporter importer, DataRegistry registry)
    {
        myRegistry = registry;
        myImporter = importer;
    }

    /**
     * Ensures that we have all data within the data registry for the geopackage
     * files that we have imported. If we do not, we remove what we do have from
     * the data registry and re-import the file.
     *
     * @param imports The set of files we have imported.
     */
    public void ensureData(Set<String> imports)
    {
        for (String importFile : imports)
        {
            LOGGER.info("Verifying geopackage file is cached for " + importFile);
            File theFile = new File(importFile);
            if (theFile.exists())
            {
                DataModelCategory category = new DataModelCategory(importFile, null, null);
                DefaultQuery layerQuery = new DefaultQuery(category, New.collection());
                long[] ids = myRegistry.performLocalQuery(layerQuery);
                if (ids == null || ids.length <= 0)
                {
                    LOGGER.info("Could not find geopackage file in cache reimporting " + importFile);
                    myRegistry.removeModels(category, false);
                    myImporter.importFile(theFile, null);
                }
                else
                {
                    LOGGER.info("Geopackage file is cached for " + importFile);
                }
            }
        }
    }
}
