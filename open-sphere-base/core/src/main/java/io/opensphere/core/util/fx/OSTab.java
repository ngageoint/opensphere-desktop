package io.opensphere.core.util.fx;

import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

/**
 *
 */
public class OSTab extends AnchorPane
{
    private final StringProperty myTextProperty = new ConcurrentStringProperty();

    private final ObjectProperty<Node> myGraphicProperty = new ConcurrentObjectProperty<>();

    private final ObjectProperty<Side> myGraphicLocationProperty = new ConcurrentObjectProperty<>();

    private final BooleanProperty myEditableProperty = new ConcurrentBooleanProperty(false);

    private final BooleanProperty myClosableProperty = new ConcurrentBooleanProperty(true);

    private final ObjectProperty<Node> myContentProperty = new ConcurrentObjectProperty<>();

    /**
     *
     */
    public OSTab()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     *
     */
    public OSTab(String tabName)
    {
        myTextProperty.set(tabName);
    }

    /**
     * Gets the value of the {@link #myTextProperty} field.
     *
     * @return the value stored in the {@link #myTextProperty} field.
     */
    public StringProperty textProperty()
    {
        return myTextProperty;
    }

    /**
     * Gets the value of the {@link #myGraphicProperty} field.
     *
     * @return the value stored in the {@link #myGraphicProperty} field.
     */
    public ObjectProperty<Node> graphicProperty()
    {
        return myGraphicProperty;
    }

    /**
     * Gets the value of the {@link #myGraphicLocationProperty} field.
     *
     * @return the value stored in the {@link #myGraphicLocationProperty} field.
     */
    public ObjectProperty<Side> graphicLocationProperty()
    {
        return myGraphicLocationProperty;
    }

    /**
     * Gets the value of the {@link #myEditableProperty} field.
     *
     * @return the value stored in the {@link #myEditableProperty} field.
     */
    public BooleanProperty editableProperty()
    {
        return myEditableProperty;
    }

    /**
     * Gets the value of the {@link #myClosableProperty} field.
     *
     * @return the value stored in the {@link #myClosableProperty} field.
     */
    public BooleanProperty closableProperty()
    {
        return myClosableProperty;
    }

    /**
     * Gets the value of the {@link #myContentProperty} field.
     *
     * @return the value stored in the {@link #myContentProperty} field.
     */
    public ObjectProperty<Node> contentProperty()
    {
        return myContentProperty;
    }
}
