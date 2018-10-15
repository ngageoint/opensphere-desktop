package io.opensphere.mantle.iconproject.panels;

import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.scene.layout.BorderPane;

/**
 * A panel in which a user may select an existing collection or create a new
 * one.
 */
public class AddIconPane extends BorderPane
{
    /** The pane containing controls for the collection name choice. */
    private CollectNamesPane myCollectionNamePane;

    /** The pane containing controls for the sub collection name choice. */
    private SubCollectPane mySubCollectPane;

    /**
     * Instantiates the panel for getting the category and sub category from the
     * user when importing icons.
     *
     * @param panelModel The current UI model to use for registry items.
     */
    public AddIconPane(PanelModel panelModel)
    {
        myCollectionNamePane = new CollectNamesPane(panelModel);
        mySubCollectPane = new SubCollectPane(panelModel);

        setTop(myCollectionNamePane);
        setCenter(mySubCollectPane);
    }

    /**
     * Gets the current collection name pane.
     *
     * @return the collection name input pane
     */
    public CollectNamesPane getCollectionNamePane()
    {
        return myCollectionNamePane;
    }

    /**
     * Gets the current sub collection pane.
     *
     * @return the sub collection name input pane.
     */
    public SubCollectPane getSubCollectPane()
    {
        return mySubCollectPane;
    }
}
