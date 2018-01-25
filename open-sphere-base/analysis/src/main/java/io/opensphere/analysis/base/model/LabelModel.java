package io.opensphere.analysis.base.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

/**
 * Model for label settings.
 */
public class LabelModel
{
    /** The type of label model. */
    private final LabelType myType;

    /** The label color. */
    private final ObjectProperty<Color> myColor;

    /** The label font family. */
    private final StringProperty myFont;

    /** The label font size. */
    private final IntegerProperty mySize;

    /**
     * Constructor.
     *
     * @param type the label model type
     */
    public LabelModel(LabelType type)
    {
        myType = type;
        myColor = new SimpleObjectProperty<>(this, myType + "Color", null);
        myFont = new SimpleStringProperty(this, myType + "Font");
        mySize = new SimpleIntegerProperty(this, myType + "Size");
    }

    /**
     * Gets the type.
     *
     * @return my type
     */
    public LabelType getType()
    {
        return myType;
    }

    /**
     * The label model color.
     *
     * @return my color
     */
    public ObjectProperty<Color> colorProperty()
    {
        return myColor;
    }

    /**
     * The label model font family.
     *
     * @return my font
     */
    public StringProperty fontProperty()
    {
        return myFont;
    }

    /**
     * The label model font size.
     *
     * @return my size
     */
    public IntegerProperty sizeProperty()
    {
        return mySize;
    }
}
