package io.opensphere.mantle.iconproject.panels;

import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.scene.layout.BorderPane;

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

    /** The current model for UI elements. */
    private PanelModel myPanelModel;

    /** The pane containing controls for the collection name choice. */
    private CollectNamesPane myCollectionNamePane;

    /** The pane containing controls for the sub collection name choice. */
    private SubCollectPane mySubCollectPane;

    /**
     * Instantiates the panel for getting the category and sub category from the
     * user when importing icons.
     *
     * @param thePanelModel The current UI model to use for registry items.
     */
    public AddIconPane(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;

        setCollectionNamePane(new CollectNamesPane(myPanelModel));
        setSubCollectPane(new SubCollectPane(myPanelModel));

        setTop(getCollectionNamePane());
        setCenter(getSubCollectPane());
    }

    /**
     * Sets the current collection name pane.
     *
     * @param theCollectionNamePane the collection name input pane.
     */
    public void setCollectionNamePane(CollectNamesPane theCollectionNamePane)
    {
        myCollectionNamePane = theCollectionNamePane;
    }

    /**
     * Gets the current collection name pane.
     *
     * @return myCollectionNamePane the collection name input pane
     */
    public CollectNamesPane getCollectionNamePane()
    {
        return myCollectionNamePane;
    }

    /**
     * Sets the current sub collection pane.
     *
     * @param theSubCollectPane the sub collection name input pane.
     */
    private void setSubCollectPane(SubCollectPane theSubCollectPane)
    {
        mySubCollectPane = theSubCollectPane;
    }

    /**
     * Gets the current sub collection pane.
     *
     * @return mySubCollectPane the sub collection name input pane.
     */
    public SubCollectPane getSubCollectPane()
    {
        return mySubCollectPane;
    }
}
