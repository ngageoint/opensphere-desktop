package io.opensphere.mantle.data.util.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.rangeset.RangedLongSet;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.CacheEntryView;
import io.opensphere.mantle.data.cache.CacheQueryException;
import io.opensphere.mantle.data.cache.QueryAccessConstraint;
import io.opensphere.mantle.data.cache.query.SimpleListResultCacheIdQuery;
import io.opensphere.mantle.data.cache.query.SimpleResultCacheIdQuery;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.DynamicMetaDataList;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Assistant utility for finding components for a DataElement within a registry.
 */
@SuppressWarnings("PMD.GodClass")
public class DataElementLookupUtilsImpl implements DataElementLookupUtils
{
    /** The Constant DATA_ELEMENT_IDS. */
    private static final String DATA_ELEMENT_IDS = "dataElementIds";

    /** The Constant DTI_KEY_PARAMETER_NAME. */
    private static final String DTI_KEY_PARAMETER_NAME = "dtiKey";

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(DataElementLookupUtilsImpl.class);

    /** The Constant TOOLBOX_PARAMETER_NAME. */
    private static final String TOOLBOX_PARAMETER_NAME = "toolbox - tb";

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Checks if is map data element.
     *
     * @param dti the dti
     * @return true, if is map data element
     */
    private static boolean isMapDataElement(DataTypeInfo dti)
    {
        Utilities.checkNull(dti, "dti");
        boolean isMapDataElement = false;
        try
        {
            isMapDataElement = dti.getMapVisualizationInfo() != null
                    && dti.getMapVisualizationInfo().getVisualizationType().isMapDataElementType();
        }
        catch (RuntimeException e)
        {
            isMapDataElement = false;
            LOGGER.error(e);
        }
        return isMapDataElement;
    }

