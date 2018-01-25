package io.opensphere.core.util.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import io.opensphere.core.util.lang.ImpossibleException;

/**
 * Convenience class to make DocumentListeners look cleaner.
 */
public abstract class DocumentListenerAdapter implements DocumentListener
{
    /**
     * Get the text from a document event.
     *
     * @param e The event.
     * @return The text.
     */
    public static String getText(DocumentEvent e)
    {
        try
        {
            return e.getDocument().getText(0, e.getDocument().getLength());
        }
        catch (BadLocationException e1)
        {
            throw new ImpossibleException(e1);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        updateAction(e);
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        updateAction(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        updateAction(e);
    }

    /**
     * A catch-all update action if the user wants all updates for a
     * DocumentListener to do the same thing.
     *
     * @param e the DocumentEvent to act upon.
     */
    protected abstract void updateAction(DocumentEvent e);
}
