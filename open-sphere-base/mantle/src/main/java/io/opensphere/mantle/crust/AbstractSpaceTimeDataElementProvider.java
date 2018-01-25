package io.opensphere.mantle.crust;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.impl.AbstractDataElementProvider;

/**
 * DataElementProvider that updates the data type with spatial and temporal bounds.
 *
 * @param <T> The type of the source items
 */
public abstract class AbstractSpaceTimeDataElementProvider<T> extends AbstractDataElementProvider<T>
{
    /** The space/time helper. */
    private final SpaceTimeHelper mySpaceTimeHelper;

    /**
     * Constructor.
     *
     * @param dataType The data type
     * @param sourceItems The items that will be used to create the data elements
     */
    public AbstractSpaceTimeDataElementProvider(DataTypeInfo dataType, Iterable<? extends T> sourceItems)
    {
        super(dataType, sourceItems);
        mySpaceTimeHelper = new SpaceTimeHelper(dataType);
    }

    @Override
    public boolean hasNext()
    {
        boolean hasNext = super.hasNext();
        mySpaceTimeHelper.handleHasNext(hasNext);
        return hasNext;
    }

    @Override
    public DataElement next()
    {
        DataElement element = super.next();
        mySpaceTimeHelper.handleNext(element);
        return element;
    }

    @Override
    protected abstract MapDataElement createDataElement(T item);
}
