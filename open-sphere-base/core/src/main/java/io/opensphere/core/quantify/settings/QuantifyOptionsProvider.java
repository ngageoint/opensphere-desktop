package io.opensphere.core.quantify.settings;

import io.opensphere.core.options.impl.AbstractJFXOptionsProvider;
import io.opensphere.core.quantify.Quantify;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
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

    /** The checkbox used to enable / disable metrics capture to local log. */
    private final CheckBox myCaptureToLogCheckbox;

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
        myEnableCheckbox.selectedProperty().addListener(e -> Quantify.collectEnableDisableMetric(
                "mist3d.settings.usage-statistics.send-anonymous-usage-stats", myEnableCheckbox.isSelected()));
        myEnableCheckbox.selectedProperty().bindBidirectional(myModel.enabledProperty());

        myCaptureToLogCheckbox = new CheckBox("Capture statistics to log?");
        myCaptureToLogCheckbox.selectedProperty()
        .addListener(e -> Quantify.collectEnableDisableMetric("mist3d.settings.usage-statistics.capture-stats-to-log",
                myCaptureToLogCheckbox.isSelected()));
        myCaptureToLogCheckbox.selectedProperty().bindBidirectional(myModel.captureToLogProperty());

        Label disclaimer = new Label("MIST Desktop collects technical data about usage of the application "
                + "to gain better insight into which parts of the application are most frequently used. "
                + "All information collected for this purpose is anonymous, and intentionally omits all references "
                + "to data loaded into the application, areas-of-interest, filters, connected data servers, and "
                + "any other session-specific information. Information collected includes such things as buttons "
                + "clicked, functionality accessed, and other similar interactions. Specifics about collected data "
                + "from your session may be enabled through the settings window (Settings > Usage Statistics), by "
                + "enabling the \"Capture Metrics in Log\", which writes exact copies of all transmitted "
                + "information to the local log file.");
        disclaimer.setMaxWidth(Region.USE_COMPUTED_SIZE);
        disclaimer.setPrefWidth(600);
        disclaimer.setWrapText(true);

        myContainer = new VBox(disclaimer, myEnableCheckbox, myCaptureToLogCheckbox);
        myContainer.setSpacing(25);
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
