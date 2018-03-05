package io.opensphere.mantle.data.cache.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.cache.LoadedElementDataView;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.DynamicMetaDataList;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleGeometryConverterUtility;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class LoadedElementData.
 */
@SuppressWarnings("PMD.GodClass")
public class LoadedElementData implements LoadedElementDataView, Serializable
{
    /** The value representing a null origin ID. */
    private static final int NULL_ID = -1;

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(LoadedElementData.class);

    /** The our logged dynamic error once. */
    private static AtomicBoolean ourLoggedDyanmicErrorOnce = new AtomicBoolean();

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The map geometry support. */
    private MapGeometrySupport myMapGeometrySupport;

    /** The meta data. */
    private List<Object> myMetaData;

    /** The origin id. */
    private long myOriginId = NULL_ID;

    /**
     * Convert value to boolean if possible.
     *
     * @param oldValue the old value
     * @return the object
     */
    private static Object convertValueToBooleanIfPossible(Object oldValue)
    {
        String strVal = oldValue.toString();
        if ("true".equalsIgnoreCase(strVal))
        {
            return Boolean.TRUE;
        }
        else
        {
            return Boolean.FALSE;
        }
    }

    /**
     * Convert value to byte if possible.
     *
     * @param oldValue the old value
     * @return the object
     */
    private static Object convertValueToByteIfPossible(Object oldValue)
    {
        if (oldValue instanceof Number)
        {
            return Byte.valueOf(((Number)oldValue).byteValue());
        }
        else
        {
            try
            {
                return Byte.valueOf(Byte.parseByte(oldValue.toString()));
            }
            catch (NumberFormatException e)
            {
                return oldValue;
            }
        }
    }

    /**
     * Convert value to double if possible.
     *
     * @param oldValue the old value
     * @return the object
     */
    private static Object convertValueToDoubleIfPossible(Object oldValue)
    {
        if (oldValue instanceof Number)
        {
            return Double.valueOf(((Number)oldValue).doubleValue());
        }
        else
        {
            try
            {
                return Double.valueOf(Double.parseDouble(oldValue.toString()));
            }
            catch (NumberFormatException e)
            {
                return oldValue;
            }
        }
    }

    /**
     * Convert value to float if possible.
     *
     * @param oldValue the old value
     * @return the object
     */
    private static Object convertValueToFloatIfPossible(Object oldValue)
    {
        if (oldValue instanceof Number)
        {
            return Float.valueOf(((Number)oldValue).floatValue());
        }
        else
        {
            try
            {
                return Float.valueOf(Float.parseFloat(oldValue.toString()));
            }
            catch (NumberFormatException e)
            {
                return oldValue;
            }
        }
    }

    /**
     * Convert value to integer if possible.
     *
     * @param oldValue the old value
     * @return the object
     */
    private static Object convertValueToIntegerIfPossible(Object oldValue)
    {
        if (oldValue instanceof Number)
        {
            return Integer.valueOf(((Number)oldValue).intValue());
        }
        else
        {
            try
            {
                return Integer.valueOf(Integer.parseInt(oldValue.toString()));
            }
            catch (NumberFormatException e)
            {
                return oldValue;
            }
        }
    }

    /**
     * Convert value to long if possible.
     *
     * @param oldValue the old value
     * @return the object
     */
    private static Object convertValueToLongIfPossible(Object oldValue)
    {
        if (oldValue instanceof Number)
        {
            return Long.valueOf(((Number)oldValue).longValue());
        }
        else
        {
            try
            {
                return Long.valueOf(Long.parseLong(oldValue.toString()));
            }
            catch (NumberFormatException e)
            {
                return oldValue;
            }
        }
    }

    /**
     * Convert value to short if possible.
     *
     * @param oldValue the old value
     * @return the object
     */
    private static Object convertValueToShortIfPossible(Object oldValue)
    {
        if (oldValue instanceof Number)
        {
            return Short.valueOf(((Number)oldValue).shortValue());
        }
        else
        {
            try
            {
                return Short.valueOf(Short.parseShort(oldValue.toString()));
            }
            catch (NumberFormatException e)
            {
                return oldValue;
            }
        }
    }

    /**
     * Get if an error has been logged about a dynamic load error.
     *
     * @return {@code true} if an error has been logged.
     */
    private static synchronized boolean isDynamicErrorLogged()
    {
        return ourLoggedDyanmicErrorOnce.get();
    }

    /**
     * Log a dynamic load error if one hasn't been logged already.
     */
    private static synchronized void setDynamicErrorLogged()
    {
        if (ourLoggedDyanmicErrorOnce.compareAndSet(false, true))
        {
            LOGGER.error("Failed to create new instance of DynamicMetaDataList class, defaulting to non-dynamic mode."
                    + "No futher errors of ths type will be logged.");
        }
    }

    /**
     * Instantiates a new loaded element data.
     */
    public LoadedElementData()
    {
    }

