package io.opensphere.csvcommon.ui.columndefinition.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

/**
 * A combo box cell editor that commits the selected item change immediately to the table model. This class is similar to one
 * defined in the <code>org.jdesktop.swingx.autocomplete.ComboBoxCellEditor</code> class. Unfortunately, that implementation, when
 * additional behavior is added, causes race conditions through its use of events. This implementation fixes the race
 * conditions, and should be used in the other implementation's place.
 */
public class EditableComboBoxCellEditor
        implements TableCellEditor, ActionListener, KeyListener, PropertyChangeListener, Serializable
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = 9152710043121676796L;

    /**
     * The list used to maintain the set of subscriptions.
     */
    protected EventListenerList myListenerList = new EventListenerList();

    /**
     * A change event fired when editing is complete.
     */
    private transient ChangeEvent myChangeEvent;

    /**
     * The combo box used as a display / editor component.
     */
    private final JComboBox<String> myComboBox;

    /**
     * Creates a new editor for use in a table.
     *
     * @param pComboBox the component to use as an editor.
     */
    public EditableComboBoxCellEditor(JComboBox<String> pComboBox)
    {
        myComboBox = pComboBox;
        myComboBox.addActionListener(this);
        myComboBox.getEditor().getEditorComponent().addKeyListener(this);
        myComboBox.addPropertyChangeListener(this);
    }

    /**
     * Notifies all listeners that have registered interest for notification on this event type. The event instance is created
     * lazily.
     *
     * @see EventListenerList
     */
    protected void fireEditingStopped()
    {
        // Guaranteed to return a non-null array
        Object[] listeners = myListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == CellEditorListener.class)
            {
                // Lazily create the event:
                if (myChangeEvent == null)
                {
                    myChangeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener)listeners[i + 1]).editingStopped(myChangeEvent);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on this event type. The event instance is created
     * lazily.
     *
     * @see EventListenerList
     */
    protected void fireEditingCanceled()
    {
        // Guaranteed to return a non-null array
        Object[] listeners = myListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == CellEditorListener.class)
            {
                // Lazily create the event:
                if (myChangeEvent == null)
                {
                    myChangeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener)listeners[i + 1]).editingCanceled(myChangeEvent);
            }
        }
    }

    /**
     * Returns an array of all the <code>CellEditorListener</code>s added to this AbstractCellEditor with addCellEditorListener().
     *
     * @return all of the <code>CellEditorListener</code>s added or an empty array if no listeners have been added
     */
    public CellEditorListener[] getCellEditorListeners()
    {
        return myListenerList.getListeners(CellEditorListener.class);
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    @Override
    public Object getCellEditorValue()
    {
        return myComboBox.getSelectedItem();
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
     */
    @Override
    public boolean isCellEditable(EventObject pAnEvent)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
     */
    @Override
    public boolean shouldSelectCell(EventObject pAnEvent)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.CellEditor#stopCellEditing()
     */
    @Override
    public boolean stopCellEditing()
    {
        fireEditingStopped();
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.CellEditor#cancelCellEditing()
     */
    @Override
    public void cancelCellEditing()
    {
        fireEditingCanceled();
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
     */
    @Override
    public void addCellEditorListener(CellEditorListener pL)
    {
        myListenerList.add(CellEditorListener.class, pL);
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
     */
    @Override
    public void removeCellEditorListener(CellEditorListener pL)
    {
        myListenerList.remove(CellEditorListener.class, pL);
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable pTable, Object pValue, boolean pIsSelected, int pRow, int pColumn)
    {
        myComboBox.setSelectedItem(pValue);
        return myComboBox;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent pEvent)
    {
        int keyCode = pEvent.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER)
        {
            stopCellEditing();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent pEvent)
    {
        // intentionally blank
    }

    /**
     * {@inheritDoc}
     *
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent pEvent)
    {
        // intentionally blank
    }

    /**
     * {@inheritDoc}
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent pEvent)
    {
        if (myComboBox.getItemCount() > 0)
        {
            stopCellEditing();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent pEvent)
    {
        if (pEvent.getPropertyName().equals("editor"))
        {
            ComboBoxEditor editor = myComboBox.getEditor();
            if (editor != null && editor.getEditorComponent() != null)
            {
                JComponent editorComponent = (JComponent)myComboBox.getEditor().getEditorComponent();
                editorComponent.addKeyListener(this);
                editorComponent.setBorder(null);
            }
        }
    }
}
