package io.opensphere.core.util.fx;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyEvent;

/**
 * A combo-box listener that will scroll to the first matching item in the
 * combobox's pulldown list, and if the user keeps typing, update the selection.
 * When the user hits enter on a partial selection, the editor's contents are
 * replaced with the selected entry, and the pulldown is closed.
 */
public class AutoSelectComboBoxListener implements EventHandler<KeyEvent>
{

    /**
     * Updates a ComboBox to handle KeyPressed and KeyReleased events using this
     * Listener.
     *
     * @param comboBox the box
     */
    public void registerComboBox(final ComboBox<? extends Object> comboBox)
    {
        comboBox.setEditable(true);
        comboBox.setOnKeyPressed(evt -> comboBox.hide());
        comboBox.setOnKeyReleased(this);
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.event.EventHandler#handle(javafx.event.Event)
     */
    @Override
    public void handle(KeyEvent event)
    {
        Object eventSource = event.getSource();
        if (!(eventSource instanceof ComboBox<?>))
        {
            return;
        }

        @SuppressWarnings("unchecked")
        ComboBox<Object> source = (ComboBox<Object>)eventSource;
        ObservableList<? extends Object> items = source.getItems();
        TextField editor = source.getEditor();
        String editorContents = editor.getText();

        int caretPosition = -1;
        switch (event.getCode())
        {
            case BACK_SPACE:
            case DELETE:
                caretPosition = editorContents.length();
                break;
            case DOWN:
                source.show();
                return;
            case UP:
                moveCaret(editor, editorContents.length(), caretPosition);
                return;
            case ENTER:
                Set<? extends Object> matchingEntries = items.stream()
                        .filter(item -> StringUtils.startsWithIgnoreCase(item.toString(), editorContents))
                        .collect(Collectors.toSet());
                if (matchingEntries.size() == 1)
                {
                    String fullEntry = (String)matchingEntries.iterator().next();
                    editor.setText(fullEntry);
                    moveCaret(editor, fullEntry.length(), -1);
                    source.hide();
                    return;
                }
            default:
                // ignore all other cases:
                break;
        }

        // ensure that when the user starts typing, the pulldown is visible:
        source.show();
        Object matchingEntry = items.stream().filter(item -> StringUtils.startsWithIgnoreCase(item.toString(), editorContents))
                .findFirst().orElse(null);
        if (matchingEntry != null)
        {
            ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>)source.getSkin();
            int index = items.indexOf(matchingEntry);
            source.getSelectionModel().clearAndSelect(index);
            ((ListView<?>)skin.getPopupContent()).scrollTo(index);
        }
        editor.setText(editorContents);
        moveCaret(editor, editorContents.length(), caretPosition);
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
