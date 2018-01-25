package io.opensphere.core.util.swing.input.model;

import javax.swing.text.DocumentFilter;

/**
 * Text model.
 */
public class TextModel extends AbstractViewModel<String>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The number of columns that should be used for the text view. */
    private int myColumns = 10;

    /**
     * Get the number of columns that should be used for the text view.
     *
     * @return The number of columns.
     */
    public int getColumns()
    {
        return myColumns;
    }

    /**
     * Get the document filter which controls input modifications to the
     * associated text view.
     *
     * @return The filter if one is provided or null if no filtering is done.
     */
    public DocumentFilter getDocumentFilter()
    {
        return null;
    }

    /**
     * Set the number of columns that should be used for the text view.
     *
     * @param columns The columns.
     */
    public void setColumns(int columns)
    {
        myColumns = columns;
        firePropertyChangeEvent(this, PropertyChangeEvent.Property.VIEW_PARAMETERS);
    }
}
