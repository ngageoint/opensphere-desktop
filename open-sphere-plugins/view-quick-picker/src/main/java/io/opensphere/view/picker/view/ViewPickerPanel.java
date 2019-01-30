package io.opensphere.view.picker.view;

import java.util.List;
import java.util.stream.Collectors;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.view.picker.model.ViewPickerModel;
import javafx.collections.ListChangeListener.Change;
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
    public ViewPickerPanel(Toolbox toolbox, ViewPickerModel model)
    {
        super(10);
        myToolbox = toolbox;
        myModel = model;

        myModel.getViews().addListener(this::viewsChanged);
    }

    /**
     * An event handler used to react to changes in the underlying model.
     * 
     * @param c the change propagated from the underlying model.l
     */
    private void viewsChanged(Change<? extends ViewBookmark> c)
    {
        FXUtilities.runOnFXThread(() ->
        {
            while (c.next())
            {
                if (c.wasAdded())
                {
                    c.getAddedSubList().stream().map(v -> new ViewPickerButton(myToolbox, v)).forEach(this.getChildren()::add);
                }
                if (c.wasRemoved())
                {
                    List<? extends ViewBookmark> removedItems = c.getRemoved();
                    getChildren().removeAll(getChildren().stream()
                            .filter(child -> child instanceof ViewPickerButton
                                    && removedItems.contains(((ViewPickerButton)child).getBookmark()))
                            .collect(Collectors.toList()));
                }
            }
        });
    }
}
