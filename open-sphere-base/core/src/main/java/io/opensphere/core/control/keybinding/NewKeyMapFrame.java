package io.opensphere.core.control.keybinding;

import io.opensphere.core.hud.awt.AbstractInternalFrame;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;

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
     * the JInternalFrame's content pane, set the content pane to a JavaFX Panel
     * through a JFX Dialog.
     */

    public NewKeyMapFrame()
    {
        super();
        final JFXPanel fxPanel = new JFXPanel();

        setSize(748, 610);
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

    /**
     * Takes the JAVA FX panel and puts it onto an FX thread for display.
     * 
     * @param fxPanel the panel to initialize.
     */
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

    /**
     * Sets the content to the scene.
     * 
     * @param fxPanel the panel needing to be set.
     */
    private static void initFX(JFXPanel fxPanel)
    {
        Scene scene = createMainWindow(fxPanel);
        fxPanel.setScene(scene);
    }

    /**
     * @param fxPanel the panel to add onto.
     * @return scene the JavaFX Scene to be displayed.
     */
    private static Scene createMainWindow(JFXPanel fxPanel)
    {
        Group root = new Group();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("styles/opensphere.css");
        TabPane theTaps = new TabPane();
        theTaps.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        theTaps.setStyle("-fx-focus-color: #0066cc");

        Tab Map = new Tab("Globe Shortcuts");
        Map.setContent(new ControlUI(fxPanel.getWidth(), fxPanel.getHeight()));
        Tab Menu = new Tab("Menu Shortcuts");
        Menu.setContent(new MenuShortCutsUI(fxPanel.getWidth(), fxPanel.getHeight()));

        theTaps.getTabs().addAll(Map, Menu);
        root.getChildren().addAll(theTaps);
        return scene;
    }
}
