package io.opensphere.core.util.javafx.input.tags;

import java.util.function.Consumer;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.fx.FxIcons;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/**
 * A simple component containing text and a button atop a rounded colored
 * background.
 */
public class Tag extends BorderPane
{
    /** The default color applied to the tag. */
    private static final Color DEFAULT_COLOR = Color.valueOf("#8D84BD");

    /** The model of the tag instance. */
    private final TagModel myModel;

    /** The label in which the text is rendered. */
    private final Label myText;

    /** The label used as a button for the tag. */
    private final Label myButton;

    /** Creates an empty tag with the default color and no action. */
    public Tag()
    {
        this(null);
    }

    /**
     * Creates a tag with the supplied text, the default color and no action.
     *
     * @param text the text to display in the tag.
     */
    public Tag(String text)
    {
        this(text, DEFAULT_COLOR);
    }

    /**
     * Creates a tag with the supplied text and color, and no action.
     *
     * @param text the text to display in the tag.
     * @param color the background color of the tag.
     */
    public Tag(String text, Color color)
    {
        this(text, color, null);
    }

    /**
     * Creates a tag with the supplied text, color and action.
     *
     * @param text the text to display in the tag.
     * @param color the background color of the tag.
     * @param action the procedure called when the tag's button is pressed.
     */
    public Tag(String text, Color color, Consumer<Tag> action)
    {
        myModel = new TagModel(text, color, action);
        setMaxHeight(USE_PREF_SIZE);

        myText = new Label();
        myText.setMinWidth(USE_PREF_SIZE);
        myText.textProperty().bind(myModel.valueProperty());
        myText.setPadding(new Insets(0, 0, 0, 5));

        myButton = FxIcons.createClearIcon(AwesomeIconSolid.TIMES_CIRCLE, Color.WHITE, 10);
        myButton.setPadding(new Insets(0, 5, 0, 0));
        myButton.setOnMouseClicked(e -> fireAction());

        backgroundProperty().bind(myModel.backgroundProperty());
        myText.textFillProperty().bind(myModel.foregroundProperty());
        myButton.textFillProperty().bind(myModel.foregroundProperty());

        BorderPane.setAlignment(myText, Pos.CENTER);
        BorderPane.setAlignment(myButton, Pos.CENTER);

        BorderPane.setMargin(myText, new Insets(10, 5, 10, 5));
        BorderPane.setMargin(myButton, new Insets(0, 5, 0, 0));

        setCenter(myText);
        setRight(myButton);
    }

    /** Invokes the action property for the tag, if defined. */
    private void fireAction()
    {
        if (myModel.actionProperty().get() != null)
        {
            myModel.actionProperty().get().accept(this);
        }
    }

    /**
     * Gets the textual value of the field.
     *
     * @return the textual value of the field.
     */
    public StringProperty textProperty()
    {
        return myModel.valueProperty();
    }

    /**
     * Gets the property in which the color is managed.
     *
     * @return the property in which the color is managed.
     */
    public ObjectProperty<Color> colorProperty()
    {
        return myModel.colorProperty();
    }

    /**
     * Gets the property in which the action is managed.
     *
     * @return the property in which the action is managed.
     */
    public ObjectProperty<Consumer<Tag>> actionProperty()
    {
        return myModel.actionProperty();
    }

    /**
     * Gets the value of the {@link #myModel} field.
     *
     * @return the value stored in the {@link #myModel} field.
     */
    public TagModel getModel()
    {
        return myModel;
    }

    /**
     * Gets the unique identifier assigned to the tag instance.
     *
     * @return the unique identifier assigned to the tag instance.
     */
    public String getTagId()
    {
        return myModel.getId();
    }
}
