package io.opensphere.core.quantify.settings;

import io.opensphere.core.options.impl.AbstractJFXOptionsProvider;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

/**
 * An options provider used to configure the state of the quantify plugin.
 */
public class QuantifyOptionsProvider extends AbstractJFXOptionsProvider
{
    /**
     * The checkbox used to enable / disable sending usage statistics to a
     * remote server.
     */
    private final CheckBox myEnableCheckbox;

    /** The model in which the state of the user's preferences is persisted. */
    private final QuantifySettingsModel myModel;

    /** The container in which the options provider is rendered. */
    private final VBox myContainer;

    /**
     * Creates a new options provider, bound to the supplied model.
     *
     * @param model The model in which the state of the user's preferences is
     *            persisted.
     */
    public QuantifyOptionsProvider(QuantifySettingsModel model)
    {
        super("Usage Statistics");
        myModel = model;

        myEnableCheckbox = new CheckBox("Send anonymous usage statistics?");
        myEnableCheckbox.selectedProperty().bindBidirectional(myModel.enabledProperty());

        myContainer = new VBox(myEnableCheckbox);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.impl.AbstractJFXOptionsProvider#getJFXOptionsPanel()
     */
    @Override
    public Node getJFXOptionsPanel()
    {
        return myContainer;
    }
}
