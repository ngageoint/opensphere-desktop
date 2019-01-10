package io.opensphere.core.util.javafx.input.tags;

import java.util.UUID;
import java.util.function.Consumer;

import io.opensphere.core.function.Procedure;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * The model component of a tag instance.
 *
 * @see io.opensphere.core.util.javafx.input.tags.Tag
 * @see io.opensphere.core.util.javafx.input.tags.TagField
 */
public class TagModel
{
    /** The default color applied to the tag. */
    public static final Color DEFAULT_COLOR = Color.valueOf("#8D84BD");

    /** The unique ID assigned to the tag model instance. */
    private final String myId;

    /** The value of the tag. */
    private final StringProperty myValueProperty;

    /** The property in which the tag's background color is maintained. */
    private final ObjectProperty<Color> myColorProperty;

    /** The property in which the action is maintained. */
    private final ObjectProperty<Consumer<Tag>> myActionProperty;

    /**
     * The read-only property in which the background is managed. The background
     * is tied directly to the {@link #colorProperty()}, and is changed when the
     * color is changed.
     */
    private final ReadOnlyObjectWrapper<Background> myBackgroundProperty;

    /**
     * The read-only property in which the foreground is managed. The background
     * is tied directly to the {@link #colorProperty()}, and is changed when the
     * color is changed.
     */
    private final ReadOnlyObjectWrapper<Color> myForegroundProperty;

    /** Creates a new tag model instance with no value. */
    public TagModel()
    {
        this(null);
    }

    /**
     * Creates a new tag model instance with the supplied value and a default
     * color.
     *
     * @param value the value with which to initialize the instance, may be
     *            null.
     */
    public TagModel(String value)
    {
        this(value, DEFAULT_COLOR);
    }

    /**
     * Creates a new tag model instance with the supplied value and color.
     *
     * @param value the value with which to initialize the instance, may be
     *            null.
     * @param color the color with which to render the tag instance.
     */
    public TagModel(String value, Color color)
    {
        this(value, color, null);
    }

    /**
     * Creates a new tag model instance with the supplied value, color and
     * action {@link Procedure}.
     *
     * @param value the value with which to initialize the instance, may be
     *            null.
     * @param color the color with which to render the tag instance.
     * @param action the procedure to invoke when the close button on the tag is
     *            clicked.
     */
    public TagModel(String value, Color color, Consumer<Tag> action)
    {
        myId = UUID.randomUUID().toString();
        myValueProperty = new ConcurrentStringProperty(value);
        myActionProperty = new ConcurrentObjectProperty<>(action);

        // no need to initialize these, as they're set by the color property:
        myBackgroundProperty = new ReadOnlyObjectWrapper<>();
        myForegroundProperty = new ReadOnlyObjectWrapper<>();

        myColorProperty = new ConcurrentObjectProperty<>();
        myColorProperty.addListener((obs, ov, nv) ->
        {
            myBackgroundProperty
                    .set(new Background(new BackgroundFill(myColorProperty.get(), new CornerRadii(5), new Insets(5))));
            myForegroundProperty.set(FXUtilities.getTextColor(myColorProperty.get()));
        });
        myColorProperty.set(color);
    }

    /**
     * Gets the value of the {@link #myId} field.
     *
     * @return the value stored in the {@link #myId} field.
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the value of the {@link #myValueProperty} field.
     *
     * @return the value stored in the {@link #myValueProperty} field.
     */
    public StringProperty valueProperty()
    {
        return myValueProperty;
    }

    /**
     * Gets the value of the {@link #myActionProperty} field.
     *
     * @return the value stored in the {@link #myActionProperty} field.
     */
    public ObjectProperty<Consumer<Tag>> actionProperty()
    {
        return myActionProperty;
    }

    /**
     * Gets the value of the {@link #myColorProperty} field.
     *
     * @return the value stored in the {@link #myColorProperty} field.
     */
    public ObjectProperty<Color> colorProperty()
    {
        return myColorProperty;
    }

    /**
     * Gets the value of the {@link #myBackgroundProperty} field.
     *
     * @return the value stored in the {@link #myBackgroundProperty} field.
     */
    public ReadOnlyObjectProperty<Background> backgroundProperty()
    {
        return myBackgroundProperty.getReadOnlyProperty();
    }

    /**
     * Gets the value of the {@link #myForegroundProperty} field.
     *
     * @return the value stored in the {@link #myForegroundProperty} field.
     */
    public ReadOnlyObjectProperty<Color> foregroundProperty()
    {
        return myForegroundProperty.getReadOnlyProperty();
    }
}
