package io.opensphere.search.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.util.List;

import javax.swing.JFrame;

import io.opensphere.core.Toolbox;
import io.opensphere.core.search.SearchRegistry;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.search.model.SearchModel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

/**
 * The shows the search results in a list.
 */
public class ResultPane extends BorderPane
{
    /**
     * The list view showing the search results.
     */
    private ListView<SearchResult> myResultList;

    /**
     * The list to bind to the UI, this helps us ensure the changes to the ui
     * occur on the fx thread.
     */
    private final ObservableList<SearchResult> myResultsOnFxThread = FXCollections.observableArrayList();

    /**
     * Used to get any custom search result UIs.
     */
    private final SearchRegistry mySearchRegistry;

    /**
     * The search model.
     */
    private final SearchModel myModel;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The popup dialog. */
    private JFXDialog myPopupDialog;

    /**
     * Constructs a new result pane.
     *
     * @param searchRegistry Used to get any custom search result UIs.
     * @param model The main search model.
     * @param toolbox The toolbox
     */
    public ResultPane(SearchRegistry searchRegistry, SearchModel model, Toolbox toolbox)
    {
        myModel = model;
        mySearchRegistry = searchRegistry;
        myToolbox = toolbox;
        createUI();
    }

    /**
     * Creates the criteria list view.
     *
     * @return the list view
     */
    private Node createResultList()
    {
        myResultList = new ListView<>();
        myResultList.setCellFactory((searchResult) ->
        {
            SearchResultUI row = new SearchResultUI(mySearchRegistry);
            row.setMaxWidth(myResultList.widthProperty().get() - 100);
            row.setPrefWidth(myResultList.widthProperty().get() - 100);
            myResultList.widthProperty().addListener((observable, oldValue, newValue) ->
            {
                row.setPrefWidth(newValue.intValue() - 100);
                row.setMaxWidth(newValue.intValue() - 100);
            });
            row.setWrapText(true);
            row.setStyle("-fx-background-color: transparent;");

            // Mimicking the behavior of web, don't select the item from the
            // list, only fly to it. Selection may occur from the map.
            row.setOnMouseClicked(e -> mouseClicked(row, e));
            row.setOnMouseEntered(e -> hoverChange(row, true));
            row.setOnMouseExited(e -> hoverChange(row, false));

            return row;
        });

        myResultsOnFxThread.addAll(myModel.getShownResults());
        myModel.getShownResults().addListener(this::onResultsChanged);
        myModel.getDoubleSelectedResult().addListener((obs, o, n) -> showDetailsPopup(n));
        myResultList.setItems(myResultsOnFxThread);
        myResultList.setStyle("-fx-background-color: transparent; -fx-padding: 3px;");

        return myResultList;
    }

    /**
     * An event handler used to react to mouse click events.
     *
     * @param result the result for which the mouse was clicked.
     * @param event the mouse event.
     */
    private void mouseClicked(SearchResultUI result, MouseEvent event)
    {
        if (event.getClickCount() == 1)
        {
            myModel.getFocusedResult().set(result.getResult());
        }
        else if (event.getClickCount() == 2)
        {
            Node node = result.createUI(true);
            EventQueueUtilities.runOnEDT(() -> showDetailsPopup(result.getResult(), node));
        }
    }

    /**
     * Shows the details popup for the given search result. Can be called from any thread.
     *
     * @param result the search result
     */
    private void showDetailsPopup(SearchResult result)
    {
        if (result != null)
        {
            FXUtilities.runOnFXThread(() ->
            {
                SearchResultUI row = new SearchResultUI(mySearchRegistry);
                row.updateItem(result, false);
                Node node = row.createUI(true);
                EventQueueUtilities.runOnEDT(() -> showDetailsPopup(result, node));
            });
        }
    }

    /**
     * Shows the details popup for the given search result.
     *
     * @param result the search result
     * @param node the JavaFX node
     */
    private void showDetailsPopup(SearchResult result, Node node)
    {
        if (myPopupDialog == null || !myPopupDialog.isVisible())
        {
            JFrame parent = myToolbox.getUIRegistry().getMainFrameProvider().get();
            JFXDialog dialog = new JFXDialog(parent, null, false);
            dialog.setSize(new Dimension(400, 400));
            dialog.setResizable(true);
            setLocationRelativeTo(dialog, parent);
            dialog.setModal(false);
            myPopupDialog = dialog;
        }

        myPopupDialog.setTitle(result.getText() + " Details");
        myPopupDialog.setFxNode(node);
        myPopupDialog.setVisible(true);
    }

    /**
     * Sets the window location relative to the parent component.
     *
     * @param w the window
     * @param c the parent component
     */
    private void setLocationRelativeTo(Window w, Component c)
    {
        Dimension windowSize = w.getSize();
        Dimension compSize = c.getSize();
        Point compLocation = c.getLocationOnScreen();
        int dx = compLocation.x + ((compSize.width - windowSize.width) / 2);
        int dy = compLocation.y + ((compSize.height - 2 * windowSize.height) / 2);
        w.setLocation(dx, dy);
    }

    /**
     * An event handler used to react to hover change events.
     *
     * @param result the result for which the hover changed.
     * @param hovered a flag used to indicate the hover state.
     */
    private void hoverChange(SearchResultUI result, boolean hovered)
    {
        if (result.getResult() != null)
        {
            result.getResult().hoveredProperty().set(hovered);
        }
        if (hovered)
        {
            myModel.getHoveredResult().set(result.getResult());
        }
        else
        {
            myModel.getHoveredResult().set(null);
        }
    }

    /** Creates the center pane. */
    private void createUI()
    {
        setPadding(new Insets(5, 0, 0, 0));
        setCenter(createResultList());
    }

    /**
     * Makes the changes to the results list and ensures it is on an fx thread.
     *
     * @param change The change event.
     */
    private void onResultsChanged(Change<? extends SearchResult> change)
    {
        FXUtilities.runOnFXThread(() ->
        {
            while (change.next())
            {
                if (change.wasAdded())
                {
                    myResultsOnFxThread.addAll(change.getAddedSubList());
                }

                if (change.wasRemoved())
                {
                    myResultsOnFxThread.removeAll(change.getRemoved());
                }

                if (change.wasPermutated())
                {
                    int from = change.getFrom();
                    int to = change.getTo();
                    if (to <= myResultsOnFxThread.size())
                    {
                        List<SearchResult> copy = New.list(myResultsOnFxThread.subList(from, to));
                        for (int oldIndex = from; oldIndex < to; oldIndex++)
                        {
                            int newIndex = change.getPermutation(oldIndex);
                            myResultsOnFxThread.set(newIndex, copy.get(oldIndex - from));
                        }
                    }
                }
            }
        });
    }
}
