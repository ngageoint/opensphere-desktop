package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;
import java.net.URL;
import java.util.function.Supplier;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.model.ViewModel;
import io.opensphere.mantle.iconproject.panels.IconEditor;
import io.opensphere.mantle.util.MantleToolboxUtils;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

/** Main UI Frame. */
@SuppressWarnings("serial")
public class IconProjDialog extends JFXDialog
{
    /** The model to be shared between all the UI elements. */
    private final PanelModel myPanelModel;

    /** The model for the display panels. */
    private final ViewModel myViewModel;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * packages anchorpane into swing dialog for MIST
     *
     * @param owner the calling window.
     * @param tb the toolbox for registry items.
     * @param showCancel the boolean to toggle the JFX Dialog to show or not
     *            show the cancel option.
     * @param multiSelectEnabled the option to enable or disable selecting
     *            multiple icons.
     * @param style
     */

    public IconProjDialog(Window owner, Toolbox tb, boolean showCancel, boolean multiSelectEnabled,
            Supplier<String> initialValueSupplier)
    {
        super(owner, "Icon Manager", showCancel);
        myToolbox = tb;
        myPanelModel = new PanelModel(myToolbox);

        myPanelModel.setOwner(owner);
        myPanelModel.setIconRegistry(MantleToolboxUtils.getMantleToolbox(tb).getIconRegistry());

        if (initialValueSupplier != null)
        {
            String value = initialValueSupplier.get();
            URL url = UrlUtilities.toURL(value);
            if (url != null)
            {
                myPanelModel.getSelectedRecord().set(myPanelModel.getIconRegistry().getIconRecord(url));
            }
        }

        myViewModel = new ViewModel();
        myViewModel.setMultiSelectEnabled(multiSelectEnabled);
        myPanelModel.setViewModel(myViewModel);
        setMinimumSize(new Dimension(800, 600));
        setSize(875, 600);
        setFxNode(new IconProjView(myPanelModel));

        if (myPanelModel.getIconRegistry().getManagerPrefs().getIconWidth() != 0)
        {
            myPanelModel.tileWidthProperty().set(myPanelModel.getIconRegistry().getManagerPrefs().getIconWidth());
        }

        setLocationRelativeTo(owner);
        setAcceptListener(() -> savePrefs());
    }

    /**
     * Saves icon manager preferences such as display width, display view, tree
     * selection. This ONLY saves during session. NOT across sessions.
     */
    // For now the only saved preference is display width.
    private void savePrefs()
    {
        MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry().getManagerPrefs()
                .setIconWidth((int)myPanelModel.tileWidthProperty().get());

//        MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry().getManagerPrefs().setTreeSelection(
//                myPanelModel.getTreeObject().getSelectedTree().getSelectionModel().selectedItemProperty().get());
    }

    /** Packages UI elements into one pane. */
    public class IconProjView extends AnchorPane
    {
        /** Panel comprised of Tree and icon display. */
        final private Node myMainPanel;

        /** The Model for the entire UI. */
        private final PanelModel myPanelModel;

        /** The model for the display panels. */
        private final ViewModel myViewModel;

        /**
         * Creates subpannels for UI.
         *
         * @param panelModel the model used for the UI.
         */
        public IconProjView(PanelModel panelModel)
        {
            myPanelModel = panelModel;
            myViewModel = myPanelModel.getViewModel();

            myMainPanel = new IconEditor(myPanelModel);
            myViewModel.setMainPanel(myMainPanel);

            setTopAnchor(myMainPanel, 0.0);
            setBottomAnchor(myMainPanel, 0.0);
            setLeftAnchor(myMainPanel, 0.);
            setRightAnchor(myMainPanel, 0.);

            myPanelModel.setViewModel(myViewModel);
            getChildren().addAll(myMainPanel);

            setOnKeyTyped(e ->
            {
                if (e.getCharacter().equals("\b"))
                {
                    String current = myPanelModel.searchTextProperty().get();
                    if (current.length() > 0)
                    {
                        current = current.substring(0, current.length() - 1);
                    }
                    myPanelModel.searchTextProperty().set(current);
                }
                else
                {
                    String current = myPanelModel.searchTextProperty().get();
                    if (current == null)
                    {
                        current = "";
                    }
                    myPanelModel.searchTextProperty().set(current + e.getCharacter());
                }
            });
        }
    }

    /**
     * Gets the model.
     *
     * @return the model used for the UI.
     */
    public PanelModel getPanelModel()
    {
        return myPanelModel;
    }
}
