package io.opensphere.core.control.keybinding;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * The Class KeyMapFrame. This class will show the control and shortcut keys.
 */

public class NewKeyMapFrame extends AbstractInternalFrame

{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The title of the window. */
    public static final String TITLE = "Key Map";

    /**
     * 
     * The Container panel. Since this JInternalFrame can be 'torn off' and uses
     * the JInternalFrame's content pane, set the content pane to a JPanel
     * created in this class.
     * 
     */

    /** A reference to the control registry. */
    private final ControlRegistry myControlRegistry;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * 
     * Instantiates a new key map frame.
     * 
     * @param toolbox the toolbox
     */

    public NewKeyMapFrame(Toolbox toolbox)
    {
        super();
        myToolbox = toolbox;
        myControlRegistry = toolbox.getControlRegistry();
        final JFXPanel fxPanel = new JFXPanel();

        setSize(800, 600);
        setPreferredSize(getSize());
        setMinimumSize(getSize());
        setTitle(TITLE);
        setOpaque(false);

        setIconifiable(false);
        setClosable(true);
        setResizable(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setContentPane(fxPanel);
        initAndShowGUI(fxPanel);
    }

    private static void initAndShowGUI(JFXPanel fxPanel)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                initFX(fxPanel);
            }
        });
    }

    private static void initFX(JFXPanel fxPanel)
    {
        Scene scene = createMainWindow(fxPanel);
        fxPanel.setScene(scene);
    }

    private static Scene createMainWindow(JFXPanel fxPanel)
    {
        Group root = new Group();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("styles/opensphere.css");
        TabPane theTaps = new TabPane();
        theTaps.setStyle("-fx-focus-color: #0066cc");

        Tab Map = new Tab("Map Controls");
        Map.setContent(new ControlUI(fxPanel.getWidth(), fxPanel.getHeight()));
        Tab Menu = new Tab("Menu Shortcuts");
        Menu.setContent(new MenuShortCutsUI(fxPanel.getWidth(), fxPanel.getHeight()));

        theTaps.getTabs().addAll(Map, Menu);
        root.getChildren().addAll(theTaps);
        return (scene);
    }

}