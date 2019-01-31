package io.opensphere.view.picker.view;

import java.util.List;
import java.util.stream.Collectors;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.view.picker.model.ViewPickerModel;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

/** The base panel in which the view picker is rendered. */
public class ViewPickerPanel extends VBox
{
    /** The model in which data is stored. */
    private final ViewPickerModel myModel;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Creates a new picker panel.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param model the model in which data is stored.
     */
    public ViewPickerPanel(final Toolbox toolbox, final ViewPickerModel model)
    {
        super(10);
        setPadding(new Insets(10, 0, 10, 0));
        myToolbox = toolbox;
        myModel = model;

        myModel.getViews().addListener(this::viewsChanged);
    }

    /**
     * An event handler used to react to changes in the underlying model.
     *
     * @param change the change propagated from the underlying model.
     */
    private void viewsChanged(final Change<? extends ViewBookmark> change)
    {
        FXUtilities.runOnFXThread(() ->
        {
            while (change.next())
            {
                if (change.wasAdded())
                {
                    change.getAddedSubList().stream().map(v -> new ViewPickerButton(myToolbox, v)).forEach(getChildren()::add);
                }
                if (change.wasRemoved())
                {
                    final List<? extends ViewBookmark> removedItems = change.getRemoved();
                    getChildren().removeAll(getChildren().stream()
                            .filter(child -> child instanceof ViewPickerButton
                                    && removedItems.contains(((ViewPickerButton)child).getBookmark()))
                            .collect(Collectors.toList()));
                }
            }
        });
    }
}
