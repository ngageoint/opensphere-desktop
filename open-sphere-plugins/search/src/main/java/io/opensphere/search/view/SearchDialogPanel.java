package io.opensphere.search.view;

import io.opensphere.core.Toolbox;
import io.opensphere.search.controller.SearchController;
import io.opensphere.search.model.SearchModel;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * A panel in which results are rendered.
 */
public class SearchDialogPanel extends BorderPane
{
    /** The default minimum size of a component. */
    private static final double DEFAULT_MINIMUM_SIZE = 5;

    /** The maximum height of a header / footer component. */
    private static final int HEADER_MAXIMUM_HEIGHT = 25;

    /** The configuration pane in which the user can modify the search. */
    private final ConfigurationPane myConfigurationPane;

    /**
     * The search controller that does the searching as well as populates the
     * model.
     */
    private final SearchController myController;

    /** The metadata pane that displays information about the results. */
    private final MetadataPane myMetadataPane;

    /** The model in which the search configuration is stored. */
    private final SearchModel myModel;

    /** The main panel in which results are displayed. */
    private final ResultPane myResultPane;

    /**
     * Creates a new search results panel.
     *
     * @param toolbox The system toolbox.
     * @param searchModel The main search model.
     */
    public SearchDialogPanel(Toolbox toolbox, SearchModel searchModel)
    {
        setPadding(new Insets(10));

        myModel = searchModel;
        myController = new SearchController(toolbox, myModel);

        myConfigurationPane = new ConfigurationPane(myModel);
        myConfigurationPane.setMinSize(DEFAULT_MINIMUM_SIZE, HEADER_MAXIMUM_HEIGHT);

        myMetadataPane = new MetadataPane(myModel);
        myResultPane = new ResultPane(toolbox.getSearchRegistry(), myModel, toolbox);

        Separator verticalSpacer = new Separator(Orientation.VERTICAL);
        verticalSpacer.setPadding(new Insets(5, 5, 5, 5));

        setTop(myMetadataPane);
        setLeft(new HBox(myConfigurationPane, verticalSpacer));
        setCenter(myResultPane);
    }

    /**
     * Stops listening for user input.
     */
    public void close()
    {
        myController.close();
    }

    /**
     * Gets the value of the {@link #myModel} field.
     *
     * @return the value stored in the {@link #myModel} field.
     */
    public SearchModel getModel()
    {
        return myModel;
    }

    /**
     * Performs a search.
     */
    public void performSearch()
    {
        myController.performSearch();
    }

    /**
     * Sets whether the search result dialog is visible.
     *
     * @param visible whether the dialog is visible
     */
    public void setDialogVisible(boolean visible)
    {
        myController.setDialogVisible(visible);
    }
}
