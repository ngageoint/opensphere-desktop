package io.opensphere.mantle.data.element.impl;

import java.util.Collections;
import java.util.Iterator;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.DataElementProvider;

/**
 * A simple implementation of {@link DataElementProvider} that converts source items to data elements on the fly.
 *
 * @param <T> The type of the source items
 */
public abstract class AbstractDataElementProvider<T> extends SimpleDataElementProvider
{
    /** The source items iterator. */
    private final Iterator<? extends T> mySourceIterator;

    /**
     * Constructor.
     *
     * @param dataType The data type
     * @param sourceItems The items that will be used to create the data elements
     */
    public AbstractDataElementProvider(DataTypeInfo dataType, Iterable<? extends T> sourceItems)
    {
        super(dataType, Collections.emptyList());
        mySourceIterator = sourceItems.iterator();
    }

    @Override
    public boolean hasNext()
    {
        return mySourceIterator.hasNext();
    }

    @Override
    public DataElement next()
    {
        return createDataElement(mySourceIterator.next());
    }

    /**
     * Creates a DataElement from the item.
     *
     * @param item the source item
     * @return the DataElement
     */
    protected abstract DataElement createDataElement(T item);
}
