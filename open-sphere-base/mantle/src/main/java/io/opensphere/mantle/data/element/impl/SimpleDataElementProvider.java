package io.opensphere.mantle.data.element.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.DataElementProvider;

/**
 * Completely generic DataElementProvider class that delegates to an ordinary Iterator; can be subclassed or used as-is.
 */
public class SimpleDataElementProvider implements DataElementProvider
{
    /** The type of data elements managed by this provider. */
    private final DataTypeInfo myDataType;

    /** An Iterator that will visit the DataElements to be provided. */
    private final Iterator<? extends DataElement> myIterator;

    /**
     * Construct a provider with the type and element Iterator.
     *
     * @param dataType the type
     * @param elements the elements
     */
    public SimpleDataElementProvider(DataTypeInfo dataType, Iterable<? extends DataElement> elements)
    {
        this(dataType, elements.iterator());
    }

    /**
     * Construct a provider with the type and element Iterator.
     *
     * @param dataType the type
     * @param iterator the elements iterator
     */
    public SimpleDataElementProvider(DataTypeInfo dataType, Iterator<? extends DataElement> iterator)
    {
        myDataType = dataType;
        myIterator = iterator;
    }

    @Override
    public boolean hasNext()
    {
        return myIterator.hasNext();
    }

    @Override
    public DataElement next()
    {
        return myIterator.next();
    }

    @Override
    public DataTypeInfo getDataTypeInfo()
    {
        return myDataType;
    }

    @Override
    public List<String> getErrorMessages()
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> getWarningMessages()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean hadError()
    {
        return false;
    }

    @Override
    public boolean hadWarning()
    {
        return false;
    }
}
