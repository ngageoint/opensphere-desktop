package io.opensphere.featureactions.editor.model;

import io.opensphere.featureactions.model.FeatureAction;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

/**
 * Model class representing a single feature action within the feature action
 * editor.
 */
public class SimpleFeatureAction
{
    /**
     * The available columns to create a feature action for.
     */
    private final ObservableList<String> myAvailableColumns = FXCollections.observableArrayList();

    /**
     * The color to use for this action.
     */
    private final ObjectProperty<Color> myColorProperty = new SimpleObjectProperty<>();

    /**
     * The selected column.
     */
    private final StringProperty myColumn = new SimpleStringProperty();

    /**
     * The feature action we are editing.
     */
    private final FeatureAction myFeatureAction;

    /**
     * The id of the icon to use for the action.
     */
    private final LongProperty myIconId = new SimpleLongProperty();

    /**
     * The maximum value if a range.
     */
    private final StringProperty myMaximumValue = new SimpleStringProperty();

    /**
     * The minimum value if a range.
     */
    private final StringProperty myMinimumValue = new SimpleStringProperty();

    /**
     * The selected criteria options.
     */
    private final ObjectProperty<CriteriaOptions> myOption = new SimpleObjectProperty<>(CriteriaOptions.VALUE);

    /**
     * The criteria options.
     */
    private final ObservableList<CriteriaOptions> myOptions = FXCollections.observableArrayList(CriteriaOptions.values());

    /**
     * The value.
     */
    private final StringProperty myValue = new SimpleStringProperty();

    /**
     * Constructs a new simple feature action.
     *
     * @param action The action to edit.
     */
    public SimpleFeatureAction(FeatureAction action)
    {
        myFeatureAction = action;
    }

    /**
     * The property of the color to use for this action.
     *
     * @return The color property.
     */
    public ObjectProperty<Color> colorProperty()
    {
        return myColorProperty;
    }

    /**
     * The available columns to create a feature action for.
     *
     * @return the availableColumns.
     */
    public ObservableList<String> getAvailableColumns()
    {
        return myAvailableColumns;
    }

    /**
     * Gets the color to use for this action.
     *
     * @return The color for the action.
     */
    public Color getColor()
    {
        return myColorProperty.get();
    }

    /**
     * The currently selected column.
     *
     * @return the column
     */
    public StringProperty getColumn()
    {
        return myColumn;
    }

    /**
     * Gets the feature action we are editing.
     *
     * @return the featureAction.
     */
    public FeatureAction getFeatureAction()
    {
        return myFeatureAction;
    }

    /**
     * Gets the id of the icon to use for the action.
     *
     * @return The icon id.
     */
    public long getIconId()
    {
        return myIconId.get();
    }

    /**
     * The maximum value if a range.
     *
     * @return the maximumValue.
     */
    public StringProperty getMaximumValue()
    {
        return myMaximumValue;
    }

    /**
     * The minimum value if a range.
     *
     * @return the minimumValue.
     */
    public StringProperty getMinimumValue()
    {
        return myMinimumValue;
    }

    /**
     * Gets the selected criteria option.
     *
     * @return the option.
     */
    public ObjectProperty<CriteriaOptions> getOption()
    {
        return myOption;
    }

    /**
     * The available criteria options.
     *
     * @return the options.
     */
    public ObservableList<CriteriaOptions> getOptions()
    {
        return myOptions;
    }

    /**
     * Gets the value property if value criteria options.
     *
     * @return the value.
     */
    public StringProperty getValue()
    {
        return myValue;
    }

    /**
     * The property of the icon id used for the action.
     *
     * @return The icon id property.
     */
    public LongProperty iconIdProperty()
    {
        return myIconId;
    }

    /**
     * Sets the color to use for this action.
     *
     * @param color The color for the action.
     */
    public void setColor(Color color)
    {
        myColorProperty.set(color);
    }

    /**
     * Sets the id of the icon to use for the action.
     *
     * @param iconId The icon id.
     */
    public void setIconId(long iconId)
    {
        myIconId.set(iconId);
    }
}
