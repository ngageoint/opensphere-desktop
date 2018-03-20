package io.opensphere.mantle.controller.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.annotation.concurrent.GuardedBy;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;

import gnu.trove.TLongCollection;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongLongHashMap;
import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.length.Feet;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.core.units.length.StatuteMiles;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.event.impl.CurrentDataTypeChangedEvent;
import io.opensphere.mantle.controller.event.impl.DataTypeAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataTypeRemovedEvent;
import io.opensphere.mantle.data.ColumnTypeDetector;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.cache.impl.DataElementCacheImpl;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.DataElementProvider;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.SimpleDataElementProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.AltitudeKey;
import io.opensphere.mantle.transformer.MapDataElementTransformer;
import io.opensphere.mantle.transformer.TransformerGeomRegistryUpdateTaskActivity;
import io.opensphere.mantle.transformer.impl.DefaultMapDataElementTransformer;
import io.opensphere.mantle.transformer.impl.StyleMapDataElementTransformer;

/** The Class DataTypeController. */
@SuppressWarnings("PMD.GodClass")
public class DataTypeControllerImpl implements DataTypeController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataTypeControllerImpl.class);

    /** Conversion factors for other units into meters. */
    private static Map<String, Double> scaleMap = new TreeMap<>();
    static
    {
        // conversion for feet (which was the observed use case)
        Double meterPerFt = Length.METERS_PER_FOOT;
        scaleMap.put(Feet.FEET_LONG_LABEL1.toUpperCase(), meterPerFt);
        scaleMap.put(Feet.FEET_LONG_LABEL2.toUpperCase(), meterPerFt);
        scaleMap.put(Feet.FEET_SHORT_LABEL.toUpperCase(), meterPerFt);

        // conversion for nautical miles
        Double meterPerNm = (double)NauticalMiles.METERS_PER_NAUTICAL_MILE;
        scaleMap.put(NauticalMiles.NM_LONG_LABEL.toUpperCase(), meterPerNm);
        scaleMap.put(NauticalMiles.NM_SHORT_LABEL.toUpperCase(), meterPerNm);

        // conversion for miles
        Double meterPerMi = StatuteMiles.FEET_PER_STATUTE_MILE * Length.METERS_PER_FOOT;
        scaleMap.put(StatuteMiles.MILES_LONG_LABEL.toUpperCase(), meterPerMi);
        scaleMap.put(StatuteMiles.MILES_SHORT_LABEL.toUpperCase(), meterPerMi);

        // conversion for kilometers
        Double meterPerKm = 1000.0;
        scaleMap.put(Kilometers.KILOMETERS_LONG_LABEL.toUpperCase(), meterPerKm);
        scaleMap.put(Kilometers.KILOMETERS_SHORT_LABEL.toUpperCase(), meterPerKm);

        // "conversion" for meters; actually, short-circuit to avoid unnecessary
        // work
        Double meterPerMeter = 1.0;
        scaleMap.put(Meters.METERS_LONG_LABEL.toUpperCase(), meterPerMeter);
        scaleMap.put(Meters.METERS_SHORT_LABEL.toUpperCase(), meterPerMeter);
    }

    /** Pattern used to recognize altitude units field names. */
    private static final Pattern altUnitRegex = Pattern
            .compile("(?i)(?:" + substringRegex("altitude", 3) + "|" + substringRegex("elevation", 3) + ")_units");

    /** The toolbox. */
    private final Toolbox myToolBox;

    /** The data element cache. */
    private final DataElementCacheImpl myDECache;

    /** The DataTypeInfo key to DataTypeInfo map. */
    @GuardedBy("myDTIKeyToDTIMap")
    private final Map<String, DataTypeInfo> myDTIKeyToDTIMap = New.map();

    /** The DataTypeInfo key to category map. */
    @GuardedBy("myDTIKeyToDTIMap")
    private final Map<String, String> myDTIKeyToCategoryMap = New.map();

    /** The DataTypeInfo key to source map. */
    @GuardedBy("myDTIKeyToDTIMap")
    private final Map<String, String> myDTIKeyToSourceMap = New.map();

    /** The DataTypeInfo key to Transformer map. */
    @GuardedBy("myDTIKeyToDTIMap")
    private final Map<String, MapDataElementTransformer> myDTIKeyToTransformerMap = New.map();

    /** The Geom registry update task activity. */
    @GuardedBy("this")
    private TransformerGeomRegistryUpdateTaskActivity myGeomRegistryUpdateTaskActivity;

    /** The current data type info. */
    @GuardedBy("this")
    private DataTypeInfo myCurrentDataTypeInfo;

    /**
     * Manages the data type info's that are currently in our maps and add or
     * removes data type infos based on activation events.
     */
    private final DataTypeAutoAdder myAutoAdder;

    /** The column type detector used to find special keys. */
    private final ColumnTypeDetector myColumnTypeDetector;

    /**
     * Instantiates a new data type controller.
     *
     * @param toolbox the {@link Toolbox}
     * @param deCache the data element cache
     * @param columnTypeDetector the column type detector used to find special keys.
     */
    public DataTypeControllerImpl(Toolbox toolbox, DataElementCacheImpl deCache, ColumnTypeDetector columnTypeDetector)
    {
        myToolBox = toolbox;
        myDECache = deCache;
        myAutoAdder = new DataTypeAutoAdder(toolbox.getEventManager(), this);
        myColumnTypeDetector = columnTypeDetector;
    }

    /** Initializes the controller. */
    public void initialize()
    {
        new DataTypeMenuCreator(myToolBox, this::getDataTypeInfo).createMenus();
    }

    /** Stops listening for events. */
    public void close()
    {
        myAutoAdder.close();
    }

    @Override
    public void addDataType(String source, String category, DataTypeInfo dti, Object originator)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Adding Data Type Info: " + dti.getTypeKey() + ", TypeName: " + dti.getTypeName() + ", DisplayName: "
                    + dti.getDisplayName());
        }

        boolean added = false;

        final String key = dti.getTypeKey();
        synchronized (myDTIKeyToDTIMap)
        {
            if (!myDTIKeyToDTIMap.containsKey(key))
            {
                myDTIKeyToDTIMap.put(key, dti);
                myDTIKeyToCategoryMap.put(key, category);
                myDTIKeyToSourceMap.put(key, source);
                myDECache.addDataType(dti);

                // Detect column types, if needed:
                if (dti.getMetaDataInfo().isAutoDetectColumnTypes() && dti.getMetaDataInfo().isSpecialKeyExaminationRequired())
                {
                    myColumnTypeDetector.detectColumnTypes(dti.getMetaDataInfo());
                }

                // If there is a DefaultMetaDataInfo as the MetaDataInfo
                // and there are keys, but the original key set is empty
                // copy the keys to the original keys to ensure it happens.
                if (dti.getMetaDataInfo() instanceof DefaultMetaDataInfo && !dti.getMetaDataInfo().getKeyNames().isEmpty()
                        && dti.getMetaDataInfo().getOriginalKeyNames().isEmpty())
                {
                    ((DefaultMetaDataInfo)dti.getMetaDataInfo()).copyKeysToOriginalKeys();
                }

                if (dti.getMapVisualizationInfo() != null && dti.getMapVisualizationInfo().usesMapDataElements())
                {
                    if (dti.getMapVisualizationInfo().usesVisualizationStyles())
                    {
                        myDTIKeyToTransformerMap.put(key, new StyleMapDataElementTransformer(myToolBox, dti, source, category,
                                getGeomRegistryUpdateTaskActivity()));
                    }
                    else
                    {
                        myDTIKeyToTransformerMap.put(key, new DefaultMapDataElementTransformer(myToolBox, dti, source, category,
                                getGeomRegistryUpdateTaskActivity()));
                    }
                }

                added = true;
            }
        }

        if (added)
        {
            myToolBox.getEventManager().publishEvent(new DataTypeAddedEvent(dti, originator));
            synchronized (this)
            {
                if (myCurrentDataTypeInfo == null)
                {
                    setCurrentDataType(dti, originator);
                }
            }
        }
    }

    @Override
    public boolean removeDataType(DataTypeInfo dti, Object originator)
    {
        if (dti == null)
        {
            return false;
        }

        boolean removed = false;
        String key = dti.getTypeKey();
        synchronized (myDTIKeyToDTIMap)
        {
            if (myDTIKeyToDTIMap.containsKey(key))
            {
                myDECache.removeDataType(dti);

                myDTIKeyToDTIMap.remove(key);
                myDTIKeyToCategoryMap.remove(key);
                myDTIKeyToSourceMap.remove(key);
                MapDataElementTransformer xFormer = myDTIKeyToTransformerMap.remove(key);
                if (xFormer != null)
                {
                    xFormer.shutdown();
                }

                removed = true;
            }
        }

        if (removed)
        {
            myToolBox.getEventManager().publishEvent(new DataTypeRemovedEvent(dti, originator));
            synchronized (this)
            {
                if (myCurrentDataTypeInfo != null && Utilities.sameInstance(myCurrentDataTypeInfo, dti))
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Setting current data type to NULL - type removed");
                    }
                    myCurrentDataTypeInfo = null;
                }
            }
        }

        return removed;
    }

    @Override
    public boolean removeDataType(String dataTypeInfoKey, Object originator)
    {
        return removeDataType(getDataTypeInfoForType(dataTypeInfoKey), originator);
    }

    @Override
    public List<Long> addDataElements(DataElementProvider dep, Geometry boundingRegion, TimeSpan overallTimespan,
            Object originator)
        throws IllegalStateException
    {
        DataTypeInfo dti = dep.getDataTypeInfo();
        TLongCollection idList = new TLongArrayList();

        int preferredInsertBlockSize = myDECache.getPreferredInsertBlockSize();

        TLongLongHashMap elementIdToCacheIdMap = new TLongLongHashMap();

        List<MapDataElement> mdeList = New.list(preferredInsertBlockSize);
        List<DataElement> deList = New.list(preferredInsertBlockSize);
        long start = System.nanoTime();
        boolean keepReading = true;
        while (keepReading && dep.hasNext())
        {
            try
            {
                DataElement de = dep.next();
                if (de != null && !dep.hadError())
                {
                    idList.add(de.getId());

                    if (de instanceof MapDataElement)
                    {
                        mdeList.add((MapDataElement)de);
                    }
                    else
                    {
                        deList.add(de);
                    }
                    if (deList.size() == preferredInsertBlockSize)
                    {
                        long[] deIds = addDataElements(dti, overallTimespan, deList, originator);
                        updateMap(elementIdToCacheIdMap, deList, deIds);
                        deList = New.list(preferredInsertBlockSize);
                    }
                    if (mdeList.size() == preferredInsertBlockSize)
                    {
                        long[] mdeIds = addMapDataElementsInternal(dti, mdeList);
                        updateMap(elementIdToCacheIdMap, mdeList, mdeIds);
                        mdeList = New.list(preferredInsertBlockSize);
                    }
                }
            }
            catch (NoSuchElementException e)
            {
                LOGGER.error("Aborting read from provider because of exception.", e);
                keepReading = false;
            }
        }
        handleWarningsAndErrors(dep, dti);

        if (!deList.isEmpty())
        {
            long[] deIds = addDataElements(dti, overallTimespan, deList, originator);
            updateMap(elementIdToCacheIdMap, deList, deIds);
        }
        if (!mdeList.isEmpty())
        {
            long[] mdeIds = addMapDataElementsInternal(dti, mdeList);
            updateMap(elementIdToCacheIdMap, mdeList, mdeIds);
        }
        long end = System.nanoTime();
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(StringUtilities.formatTimingMessage("Provider Overall Insert Into Cache of " + idList.size() + " in ",
                    end - start));
        }

        List<Long> orderedIdList = New.list(idList.size());
        for (TLongIterator iter = idList.iterator(); iter.hasNext();)
        {
            long id = iter.next();
            orderedIdList.add(Long.valueOf(elementIdToCacheIdMap.get(id)));
        }
        return orderedIdList;
    }

    @Override
    public long[] addDataElements(DataTypeInfo dti, TimeSpan overallTimespan, Collection<DataElement> dataElements,
            Object originator)
        throws IllegalStateException
    {
        String key = dti.getTypeKey();
        String source;
        String category;
        synchronized (myDTIKeyToDTIMap)
        {
            if (!myDTIKeyToDTIMap.containsKey(key))
            {
                throw new IllegalStateException(
                        "Elements cannot be added until the DataTypeInfo is controlled by the DataTypeController!");
            }
            source = myDTIKeyToSourceMap.get(key);
            category = myDTIKeyToCategoryMap.get(key);
        }
        if (dti.getMetaDataInfo() == null)
        {
            throw new IllegalStateException(
                    "DataElements cannot be added to the DataType \"" + key + "\" because it does not support MetaData");
        }

        return cacheDataElements(category, source, dataElements, null);
    }

    @Override
    public long[] addMapDataElements(DataTypeInfo dti, Geometry boundingRegion, TimeSpan overallTimespan,
            Collection<? extends MapDataElement> dataElements, Object originator)
        throws IllegalStateException
    {
        Utilities.checkNull(dataElements, "dataElements");
        if (dataElements.size() > myDECache.getPreferredInsertBlockSize())
        {
            DataElementProvider provider = new SimpleDataElementProvider(dti, dataElements);
            List<Long> ids = addDataElements(provider, boundingRegion, overallTimespan, originator);
            return CollectionUtilities.toLongArray(ids);
        }
        else
        {
            return addMapDataElementsInternal(dti, dataElements);
        }
    }

    @Override
    public void removeDataElements(DataTypeInfo dtiHint, long[] ids)
    {
        Utilities.checkNull(ids, "ids");
        if (ids.length == 0)
        {
            return;
        }

        List<String> dtiKeyList;
        if (dtiHint != null)
        {
            dtiKeyList = Collections.singletonList(dtiHint.getTypeKey());
        }
        else
        {
            List<Long> idList = CollectionUtilities.listView(ids);
            dtiKeyList = myDECache.getDataTypeInfoKeys(idList);
        }

        synchronized (myDTIKeyToDTIMap)
        {
            for (String dtiKey : dtiKeyList)
            {
                MapDataElementTransformer xFormer = myDTIKeyToTransformerMap.get(dtiKey);
                if (xFormer != null)
                {
                    xFormer.removeMapDataElements(ids);
                }
            }
        }

        myDECache.remove(CollectionUtilities.listView(ids), false);
    }

    @Override
    public Set<String> getAllDataTypeInfoKeys()
    {
        Set<String> keys;
        synchronized (myDTIKeyToDTIMap)
        {
            keys = New.set(myDTIKeyToDTIMap.keySet());
        }
        return keys;
    }

    @Override
    public synchronized DataTypeInfo getCurrentDataType()
    {
        return myCurrentDataTypeInfo;
    }

    @Override
    public List<DataTypeInfo> getDataTypeInfo()
    {
        List<DataTypeInfo> dtiList;
        synchronized (myDTIKeyToDTIMap)
        {
            dtiList = New.list(myDTIKeyToDTIMap.values());
        }
        return dtiList;
    }

    @Override
    public DataTypeInfo getDataTypeInfoForGeometryId(long geomId)
    {
        DataTypeInfo result = null;
        synchronized (myDTIKeyToDTIMap)
        {
            for (MapDataElementTransformer xFormer : myDTIKeyToTransformerMap.values())
            {
                long id = xFormer.getDataModelIdFromGeometryId(geomId);
                if (id != -1)
                {
                    result = xFormer.getDataType();
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public DataTypeInfo getDataTypeInfoForType(String dtiKey)
    {
        synchronized (myDTIKeyToDTIMap)
        {
            return myDTIKeyToDTIMap.get(dtiKey);
        }
    }

    @Override
    public long getElementIdForGeometryId(long geomId)
    {
        synchronized (myDTIKeyToDTIMap)
        {
            for (MapDataElementTransformer xFormer : myDTIKeyToTransformerMap.values())
            {
                long id = xFormer.getDataModelIdFromGeometryId(geomId);
                if (id != -1)
                {
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.controller.DataTypeController#getTransformerForType(java.lang.String)
     */
    @Override
    public MapDataElementTransformer getTransformerForType(String dtiKey)
    {
        return myDTIKeyToTransformerMap.get(dtiKey);
    }

    @Override
    public boolean hasDataTypeInfoForTypeKey(String dtiKey)
    {
        return getDataTypeInfoForType(dtiKey) != null;
    }

    @Override
    public void setCurrentDataType(DataTypeInfo dti, Object source)
    {
        boolean set = false;

        synchronized (this)
        {
            if (!Utilities.sameInstance(dti, myCurrentDataTypeInfo))
            {
                myCurrentDataTypeInfo = dti;
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Setting current data type to: "
                            + (myCurrentDataTypeInfo == null ? "NULL" : myCurrentDataTypeInfo.getDisplayName()));
                }
                set = true;
            }
        }

        if (set)
        {
            myToolBox.getEventManager().publishEvent(new CurrentDataTypeChangedEvent(dti, source));
        }
    }

    /**
     * Internal addMapDataElements.
     *
     * @param dti the {@link DataTypeInfo}
     * @param dataElements the data elements
     * @return the long[]
     * @throws IllegalStateException the illegal state exception
     */
    private long[] addMapDataElementsInternal(DataTypeInfo dti, Collection<? extends MapDataElement> dataElements)
        throws IllegalStateException
    {
        String key = dti.getTypeKey();
        String source;
        String category;
        MapDataElementTransformer xFormer;
        synchronized (myDTIKeyToDTIMap)
        {
            boolean inModel = myDTIKeyToDTIMap.containsKey(key);
            if (!inModel)
            {
                addDataType(toString(), toString(), dti, this);
                inModel = myDTIKeyToDTIMap.containsKey(key);
            }
            if (!inModel)
            {
                throw new IllegalStateException(
                        "Elements cannot be added until the DataTypeInfo is controlled by the DataTypeController!");
            }

            source = myDTIKeyToSourceMap.get(key);
            category = myDTIKeyToCategoryMap.get(key);
            xFormer = myDTIKeyToTransformerMap.get(key);
        }
        if (xFormer == null)
        {
            throw new IllegalStateException("The specified DataTypeInfo \"" + key + "\" does not support MapDataElements");
        }

        // perform normalizations
        normalizeData(dataElements, dti);
        // add to data cache
        return cacheDataElements(category, source, dataElements, ids -> xFormer.addMapDataElements(dataElements, ids));
    }

    /**
     * Inserts the specified data elements into cache using the provided
     * category and source identifiers. The transformer function, if present, is
     * also invoked.
     *
     * @param category the category identifier
     * @param source the source identifier
     * @param data the data elements to be inserted
     * @param xFormer if present, a transformer for map data elements
     * @return the ids of the inserted elements
     * @throws CacheException in case the cache has a problem
     */
    private long[] cacheDataElements(String category, String source, Collection<? extends DataElement> data,
            Consumer<long[]> xFormer)
    {
        try
        {
            long start = System.nanoTime();
            long[] ids = myDECache.insert(category, source, data);
            long end = System.nanoTime();
            if (xFormer != null)
            {
                xFormer.accept(ids);
            }
            logDataElementInsertionMessage(data.size(), ids, start, end);
            return ids;
        }
        catch (CacheException e)
        {
            LOGGER.error("Encountered error trying to cache elements.", e);
        }
        return null;
    }

    /**
     * Operate on the collection of incoming data elements, if necessary, to
     * conform to system requirements. In particular, all altitude values that
     * appear to be expressed in units other than meters are converted to meters
     * for consistency.
     *
     * @param data the incoming data elements
     * @param type the layer to which the data elements belong
     */
    private void normalizeData(Collection<? extends MapDataElement> data, DataTypeInfo type)
    {
        MetaDataInfo metaInf = type.getMetaDataInfo();
        // get the altitude key--if it does not exist, we are done
        String altKey = metaInf.getKeyForSpecialType(AltitudeKey.DEFAULT);
        if (altKey == null)
        {
            return;
        }
        // get the altitude unit key--again, it must exist or we are done
        String unitKey = findAltUnit(metaInf.getKeyNames());
        if (unitKey == null)
        {
            return;
        }

        // examine each data element
        for (MapDataElement elt : data)
        {
            MetaDataProvider mdp = elt.getMetaData();
            Object altObj = mdp.getValue(altKey);
            double altVal = doubleVal(altObj);
            // no altitude value => no conversion is possible
            if (Double.isNaN(altVal))
            {
                continue;
            }
            Object unitObj = mdp.getValue(unitKey);
            if (!(unitObj instanceof String))
            {
                continue;
            }
            double scale = meterConversion((String)unitObj);
            // scale factor is one => no conversion is necessary
            if (scale == 1.0)
            {
                continue;
            }
            double newAlt = altVal * scale;

            // replace the "metadata"
            mdp.setValue(unitKey, "M");
            mdp.setValue(altKey, newAlt);

            // alter the map geometry
            MapGeometrySupport geom = elt.getMapGeometrySupport();
            if (geom instanceof MapLocationGeometrySupport)
            {
                convertAltitude((MapLocationGeometrySupport)geom, newAlt);
            }
        }
    }

    /**
     * Look up the conversion factor for the specified unit name. If the unit is
     * null or is not recognized, then this method returns the value 1.
     *
     * @param unit the unit to convert to meters
     * @return the requested conversion factor
     */
    private static double meterConversion(String unit)
    {
        if (unit == null)
        {
            return 1.0;
        }
        Double ret = scaleMap.get(unit.toUpperCase());
        if (ret != null)
        {
            return ret;
        }
        return 1.0;
    }

    /**
     * Assign a new altitude value for the map geometry.
     *
     * @param geom the map geometry
     * @param newAlt the new altitude value
     */
    private static void convertAltitude(MapLocationGeometrySupport geom, double newAlt)
    {
        LatLonAlt loc = geom.getLocation();
        geom.setLocation(LatLonAlt.createFromDegreesMeters(loc.getLatD(), loc.getLonD(), newAlt, loc.getAltitudeReference()));
    }

    /**
     * Extract the numerical value of the argument as a double, if possible. If
     * the object cannot be interpreted as a number, then the return value will
     * be NaN.
     *
     * @param val some kind of object
     * @return the numerical value of the argument
     */
    private static double doubleVal(Object val)
    {
        if (val == null)
        {
            return Double.NaN;
        }
        if (val instanceof Double)
        {
            return (Double)val;
        }
        if (val instanceof Number)
        {
            return ((Number)val).doubleValue();
        }
        if (val instanceof String)
        {
            return parseOrNaN((String)val);
        }
        return Double.NaN;
    }

    /**
     * Attempt to parse a string value as a double, converting any error
     * condition to NaN.
     *
     * @param val the String to be parsed
     * @return the encoded double, if any, or NaN
     */
    private static double parseOrNaN(String val)
    {
        try
        {
            return Double.parseDouble(val);
        }
        catch (NumberFormatException nfe)
        {
            return Double.NaN;
        }
    }

    /**
     * Search the list for an altitude units field. The first element found to
     * match the regular expression is returned.
     *
     * @param keys the list of keys
     * @return the first matching key, if any, or null
     */
    private static String findAltUnit(List<String> keys)
    {
        for (String k : keys)
        {
            if (altUnitRegex.matcher(k).matches())
            {
                return k;
            }
        }
        return null;
    }

    /**
     * Construct a regular expression that matches a substring of a given text.
     * Note: this method does not escape metacharacters that appear in the
     * input.
     *
     * @param text the text to be partially matched
     * @param min the minimum number of matching characters
     * @return the requested regular expression
     */
    private static String substringRegex(String text, int min)
    {
        StringBuilder buf = new StringBuilder(text.substring(0, min));
        for (int i = min; i < text.length(); i++)
        {
            buf.append("(?:").append(text.charAt(i));
        }
        for (int i = min; i < text.length(); i++)
        {
            buf.append(")?");
        }
        return buf.toString();
    }

    /**
     * Gets the geom registry update task activity.
     *
     * @return the geom registry update task activity
     */
    private synchronized TransformerGeomRegistryUpdateTaskActivity getGeomRegistryUpdateTaskActivity()
    {
        if (myGeomRegistryUpdateTaskActivity == null)
        {
            myGeomRegistryUpdateTaskActivity = new TransformerGeomRegistryUpdateTaskActivity();
            myToolBox.getUIRegistry().getMenuBarRegistry().addTaskActivity(myGeomRegistryUpdateTaskActivity);
        }
        return myGeomRegistryUpdateTaskActivity;
    }

    /**
     * Handle warnings and errors.
     *
     * @param dep the {@link DataElementProvider}
     * @param dti the {@link DataTypeInfo}
     */
    private void handleWarningsAndErrors(DataElementProvider dep, DataTypeInfo dti)
    {
        if (dep.hadWarning())
        {
            String warningMessage = null;
            if (dep.getWarningMessages() != null && !dep.getWarningMessages().isEmpty())
            {
                warningMessage = createCompositeMessage("Warnings were encountered while loading data source \""
                        + dti.getDisplayName() + "\"\nmessages as follows:\n", dep.getWarningMessages());
            }
            else
            {
                warningMessage = "A warning was encountered while loading data source \"" + dti.getDisplayName()
                        + "\"\nno details were provided.\nSee logs for possible additional inforamtion.";
            }
            Notify.warn(warningMessage, Method.ALERT);
        }
        if (dep.hadError())
        {
            String errorMessage = null;
            if (dep.getErrorMessages() != null && !dep.getErrorMessages().isEmpty())
            {
                errorMessage = createCompositeMessage("Errors were encountered while loading data source \""
                        + dti.getDisplayName() + "\"\nmessages as follows:\n", dep.getErrorMessages());
            }
            else
            {
                errorMessage = "An error was encountered while loading data source \"" + dti.getDisplayName()
                        + "\"\nno details were provided.\nSee logs for possible additional inforamtion.";
            }
            Notify.error(errorMessage, Method.ALERT);
        }
    }

    /**
     * Creates the composite message.
     *
     * @param header the header for the message.
     * @param warningMessages the warning messages
     * @return the string
     */
    private static String createCompositeMessage(String header, List<String> warningMessages)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        if (warningMessages != null && !warningMessages.isEmpty())
        {
            for (String msg : warningMessages)
            {
                sb.append("  ").append(msg);
                if (!msg.endsWith("\n"))
                {
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }

    /**
     * Utility method to update the elementIdToCacheIdMap.
     *
     * @param elementIdToCacheIdMap The element id to cache id map
     * @param deList The data element list
     * @param deIds The data element ids
     */
    private static void updateMap(TLongLongHashMap elementIdToCacheIdMap, List<? extends DataElement> deList, long[] deIds)
    {
        for (int i = 0, n = deList.size(); i < n; i++)
        {
            elementIdToCacheIdMap.put(deList.get(i).getId(), deIds[i]);
        }
    }

    /**
     * Log insertion message.
     *
     * @param total the total
     * @param ids the ids
     * @param start the start
     * @param end the end
     */
    private static void logDataElementInsertionMessage(int total, long[] ids, long start, long end)
    {
        if (LOGGER.isDebugEnabled())
        {
            int filtered = filteredCount(ids);
            int inserted = total - filtered;
            StringBuilder sb = new StringBuilder();
            sb.append("Insert Into Cache of ");
            sb.append(inserted);
            if (filtered > 0)
            {
                sb.append(" [filtered ").append(filtered).append(" of ").append(total).append(" on insert]");
            }
            sb.append(" in ");
            LOGGER.debug(StringUtilities.formatTimingMessage(sb.toString(), end - start));
        }
    }

    /**
     * Gets the count of ids filtered in the id array.
     *
     * @param ids the ids to count
     * @return the count of ids that were filtered.
     */
    private static int filteredCount(long[] ids)
    {
        int count = 0;
        for (long id : ids)
        {
            if (id == DataElement.FILTERED)
            {
                count++;
            }
        }
        return count;
    }
}
