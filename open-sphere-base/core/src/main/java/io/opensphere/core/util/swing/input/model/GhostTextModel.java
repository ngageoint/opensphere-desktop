package io.opensphere.core.util.swing.input.model;

/** A text model that also supports displaying ghost text in the view. */
public class GhostTextModel extends TextModel
{
    /** The ghost text. */
    private String myGhostText;

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Set the ghost text.
     *
     * @param ghostText The ghost text.
     */
    public void setGhostText(String ghostText)
    {
        myGhostText = ghostText;
        firePropertyChangeEvent(this, PropertyChangeEvent.Property.VIEW_PARAMETERS);
    }

    /**
     * Get the ghost text.
     *
     * @return The ghost text.
     */
    public String getGhostText()
    {
        return myGhostText;
    }
}
