package icon.manager.gui;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

import icon.manager.model.IconManagerBuilder;
import io.opensphere.core.Toolbox;
import io.opensphere.icon.manager.ResizeHelper;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.IconRegistryListener;
import io.opensphere.mantle.util.MantleToolboxUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
/**
 * A icon manager user interface.
 */
@SuppressWarnings("PMD.GodClass")
public class MainFrameDialog extends IconManagerBuilder implements IconRegistryListener
{
    /** The Toolbox. */
    private final Toolbox myToolbox;

    private Scene myScene;

    private Stage myIconManagerInterFace;

    private IconRegistry myIconRegistry;

    /**
     * Instantiates a new icon chooser dialog.
     *
     * @param tb the {@link Toolbox}
     * @throws FileNotFoundException 
     */
    @SuppressWarnings("unchecked")
    public MainFrameDialog(Toolbox tb) throws FileNotFoundException
    {
        super();
        myToolbox = tb;
        myIconRegistry = MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry();
      //  setIconImage(myToolbox.getUIRegistry().getMainFrameProvider().get().getIconImage());

        AnchorPane myMainAnchorPane = IconManagerBuilder();

        myScene = new Scene(myMainAnchorPane, 720, 400);
        myScene.getStylesheets().add(getClass().getResource("iconmanager.css").toExternalForm());
        myIconManagerInterFace.setScene(myScene);
        myIconManagerInterFace.setMinHeight(400);
        myIconManagerInterFace.setMinWidth(720);
        
       // Image myWindowIcon = new Image(new FileInputStream("src/main/resources/Images/caci.jpg"));
       // myIconManagerInterFace.getIcons().add(myWindowIcon);
        
        myIconManagerInterFace.getIcons().addAll((Collection<? extends Image>)(myToolbox.getUIRegistry().getMainFrameProvider().get().getIconImage()));
        myIconManagerInterFace.setTitle("Icon Manager TEST");
        myIconManagerInterFace.show();
        ResizeHelper.addResizeListener(myIconManagerInterFace);

    }

    @Override
    public void iconAssigned(long iconId, List<Long> deIds, Object source)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void iconsAdded(List<IconRecord> added, Object source)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void iconsRemoved(List<IconRecord> removed, Object source)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void iconsUnassigned(List<Long> deIds, Object source)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void shrink(MouseEvent mouseEvent)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void enlarge(MouseEvent mouseEvent)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void LISTV(MouseEvent mouseEvent)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void GRIDV(MouseEvent mouseEvent)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void ADD(MouseEvent mouseEvent)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void handleAddIconButtonAction(MouseEvent mouseEvent)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void CUSTOMIZE(MouseEvent mouseEvent)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void GENERATE(MouseEvent mouseEvent)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void Close(MouseEvent mouseEvent)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void start(Stage arg0) throws Exception
    {
        // TODO Auto-generated method stub
        
    }
}
