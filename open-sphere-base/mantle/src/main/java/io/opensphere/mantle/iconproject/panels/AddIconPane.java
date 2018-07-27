package io.opensphere.mantle.iconproject.panels;

import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * A panel in which a user may select an existing collection or create a new
 * one.
 */
public class AddIconPane extends BorderPane
{
    /**
     * serialVersionUID.
     */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    private PanelModel myPanelModel = new PanelModel();

    /**
     * Instantiates a new collection name panel.
     *
     * @param collectionNameSet The set of collection names to display in the
     *            panel.
     */
    public AddIconPane(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;

        setTop(createCollection());
        setBottom(createSubCollection());

    }

    private VBox createCollection()
    {
        CollectNamesPane topPane = new CollectNamesPane(myPanelModel);
        return topPane;
    }

    private VBox createSubCollection()
    {

        SubCollectPane bottomPane = new SubCollectPane(myPanelModel);
        return bottomPane;
    }

}
