package icon.manager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import io.opensphere.core.Toolbox;
import io.opensphere.icon.manager.ResizeHelper;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.IconRegistryListener;
import io.opensphere.mantle.icon.impl.gui.IconBuilderDialog;
import io.opensphere.mantle.icon.impl.gui.IconChooserPanel;
import io.opensphere.icon.manager.IconManagerBuilder;
import io.opensphere.mantle.util.MantleToolboxUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;

/**
 * A icon manager user interface.
 */
@SuppressWarnings("PMD.GodClass")
public class MainFrame extends JFrame implements IconRegistryListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MainFrame.class);

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Chooser panel. */
    private final IconChooserPanel myChooserPanel;

    /** The Edit menu. */
    private final JMenu myEditMenu;

    /** The File menu. */
    private final JMenu myFileMenu;

    /** The Icon registry. */
    private final IconRegistry myIconRegistry;

    /** The Menu bar. */
    @SuppressWarnings("PMD.SingularField")
    private final JMenuBar myMenuBar;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new icon chooser dialog.
     *
     * @param tb the {@link Toolbox}
     */
    public MainFrame(Toolbox tb)
    {
        super();
        myToolbox = tb;
        myIconRegistry = MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry();
        setTitle("Icon Manager TEST");
        setIconImage(myToolbox.getUIRegistry().getMainFrameProvider().get().getIconImage());
        setMinimumSize(new Dimension(600, 400));

        AnchorPane myMainAnchorPane = IconManagerBuilder.createIconManagerPane();

        myScene = new Scene(myMainAnchorPane, 720, 400);
        myScene.getStylesheets().add(getClass().getResource("iconmanager.css").toExternalForm());
        myIconManagerInterFace.setScene(myScene);
        myIconManagerInterFace.setMinHeight(400);
        myIconManagerInterFace.setMinWidth(720);
        Image myWindowIcon = new Image(new FileInputStream("src/main/resources/Images/caci.jpg"));
        myIconManagerInterFace.getIcons().add(myWindowIcon);
        myIconManagerInterFace.setTitle("Icon Manager");
        myIconManagerInterFace.show();
        ResizeHelper.addResizeListener(myIconManagerInterFace);

    }

    /**
     * Shows the icon builder dialog.
     */
    private void showBuilderDialog()
    {
        IconRegistry iconRegistry = MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry();
        IconBuilderDialog dialog = new IconBuilderDialog(this, iconRegistry, myChooserPanel);
        dialog.setVisible(true);

    }
}
