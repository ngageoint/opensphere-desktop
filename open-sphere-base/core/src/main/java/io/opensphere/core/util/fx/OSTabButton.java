package io.opensphere.core.util.fx;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;

/**
 *
 */
public class OSTabButton extends ToggleButton
{
    private final StringProperty myBoundTextProperty;

    private final TextField myTextField = new TextField();

    /**
     * @param text
     * @param graphic
     */
    public OSTabButton(StringProperty boundTextProperty, Node graphic)
    {
        super(boundTextProperty.get());
        setText(boundTextProperty.get());
        myBoundTextProperty = boundTextProperty;
        initialize();
    }

    /**
     * @param text
     */
    public OSTabButton(StringProperty boundTextProperty)
    {
        this(boundTextProperty, null);
    }

    private void initialize()
    {
        textProperty().bind(myBoundTextProperty);
        myTextField.setPadding(new Insets(0));

        setOnMouseClicked(e ->
        {
            if (e.getClickCount() >= 2)
            {
                editText();
            }
        });

        myTextField.setOnKeyPressed(e ->
        {
            if (e.getCode() == KeyCode.ESCAPE)
            {
                cancelEdit();
            }
        });

        myTextField.focusedProperty().addListener((obs, ov, nv) ->
        {
            if (!nv)
            {
                cancelEdit();
            }
        });

        myTextField.setOnAction(e -> saveEdit());
    }

    private void saveEdit()
    {
        setGraphic(null);
        textProperty().bind(myBoundTextProperty);
        myBoundTextProperty.set(myTextField.getText());
    }

    private void cancelEdit()
    {
        myTextField.textProperty().set("");
        setGraphic(null);
        textProperty().bind(myBoundTextProperty);
    }

    public void editText()
    {
        myTextField.setText(myBoundTextProperty.get());
        setGraphic(myTextField);
        textProperty().unbind();
        setText("");
        myTextField.selectAll();
        myTextField.requestFocus();
        myTextField.maxWidthProperty().set(widthProperty().get());
    }
}
