package io.opensphere.mantle.data.geom;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.BitArrays;

/**
 * Abstract implementation for a {@link MapGeometrySupport}.
 */
@SuppressWarnings({ "PMD.AvoidUsingShortType", "PMD.GodClass" })
public abstract class AbstractMapGeometrySupport implements MapGeometrySupport
{
    /** Mask for Follow Terrain flag. */
    public static final byte FOLLOW_TERRAIN_MASK = 1;

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The value of the highest bit defined by this class. This is to be used as
     * an offset by subclasses that need to define their own bits.
     */
    protected static final byte HIGH_BIT = FOLLOW_TERRAIN_MASK;

    /** Bit Field for flag storage. 8 bits max. */
    private byte myBitField1;

    /**
     * A dynamic object storage array with at most one entry per inserted class.
     * Basically a Map but implemented for small stores where hashing is not
     * necessary to keep memory footprint to a minimum.
     */
    private Object[] myDynamicStorage;

    /**
     * Default constructor.
     */
    public AbstractMapGeometrySupport()
    {
        /* intentionally blank */
    }

    /**
     * Copy constructor.
     *
     * @param source the source object from which to copy data.
     */
    public AbstractMapGeometrySupport(AbstractMapGeometrySupport source)
    {
        myBitField1 = source.myBitField1;
        if (source.myDynamicStorage != null)
        {
            myDynamicStorage = Arrays.copyOf(source.myDynamicStorage, source.myDynamicStorage.length);
        }
    }

    /**
     * Adds a child to the child list.
     *
     * @param mapGeomSupport - the child to add
     * @return true if added, false if not
     */
    public boolean addChild(MapGeometrySupport mapGeomSupport)
    {
        if (mapGeomSupport == null)
        {
            return false;
        }

        boolean added = false;
        createChildList();
        ChildList children = (ChildList)getItemFromDynamicStorage(ChildList.class);
        synchronized (children)
        {
            added = children.add(mapGeomSupport);
        }
        return added;
    }

    /**
     * Clears the children list.
     */
    @SuppressWarnings("unchecked")
    public void clearChildren()
    {
        // boolean changed = false;
        List<MapGeometrySupport> children = (List<MapGeometrySupport>)getItemFromDynamicStorage(List.class);
        if (children != null)
        {
            synchronized (children)
            {
                children.clear();
            }
        }
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        AbstractMapGeometrySupport other = (AbstractMapGeometrySupport)obj;
        return myBitField1 == other.getFlagField() && Arrays.equals(myDynamicStorage, other.getDynamicStorage());
    }

    @Override
    public boolean followTerrain()
    {
        return isFlagSet(FOLLOW_TERRAIN_MASK);
    }

    @Override
    public CallOutSupport getCallOutSupport()
    {
//        return myCallOutSupport;
        return (CallOutSupport)getItemFromDynamicStorage(CallOutSupport.class);
    }

    @Override
    public List<MapGeometrySupport> getChildren()
    {
        ChildList children = (ChildList)getItemFromDynamicStorage(ChildList.class);
        List<MapGeometrySupport> resultList = null;
        if (children != null)
        {
            synchronized (children)
            {
                resultList = Collections.unmodifiableList(children);
            }
        }
        return resultList;
    }

