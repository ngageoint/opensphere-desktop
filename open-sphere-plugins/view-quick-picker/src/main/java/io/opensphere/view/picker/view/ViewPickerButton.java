package io.opensphere.view.picker.view;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewbookmark.util.ViewBookmarkUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/** A button used with the view picker to navigate to a view bookmark. */
public class ViewPickerButton extends BorderPane
{
    /** The button foreground color when the mouse rolls over. */
    private static final Color ROLLOVER_FOREGROUND = FXUtilities.fromAwtColor(IconUtil.DEFAULT_ICON_ROLLOVER);

    /** The default color to use for the button's foreground. */
    private static final Color DEFAULT_FOREGROUND = FXUtilities.fromAwtColor(IconUtil.DEFAULT_ICON_FOREGROUND);

    /** The bookmark to which to navigate when clicked. */
    private final ViewBookmark myBookmark;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Creates a new button bound to the supplied bookmark.
     * 
     * @param toolbox The toolbox through which application state is accessed.
     * @param bookmark The bookmark to which to navigate when clicked.
     */
    public ViewPickerButton(Toolbox toolbox, ViewBookmark bookmark)
    {
        myToolbox = toolbox;
        myBookmark = bookmark;

        Label icon = FxIcons.createClearIcon(AwesomeIconSolid.DOT_CIRCLE, DEFAULT_FOREGROUND, 16);
        icon.onMouseEnteredProperty().set(e -> icon.textFillProperty().set(ROLLOVER_FOREGROUND));
        icon.onMouseExitedProperty().set(e -> icon.textFillProperty().set(DEFAULT_FOREGROUND));
        icon.onMouseClickedProperty().set(
                e -> EventQueueUtilities.runOnEDT(() -> ViewBookmarkUtil.gotoViewWithoutConfirmation(myToolbox, myBookmark)));
        icon.setTooltip(new Tooltip(bookmark.getViewName()));

        setCenter(icon);
        Label text = new Label(bookmark.getViewName());
        text.setAlignment(Pos.CENTER);
        text.onMouseEnteredProperty().set(e -> icon.textFillProperty().set(ROLLOVER_FOREGROUND));
        text.onMouseExitedProperty().set(e -> icon.textFillProperty().set(DEFAULT_FOREGROUND));
        text.onMouseClickedProperty().set(
                e -> EventQueueUtilities.runOnEDT(() -> ViewBookmarkUtil.gotoViewWithoutConfirmation(myToolbox, myBookmark)));
        setBottom(text);

        setAlignment(text, Pos.CENTER);
    }

    /**
     * Gets the value of the {@link #myBookmark} field.
     *
     * @return the value of the myBookmark field.
     */
    public ViewBookmark getBookmark()
    {
        return myBookmark;
    }
}
