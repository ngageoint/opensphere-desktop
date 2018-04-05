package io.opensphere.wps.ui.detail;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.javafx.input.IdentifiedControl;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.crust.DataTypeInfoUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.wps.config.v2.ProcessSetting;
import io.opensphere.wps.ui.detail.provider.DecorationUtils;
import io.opensphere.wps.util.WpsUtilities;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import net.opengis.wps._100.InputDescriptionType;

/** Links layer and column. */
public class LayerColumnLinker
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public LayerColumnLinker(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Links the column control to the layer control.
     *
     * @param inputDescription the input description of the potential column
     *            field
     * @param control the control of the potential column field
     * @param controls all the controls that have been created so far
     * @param processSetting The user process settings
     */
    @SuppressWarnings("unchecked")
    public void linkLayerAndColumn(InputDescriptionType inputDescription, IdentifiedControl<?> control,
            Collection<IdentifiedControl<? extends Control>> controls, ProcessSetting processSetting)
    {
        if (WpsUtilities.isColumnField(inputDescription))
        {
            IdentifiedControl<? extends Control> layerControl = controls.stream()
                    .filter(c -> "TYPENAME".equals(c.getVariableName())).findAny().orElse(null);
            if (layerControl != null)
            {
                ComboBox<String> columnBox = (ComboBox<String>)control.getControl();
                ComboBox<String> layerBox = (ComboBox<String>)layerControl.getControl();
                FXUtilities.addListenerAndInit(layerBox.valueProperty(),
                        layer -> handleLayerChange(layer, inputDescription, columnBox, processSetting));
            }
        }
    }

    /**
     * Handles a change in the layer selection.
     *
     * @param layer the new layer
     * @param inputDescription the input description
     * @param columnBox the column choice box
     * @param processSetting The user process settings
     */
    private void handleLayerChange(String layer, InputDescriptionType inputDescription, ComboBox<String> columnBox,
            ProcessSetting processSetting)
    {
        if (layer == null)
        {
            return;
        }

        /* This is done on a background thread because we may have to activate
         * the layer in order to get columns. */
        ThreadUtilities.runCpu(() ->
        {
            List<String> columns = getColumns(layer, inputDescription);

            FXUtilities.runOnFXThreadAndWait(() ->
            {
                columnBox.getItems().setAll(columns);
                String defaultValue = processSetting != null ? processSetting.getLastUsedColumns().get(layer) : null;
                DecorationUtils.setValue(columnBox, columns, defaultValue);
            });
        });
    }

    /**
     * Gets the columns for the layer.
     *
     * @param layer the layer
     * @param inputDescription the input description for the column field
     * @return the columns
     */
    private List<String> getColumns(String layer, InputDescriptionType inputDescription)
    {
        List<String> columns;

        List<String> allowedValues = WpsUtilities.getAllowedValues(inputDescription);
        if (!allowedValues.isEmpty())
        {
            String prefix = layer + ":";
            columns = allowedValues.stream().filter(v -> v.startsWith(prefix)).map(v -> v.replace(prefix, ""))
                    .collect(Collectors.toList());
        }
        else
        {
            List<String> mantleColumns = getMantleColumns(layer);
            columns = CollectionUtilities.sort(mantleColumns);
        }

        boolean required = inputDescription.getMinOccurs() != null && inputDescription.getMinOccurs().intValue() > 0;
        if (!required)
        {
            columns.add(0, "");
        }

        return columns;
    }

    /**
     * Gets the columns in mantle for the given data type name.
     *
     * @param typeName the data type name
     * @return the list of columns
     */
    private List<String> getMantleColumns(String typeName)
    {
        List<String> columns;
        MantleToolbox mantleToolbox = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        Set<DataTypeInfo> types = mantleToolbox.getDataGroupController().findMembers(t -> typeName.equals(t.getTypeName()), true);
        DataTypeInfo dataType = !types.isEmpty() ? types.iterator().next() : null;
        if (dataType != null && (dataType.getMetaDataInfo() == null || dataType.getMetaDataInfo().getKeyCount() == 0))
        {
            DataTypeInfoUtilities.ensureHasMetadata(dataType);
        }
        columns = dataType != null && dataType.getMetaDataInfo() != null ? dataType.getMetaDataInfo().getKeyNames()
                : Collections.emptyList();
        return columns;
    }
}
