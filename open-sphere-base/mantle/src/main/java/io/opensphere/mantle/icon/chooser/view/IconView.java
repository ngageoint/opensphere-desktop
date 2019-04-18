package io.opensphere.mantle.icon.chooser.view;

import javafx.scene.layout.AnchorPane;

import io.opensphere.mantle.icon.chooser.model.IconModel;

/** Packages UI elements into one pane. */
public class IconView extends AnchorPane
{
    /** Panel comprised of Tree and icon display. */
    final private IconSelectionPanel myMainPanel;

    /** The Model for the entire UI. */
    private final IconModel myPanelModel;

    /**
     * Creates sub-panels for UI.
     *
     * @param panelModel the model used for the UI.
     */
    public IconView(IconModel panelModel)
    {
        myPanelModel = panelModel;

        myMainPanel = new IconSelectionPanel(myPanelModel);

        setTopAnchor(myMainPanel, Double.valueOf(0.0));
        setBottomAnchor(myMainPanel, Double.valueOf(0.0));
        setLeftAnchor(myMainPanel, Double.valueOf(0.0));
        setRightAnchor(myMainPanel, Double.valueOf(0.0));

        getChildren().addAll(myMainPanel);
    }

    /**
     * Gets the panel on which details are rendered.
     *
     * @return the panel on which details are rendered.
     */
    public IconDetail getDetailPanel()
    {
        return myMainPanel.getDetailPane();
    }
}
