package io.opensphere.core.util.fx;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyEvent;

/**
 * An extension to the combo box that allows the user to type and selects the
 * first matching item. If the user then presses enter, the item is selected,
 * the full text of the item is placed into the editor, and a selected event is
 * fired.
 *
 * @param <T> the type of data stored in the combo box.
 */
public class AutoSelectComboBox<T> extends ComboBox<T>
{
    /**
     * Creates a new combobox. All auto-selectable combo boxes are editable by
     * default.
     */
    public AutoSelectComboBox()
    {
        initialize();
    }

    /**
     * Creates a new combobox bound to the supplied observable list. All
     * auto-selectable combo boxes are editable by default
     *
     * @param items the items to show in the list.
     */
    public AutoSelectComboBox(ObservableList<T> items)
    {
        super(items);
        initialize();
    }

    /** Initializes the combobox to be editable and registers all listeners. */
    private void initialize()
    {
        setEditable(true);
        setOnKeyReleased(evt -> handleKeyRelease(evt));
    }

    /**
     * Handles the release of the key described in the event.
     *
     * @param event the event describing the key release.
     */
    public void handleKeyRelease(KeyEvent event)
    {
        Object eventSource = event.getSource();
        if (eventSource != this)
        {
            return;
        }

        ObservableList<T> items = getItems();

        if (isEditable())
        {
            TextField editor = getEditor();
            String editorContents = editor.getText();

            int caretPosition = -1;
            switch (event.getCode())
            {
                case BACK_SPACE:
                case DELETE:
                    caretPosition = editorContents.length();
                    break;
                case DOWN:
                    show();
                    return;
                case UP:
                    moveCaret(editor, editorContents.length(), caretPosition);
                    return;
                case ENTER:
                    String targetItem = null;
                    if (getSelectionModel().getSelectedIndex() >= 0)
                    {
                        targetItem = getSelectionModel().getSelectedItem().toString();
                    }
                    else
                    {
                        List<? extends Object> matchingEntries = items.stream()
                                .filter(item -> StringUtils.startsWithIgnoreCase(item.toString(), editorContents))
                                .collect(Collectors.toList());
                        if (matchingEntries.size() >= 1)
                        {
                            targetItem = (String)matchingEntries.iterator().next();
                        }
                    }
                    
                    if(targetItem != null) 
                    {
                        editor.setText(targetItem);
                        moveCaret(editor, targetItem.length(), -1);
                        hide();
                        return;
                    }
                    break;
                default:
                    // ignore all other cases:
                    break;
            }

            // ensure that when the user starts typing, the pulldown is visible:
            show();
            Object matchingEntry = items.stream()
                    .filter(item -> StringUtils.startsWithIgnoreCase(item.toString(), editorContents)).findFirst().orElse(null);
            if (matchingEntry != null)
            {
                ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>)getSkin();
                int index = items.indexOf(matchingEntry);
                getSelectionModel().clearAndSelect(index);
                ((ListView<?>)skin.getPopupContent()).scrollTo(index);
            }
            editor.setText(editorContents);
            moveCaret(editor, editorContents.length(), caretPosition);
        }
        else
        {
            String character = event.getCharacter();
            switch (event.getCode())
            {
                case DOWN:
                    show();
                    return;
                default:
                    // ignore all other cases:
                    break;
            }
            // ensure that when the user starts typing, the pulldown is visible:
            show();
            Object matchingEntry = items.stream().filter(item -> StringUtils.startsWithIgnoreCase(item.toString(), character))
                    .findFirst().orElse(null);
            if (matchingEntry != null)
            {
                ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>)getSkin();
                int index = items.indexOf(matchingEntry);
                ((ListView<?>)skin.getPopupContent()).scrollTo(index);
            }
        }
    }

    /**
     * Moves the caret.
     *
     * @param editor the text editor
     * @param textLength the length of the text
     * @param caretPosition where to position the caret in the editor; the end
     *            if -1
     */
    private void moveCaret(TextField editor, int textLength, int caretPosition)
    {
        editor.positionCaret(caretPosition == -1 ? textLength : caretPosition);
    }
}
