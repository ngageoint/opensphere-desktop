package io.opensphere.geopackage.importer;

import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.CompositeService;

/**
 * Adds the {@link GeoPackageImporter} to the import services.
 */
public class GeoPackageImportController extends CompositeService
{
    /**
     * Imports geopackage files.
     */
    private final GeoPackageImporter myImporter;

    /**
     * Adds the {@link GeoPackageImporter} to the import services.
     *
     * @param toolbox The system toolbox.
     * @param alreadyImported The set of files that have already been imported
     *            into the system.
     */
    public GeoPackageImportController(Toolbox toolbox, Set<String> alreadyImported)
    {
        super(1);
        myImporter = new GeoPackageImporter(toolbox.getDataRegistry(), toolbox.getUIRegistry(), alreadyImported);
        addService(toolbox.getImporterRegistry().createImporterService(myImporter));
    }
}
