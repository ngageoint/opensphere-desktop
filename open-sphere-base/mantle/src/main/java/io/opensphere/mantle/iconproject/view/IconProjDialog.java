package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.model.ViewModel;
import io.opensphere.mantle.iconproject.panels.MainPanel;
import io.opensphere.mantle.iconproject.panels.TopMenuBar;
import io.opensphere.mantle.util.MantleToolboxUtils;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;

/** Main UI Frame. */
@SuppressWarnings("serial")
public class IconProjDialog extends JFXDialog
{
    /** The model to be shared between all the UI elements. */
    private PanelModel myPanelModel = new PanelModel();

    /** The model for the display panels. */
    private ViewModel myViewModel = new ViewModel();

    /** The Toolbox. */
    private Toolbox myToolbox;

    /**
     * Constructor.
     *
     * packages anchorpane into swing dialog for MIST
     *
     * @param owner the calling window.
     * @param tb the toolbox for registry items.
     * @param showCancel the boolean to toggle the JFX Dialog to show or not
     *            show the cancel option.
     * @param theMulti the option to enable or disable selecting multiple icons.
     */

    @SuppressWarnings("unchecked")
    public IconProjDialog(Window owner, Toolbox tb, boolean showCancel, boolean theMulti)
    {
        super(owner, "Icon Manager", showCancel);
        myToolbox = tb;
        myPanelModel.setToolBox(tb);
        myPanelModel.setOwner(owner);
        myPanelModel.setIconRegistry(MantleToolboxUtils.getMantleToolbox(tb).getIconRegistry());
        myViewModel.setMulti(theMulti);
        myPanelModel.setViewModel(myViewModel);
        setMinimumSize(new Dimension(800, 600));
        setSize(875, 600);
        setFxNode(new IconProjView(myPanelModel));
        if (myPanelModel.getIconRegistry().getManagerPrefs().getIconWidth().getValue() != 0)
        {
            myPanelModel.getCurrentTileWidth().set(myPanelModel.getIconRegistry().getManagerPrefs().getIconWidth().get());
        }

        myPanelModel.getViewModel().getMainPanel().refresh();
        setLocationRelativeTo(owner);
        setAcceptListener(() -> savePrefs());
        myPanelModel.getViewModel().getMainPanel().setDividerPositions(.28);
    }

    /**
     * Saves icon manager preferences such as display width, display view, tree
     * selection. This ONLY saves during session. NOT across sessions.
     */
    // For now the only saved preference is display width.
    @SuppressWarnings("unchecked")
    private void savePrefs()
    {
        MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry().getManagerPrefs().getIconWidth()
                .set(myPanelModel.getCurrentTileWidth().get());
        MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry().getManagerPrefs().getInitTreeSelection()
                .set((TreeItem<String>)myPanelModel.getTreeObj().getMyObsTree().get().getSelectionModel().selectedItemProperty()
                        .get());
    }

    /** Packages UI elements into one pane. */
    public class IconProjView extends AnchorPane
    {
        /** The top bar consisting of view,sizing, and filter. */
        final private TopMenuBar myTopMenuBar;

        /** Panel comprised of Tree and icon display. */
        final private MainPanel myMainPanel;

        /** The Model for the entire UI. */
        private PanelModel myPanelModel;

        /** The model for the display panels. */
        private ViewModel myViewModel;

        /**
         * Creates subpannels for UI.
         *
         * @param thePanelModel the model used for the UI.
         */
        public IconProjView(PanelModel thePanelModel)
        {
            myPanelModel = thePanelModel;
            myViewModel = myPanelModel.getViewModel();

            myMainPanel = new MainPanel(myPanelModel);
            myViewModel.setMainPanel(myMainPanel);

            myTopMenuBar = new TopMenuBar(myPanelModel);
            myViewModel.setTopMenuBar(myTopMenuBar);

            setTopAnchor(myMainPanel, 30.0);
            setBottomAnchor(myMainPanel, 0.0);
            setLeftAnchor(myMainPanel, -8.);
            setRightAnchor(myMainPanel, 0.);
            setLeftAnchor(myTopMenuBar, 0.);
            setRightAnchor(myTopMenuBar, 0.);

            myPanelModel.setViewModel(myViewModel);
            getChildren().addAll(myMainPanel, myTopMenuBar);
        }
    }

    /**
     * Gets the model.
     *
     * @return myPanelModel the model used for the UI.
     */
    public PanelModel getMyPanelModel()
    {
        return myPanelModel;
    }
}
