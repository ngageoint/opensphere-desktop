package io.opensphere.core.control.keybinding;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.BindingsToListener;
import io.opensphere.core.control.BoundEventListener;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GridBagPanel;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

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

    /** The Button bindings. */
    private final Map<String, ButtonBinding> myButtonBindings;

    private GridBagPanel myShortcutKeyPanel;

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
        myButtonBindings = New.map();
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

    public void crap() {
        
    	int rowCounter = 0;
        String previousCategory = "";
        
    	
        ControlContext controlContext = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        Map<String, List<BindingsToListener>> eventListeners = controlContext.getEventListenersByCategory();
        List<String> bindingKeys = New.list(eventListeners.keySet());
        Collections.sort(bindingKeys);
        
    	 for (String category : bindingKeys)
         {
             for (BindingsToListener btl : eventListeners.get(category))
             {
                 BoundEventListener listener = btl.getListener();
                 // Don't list things that can't be reassigned. This should be
                 // limited to such things as
                 // overlay readouts that are triggered by Mouse Moved events,
                 // and other similarly "not specifically user invoked"
                 // controls.
                 if (listener.isReassignable())
                 {
                     if (category.equals(previousCategory))
                     {
                     	System.out.println("The listener is: " + listener);
                       //  addListenerTitle(listener, rowCounter);
                       //  addBindingButton(btl, rowCounter);
                     }
                     else
                     {
                      //   addCategoryLabel(category, rowCounter);
                         rowCounter++;
                         System.out.println("Thie category is: ");
                       //  addListenerTitle(listener, rowCounter);
                      //   addBindingButton(btl, rowCounter);
                         previousCategory = category;
                     }
                     rowCounter++;
                 }
             }
         }
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
        theTaps.setOnContextMenuRequested(e->crap());

        Tab Map = new Tab("Map Controls");
        Map.setContent(new ControlUI(fxPanel.getWidth(), fxPanel.getHeight()));
        Tab Menu = new Tab("Menu Shortcuts");
        Menu.setContent(new MenuShortCutsUI(fxPanel.getWidth(), fxPanel.getHeight()));
        
        theTaps.getTabs().addAll(Map, Menu);
        root.getChildren().addAll(theTaps);
        return (scene);
    }

}