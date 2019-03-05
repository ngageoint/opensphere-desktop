package io.opensphere.core.util.javafx.input.tags;

import io.opensphere.core.util.collections.observable.MappedObservableList;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** A field of tags. Tags can be dismissed and added. */
public class TagField extends VBox
{
    /** The model in which the field's state is maintained. */
    private final TagFieldModel myModel = new TagFieldModel();

    /** The field in which new tags are created. */
    private final TextField myEditField;

    /** The box in which tags are placed. */
    private final VBox myTagBox;

    /** Creates a new tag field. */
    public TagField()
    {
        super();

        myModel.tagColorProperty().set(Color.BLUEVIOLET);

        scaleShapeProperty().set(false);
        myTagBox = new VBox();
        myTagBox.setPadding(Insets.EMPTY);
        myTagBox.scaleShapeProperty().set(false);

        myEditField = new TextField();
        myEditField.visibleProperty().set(true);
        myEditField.onActionProperty().set(e -> createTag(myEditField.getText()));
        myEditField.setOnKeyTyped(e -> processKey(e));

        myModel.getTags().addListener((ListChangeListener<? super Tag>)c ->
        {
            while (c.next())
            {
                if (c.wasAdded())
                {
                    c.getAddedSubList().forEach(this::addTag);
                }
                if (c.wasRemoved())
                {
                    c.getRemoved().forEach(myTagBox.getChildren()::remove);
                }
            }
        });

        VBox.setMargin(myEditField, new Insets(5, 0, 5, 5));

        setOnMouseClicked(e -> invokeEditor(e));

        getChildren().add(myTagBox);
        getChildren().add(myEditField);
    }

    /**
     * Adds the supplied tag to the field. Overrides the action property in the
     * supplied tag to allow removal.
     *
     * @param tag the tag to add.
     */
    public void addTag(final Tag tag)
    {
        tag.colorProperty().bind(myModel.tagColorProperty());
        tag.actionProperty().set(this::removeTag);
        myTagBox.getChildren().add(tag);
    }

    /**
     * Processes the supplied key event.
     *
     * @param e the event describing the key action.
     */
    private void processKey(final KeyEvent e)
    {
        if (e.getCode() == KeyCode.ESCAPE)
        {
            myEditField.setText("");
            myEditField.setVisible(false);
        }
    }

    /**
     * Creates a new tag using the supplied text.
     *
     * @param text the text with which to create the tag.
     */
    private void createTag(final String text)
    {
        if (myModel.editableProperty().get())
        {
            addNewTag(text);
        }
        myEditField.setText("");
        myEditField.setVisible(true);
    }

    /**
     * Creates a new tag, and adds the tag to the field.
     *
     * @param text the text with which to create the new tag.
     */
    private void addNewTag(final String text)
    {
        final Tag tag = new Tag(text);
        tag.actionProperty().set(this::removeTag);
        myModel.getTags().add(tag);
    }

    /**
     * Displays the new tag editor if the field is editable.
     *
     * @param e the mouse event that triggered the editor's invocation.
     */
    private void invokeEditor(final MouseEvent e)
    {
        if (myModel.editableProperty().get())
        {
            final PickResult pickResult = e.getPickResult();
            if (pickResult.getIntersectedNode().equals(this))
            {
                // don't show the editor if the user is attempting to dismiss a
                // tag.
                myEditField.visibleProperty().set(true);
                myEditField.requestFocus();
            }
        }
    }

    /**
     * Gets the list of tags defined within the field.
     *
     * @return the list of tags defined within the field.
     */
    public ObservableList<Tag> getTags()
    {
        return myModel.getTags();
    }

    /**
     * Gets the list of tag values defined within the field.
     *
     * @return the list of tag values defined within the field.
     */
    public ObservableList<String> getTagValues()
    {
        return new MappedObservableList<>(myModel.getTags(), t -> t.textProperty().get());
    }

    /**
     * Removes the supplied tag from the field.
     *
     * @param tag the tag to remove.
     */
    public void removeTag(final Tag tag)
    {
        myModel.getTags().remove(tag);
    }

    /**
     * Gets the color of the tags within the field.
     *
     * @return the color of the tags within the field.
     * @see io.opensphere.core.util.javafx.input.tags.TagFieldModel#tagColorProperty()
     */
    public ObjectProperty<Color> tagColorProperty()
    {
        return myModel.tagColorProperty();
    }
}
