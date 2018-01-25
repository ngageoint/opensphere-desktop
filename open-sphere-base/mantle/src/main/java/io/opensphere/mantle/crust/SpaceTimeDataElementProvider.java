package io.opensphere.mantle.crust;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.impl.SimpleDataElementProvider;

/**
 * DataElementProvider that updates the data type with spatial and temporal bounds.
 */
public class SpaceTimeDataElementProvider extends SimpleDataElementProvider
{
    /** The space/time helper. */
    private final SpaceTimeHelper mySpaceTimeHelper;

    /**
     * Constructor.
     *
     * @param dataType The data type
     * @param elements The data elements
     */
    public SpaceTimeDataElementProvider(DataTypeInfo dataType, Iterable<? extends DataElement> elements)
    {
        super(dataType, elements);
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
}
