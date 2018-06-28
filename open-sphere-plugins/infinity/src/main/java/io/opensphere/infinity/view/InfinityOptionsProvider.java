package io.opensphere.infinity.view;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import io.opensphere.core.options.impl.AbstractJFXOptionsProvider;
import io.opensphere.infinity.model.InfinitySettingsModel;

/** The options provider for infinity configurations. */
public class InfinityOptionsProvider extends AbstractJFXOptionsProvider
{
    /** The model to which the options provider is bound. */
    private final InfinitySettingsModel myModel;

    /**
     * Creates a new options provider using the supplied model.
     *
     * @param model the model to which the options provider is bound.
     */
    public InfinityOptionsProvider(InfinitySettingsModel model)
    {
        super("Infinity");
        myModel = model;
    }

    @Override
    public Node getJFXOptionsPanel()
    {
        VBox pane = new VBox(5);

        CheckBox enabledBox = new CheckBox("Enable infinity layer count");
        enabledBox.setTooltip(new Tooltip(
                "Whether to update the visible feature count for infinity-enabled layers (when the map or time is changed)"));
        enabledBox.selectedProperty().bindBidirectional(myModel.enabledProperty());
        pane.getChildren().add(enabledBox);

        return pane;
    }
}
