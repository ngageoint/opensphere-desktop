package io.opensphere.featureactions.editor.model;

/**
 * The available styles for a bulls eye.
 */
public enum CriteriaOptions
{
    /**
     * No style just a label.
     */
    VALUE("Value"),

    /**
     * Displays a point.
     */
    RANGE("Range");

    /**
     * The user friendly label of the style.
     */
    private String myLabel;

    /**
     * Constructs a new style.
     *
     * @param label The user friendly label of the style.
     */
    private CriteriaOptions(String label)
    {
        myLabel = label;
    }

    @Override
    public String toString()
    {
        return myLabel;
    }
}
