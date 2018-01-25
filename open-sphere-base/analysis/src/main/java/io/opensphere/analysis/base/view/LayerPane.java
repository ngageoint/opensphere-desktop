package io.opensphere.analysis.base.view;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import io.opensphere.analysis.base.model.SettingsModel;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.data.DataTypeInfo;

/** Pane for layer settings. */
public class LayerPane extends HBox
{
    /** The settings model. */
    private final SettingsModel mySettingsModel;

    /**
     * Constructor.
     *
     * @param settingsModel The settings model
     */
    public LayerPane(SettingsModel settingsModel)
    {
        super(5);
        setAlignment(Pos.CENTER_LEFT);
        mySettingsModel = settingsModel;
        getChildren().addAll(createLockedCheckBox(), FXUtilities.newHSpacer(5), new Label("Type:"), createLayerCombo());
    }

    /**
     * Creates the locked check box.
     *
     * @return the check box
     */
    private CheckBox createLockedCheckBox()
    {
        return SettingsUtilities.createCheckBox("Lock", mySettingsModel.lockedProperty());
    }

    /**
     * Creates the layer combo box.
     *
     * @return the combo box
     */
    private ComboBox<DataTypeInfo> createLayerCombo()
    {
        ComboBox<DataTypeInfo> combo = new ComboBox<>(mySettingsModel.getCommonSettings().availableLayersProperty());
        Callback<ListView<DataTypeInfo>, ListCell<DataTypeInfo>> cellFactory = p -> new ListCell<DataTypeInfo>()
        {
            @Override
            protected void updateItem(DataTypeInfo item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(item == null || empty ? null : item.getSourcePrefixAndDisplayNameCombo());
            }
        };
        combo.setCellFactory(cellFactory);
        combo.setButtonCell(cellFactory.call(null));
        combo.valueProperty().bindBidirectional(mySettingsModel.currentLayerProperty());
        return combo;
    }
}
