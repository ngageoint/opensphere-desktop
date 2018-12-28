package io.opensphere.mantle.icon.chooser.view;

import io.opensphere.mantle.icon.chooser.model.IconModel;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

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

        setTopAnchor(myMainPanel, 0.0);
        setBottomAnchor(myMainPanel, 0.0);
        setLeftAnchor(myMainPanel, 0.);
        setRightAnchor(myMainPanel, 0.);

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
            else if (KeyCode.ESCAPE.equals(e.getCode()) || e.getCharacter().charAt(0) == 27)
            {
                myPanelModel.searchTextProperty().set("");
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
