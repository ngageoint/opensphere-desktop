package io.opensphere.analysis.base.model;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;

import net.jcip.annotations.NotThreadSafe;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.util.DataTypeUtilities;
import io.opensphere.mantle.data.element.DataElement;

/**
 * UI bin model that composes a regular bin.
 */
@NotThreadSafe
public class UIBin implements Bin<DataElement>, Comparable<UIBin>
{
    /** The bin to delegate to. */
    private final Bin<DataElement> myDelegate;

    /** The number of items in the bin. */
    private final IntegerProperty myCount = new SimpleIntegerProperty(this, "count");

    /** The bin color. */
    private Color myColor;

    /** The data element cache IDs. */
    private TLongList myElementCacheIds;

    /**
     * Constructor.
     *
     * @param bin the delegate bin
     */
    public UIBin(Bin<DataElement> bin)
    {
        myDelegate = bin;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myDelegate.toString();
    }

    /**
     * Sets the count.
     *
     * @param count the count
     */
    public void setCount(int count)
    {
        Platform.runLater(() -> myCount.set(count));
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount()
    {
        return myCount.get();
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public IntegerProperty countProperty()
    {
        return myCount;
    }

    /**
     * Sets the color.
     *
     * @param color the color
     */
    public void setColor(Color color)
    {
        myColor = color;
    }

    /**
     * Gets the color.
     *
     * @return the color
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Gets the elementCacheIds.
     *
     * @return the elementCacheIds
     */
    public TLongList getElementCacheIds()
    {
        if (myElementCacheIds == null)
        {
            myElementCacheIds = new TLongArrayList();
        }
        return myElementCacheIds;
    }

    @Override
    public boolean accepts(DataElement data)
    {
        return myDelegate.accepts(data);
    }

    @Override
    public Object getValueObject()
    {
        return myDelegate.getValueObject();
    }

    @Override
    public boolean add(DataElement data)
    {
        boolean added = myDelegate.add(data);
        if (added)
        {
            setCount(myDelegate.getSize());
            getElementCacheIds().add(data.getIdInCache());
        }
        return added;
    }

    @Override
    public boolean addAll(Collection<? extends DataElement> dataItems)
    {
        boolean added = myDelegate.addAll(dataItems);
        if (added)
        {
            setCount(myDelegate.getSize());
            TLongList elementCacheIds = getElementCacheIds();
            for (DataElement data : dataItems)
            {
                elementCacheIds.add(data.getIdInCache());
            }
        }
        return added;
    }

    @Override
    public UUID getBinId()
    {
        return myDelegate.getBinId();
    }

    @Override
    public boolean remove(DataElement data)
    {
        boolean removed = myDelegate.remove(data);
        if (removed)
        {
            setCount(myDelegate.getSize());
            getElementCacheIds().remove(data.getIdInCache());
        }
        return removed;
    }

    @Override
    public boolean removeAll(Collection<? extends DataElement> dataItems)
    {
        boolean removed = myDelegate.removeAll(dataItems);
        if (removed)
        {
            setCount(myDelegate.getSize());
            TLongList elementCacheIds = getElementCacheIds();
            for (DataElement data : dataItems)
            {
                elementCacheIds.remove(data.getIdInCache());
            }
        }
        return removed;
    }

    @Override
    public boolean removeIf(Predicate<? super DataElement> filter)
    {
        boolean removed = myDelegate.removeIf(filter);
        if (removed)
        {
            setCount(myDelegate.getSize());
            TLongList elementCacheIds = getElementCacheIds();
            elementCacheIds.clear();
            for (DataElement data : myDelegate.getData())
            {
                elementCacheIds.add(data.getIdInCache());
            }
        }
        return removed;
    }

    @Override
    public int getSize()
    {
        return myDelegate.getSize();
    }

    @Override
    public List<DataElement> getData()
    {
        return myDelegate.getData();
    }

    @Override
    public Bin<DataElement> getBin()
    {
        return myDelegate;
    }

    @Override
    public String toString()
    {
        return getLabel() + " " + myCount.get();
    }

    @Override
    public int compareTo(UIBin o)
    {
        return DataTypeUtilities.compareTo(getValueObject(), o.getValueObject());
    }
}
