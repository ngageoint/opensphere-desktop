package io.opensphere.mantle.controller.event;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.TypeFocusEvent;

/**
 * The Class DataTypeInfoFocusEvent.
 */
public class DataTypeInfoFocusEvent extends TypeFocusEvent<DataTypeInfo>
{
    /**
     * Instantiates a new data type info focus event.
     *
     * @param dtis the {@link DataTypeInfo}s
     * @param source the source
     */
    public DataTypeInfoFocusEvent(Collection<? extends DataTypeInfo> dtis, Object source)
    {
        super(dtis, source);
    }

    /**
     * Instantiates a new data type info focus event.
     *
     * @param dtis the {@link DataTypeInfo}s
     * @param source the source
     * @param focusType the focus type
     */
    public DataTypeInfoFocusEvent(Collection<? extends DataTypeInfo> dtis, Object source, FocusType focusType)
    {
        super(dtis, source, focusType);
    }

    /**
     * Instantiates a new data type info focus event.
     *
     * @param dti the {@link DataTypeInfo}s
     * @param source the source
     * @param focusType the focus type
     */
    public DataTypeInfoFocusEvent(DataTypeInfo dti, Object source, FocusType focusType)
    {
        super(Collections.singleton(dti), source, focusType);
    }
}