    /**
     * Gets an item from dynamic storage by class. If it exists.
     *
     * @param typeToRetrieve the class of the type to retrieve.
     * @return the item from dynamic storage or null if not found.
     */
    public synchronized Object getItemFromDynamicStorage(Class<?> typeToRetrieve)
    {
        Object result = null;
        if (myDynamicStorage != null && typeToRetrieve != null && myDynamicStorage.length > 0)
        {
            if (myDynamicStorage.length == 1)
            {
                Object item = myDynamicStorage[0];
                if (item != null && typeToRetrieve.isAssignableFrom(item.getClass()))
                {
                    result = item;
                }
            }
            else
            {
                Object item = null;
                for (int i = 0; i < myDynamicStorage.length; i++)
                {
                    item = myDynamicStorage[i];
                    if (item != null && typeToRetrieve.isAssignableFrom(item.getClass()))
                    {
                        result = item;
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getToolTip()
    {
        return (String)getItemFromDynamicStorage(String.class);
    }

    @Override
    public boolean hasChildren()
    {
        ChildList list = (ChildList)getItemFromDynamicStorage(ChildList.class);
        return !(list == null || list.isEmpty());
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + getFlagField();
        return prime * result + Arrays.hashCode(myDynamicStorage);
    }

    /**
     * Checks to see if a flag is set in the internal bit field.
     *
     * @param mask - the mask to check
     * @return true if set, false if not
     */
    public synchronized boolean isFlagSet(byte mask)
    {
        return BitArrays.isFlagSet(mask, myBitField1);
    }

    /**
     * Put item in dynamic storage. Only one item per class type can be stored.
     * Returns the old item of that class type if there was one.
     *
     * @param item the item to put in storage.
     * @return the object of item's class type that was previously in storage
     *         that was replace or null if there was no previous item.
     */
    public synchronized Object putItemInDynamicStorage(Object item)
    {
        Utilities.checkNull(item, "item");
        Object result = null;
        if (myDynamicStorage != null)
        {
            int previousIndex = findIndexOfItemInDynamicStorage(item.getClass());
            if (previousIndex == -1)
            {
                // Nothing of that type is in storage already so we need to
                // resize the array and copy all the references and add the new
                // item at the end.
                myDynamicStorage = Arrays.copyOf(myDynamicStorage, myDynamicStorage.length + 1);
                myDynamicStorage[myDynamicStorage.length - 1] = item;
            }
            else
            {
                // Grab the previous entry for the type to be returned
                // and put the new value into the storage.
                result = myDynamicStorage[previousIndex];
                myDynamicStorage[previousIndex] = item;
            }
        }
        else
        {
            myDynamicStorage = new Object[1];
            myDynamicStorage[0] = item;
        }
        return result;
    }

    /**
     * Removes a child from the child support list.
     *
     * @param mapGeomSupport - the child to remove
     * @return true if removed, false if not
     */
    public boolean removeChild(MapGeometrySupport mapGeomSupport)
    {
        boolean removed = false;
        ChildList children = (ChildList)getItemFromDynamicStorage(ChildList.class);
        if (children != null)
        {
            synchronized (children)
            {
                children.remove(mapGeomSupport);
            }
        }
        return removed;
    }

    /**
     * Removes the item from dynamic storage.
     *
     * @param typeToRemove the {@link Class} of the type to remove.
     * @return the object removed, or null if not in storage
     */
    public synchronized Object removeItemFromDynamicStorage(Class<?> typeToRemove)
    {
        Object result = null;
        if (myDynamicStorage != null && typeToRemove != null && myDynamicStorage.length > 0)
        {
            // First find the index of the item to be removed, if it exists in
            // the storage
            int indexOfItem = findIndexOfItemInDynamicStorage(typeToRemove);

            // If we found the item, then we need to make a new resized array
            // and copy the references
            // for any items not being removed to the new array, then replace
            // the old array with the new.
            if (indexOfItem != -1)
            {
                if (myDynamicStorage.length == 1)
                {
                    myDynamicStorage = null;
                }
                else
                {
                    Object[] temp = new Object[myDynamicStorage.length - 1];
                    int insertIndex = 0;
                    for (int i = 0; i < myDynamicStorage.length; i++)
                    {
                        if (i != indexOfItem)
                        {
                            temp[insertIndex] = myDynamicStorage[i];
                            insertIndex++;
                        }
                    }
                    myDynamicStorage = temp;
                }
            }
        }
        return result;
    }

    @Override
    public void setCallOutSupport(CallOutSupport cos)
    {
        putItemInDynamicStorage(cos);
    }

    /**
     * Sets (or un-sets) a flag in the internal bit field.
     *
     * @param mask - the mask to use
     * @param on - true to set on, false to set off
     * @return true if changed.
     */
    public synchronized boolean setFlag(byte mask, boolean on)
    {
        byte newBitField = BitArrays.setFlag(mask, on, myBitField1);
        myBitField1 = newBitField;
        return newBitField != myBitField1;
    }

    @Override
    public void setFollowTerrain(boolean follow, Object source)
    {
        setFlag(FOLLOW_TERRAIN_MASK, follow);
    }

    @Override
    public void setToolTip(String tip)
    {
        putItemInDynamicStorage(tip);
    }

    /**
     * Creates the child list (if necessary) in a thread safe way.
     */
    private synchronized void createChildList()
    {
        ChildList children = (ChildList)getItemFromDynamicStorage(ChildList.class);
        if (children == null)
        {
            children = new ChildList();
            putItemInDynamicStorage(children);
        }
    }

    /**
     * Finds the index of item in dynamic storage array.
     *
     * @param type the Class to find.
     * @return the index, or -1 if not in storage.
     */
    private int findIndexOfItemInDynamicStorage(Class<?> type)
    {
        int indexOfItem = -1;
        if (myDynamicStorage != null && type != null && myDynamicStorage.length > 0)
        {
            // First find the index of the item to be removed, if it exists in
            // the storage
            Object item = null;
            for (int i = 0; i < myDynamicStorage.length; i++)
            {
                item = myDynamicStorage[i];
                if (item != null && type.isAssignableFrom(item.getClass()))
                {
                    indexOfItem = i;
                    break;
                }
            }
        }
        return indexOfItem;
    }

    /**
     * Gets the dynamic storage.
     *
     * @return the dynamic storage
     */
    private synchronized Object[] getDynamicStorage()
    {
        return myDynamicStorage == null ? null : Arrays.copyOf(myDynamicStorage, myDynamicStorage.length);
    }

    /**
     * Gets the flag field.
     *
     * @return the flag field
     */
    private synchronized byte getFlagField()
    {
        return myBitField1;
    }

    /**
     * The ChildList class which only exists as a marker for unambiguous lookup
     * in the dynamic storage.
     */
    private static class ChildList extends LinkedList<MapGeometrySupport>
    {
        /**
         * serialVersionUID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new child list.
         */
        public ChildList()
        {
            super();
        }
    }
}
