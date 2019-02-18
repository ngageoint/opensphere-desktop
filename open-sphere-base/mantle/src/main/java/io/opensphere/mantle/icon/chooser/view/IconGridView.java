package io.opensphere.mantle.icon.chooser.view;

import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.GridView;

import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.chooser.model.IconChooserModel;
import io.opensphere.mantle.icon.chooser.model.IconModel;

/**
 * A grid view panel in which all icons for a given set are rendered. Paired
 * with the {@link IconGridCell} for rendering.
 */
public class IconGridView extends AnchorPane
{
    /** The grid in which the icons are rendered. */
    private final GridView<IconRecord> myGrid;

    /** The predicate used to show all icons in the grid. */
    private final Predicate<IconRecord> myPredicate;

    /** The model in which state is maintained. */
    private final IconModel myModel;

    /** Display state of the grid view. tied to the empty state of the items. */
    private final BooleanProperty myDisplayProperty;

    /** The icon chooser model backing the grid view. */
    private final IconChooserModel myIconChooserModel;

    /**
     * Keeps track of the previously selected cell for rendering purposes.
     */
    private final ObjectProperty<IconGridCell> myPreviousSelectedCellProperty;

    /**
     * Creates a new grid view bound to the supplied model, and using the
     * supplied predicate to define the initial set of displayed icons.
     *
     * @param model the model to which to bind.
     * @param predicate the predicate used to define the set of included icons.
     */
    public IconGridView(final IconModel model, final Predicate<IconRecord> predicate)
    {
        myModel = model;

        myIconChooserModel = myModel.getModel();

        myGrid = new GridView<>();
        myGrid.setHorizontalCellSpacing(4);
        myGrid.setVerticalCellSpacing(4);
        myGrid.cellWidthProperty().bind(myModel.tileWidthProperty());
        myGrid.cellHeightProperty().bind(myModel.tileWidthProperty());
        myPreviousSelectedCellProperty = new SimpleObjectProperty<>();
        myGrid.setCellFactory(param -> new IconGridCell(model, myPreviousSelectedCellProperty));

        myPredicate = predicate;

        myModel.searchTextProperty().addListener((obs, ov, nv) ->
        {
            final ObservableList<IconRecord> filteredRecords = myIconChooserModel.getIconRecords(myPredicate).filtered(r ->
            {
                if (StringUtils.isEmpty(myModel.searchTextProperty().get()))
                {
                    return true;
                }

                return r.nameProperty().get().contains(myModel.searchTextProperty().get());
            });

            myGrid.itemsProperty().set(filteredRecords);
        });

        myGrid.itemsProperty().set(myIconChooserModel.getIconRecords(myPredicate));

        getChildren().add(myGrid);
        AnchorPane.setRightAnchor(myGrid, Double.valueOf(0.0));
        AnchorPane.setTopAnchor(myGrid, Double.valueOf(0.0));
        AnchorPane.setLeftAnchor(myGrid, Double.valueOf(0.0));
        AnchorPane.setBottomAnchor(myGrid, Double.valueOf(0.0));

        myDisplayProperty = new ConcurrentBooleanProperty(true);

        Platform.runLater(() -> requestFocus());
    }

    /**
     * Gets the value of the {@link #myDisplayProperty} field.
     *
     * @return the value stored in the {@link #myDisplayProperty} field.
     */
    public BooleanProperty displayProperty()
    {
        return myDisplayProperty;
    }
}
