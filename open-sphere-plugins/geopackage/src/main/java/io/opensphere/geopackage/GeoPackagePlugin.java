package io.opensphere.geopackage;

import java.util.Collection;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.importer.GeoPackageImportController;
import io.opensphere.geopackage.importer.GeoPackageImporter;
import io.opensphere.geopackage.mantle.GeoPackageDataGroupController;
import io.opensphere.geopackage.mantle.LayerActivationHandler;
import io.opensphere.geopackage.transformer.GeoPackageLayerTransformer;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The plugin class for the GeoPackage plugin responsible for importing and
 * exporting geopackage files to and from the application.
 */
public class GeoPackagePlugin extends PluginAdapter
{
    /**
     * Manages the {@link DataGroupInfo} create from package files.
     */
    private GeoPackageDataGroupController myGroupController;

    /**
     * The import controller that adds the {@link GeoPackageImporter} to the
     * system's import registry.
     */
    private GeoPackageImportController myImportController;

    /**
     * The transformer for the geopackage tiles.
     */
    private GeoPackageLayerTransformer myLayerTransformer;

    @Override
    public void close()
    {
        super.close();
        myGroupController.close();
        myImportController.close();
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return New.list(myLayerTransformer);
    }

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);

        myLayerTransformer = new GeoPackageLayerTransformer(toolbox.getDataRegistry(), toolbox.getUIRegistry());
        myGroupController = new GeoPackageDataGroupController(toolbox, new LayerActivationHandler(toolbox, myLayerTransformer));

        myImportController = new GeoPackageImportController(toolbox, myGroupController.getImports());
        myImportController.open();
    }
}
