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
import javafx.scene.layout.AnchorPane;

/** Main UI Frame. */
@SuppressWarnings("serial")
public class IconProjDialog extends JFXDialog
{
    /** The model to be shared between all the UI elements. */
    private PanelModel myPanelModel = new PanelModel();

    /**
     * Constructor.
     *
     * packages anchorpane into swing dialog for MIST
     *
     * @param owner the calling window.
     * @param tb the toolbox for registry items.
     * @param showCancel the boolean to toggle the JFX Dialog to show or not
     *            show the cancel option.
     */

    public IconProjDialog(Window owner, Toolbox tb, boolean showCancel)
    {
        super(owner, "Intern Icon Manager", showCancel);
        myPanelModel.setToolBox(tb);
        myPanelModel.setOwner(owner);
        myPanelModel.setIconRegistry(MantleToolboxUtils.getMantleToolbox(tb).getIconRegistry());
        setLocationRelativeTo(owner);
        setSize(875, 600);
        setFxNode(new IconProjView(myPanelModel));
        setMinimumSize(new Dimension(800, 600));
    }

    /** Packages UI elements into one pane. */
    public class IconProjView extends AnchorPane
    {
        /** The top bar consisting of view,sizing, and filter. */
        final TopMenuBar myTopMenuBar;

        /** Panel comprised of Tree and icon display. */
        final MainPanel myMainPanel;

        /** The Model for the entire UI. */
        private PanelModel myPanelModel;

        /** The model for the display panels. */
        private ViewModel myViewModel = new ViewModel();

        /**
         * Creates subpannels for UI.
         *
         * @param thePanelModel the model used for the UI.
         */

        public IconProjView(PanelModel thePanelModel)
        {
            myPanelModel = thePanelModel;

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