    /**
     * Instantiates a new in memory cache assist.
     *
     * @param deReg the de reg
     * @param el the DataElement
     * @param useDynamicClasses the use dynamic classes
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public LoadedElementData(DynamicEnumerationRegistry deReg, DataElement el, boolean useDynamicClasses)
    {
        if (useDynamicClasses && DynamicMetaDataClassRegistry.getInstance().canCompile() && !isDynamicErrorLogged())
        {
            Class<DynamicMetaDataList> cl = DynamicMetaDataClassRegistry.getInstance()
                    .getLatestDynamicClassForDataTypeKey(el.getDataTypeInfo().getTypeKey());
            DynamicMetaDataList dmdl = null;
            try
            {
                if (cl != null)
                {
                    dmdl = cl.newInstance();
                    dmdl.setEqualTo(el.getMetaData());
                    myMetaData = dmdl;
                }
                else
                {
                    // Default to not using dynamic classes if we could not get
                    // the compiled class instance so we continue to operate
                    // even if not as efficiently as we could.
                    myMetaData = el.getMetaData().getValues() == null ? null
                            : new ArrayList<Object>(convertValuesIfNecessary(deReg, el, el.getMetaData().getValues()));
                }
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                setDynamicErrorLogged();
                myMetaData = convertValuesIfNecessary(deReg, el, el.getMetaData().getValues());
            }
        }
        else
        {
            myMetaData = el.getMetaData().getValues() == null ? null
                    : new ArrayList<Object>(convertValuesIfNecessary(deReg, el, el.getMetaData().getValues()));
        }

        myOriginId = el.getId();
        if (el instanceof MapDataElement)
        {
            myMapGeometrySupport = ((MapDataElement)el).getMapGeometrySupport();
            myMapGeometrySupport = SimpleGeometryConverterUtility.convertSupportToSimpleFormIfPossible(myMapGeometrySupport);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        LoadedElementData other = (LoadedElementData)obj;

        return Objects.equals(myMapGeometrySupport, other.myMapGeometrySupport) && Objects.equals(myMetaData, other.myMetaData)
                && myOriginId == other.myOriginId;
    }

    @Override
    public MapGeometrySupport getMapGeometrySupport()
    {
        return myMapGeometrySupport;
    }

    @Override
    public List<Object> getMetaData()
    {
        return myMetaData;
    }

    @Override
    public Long getOriginId()
    {
        return myOriginId != NULL_ID ? Long.valueOf(myOriginId) : null;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myMapGeometrySupport == null ? 0 : myMapGeometrySupport.hashCode());
        result = prime * result + (myMetaData == null ? 0 : myMetaData.hashCode());
        result = prime * result + HashCodeHelper.getHashCode(myOriginId);
        return result;
    }

    /**
     * Sets all three parts of the LED with one call.
     *
     * @param originId the origin id
     * @param mgs the mgs
     * @param metaData the meta data
     */
    public void setAll(Long originId, MapGeometrySupport mgs, List<Object> metaData)
    {
        setOriginId(originId);
        setMapGeometrySupport(mgs);
        setMetaData(metaData);
    }

    /**
     * Sets the map geometry support.
     *
     * @param mgs the new map geometry support
     */
    public void setMapGeometrySupport(MapGeometrySupport mgs)
    {
        myMapGeometrySupport = mgs;
    }

    /**
     * Sets the meta data.
     *
     * @param metaData the new meta data
     */
    public void setMetaData(List<Object> metaData)
    {
        myMetaData = metaData;
    }

    /**
     * Sets the origin id.
     *
     * @param id the new origin id
     */
    public void setOriginId(long id)
    {
        myOriginId = id;
    }

    /**
     * Sets the origin id.
     *
     * @param id the new origin id
     */
    public void setOriginId(Long id)
    {
        myOriginId = id != null ? id.longValue() : NULL_ID;
    }

    /**
     * Convert values if they are not the correct type for their declared class
     * type.
     *
     * In this case converts strings to number types and produces dynamic
     * enumeration key values when the input value is not a valid key type.
     *
     * @param deReg the {@link DynamicEnumerationRegistry}
     * @param el the {@link DataElement}
     * @param values the values to be converted if necessary.
     * @return the list of input values converted if necessary.
     */
    private List<Object> convertValuesIfNecessary(DynamicEnumerationRegistry deReg, DataElement el, List<Object> values)
    {
        List<Object> resultList = null;
        MetaDataInfo mdi = el.getDataTypeInfo().getMetaDataInfo();
        if (mdi != null)
        {
            resultList = new ArrayList<>(values);
            List<String> keyNames = mdi.getKeyNames();
            Class<?> keyClass = null;
            String key = null;
            for (int i = 0; i < keyNames.size(); i++)
            {
                key = keyNames.get(i);
                keyClass = mdi.getKeyClassType(key);
                if (keyClass != null && i < resultList.size())
                {
                    Object oldValue = resultList.get(i);
                    if (keyClass == DynamicEnumerationKey.class)
                    {
                        if (oldValue == null || !DynamicEnumerationKey.class.isAssignableFrom(oldValue.getClass()))
                        {
                            resultList.set(i, deReg.addValue(el.getDataTypeInfo().getTypeKey(), key, oldValue));
                        }
                    }
                    else if (oldValue != null)
                    {
                        if (keyClass == Long.class && oldValue.getClass() != Long.class)
                        {
                            resultList.set(i, convertValueToLongIfPossible(oldValue));
                        }
                        else if (keyClass == Integer.class && oldValue.getClass() != Integer.class)
                        {
                            resultList.set(i, convertValueToIntegerIfPossible(oldValue));
                        }
                        else if (keyClass == Float.class && oldValue.getClass() != Float.class)
                        {
                            resultList.set(i, convertValueToFloatIfPossible(oldValue));
                        }
                        else if (keyClass == Double.class && oldValue.getClass() != Double.class)
                        {
                            resultList.set(i, convertValueToDoubleIfPossible(oldValue));
                        }
                        else if (keyClass == Short.class && oldValue.getClass() != Short.class)
                        {
                            resultList.set(i, convertValueToShortIfPossible(oldValue));
                        }
                        else if (keyClass == Byte.class && oldValue.getClass() != Byte.class)
                        {
                            resultList.set(i, convertValueToByteIfPossible(oldValue));
                        }
                        else if (keyClass == Boolean.class && oldValue.getClass() != Boolean.class)
                        {
                            resultList.set(i, convertValueToBooleanIfPossible(oldValue));
                        }
                    }
                }
            }
        }

        return resultList == null ? values : resultList;
    }
}
