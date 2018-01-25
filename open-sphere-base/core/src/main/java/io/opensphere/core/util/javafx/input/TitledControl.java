package io.opensphere.core.util.javafx.input;

/**
 * An extension of the Control class, in which a title is associated with the control.
 */
public abstract class TitledControl extends VisitableControl
{
    /**
     * The textual title of the control.
     */
    private final String myTitle;

    /**
     * Creates a new titled control.
     *
     * @param pTitle the title of the control.
     */
    public TitledControl(String pTitle)
    {
        myTitle = pTitle;
    }

    /**
     * Gets the value of the {@link #myTitle} field.
     *
     * @return the value stored in the {@link #myTitle} field.
     */
    public String getTitle()
    {
        return myTitle;
    }
}
