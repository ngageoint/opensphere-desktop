package io.opensphere.core.net.manager.view;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.net.manager.controller.NetworkManagerController;
import io.opensphere.core.util.fx.FXUtilities;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

/** The frame in which the network manager panel is rendered. */
public class NetworkManagerFrame extends AbstractInternalFrame
{
    /** The unique identifier used during serialization operations. */
    private static final long serialVersionUID = 5033991918312540800L;

    /** The JavaFX panel on which the network manager is rendered. */
    private JFXPanel myPanel;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /** The panel on which the network status is displayed. */
    private NetworkManagerPanel myNetworkPanel;

    /** The controller used to orchestrate the network manager. */
    private NetworkManagerController myNetworkManagerController;

    /**
     * Creates a new network manager frame with the supplied toolbox.
     *
     * @param toolbox The toolbox through which application state is accessed.
     * @param controller the controller used to orchestrate the network manager.
     */
    public NetworkManagerFrame(Toolbox toolbox, NetworkManagerController controller)
    {
        myToolbox = toolbox;
        myNetworkManagerController = controller;

        FXUtilities.runOnFXThread(this::createView);

        setSize(1215, 450);
        setPreferredSize(getSize());
        setMinimumSize(getSize());

        setTitle("Network Monitor");
        setOpaque(false);
        setIconifiable(false);
        setClosable(true);
        setResizable(true);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    /**
     * Creates the user interface. Declared as a separate method to allow it to
     * run on the JavaFX thread.
     */
    private final void createView()
    {
        assert Platform.isFxApplicationThread();

        myPanel = new JFXPanel();

        BorderPane borderPane = new BorderPane();

        myNetworkPanel = new NetworkManagerPanel(myToolbox, myNetworkManagerController);
        borderPane.setCenter(myNetworkPanel);

        myPanel.setScene(FXUtilities.addDesktopStyle(new Scene(borderPane)));
        setContentPane(myPanel);
    }
}
