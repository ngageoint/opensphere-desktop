package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Events from changes to DataTypeInfo.
 */
public class DataTypeVisibilityChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /** If true, save visibility to preferences. */
    private final boolean mySavePreference;

    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param visible - true if changed to visible, false if changed to
     *            invisible.
     * @param savePreference - true if visibility should be saved to
     *            preferences.
     * @param source - the source of the event.
     */
    public DataTypeVisibilityChangeEvent(DataTypeInfo dti, boolean visible, boolean savePreference, Object source)
    {
        super(dti, Type.VISIBILITY_CHANGED, visible ? Boolean.TRUE : Boolean.FALSE, source);
        mySavePreference = savePreference;
    }

    /**
     * Checks visibility should be saved as a preference.
     *
     * @return true, if preference should be saved
     */
    public boolean isSavePreference()
    {
        return mySavePreference;
    }

    /**
     * Gets if in use.
     *
     * @return true if in use, false if no longer in use.
     */
    public boolean isVisible()
    {
        return Boolean.TRUE.equals(getValue());
    }
}
