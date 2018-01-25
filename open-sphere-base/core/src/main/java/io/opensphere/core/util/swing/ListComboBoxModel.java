package io.opensphere.core.util.swing;

import java.util.Collection;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * A more efficient list model for combo boxes.
 * <p>
 * <b>Note:</b> This model does not allow selection of an item that has not been
 * added to the model.
 *
 * @param <E> the type of the elements of this model
 */
public class ListComboBoxModel<E> extends AbstractListModel<E> implements MutableComboBoxModel<E>
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The list of objects. */
    private final List<E> myObjects;

    /** The selected object. */
    private E mySelectedObject;

    /**
     * Constructs an empty ListComboBoxModel object.
     */
    public ListComboBoxModel()
    {
        myObjects = New.list();
    }

    /**
     * Constructs a DefaultComboBoxModel object initialized with an array of
     * objects.
     *
     * @param items an array of Object objects
     */
    @SafeVarargs
    @SuppressWarnings({ "varargs", "PMD.ConstructorCallsOverridableMethod" })
    public ListComboBoxModel(E... items)
    {
        this(New.list(items));
    }

    /**
     * Constructs a DefaultComboBoxModel object initialized with a collection of
     * objects.
     *
     * @param items an array of Object objects
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ListComboBoxModel(Collection<? extends E> items)
    {
        this(New.list(items));
    }

    /**
     * Internal constructor.
     *
     * @param items The initial items.
     */
    private ListComboBoxModel(List<E> items)
    {
        myObjects = items;

        if (!myObjects.isEmpty())
        {
            mySelectedObject = getElementAt(0);
        }
    }

    // implements javax.swing.ComboBoxModel
    /**
     * Set the value of the selected item. The selected item may be null.
     * <p>
     *
     * @param anObject The combo box value or null for no selection.
     *
     * @deprecated Use {@link #setSelectedElement(Object)} for better
     *             type-safety.
     */
    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public void setSelectedItem(Object anObject)
    {
        setSelectedElement((E)anObject);
    }

    /**
     * Set the value of the selected item. The selected item may be null.
     * <p>
     *
     * @param anObject The combo box value or null for no selection.
     * @throws IllegalArgumentException If the argument is not one of the
     *             available choices.
     */
    public void setSelectedElement(E anObject) throws IllegalArgumentException
    {
        if (!EqualsHelper.equals(anObject, mySelectedObject))
        {
            if (anObject == null || myObjects.contains(anObject))
            {
                mySelectedObject = anObject;
                fireContentsChanged(this, -1, -1);
            }
            else
            {
                throw new IllegalArgumentException("Selected element is not a valid choice: " + anObject);
            }
        }
    }

    // implements javax.swing.ComboBoxModel
    @Override
    public E getSelectedItem()
    {
        return mySelectedObject;
    }

    // implements javax.swing.ListModel
    @Override
    public int getSize()
    {
        return myObjects.size();
    }

    // implements javax.swing.ListModel
    @Override
    public E getElementAt(int index)
    {
        if (index >= 0 && index < myObjects.size())
        {
            return myObjects.get(index);
        }
        return null;
    }

    /**
     * Returns the index-position of the specified object in the list.
     *
     * @param anObject an object
     * @return an int representing the index position, where 0 is the first
     *         position
     */
    public int getIndexOf(Object anObject)
    {
        return myObjects.indexOf(anObject);
    }

    // implements javax.swing.MutableComboBoxModel
    @Override
    public void addElement(E anObject)
    {
        myObjects.add(anObject);
        fireIntervalAdded(this, myObjects.size() - 1, myObjects.size() - 1);
        if (myObjects.size() == 1 && mySelectedObject == null && anObject != null)
        {
            setSelectedElement(anObject);
        }
    }

    // implements javax.swing.MutableComboBoxModel
    @Override
    public void insertElementAt(E anObject, int index)
    {
        myObjects.add(index, anObject);
        fireIntervalAdded(this, index, index);
    }

    // implements javax.swing.MutableComboBoxModel
    @Override
    public void removeElementAt(int index)
    {
        if (getElementAt(index) == mySelectedObject)
        {
            if (index == 0)
            {
                setSelectedElement(getSize() == 1 ? null : getElementAt(index + 1));
            }
            else
            {
                setSelectedElement(getElementAt(index - 1));
            }
        }

        myObjects.remove(index);

        fireIntervalRemoved(this, index, index);
    }

    // implements javax.swing.MutableComboBoxModel
    @Override
    public void removeElement(Object anObject)
    {
        int index = myObjects.indexOf(anObject);
        if (index != -1)
        {
            removeElementAt(index);
        }
    }

    /**
     * Empties the list.
     */
    public void removeAllElements()
    {
        if (!myObjects.isEmpty())
        {
            int firstIndex = 0;
            int lastIndex = myObjects.size() - 1;
            myObjects.clear();
            mySelectedObject = null;
            fireIntervalRemoved(this, firstIndex, lastIndex);
        }
        else
        {
            mySelectedObject = null;
        }
    }
}