    /**
     * Checks if is valid.
     *
     * @param tsArray the ts array
     * @return true, if is valid
     */
    private static boolean isValid(TimeSpan[] tsArray)
    {
        if (tsArray != null && tsArray.length > 0)
        {
            for (TimeSpan ts : tsArray)
            {
                if (ts != null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if is valid.
     *
     * @param tsList the ts array
     * @return true, if is valid
     */
    private static boolean isValid(TimeSpanList tsList)
    {
        if (tsList != null && !tsList.isEmpty())
        {
            for (TimeSpan ts : tsList)
            {
                if (ts != null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Instantiates a new data element lookup utils impl.
     *
     * @param tb the {@link Toolbox}
     */
    public DataElementLookupUtilsImpl(Toolbox tb)
    {
        myToolbox = tb;
    }

    @Override
    public List<Long> filterIdsByTimeOfInterest(TimeSpan[] tsOfInterest, Collection<? extends Long> cacheIds)
    {
        Utilities.checkNull(cacheIds, "origIds");
        Utilities.checkNull(tsOfInterest, "tsOfInterest");

        List<TimeSpan> tsList = getTimespans(cacheIds);

        List<Long> filteredIdList;
        if (!tsList.isEmpty())
        {
            filteredIdList = new ArrayList<>(tsList.size());

            Iterator<? extends Long> idIter = cacheIds.iterator();
            for (TimeSpan ts : tsList)
            {
                Long cacheId = idIter.next();
                if (ts != null)
                {
                    for (TimeSpan toi : tsOfInterest)
                    {
                        if (toi != null && toi.overlaps(ts))
                        {
                            filteredIdList.add(cacheId);
                            break;
                        }
                    }
                }
            }
        }
        else
        {
            filteredIdList = Collections.<Long>emptyList();
        }
        return filteredIdList;
    }

    @Override
    public List<Long> filterIdsByTimeOfInterest(TimeSpanList tsOfInterest, Collection<? extends Long> cacheIds)
    {
        Utilities.checkNull(cacheIds, "origIds");
        Utilities.checkNull(tsOfInterest, "tsOfInterest");

        List<TimeSpan> tsList = getTimespans(cacheIds);

        List<Long> filteredIdList;
        if (!tsList.isEmpty())
        {
            filteredIdList = new ArrayList<>(tsList.size());

            Iterator<? extends Long> idIter = cacheIds.iterator();
            for (TimeSpan ts : tsList)
            {
                Long cacheId = idIter.next();
                if (ts != null && tsOfInterest.intersects(ts))
                {
                    filteredIdList.add(cacheId);
                }
            }
        }
        else
        {
            filteredIdList = Collections.<Long>emptyList();
        }
        return filteredIdList;
    }

    @Override
    public DataElement getDataElement(long dataElementId, DataTypeInfo dtiHint, String dataTypeInfoKeyHint)
    {
        return getDataElement(Long.valueOf(dataElementId), dtiHint, dataTypeInfoKeyHint);
    }

    /**
     * Long overload.
     *
     * @see #getDataElement(long, DataTypeInfo, String)
     * @param dataElementId
     * @param dtiHint
     * @param dataTypeInfoKeyHint
     * @return the data element
     */
    public DataElement getDataElement(Long dataElementId, DataTypeInfo dtiHint, String dataTypeInfoKeyHint)
    {
        DataElement result = null;
        try
        {
            List<DataElement> resultList = retrieveDataElements(Collections.singletonList(dataElementId), dtiHint,
                    dataTypeInfoKeyHint, false);
            result = resultList.get(0);
        }
        catch (DataElementLookupException e)
        {
            // Do nothing, lookup failed.
            LOGGER.error("Failed Lookup for DataElement " + dataElementId, e);
        }
        return result;
    }

    @Override
    public List<Long> getDataElementCacheIds(DataTypeInfo dti, TimeSpan... tsOfInterest)
    {
        Utilities.checkNull(dti, DTI_KEY_PARAMETER_NAME);
        MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(myToolbox);

        List<Long> ids = mtb.getDataElementCache().getElementIdsForTypeAsList(dti);

        if (ids != null && isValid(tsOfInterest))
        {
            ids = filterIdsByTimeOfInterest(tsOfInterest, ids);
        }

        if (ids == null)
        {
            ids = Collections.<Long>emptyList();
        }
        return ids;
    }

    @Override
    public List<Long> getDataElementCacheIds(DataTypeInfo pDTI, TimeSpanList tsOfInterest)
    {
        Utilities.checkNull(pDTI, DTI_KEY_PARAMETER_NAME);
        MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(myToolbox);

        List<Long> ids = mtb.getDataElementCache().getElementIdsForTypeAsList(pDTI);

        if (ids != null && isValid(tsOfInterest))
        {
            ids = filterIdsByTimeOfInterest(tsOfInterest, ids);
        }

        if (ids == null)
        {
            ids = Collections.<Long>emptyList();
        }
        return ids;
    }

    @Override
    public List<Long> getDataElementCacheIds(String dtiKey, TimeSpan... tsOfInterest)
    {
        Utilities.checkNull(dtiKey, DTI_KEY_PARAMETER_NAME);
        MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(myToolbox);
        DataTypeInfo dti = mtb.getDataTypeController().getDataTypeInfoForType(dtiKey);
        return getDataElementCacheIds(dti, tsOfInterest);
    }

    @Override
    public List<Long> getDataElementCacheIds(String dtiKey, TimeSpanList tsOfInterest)
    {
        Utilities.checkNull(dtiKey, DTI_KEY_PARAMETER_NAME);
        MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(myToolbox);
        DataTypeInfo dti = mtb.getDataTypeController().getDataTypeInfoForType(dtiKey);
        return getDataElementCacheIds(dti, tsOfInterest);
    }

    @Override
    public List<DataElement> getDataElements(List<Long> dataElementIds, DataTypeInfo dtiHint, String dataTypeInfoKeyHint,
            boolean ignoreMapGeometrySupport)
        throws DataElementLookupException
    {
        Utilities.checkNull(dataElementIds, DATA_ELEMENT_IDS);
        if (dataElementIds.isEmpty())
        {
            return Collections.emptyList();
        }

        List<DataElement> resultList = retrieveDataElements(dataElementIds, dtiHint, dataTypeInfoKeyHint,
                ignoreMapGeometrySupport);
        return resultList;
    }

    @Override
    public List<DataElement> getDataElements(DataTypeInfo type)
    {
        try
        {
            MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(myToolbox);
            List<Long> ids = mtb.getDataElementCache().getElementIdsForTypeAsList(type);
            return getDataElements(ids, type, type.getTypeKey(), true);
        }
        catch (DataElementLookupException eek)
        {
            LOGGER.error("Error in data lookup", eek);
            return new LinkedList<>();
        }
    }

    @Override
    public DataTypeInfo getDataTypeInfo(long dataElementId)
    {
        return getDataTypeInfo(dataElementId, null);
    }

    @Override
    public DataTypeInfo getDataTypeInfo(long dataElementId, String dtiKeyHint)
    {
        String dtiKey = dtiKeyHint == null ? getDataTypeInfoKey(dataElementId) : dtiKeyHint;
        MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(myToolbox);
        return mtb.getDataTypeController().getDataTypeInfoForType(dtiKey);
    }

    @Override
    public String getDataTypeInfoKey(long dataElementId)
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getDataTypeInfoKey(dataElementId);
    }

    @Override
    public List<String> getDataTypeInfoKeys(List<Long> dataElementIds)
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getDataTypeInfoKeys(dataElementIds);
    }

    @Override
    public Set<String> getDataTypeInfoKeySet(List<Long> dataElementIds)
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getDataTypeInfoKeySet(dataElementIds);
    }

    @Override
    public MapDataElement getMapDataElement(long dataElementId, DataTypeInfo dtiHint, String dataTypeInfoKeyHint,
            boolean ignoreMapGeometrySupport)
    {
        DataElement de = getDataElement(dataElementId, dtiHint, dataTypeInfoKeyHint);
        MapDataElement result = null;
        if (de instanceof MapDataElement)
        {
            result = (MapDataElement)de;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.util.DataElementLookupUtils#getMapDataElements(java.util.List,
     *      io.opensphere.mantle.data.DataTypeInfo, java.lang.String, boolean)
     */
    @Override
    public List<MapDataElement> getMapDataElements(List<Long> dataElementIds, DataTypeInfo dtiHint, String dataTypeInfoKeyHint,
            boolean ignoreMapGeometrySupport)
        throws DataElementLookupException
    {
        Utilities.checkNull(dataElementIds, DATA_ELEMENT_IDS);
        if (dataElementIds.isEmpty())
        {
            return Collections.emptyList();
        }
        List<DataElement> resultList = retrieveDataElements(dataElementIds, dtiHint, dataTypeInfoKeyHint,
                ignoreMapGeometrySupport);

        return resultList.stream().filter(e -> e instanceof MapDataElement).map(e -> (MapDataElement)e)
                .collect(Collectors.toList());
    }

    @Override
    public List<MapGeometrySupport> getMapGeometrySupport(List<Long> dataElementIds)
    {
        Utilities.checkNull(dataElementIds, DATA_ELEMENT_IDS);
        if (dataElementIds.isEmpty())
        {
            return Collections.<MapGeometrySupport>emptyList();
        }
        SimpleListResultCacheIdQuery<MapGeometrySupport> query = new SimpleListResultCacheIdQuery<>(dataElementIds,
                new QueryAccessConstraint(false, false, false, false, true))
        {
            @Override
            public void process(Long id, CacheEntryView entry)
            {
                getIdToResultMap().put(id.longValue(),
                        entry.getLoadedElementData() == null ? null : entry.getLoadedElementData().getMapGeometrySupport());
            }
        };
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(query);
        return query.getResults();
    }

    @Override
    public MapGeometrySupport getMapGeometrySupport(long dataElementId)
    {
        return getMapGeometrySupport(Long.valueOf(dataElementId));
    }

    /**
     * Long overload.
     *
     * @see #getMapGeometrySupport(long)
     * @param dataElementId
     * @return the MapGeometrySupport or null if not found.
     */
    public MapGeometrySupport getMapGeometrySupport(Long dataElementId)
    {
        SimpleResultCacheIdQuery<MapGeometrySupport> query = new SimpleResultCacheIdQuery<>(
                Collections.singletonList(dataElementId), new QueryAccessConstraint(false, false, false, false, true))
        {
            @Override
            public void process(Long id, CacheEntryView entry)
            {
                setResult(entry.getLoadedElementData() == null ? null : entry.getLoadedElementData().getMapGeometrySupport());
            }
        };
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(query);
        return query.getResult();
    }

    @Override
    public List<MapGeometrySupport> getMapGeometrySupport(RangedLongSet dataElementIds)
    {
        return getMapGeometrySupport(CollectionUtilities.listView(dataElementIds.getValues()));
    }

    @Override
    public List<Object> getMetaData(long dataElementId)
    {
        return getMetaData(Long.valueOf(dataElementId));
    }

    /**
     * Long overload.
     *
     * @see #getMetaData(long)
     * @param dataElementId the data element ID for which to get metadata.
     * @return the meta data List or null if id not found.
     */
    public List<Object> getMetaData(Long dataElementId)
    {
        SimpleResultCacheIdQuery<List<Object>> query = new SimpleResultCacheIdQuery<>(Collections.singletonList(dataElementId),
                new QueryAccessConstraint(false, false, false, true, false))
        {
            @Override
            public void process(Long id, CacheEntryView entry)
            {
                setResult(entry.getLoadedElementData() == null ? null : entry.getLoadedElementData().getMetaData());
            }
        };
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(query);
        return query.getResult();
    }

    @Override
    public List<Object> getMetaDataPropertySamples(String keyName, DataTypeInfo dtiHint, String dataTypeInfoKeyHint,
            final int maxSamples, final int maxToQuery)
        throws DataElementLookupException
    {
        DataTypeInfo dti = dtiHint == null ? dataTypeInfoKeyHint != null ? MantleToolboxUtils.getMantleToolbox(myToolbox)
                .getDataTypeController().getDataTypeInfoForType(dataTypeInfoKeyHint) : null : dtiHint;

        screenDataTypeInfoBeforeMetaDataRetrieve(dti, keyName);

        List<Long> dataElementIds = getDataElementCacheIds(dti);
        if (dataElementIds.isEmpty())
        {
            return Collections.<Object>emptyList();
        }

        if (maxToQuery > 0 && dataElementIds.size() > maxToQuery)
        {
            dataElementIds = dataElementIds.subList(0, maxToQuery);
        }

        @SuppressWarnings("null")
        final int keyIndex = dti.getMetaDataInfo().getKeyIndex(keyName);

        SimpleResultCacheIdQuery<List<Object>> query = new SimpleResultCacheIdQuery<>(dataElementIds,
                new QueryAccessConstraint(false, false, false, true, false), new ArrayList<>(maxSamples))
        {
            @Override
            public void notFound(Long id)
            {
                getResult().add(null);
            }

            @SuppressWarnings("PMD.SimplifiedTernary")
            @Override
            public void process(Long id, CacheEntryView entry)
            {
                List<Object> metaData = entry.getLoadedElementData() == null ? null : entry.getLoadedElementData().getMetaData();
                Object value = metaData == null ? null : metaData.size() > keyIndex ? metaData.get(keyIndex) : null;
                if (value != null && (value instanceof String ? !((String)value).isEmpty() : true))
                {
                    getResult().add(value);
                }
                if (getResult().size() == maxSamples)
                {
                    setComplete();
                }
            }
        };
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(query);
        return query.getResult();
    }

    @Override
    public List<Object> getMetaDataPropertyValues(List<Long> dataElementIds, String keyName, DataTypeInfo dtiHint,
            String dataTypeInfoKeyHint)
        throws DataElementLookupException
    {
        Utilities.checkNull(dataElementIds, DATA_ELEMENT_IDS);
        if (dataElementIds.isEmpty())
        {
            return Collections.<Object>emptyList();
        }

        DataTypeInfo dti = dtiHint == null ? getDataTypeInfo(dataElementIds.get(0).longValue(), dataTypeInfoKeyHint) : dtiHint;

        screenDataTypeInfoBeforeMetaDataRetrieve(dti, keyName);

        final int keyIndex = dti.getMetaDataInfo().getKeyIndex(keyName);

        SimpleResultCacheIdQuery<List<Object>> query = new SimpleResultCacheIdQuery<>(dataElementIds,
                new QueryAccessConstraint(false, false, false, true, false), new ArrayList<>(dataElementIds.size()))
        {
            @Override
            public void notFound(Long id)
            {
                getResult().add(null);
            }

            @Override
            public void process(Long id, CacheEntryView entry)
            {
                List<Object> metaData = entry.getLoadedElementData() == null ? null : entry.getLoadedElementData().getMetaData();
                Object value = metaData == null ? null : metaData.size() > keyIndex ? metaData.get(keyIndex) : null;
                getResult().add(value);
            }
        };
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(query);
        return query.getResult();
    }

    @Override
    public MetaDataProvider getMetaDataProvider(long dataElementId)
    {
        return getMetaDataProvider(Long.valueOf(dataElementId));
    }

    /**
     * Long overload.
     *
     * @see #getMetaDataProvider(long)
     * @param dataElementId
     * @return the MetaDataProvider or null if the id is not in the registry or
     *         the DataTypeInfo cannot be located for the data element.
     */
    public MetaDataProvider getMetaDataProvider(Long dataElementId)
    {
        SimpleResultCacheIdQuery<Pair<String, List<Object>>> query = new SimpleResultCacheIdQuery<>(
                Collections.singletonList(dataElementId), new QueryAccessConstraint(false, false, false, true, false))
        {
            @Override
            public void process(Long id, CacheEntryView entry)
            {
                List<Object> metaData = entry.getLoadedElementData() == null ? null : entry.getLoadedElementData().getMetaData();
                String dataTypeKey = entry.getDataTypeKey();
                setResult(new Pair<>(dataTypeKey, metaData));
            }
        };

        MetaDataProvider result = null;
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(query);
        if (query.getResult() != null)
        {
            String dataTypeKey = query.getResult().getFirstObject();
            List<Object> metaData = query.getResult().getSecondObject();
            if (metaData instanceof DynamicMetaDataList)
            {
                result = (MetaDataProvider)metaData;
            }
            else
            {
                DataTypeInfo dti = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController()
                        .getDataTypeInfoForType(dataTypeKey);
                if (dti != null)
                {
                    result = MDILinkedMetaDataProvider.createImmutableBackedMetaDataProvider(dti.getMetaDataInfo(), metaData);
                }
            }
        }
        return result;
    }

    @Override
    public Long getOriginId(long dataElementId)
    {
        return getOriginId(Long.valueOf(dataElementId));
    }

    /**
     * Long overload.
     *
     * @see #getOriginId(long)
     * @param dataElementId
     * @return the origin id or -1 if not found.
     */
    public Long getOriginId(Long dataElementId)
    {
        Long origId = null;
        SimpleResultCacheIdQuery<Long> ciq = new SimpleResultCacheIdQuery<>(Collections.singletonList(dataElementId),
                new QueryAccessConstraint(false, false, true, false, false))
        {
            @Override
            public void process(Long id, CacheEntryView entry)
            {
                setResult(entry.getLoadedElementData() != null ? entry.getLoadedElementData().getOriginId() : null);
            }
        };
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(ciq);
        origId = ciq.getResult();

        if (origId == null)
        {
            return Long.valueOf(-1L);
        }
        return origId;
    }

    @Override
    public TLongObjectHashMap<Long> getOriginIds(List<Long> dataElementIds)
    {
        Utilities.checkNull(myToolbox, TOOLBOX_PARAMETER_NAME);
        Utilities.checkNull(dataElementIds, DATA_ELEMENT_IDS);
        if (dataElementIds.isEmpty())
        {
            return new TLongObjectHashMap<>();
        }

        SimpleResultCacheIdQuery<TLongObjectHashMap<Long>> ciq = new SimpleResultCacheIdQuery<>(dataElementIds,
                new QueryAccessConstraint(false, false, true, false, false))
        {
            @Override
            public void process(Long id, CacheEntryView entry)
            {
                if (getResult() == null)
                {
                    setResult(new TLongObjectHashMap<Long>());
                }
                Long originId = entry.getLoadedElementData() != null ? entry.getLoadedElementData().getOriginId() : null;
                getResult().put(id.longValue(), originId);
            }
        };
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(ciq);

        TLongObjectHashMap<Long> resultMap = ciq.getResult();
        if (resultMap == null)
        {
            resultMap = new TLongObjectHashMap<>();
        }
        return resultMap;
    }

    @Override
    public TimeSpan getTimespan(long dataElementId)
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getTimeSpan(dataElementId);
    }

    @Override
    public List<TimeSpan> getTimespans(Collection<? extends Long> dataElementIds)
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getTimeSpans(dataElementIds);
    }

    @Override
    public VisualizationState getVisualizationState(long dataElementId)
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getVisualizationState(dataElementId);
    }

    @Override
    public List<VisualizationState> getVisualizationStates(List<Long> dataElementIds)
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getVisualizationStates(dataElementIds);
    }

    @Override
    public List<VisualizationState> getVisualizationStates(long[] dataElementIds)
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getVisualizationStates(dataElementIds);
    }

    @Override
    public List<VisualizationState> getVisualizationStates(RangedLongSet dataElementIds)
    {
        return getVisualizationStates(CollectionUtilities.listView(dataElementIds.getValues()));
    }

    /**
     * Retrieves the DataElements by id. The dtiHint and dataTypeInfoKeyHint can
     * help prevent multiple queries from running against the data model. They
     * are used in the dtiHint first, then the dataTypeInfoKeyHint second. If
     * neither hint is provided then they will be queried first so that the
     * remainder of the element can be retrieved and reformed.
     *
     * All of the id's requested must be of the same data type or an exception
     * will be generated.
     *
     * @param dataElementIds the data element ids to lookup
     * @param dtiHint the {@link DataTypeInfo} for the point if known ( null if
     *            not known is okay )
     * @param dataTypeInfoKeyHint the key for the DataTypeInfo if known ( null
     *            if not known is okay )
     * @param ignoreMapGeometrySupport the ignore map data elements map geometry
     *            support ( don't get the extra MGS parts )
     * @return the resulting data elements
     * @throws DataElementLookupException if the dtiHint or dataTypeInfoKeyHint
     *             are the wrong type for any of the ids provided, or if the
     *             types retrieved are of different data types, or if the data
     *             type cannot be determined, or if all the ids cannot be
     *             retrieved.
     */
    private List<DataElement> retrieveDataElements(List<Long> dataElementIds, DataTypeInfo dtiHint, String dataTypeInfoKeyHint,
            boolean ignoreMapGeometrySupport)
        throws DataElementLookupException
    {
        Utilities.checkNull(dataElementIds, DATA_ELEMENT_IDS);
        if (dataElementIds.isEmpty())
        {
            return Collections.emptyList();
        }

        final DataTypeInfo dti = dtiHint == null ? getDataTypeInfo(dataElementIds.get(0).longValue(), dataTypeInfoKeyHint)
                : dtiHint;

        if (dti == null)
        {
            throw new DataElementLookupException("Could not determine DataType");
        }
        final String dtiKey = dti.getTypeKey();

        final boolean isMapDataElement = isMapDataElement(dti);

        SimpleListResultCacheIdQuery<DataElement> query = new SimpleListResultCacheIdQuery<>(dataElementIds,
                new QueryAccessConstraint(true, true, true, true, !ignoreMapGeometrySupport))
        {
            @Override
            public void notFound(Long id)
            {
                getIdToResultMap().put(id.longValue(), null);
            }

            @Override
            public void process(Long id, CacheEntryView entry)
            {
                if (!dtiKey.equals(entry.getDataTypeKey()))
                {
                    throw new CacheQueryException("One or more requested DataElements is not a member of the type: " + dtiKey
                            + " found " + entry.getDataTypeKey() + " for element cache id " + id);
                }

                MetaDataProvider mdp = null;
                MapGeometrySupport mgs = null;
                Long originId = null;
                if (entry.getLoadedElementData() != null)
                {
                    List<Object> metaData = entry.getLoadedElementData().getMetaData();
                    mdp = MDILinkedMetaDataProvider.createImmutableBackedMetaDataProvider(dti.getMetaDataInfo(), metaData);
                    originId = entry.getLoadedElementData().getOriginId();
                    mgs = entry.getLoadedElementData().getMapGeometrySupport();
                }

                if (isMapDataElement)
                {
                    ResultMapDataElement r = new ResultMapDataElement(originId == null ? 0L : originId.longValue(),
                            entry.getTime(), dti, mdp, entry.getVisState(), mgs);
                    r.setIdInCache(id.longValue());
                    getIdToResultMap().put(id.longValue(), r);
                }
                else
                {
                    ResultDataElement r = new ResultDataElement(originId == null ? 0L : originId.longValue(), entry.getTime(),
                            dti, mdp, entry.getVisState());
                    getIdToResultMap().put(id.longValue(), r);
                }
            }
        };
        try
        {
            MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(query);
        }
        catch (CacheQueryException e)
        {
            throw new DataElementLookupException(e.getMessage(), e);
        }

        return query.getResults();
    }

    /**
     * Screen data type info before meta data retrieve.
     *
     * @param dti the dti
     * @param keyName the key name
     * @throws DataElementLookupException the data element lookup exception
     */
    private void screenDataTypeInfoBeforeMetaDataRetrieve(DataTypeInfo dti, String keyName) throws DataElementLookupException
    {
        if (dti == null)
        {
            throw new DataElementLookupException("Could not determine DataType");
        }

        if (dti.getMetaDataInfo() == null)
        {
            throw new DataElementLookupException("DataType has no meta data");
        }

        if (!dti.getMetaDataInfo().hasKey(keyName))
        {
            throw new DataElementLookupException("Key " + keyName + " is not a key for this data type");
        }
    }

    /**
     * The Simple implementation of a {@link DataElement} for lookup results.
     */
    private static class ResultDataElement implements DataElement
    {
        /** The my dti. */
        private final DataTypeInfo myDTI;

        /** The my meta data provider. */
        private final MetaDataProvider myMetaDataProvider;

        /** The my orig id. */
        private final long myOrigId;

        /** The my time span. */
        private final TimeSpan myTimeSpan;

        /** The my visualization state. */
        private final VisualizationState myVisualizationState;

        /**
         * The id in the cache.
         */
        private long myCacheId;

        /**
         * Instantiates a new result data element.
         *
         * @param origId the orig id
         * @param ts the ts
         * @param dti the dti
         * @param mdp the mdp
         * @param vs the vs
         */
        public ResultDataElement(long origId, TimeSpan ts, DataTypeInfo dti, MetaDataProvider mdp, VisualizationState vs)
        {
            myOrigId = origId;
            myTimeSpan = ts;
            myDTI = dti;
            myMetaDataProvider = mdp;
            myVisualizationState = vs;
        }

        @Override
        public DataTypeInfo getDataTypeInfo()
        {
            return myDTI;
        }

        @Override
        public long getId()
        {
            return myOrigId;
        }

        @Override
        public MetaDataProvider getMetaData()
        {
            return myMetaDataProvider;
        }

        @Override
        public TimeSpan getTimeSpan()
        {
            return myTimeSpan;
        }

        @Override
        public VisualizationState getVisualizationState()
        {
            return myVisualizationState;
        }

        @Override
        public boolean isDisplayable()
        {
            return true;
        }

        @Override
        public boolean isMappable()
        {
            return false;
        }

        @Override
        public void setDisplayable(boolean displayable, Object source)
        {
            throw new UnsupportedOperationException("Use visibility flag in VisualizationState");
        }

        @Override
        public long getIdInCache()
        {
            return myCacheId;
        }

        @Override
        public void setIdInCache(long cacheId)
        {
            myCacheId = cacheId;
        }

        /**
         * {@inheritDoc}
         *
         * @see io.opensphere.mantle.data.element.DataElement#cloneForDatatype(io.opensphere.mantle.data.DataTypeInfo)
         */
        @Override
        public DataElement cloneForDatatype(DataTypeInfo datatype)
        {
            ResultDataElement clone = new ResultDataElement(myOrigId * 10, myTimeSpan, datatype, myMetaDataProvider,
                    myVisualizationState);
            return clone;
        }
    }

    /**
     * Simple implementation of a MapDataElement for lookup results.
     */
    private static class ResultMapDataElement extends ResultDataElement implements MapDataElement
    {
        /** The my map geometry support. */
        private final MapGeometrySupport myMapGeometrySupport;

        /**
         * Instantiates a new result map data element.
         *
         * @param origId the orig id
         * @param ts the ts
         * @param dti the dti
         * @param mdp the mdp
         * @param vs the vs
         * @param mgs the mgs
         */
        public ResultMapDataElement(long origId, TimeSpan ts, DataTypeInfo dti, MetaDataProvider mdp, VisualizationState vs,
                MapGeometrySupport mgs)
        {
            super(origId, ts, dti, mdp, vs);
            myMapGeometrySupport = mgs;
        }

        @Override
        public MapGeometrySupport getMapGeometrySupport()
        {
            return myMapGeometrySupport;
        }

        @Override
        public boolean isMappable()
        {
            return true;
        }

        @Override
        public void setMapGeometrySupport(MapGeometrySupport mgs)
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         *
         * @see io.opensphere.mantle.data.util.impl.DataElementLookupUtilsImpl.ResultDataElement#cloneForDatatype(io.opensphere.mantle.data.DataTypeInfo)
         */
        @Override
        public DataElement cloneForDatatype(DataTypeInfo datatype)
        {
            ResultMapDataElement clone = new ResultMapDataElement(getId() * 10, getTimeSpan(), datatype, getMetaData(),
                    getVisualizationState(), myMapGeometrySupport);
            clone.setIdInCache(this.getIdInCache());
            return clone;
        }
    }
}
