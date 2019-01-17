package io.opensphere.core.net.manager.view;

import io.opensphere.core.Toolbox;
import io.opensphere.core.net.manager.controller.NetworkManagerController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/** A thin panel in which network manager is controlled. */
public class ControlPanel extends HBox
{
    /**
     * Creates a new control panel with the supplied items.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param controller the controller through which the network manager is
     *            configured.
     */
    public ControlPanel(final Toolbox toolbox, final NetworkManagerController controller)
    {
        setAlignment(Pos.CENTER);
        final CheckBox enabled = new CheckBox("Enable Network Monitoring");
        enabled.setAlignment(Pos.CENTER_LEFT);
        enabled.selectedProperty()
                .bindBidirectional(toolbox.getSystemToolbox().getNetworkConfigurationManager().networkMonitorEnabledProperty());

        final Button clear = new Button("Clear");
        clear.onActionProperty().set(e -> controller.getModel().getTransactions().clear());

        HBox.setHgrow(enabled, Priority.NEVER);
        final Node spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(clear, Priority.NEVER);

        getChildren().addAll(enabled, spacer, clear);
    }
}
