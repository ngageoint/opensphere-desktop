package io.opensphere.mantle.icon.chooser.view;

import java.util.function.Predicate;

import org.controlsfx.control.GridView;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.chooser.model.IconModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.layout.AnchorPane;

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

    /**
     * Additional predicates used further restrict the set of displayed icons.
     * Used with the {@link #myPredicate} value to create a composite set of
     * predicates.
     */
    private final ObservableSet<Predicate<IconRecord>> myActiveRestrictionPredicates;

    /** The registry from which icons are read. */
    private final IconRegistry myRegistry;

    /** The model in which state is maintained. */
    private final IconModel myModel;

    /**
     * The display state of the grid view. tied to the empty state of the items.
     */
    private final BooleanProperty myDisplayProperty;

    /**
     * Creates a new grid view bound to the supplied model, and using the
     * supplied predicate to define the initial set of displayed icons.
     *
     * @param model the model to which to bind.
     * @param predicate the predicate used to define the set of included icons.
     */
    public IconGridView(IconModel model, Predicate<IconRecord> predicate)
    {
        myModel = model;

        myGrid = new GridView<>();
        myGrid.setHorizontalCellSpacing(4);
        myGrid.setVerticalCellSpacing(4);
        myGrid.cellWidthProperty().bind(myModel.tileWidthProperty());
        myGrid.cellHeightProperty().bind(myModel.tileWidthProperty());
        myGrid.setCellFactory(param -> new IconGridCell(model));

        myRegistry = myModel.getIconRegistry();
        myPredicate = predicate;
        myActiveRestrictionPredicates = FXCollections.observableSet(New.set());

        refresh();
        myActiveRestrictionPredicates.addListener((SetChangeListener.Change<? extends Predicate<IconRecord>> c) -> refresh());
        myModel.searchTextProperty().addListener((obs, ov, nv) ->
        {
            myActiveRestrictionPredicates.clear();
            myActiveRestrictionPredicates.add(r -> r.nameProperty().get().contains(nv));
        });

        getChildren().add(myGrid);
        AnchorPane.setRightAnchor(myGrid, 0.0);
        AnchorPane.setTopAnchor(myGrid, 0.0);
        AnchorPane.setLeftAnchor(myGrid, 0.0);
        AnchorPane.setBottomAnchor(myGrid, 0.0);

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

    /**
     * Creates a composite predicate to determine if the icon matches the group
     * and any search terms entered by the user.
     *
     * @return a composite predicate to determine if the icon matches the group
     *         and any search terms entered by the user.
     */
    private Predicate<IconRecord> composePredicate()
    {
        Predicate<IconRecord> composedPredicate = myPredicate;

        for (Predicate<IconRecord> additionalPredicate : myActiveRestrictionPredicates)
        {
            composedPredicate = composedPredicate.and(additionalPredicate);
        }
        return composedPredicate;
    }

    /**
     * Refreshes the contents of the tab.
     */
    public void refresh()
    {
        IconRecord[] iconRecords = myRegistry.getIconRecords(composePredicate()).stream().toArray(IconRecord[]::new);
        if (!Platform.isFxApplicationThread())
        {
            FXUtilities.runOnFXThread(() ->
            {
                myGrid.itemsProperty().get().clear();
                myGrid.itemsProperty().get().addAll(iconRecords);
            });
        }
        else
        {
            myGrid.itemsProperty().get().clear();
            myGrid.itemsProperty().get().addAll(iconRecords);
        }
    }
}