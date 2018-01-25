package io.opensphere.featureactions.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.CustomColumnAction;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.dynmeta.DynamicMetadataDataTypeController;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Applies metadata actions. */
public class MetaDataApplier implements ActionApplier
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MetaDataApplier.class);

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public MetaDataApplier(Toolbox toolbox)
    {
        myMantleToolbox = MantleToolboxUtils.getMantleToolbox(toolbox);
        myPreferencesRegistry = toolbox.getPreferencesRegistry();
    }

    @Override
    public void applyActions(Collection<? extends Action> actions, List<? extends MapDataElement> elements, DataTypeInfo dataType)
    {
        for (Action action : actions)
        {
            if (action instanceof CustomColumnAction)
            {
                CustomColumnAction columnAction = (CustomColumnAction)action;
                addCustomColumn(columnAction, elements, dataType);
            }
        }
    }

    @Override
    public void clearActions(Collection<Long> elementIds, DataTypeInfo dataType)
    {
        // Should consider removing the column from the controller and
        // preferences
    }

    @Override
    public void removeElements(Collection<Long> elementIds, DataTypeInfo dataType)
    {
        // Nothing to do here
    }

    /**
     * Adds a custom column to the meta data.
     *
     * @param columnAction the column action
     * @param elements the data elements
     * @param dataType the data type
     */
    private void addCustomColumn(CustomColumnAction columnAction, List<? extends MapDataElement> elements, DataTypeInfo dataType)
    {
        DynamicMetadataDataTypeController controller = myMantleToolbox.getDynamicDataElementMetadataManager()
                .getController(dataType.getTypeKey());

        String column = columnAction.getColumn();

        // Add the column
        if (!dataType.getMetaDataInfo().hasKey(column))
        {
            // Ensure it actually shows up in the baseball card popup
            Preferences prefs = myPreferencesRegistry.getPreferences("io.opensphere.externaltools.model.DotTableModel");
            String key = dataType.getTypeKey() + ".AKO";
            List<String> prefsColumns = prefs.getStringList(key, Collections.emptyList());
            if (!prefsColumns.contains(column))
            {
                prefs.addElementToList(key, column, this);
            }

            controller.addDynamicColumn(column, String.class, this);
        }

        // Set the value for each element
        List<Long> ids = FeatureActionUtilities.getIds(elements);
        try
        {
            controller.setValues(ids, column, columnAction.getValue(), this);
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.error(e);
        }
    }
}
