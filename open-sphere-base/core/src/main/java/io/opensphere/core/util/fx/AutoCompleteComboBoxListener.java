package io.opensphere.core.util.fx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Enables a javafx {@link ComboBox} to be auto completed as user types.
 *
 * @param <T> The type in the combo box.
 */
public class AutoCompleteComboBoxListener<T> implements EventHandler<KeyEvent>
{
    /**
     * The caret position.
     */
    private int myCaretPos;

    /**
     * The combo box.
     */
    private final ComboBox<T> myComboBox;

    /**
     * The data in the list of the combo box.
     */
    private final ObservableList<T> myData;

    /**
     * Indicates if the caret needs to be moved.
     */
    private boolean myMoveToCaretPos;

    /**
     * Constructs a new auto complete combo box.
     *
     * @param comboBox The combo box to make into an auto complete combo box.
     */
    public AutoCompleteComboBoxListener(final ComboBox<T> comboBox)
    {
        myComboBox = comboBox;
        myData = myComboBox.getItems();

        T value = myComboBox.getValue();
        myComboBox.setEditable(true);
        myComboBox.setValue(value);
        myComboBox.setOnKeyPressed(e -> comboBox.hide());
        myComboBox.setOnKeyReleased(AutoCompleteComboBoxListener.this);
    }

    @Override
    public void handle(KeyEvent event)
    {
        if (event.getCode() == KeyCode.UP)
        {
            myCaretPos = -1;
            moveCaret(myComboBox.getEditor().getText().length());
            return;
        }
        else if (event.getCode() == KeyCode.DOWN)
        {
            if (!myComboBox.isShowing())
            {
                myComboBox.show();
            }
            myCaretPos = -1;
            moveCaret(myComboBox.getEditor().getText().length());
            return;
        }
        else if (event.getCode() == KeyCode.BACK_SPACE)
        {
            myMoveToCaretPos = true;
            myCaretPos = myComboBox.getEditor().getCaretPosition();
        }
        else if (event.getCode() == KeyCode.DELETE)
        {
            myMoveToCaretPos = true;
            myCaretPos = myComboBox.getEditor().getCaretPosition();
        }

        if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT || event.isControlDown()
                || event.getCode() == KeyCode.HOME || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB)
        {
            return;
        }

        ObservableList<T> list = FXCollections.observableArrayList();
        String txt = myComboBox.getEditor().getText();
        myData.stream().filter(t -> containsIgnoreCase(t.toString(), txt)).forEach(t -> list.add(t));

        myComboBox.setItems(list);
        myComboBox.getEditor().setText(txt);
        if (!myMoveToCaretPos)
        {
            myCaretPos = -1;
        }
        moveCaret(txt.length());
        if (!list.isEmpty())
        {
            myComboBox.show();
        }
    }

    /**
     * Case-insensitive "contains" method.
     *
     * @param s a String
     * @param p a prefix
     * @return true iff <i>p</i> is a case-insensitive part of <i>s</i>
     */
    private static boolean containsIgnoreCase(String s, String p)
    {
        return s.toLowerCase().contains(p.toLowerCase());
    }

    /**
     * Moves the caret.
     *
     * @param textLength The length of the text.
     */
    private void moveCaret(int textLength)
    {
        if (myCaretPos == -1)
        {
            myComboBox.getEditor().positionCaret(textLength);
        }
        else
        {
            myComboBox.getEditor().positionCaret(myCaretPos);
        }
        myMoveToCaretPos = false;
    }
}
