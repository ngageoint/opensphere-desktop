package io.opensphere.controlpanels.styles.model;

/**
 * The available styles for a bulls eye.
 */
public enum Styles
{
    /**
     * No style just a label.
     */
    NONE("None"),

    /**
     * Displays a point.
     */
    POINT("Point"),

    /**
     * Displays a square.
     */
    SQUARE("Square"),

    /**
     * Displays a triangle.
     */
    TRIANGLE("Triangle"),

    /**
     * Displays a bulls eye icon.
     */
    ICON("Icon"),

    /**
     * Displays an ellipse for the bulls eye.
     */
    ELLIPSE("Ellipse"),

    /**
     * Displays an ellipse with a point in the center.
     */
    ELLIPSE_WITH_CENTER("Ellipse With Center");

    /**
     * The user friendly label of the style.
     */
    private String myLabel;

    /**
     * Constructs a new style.
     *
     * @param label The user friendly label of the style.
     */
    private Styles(String label)
    {
        myLabel = label;
    }

    @Override
    public String toString()
    {
        return myLabel;
    }
}
