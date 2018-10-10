package io.opensphere.core.util.fx;

import org.apache.commons.lang3.StringUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 * Enables a javafx {@link ComboBox} to be auto completed as user types.
 *
 * @see AutoCompleteComboBoxListener
 */
public class NewAutoCompleteComboBoxListener implements EventHandler<KeyEvent>
{
    /** The original data. */
    private ObservableList<? extends Object> myOriginalData;

    /**
     * Updates a ComboBox to handle KeyPressed and KeyReleased events using this
     * Listener.
     *
     * @param comboBox the box
     */
    public void setupComboBox(final ComboBox<? extends Object> comboBox)
    {
        comboBox.setEditable(true);
        comboBox.setOnKeyPressed(evt -> comboBox.hide());
        comboBox.setOnKeyReleased(this);

        myOriginalData = comboBox.getItems();
    }

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
        TextField editor = source.getEditor();
        String txt = editor.getText();

        int caretPosition = -1;
        switch (event.getCode())
        {
            case BACK_SPACE:
            case DELETE:
                caretPosition = txt.length();
                break;
            case DOWN:
                show(source);
            case UP:
                moveCaret(editor, txt.length(), caretPosition);
            default:
                return;
        }

        ObservableList<Object> list = FXCollections.observableArrayList();
        myOriginalData.stream().filter(item -> StringUtils.containsIgnoreCase(item.toString(), txt))
        .forEach(item -> list.add(item));

        source.setItems(list);
        editor.setText(txt);

        moveCaret(editor, txt.length(), caretPosition);

        if (!list.isEmpty())
        {
            show(source);
        }
    }

    /**
     * Moves the caret.
     *
     * @param editor the text editot
     * @param textLength the length of the text
     * @param caretPosition where to position the caret in the editor; the end
     *            if -1
     */
    private void moveCaret(TextField editor, int textLength, int caretPosition)
    {
        editor.positionCaret(caretPosition == -1 ? textLength : caretPosition);
    }

    /**
     * Makes a ComboBox visible if it isn't already.
     *
     * @param source the box
     */
    private void show(ComboBox<?> source)
    {
        if (!source.isShowing())
        {
            source.show();
        }
    }
}
