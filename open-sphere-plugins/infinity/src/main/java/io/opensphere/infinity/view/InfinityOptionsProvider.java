package io.opensphere.infinity.view;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import io.opensphere.core.options.impl.AbstractJFXOptionsProvider;
import io.opensphere.infinity.model.InfinitySettingsModel;
import io.opensphere.mantle.infinity.InfinityUtilities;

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
        super(InfinityUtilities.INFINITY);
        myModel = model;
    }

    @Override
    public Node getJFXOptionsPanel()
    {
        VBox pane = new VBox(5);

        String infinityLower = InfinityUtilities.INFINITY.toLowerCase();
        CheckBox enabledBox = new CheckBox(String.format("Enable %s layer count", infinityLower));
        enabledBox.setTooltip(new Tooltip(String.format(
                "Whether to update the visible feature count for %s-enabled layers (when the map or time is changed)",
                infinityLower)));
        enabledBox.selectedProperty().bindBidirectional(myModel.enabledProperty());
        pane.getChildren().add(enabledBox);

        return pane;
    }
}
