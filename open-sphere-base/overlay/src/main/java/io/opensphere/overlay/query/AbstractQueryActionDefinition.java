package io.opensphere.overlay.query;

import javax.swing.Icon;

import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

/**
 * An abstract base class for defining a query action.
 */
public class AbstractQueryActionDefinition
{
    /** The icon used for the query action. */
    private final ObjectProperty<Icon> myIconProperty;

    /** The (optional) icon to use when the action is selected. */
    private final ObjectProperty<Icon> mySelectedIconProperty;

    /** The (optional) icon to use when the action is rolled over. */
    private final ObjectProperty<Icon> myRolloverIconProperty;

    /** The label displayed alongside the icon. */
    private final StringProperty myLabelProperty;

    /**
     * Creates a new definition using the supplied default values.
     * 
     * @param label the label to store in the definition.
     * @param icon the icon to store in the definition.
     */
    public AbstractQueryActionDefinition(Icon icon, String label)
    {
        super();
        myIconProperty = new ConcurrentObjectProperty<>(icon);
        myRolloverIconProperty = new ConcurrentObjectProperty<>();
        mySelectedIconProperty = new ConcurrentObjectProperty<>();
        myLabelProperty = new ConcurrentStringProperty(label);
    }

    /**
     * Gets the value of the {@link #myRolloverIconProperty} field.
     *
     * @return the value of the myRolloverIconProperty field.
     */
    public ObjectProperty<Icon> rolloverIconProperty()
    {
        return myRolloverIconProperty;
    }

    /**
     * Gets the value of the {@link #mySelectedIconProperty} field.
     *
     * @return the value stored in the {@link #mySelectedIconProperty} field.
     */
    public ObjectProperty<Icon> selectedIconProperty()
    {
        return mySelectedIconProperty;
    }

    /**
     * Gets the value of the {@link #myIconProperty} field.
     *
     * @return the value stored in the {@link #myIconProperty} field.
     */
    public ObjectProperty<Icon> iconProperty()
    {
        return myIconProperty;
    }

    /**
     * Gets the value of the {@link #myLabelProperty} field.
     *
     * @return the value stored in the {@link #myLabelProperty} field.
     */
    public StringProperty labelProperty()
    {
        return myLabelProperty;
    }
}
