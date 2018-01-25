package io.opensphere.geopackage.mantle;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Creates data groups that represent the layers within a geopackage file. Given
 * the root geopackage data group, this class will create a datagroup for the
 * geopackage file and then create subsequent groups for each layer in a
 * geopackage file. The creation will happen at startup if there are any saved
 * geopackages, as well as on import.
 */
public class GeoPackageDataGroupController extends DataRegistryListenerAdapter<GeoPackageLayer>
{
    /**
     * The key to the list of imported klv files.
     */
    protected static final String ourImportsKey = "imports";

    /**
     * The data model category to use for getting {@link GeoPackageLayer}.
     */
    private static final DataModelCategory ourCategory = new DataModelCategory(null, null, GeoPackageLayer.class.getName());

    /**
     * The id of the root group.
     */
    private static final String ourRootGroupId = "GeoPackage";

    /**
     * Used to add the geopackage root data group to the layers tree.
     */
    private final DataGroupController myDataGroupController;

    /**
     * Creates the groups and data types for the layers within a geo package
     * file.
     */
    private final DataGroupBuilder myGroupBuilder;

    /**
     * The mantle toolbox.
     */
    private final MantleToolbox myMantleToolbox;

    /**
     * Used to save which files have been imported.
     */
    private final PreferencesRegistry myPrefsRegistry;

    /**
     * Contains the GeoPackage file data.
     */
    private final DataRegistry myRegistry;

    /**
     * The root GeoPackage group.
     */
    private final DataGroupInfo myRootGroup;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new DataGroupCreator.
     *
     * @param toolbox The system toolbox.
     * @param tileListener The listener wanting notification of geopackage tile
     *            layer activations.
     */
    public GeoPackageDataGroupController(Toolbox toolbox, LayerActivationListener tileListener)
    {
        myToolbox = toolbox;
        myMantleToolbox = MantleToolboxUtils.getMantleToolbox(myToolbox);
        myDataGroupController = myMantleToolbox.getDataGroupController();
        myRegistry = toolbox.getDataRegistry();
        myPrefsRegistry = toolbox.getPreferencesRegistry();
        myRootGroup = new DefaultDataGroupInfo(true, myToolbox, ourRootGroupId, ourRootGroupId, "GPKG Files");
        myDataGroupController.addRootDataGroupInfo(myRootGroup, this);
        myGroupBuilder = new DataGroupBuilder(myToolbox, tileListener);
        myRegistry.addChangeListener(this, ourCategory, GeoPackagePropertyDescriptors.GEOPACKAGE_LAYER_PROPERTY_DESCRIPTOR);
        loadExisting();
    }

    /**
     * Stops this object from listening for new imports.
     */
    public void close()
    {
        myDataGroupController.removeDataGroupInfo(myRootGroup, this);
        myRegistry.removeChangeListener(this);
        myGroupBuilder.close();
    }

    /**
     * Gets a set of all files that have been imported into the system.
     *
     * @return The set of files that are currently in the system.
     */
    public Set<String> getImports()
    {
        Preferences prefs = myPrefsRegistry.getPreferences(GeoPackageDataGroupController.class);
        return prefs.getStringSet(ourImportsKey, New.set());
    }

    @Override
    public boolean isIdArrayNeeded()
    {
        return false;
    }

    @Override
    public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends GeoPackageLayer> newValues,
            Object source)
    {
        List<GeoPackageLayer> layers = New.list();
        for (GeoPackageLayer layer : newValues)
        {
            layers.add(layer);
        }

        createDataGroups(layers);
    }

    /**
     * Gets the {@link DataGroupController}.
     *
     * @return The {@link DataGroupController}.
     */
    protected DataGroupBuilder getDataGroupBuilder()
    {
        return myGroupBuilder;
    }

    /**
     * Creates {@link DataGroupInfo} and {@link DataTypeInfo} for the passed in
     * layers. These groups and types are then added to the rootGroup passed
     * into the constructor.
     *
     * @param layers The layers to create groups and types for.
     */
    private void createDataGroups(List<GeoPackageLayer> layers)
    {
        Map<String, DataGroupInfo> dataGroups = New.map();
        Map<String, DataGroupInfo> layerGroups = New.map();
        Map<String, String> layersToPackage = New.map();
        for (GeoPackageLayer layer : layers)
        {
            String packageLayerId = layer.getPackageFile();
            String layerId = layer.getPackageFile() + layer.getName();
            layersToPackage.put(layerId, packageLayerId);

            if (!dataGroups.containsKey(packageLayerId))
            {
                if (!getImports().contains(packageLayerId))
                {
                    saveToPrefs(packageLayerId);
                }

                DataGroupInfo dataGroup = myGroupBuilder.createPackageGroup(layer, packageLayerId,
                        new GeoPackageDeleter(myMantleToolbox, myRegistry, () -> removeFromPrefs(packageLayerId)));
                myRootGroup.addChild(dataGroup, this);
                dataGroups.put(packageLayerId, dataGroup);
            }

            if (!layerGroups.containsKey(layerId))
            {
                DataGroupInfo dataGroup = myGroupBuilder.createLayerGroup(layer, layerId);
                layerGroups.put(layerId, dataGroup);
            }

            DataGroupInfo layerGroup = layerGroups.get(layerId);

            DataTypeInfo dataType = myGroupBuilder.createDataType(layer, layerId);

            layerGroup.addMember(dataType, this);
        }

        for (DataGroupInfo dataGroup : layerGroups.values())
        {
            String packageId = layersToPackage.get(dataGroup.getId());
            DataGroupInfo packageGroup = dataGroups.get(packageId);
            packageGroup.addChild(dataGroup, this);
        }
    }

    /**
     * Loads existing imported geopackage files.
     */
    private void loadExisting()
    {
        SimpleQuery<GeoPackageLayer> query = new SimpleQuery<>(ourCategory,
                GeoPackagePropertyDescriptors.GEOPACKAGE_LAYER_PROPERTY_DESCRIPTOR);
        myRegistry.performLocalQuery(query);
        if (query.getResults() != null && !query.getResults().isEmpty())
        {
            createDataGroups(query.getResults());
        }
    }

    /**
     * Removes the feed from the saved list of imported files.
     *
     * @param geoPackagePath The file path to the geopackage file that has been
     *            removed from the system.
     */
    private void removeFromPrefs(String geoPackagePath)
    {
        Preferences prefs = myPrefsRegistry.getPreferences(GeoPackageDataGroupController.class);
        prefs.removeElementFromSet(ourImportsKey, geoPackagePath, this);
    }

    /**
     * Adds the feed the the saved list of imported feeds.
     *
     * @param geoPackagePath The file path to the newly imported geo package
     *            file.
     */
    private void saveToPrefs(String geoPackagePath)
    {
        Preferences prefs = myPrefsRegistry.getPreferences(GeoPackageDataGroupController.class);
        prefs.addElementToSet(ourImportsKey, geoPackagePath, this);
    }
}
