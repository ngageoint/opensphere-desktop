package io.opensphere.mantle.data.columns.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.opensphere.core.datafilter.columns.ColumnMapping;
import io.opensphere.core.util.fx.AutoCompleteComboBoxListener;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/** A column mapping ListCell. */
class ColumnMappingCell extends ListCell<ColumnMapping>
{
    /** The column mapping resources. */
    private final ColumnMappingResources rsc;

    /** The list of mappings. */
    private final ObservableList<ColumnMapping> model;

    /** Root GUI component. */
    private final HBox rootPane = FXUtilities.newHBox();

    /** Layer selection widget. */
    private final ComboBox<String> layerSel = new ComboBox<>();

    /** Column selection widget. */
    private final ComboBox<String> columnSel = new ComboBox<>();

    /** Set to true when events originating here come back. Ignore them. */
    private boolean ignoreEdits;

    /** Index of types by display name. */
    private final Map<String, DataTypeRef> nameIndex = new TreeMap<>();

    /** Index of types by type key. */
    private final Map<String, DataTypeRef> keyIndex = new TreeMap<>();

    /** List of display names of active types. */
    private final List<String> activeList = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param cmr the column mapping resources
     * @param m the list of mappings
     * @param layers the layer keys
     */
    public ColumnMappingCell(ColumnMappingResources cmr, ObservableList<ColumnMapping> m, List<DataTypeRef> layers)
    {
        rsc = cmr;
        model = m;
        for (DataTypeRef r : layers)
        {
            nameIndex.put(r.getType().getDisplayName(), r);
            keyIndex.put(r.getType().getTypeKey(), r);
            if (r.isActive())
            {
                activeList.add(r.getType().getDisplayName());
            }
        }

        layerSel.getItems().setAll(activeList);
        layerSel.setEditable(true);
        columnSel.setEditable(true);

        layerSel.setTooltip(new Tooltip("The layer to " + Constants.MAP_VERB));
        columnSel.setTooltip(new Tooltip("The column to " + Constants.MAP_VERB));

        // new AutoCompleteComboBoxListener<>(layerSel);
        new AutoCompleteComboBoxListener<>(columnSel);

        layerSel.setPrefWidth(262);
        columnSel.setPrefWidth(262);

        layerSel.setOnAction(e -> handleLayerChanged());
        columnSel.setOnAction(e -> handleColumnChanged());
        columnSel.getEditor().textProperty().addListener((obs, o, n) -> columnSel.setValue(n));

        Button delB = FXUtilities.newIconButton(IconType.CLOSE, Color.RED);
        delB.setTooltip(new Tooltip("Remove"));
        delB.setOnAction(e -> model.remove(getIndex()));
        rootPane.getChildren().addAll(new Label("Layer:"), layerSel, new Label(" Column:"), columnSel, FXUtilities.newHSpacer(0),
                delB);
    }

    @Override
    public void updateItem(ColumnMapping mapping, boolean empty)
    {
        super.updateItem(mapping, empty);
        if (empty || mapping == null)
        {
            setGraphic(null);
            return;
        }
        ignoreEdits = true;
        layerSel.setValue(nameOf(mapping.getLayerKey()));
        updateColumnOptions();
        columnSel.setValue(mapping.getLayerColumn());
        ignoreEdits = false;
        setGraphic(rootPane);
    }

    /** Handles a change in the layer UI value. */
    private void handleLayerChanged()
    {
        if (ignoreEdits)
        {
            return;
        }
        updateColumnOptions();
        ignoreEdits = true;
        columnSel.setValue(null);
        ignoreEdits = false;
        updateModel();
    }

    /** Handles a change in the column UI value. */
    private void handleColumnChanged()
    {
        if (ignoreEdits)
        {
            return;
        }
        updateModel();
    }

    /** Updates the column options from the current layer selection. */
    private void updateColumnOptions()
    {
        columnSel.getItems().clear();
        String layerKey = keyOf(layerSel.getValue());
        if (layerKey == null)
        {
            return;
        }
        List<String> cols = new LinkedList<>(rsc.getLayerColumns(layerKey));
        Collections.sort(cols);
        columnSel.getItems().setAll(cols);
    }

    /** Writes an updated ColumnMapping instance to the model (i.e., List). */
    private void updateModel()
    {
        // Note: do not write to the model synchronously lest the resulting
        // events cause conflicts in access to the model.
        String lay = keyOf(layerSel.getValue());
        String col = columnSel.getValue();
        Platform.runLater(() -> model.set(getIndex(), new ColumnMapping(null, lay, col)));
    }

    /**
     * Lookup the display name for a given type key
     *
     * @param key key
     * @return name
     */
    private String nameOf(String key)
    {
        DataTypeRef r = getNonNull(keyIndex, key);
        if (r == null)
        {
            return null;
        }
        return r.getType().getDisplayName();
    }

    /**
     * Lookup the type key for a given display name.
     *
     * @param name name
     * @return key
     */
    private String keyOf(String name)
    {
        DataTypeRef r = getNonNull(nameIndex, name);
        if (r == null)
        {
            return null;
        }
        return r.getType().getTypeKey();
    }

    /**
     * Null-tolerant get method.
     *
     * @param m a Map
     * @param k a key
     * @return a value, or null
     */
    private static <K, V> V getNonNull(Map<K, V> m, K k)
    {
        if (k != null)
        {
            return m.get(k);
        }
        return null;
    }
}
