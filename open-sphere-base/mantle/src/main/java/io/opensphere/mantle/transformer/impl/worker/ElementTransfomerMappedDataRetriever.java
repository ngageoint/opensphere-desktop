package io.opensphere.mantle.transformer.impl.worker;

import java.util.List;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.CacheEntryView;
import io.opensphere.mantle.data.cache.CacheIdQuery;
import io.opensphere.mantle.data.cache.CacheQueryException;
import io.opensphere.mantle.data.cache.QueryAccessConstraint;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class ElementTransfomerDataRetriever.
 */
public class ElementTransfomerMappedDataRetriever extends AbstractElementTransfomerDataRetriever
{
    /** The Id to element data map. */
    private final TLongObjectHashMap<ElementData> myIdToElementDataMap;

    /**
     * Instantiates a new element transfomer data retriever.
     *
     * @param tb the tb
     * @param dti the dti
     * @param idsOfInterest the ids of interest
     * @param retrieveVS the retrieve vs
     * @param retrieveTSs the retrieve t ss
     * @param retrieveMDPs the retrieve md ps
     * @param retrieveMGS the retrieve mgs
     */
    public ElementTransfomerMappedDataRetriever(Toolbox tb, DataTypeInfo dti, List<Long> idsOfInterest, boolean retrieveVS,
            boolean retrieveTSs, boolean retrieveMDPs, boolean retrieveMGS)
    {
        super(tb, dti, idsOfInterest, retrieveVS, retrieveTSs, retrieveMDPs, retrieveMGS);
        myIdToElementDataMap = new TLongObjectHashMap<>(idsOfInterest.size());
    }

    /**
     * Clear.
     */
    public void clear()
    {
        myIdToElementDataMap.clear();
    }

    /**
     * Gets the data for the given id, after retrieval.
     *
     * @param id the id
     * @return the data
     */
    public ElementData getData(long id)
    {
        return myIdToElementDataMap.get(id);
    }

    /**
     * Retrieve data.
     */
    @Override
    public void retrieveData()
    {
        myIdToElementDataMap.clear();
        MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataElementCache().query(new RetrieveCacheIdQuery());
    }

    /**
     * The Class RetrieveCacheIdQuery.
     */
    private class RetrieveCacheIdQuery extends CacheIdQuery
    {
        /**
         * Instantiates a new retrieve cache id query.
         */
        public RetrieveCacheIdQuery()
        {
            super(getIdsOfInterest(),
                    new QueryAccessConstraint(false, isRetrieveVS(), false, isRetrieveMDPs(), isRetrieveMGSs()));
        }

        @Override
        public void finalizeQuery()
        {
        }

        @Override
        public void notFound(Long id)
        {
            myIdToElementDataMap.put(id.longValue(), new ElementData(id, null, null, null, null));
        }

        @Override
        public void process(Long id, CacheEntryView entry) throws CacheQueryException
        {
            VisualizationState vs = null;
            MetaDataProvider mdp = null;
            MapGeometrySupport mgs = null;
            TimeSpan ts = null;
            if (isRetrieveTSs())
            {
                ts = entry.getTime();
            }
            if (isRetrieveMDPs() && entry.getLoadedElementData() != null && entry.getLoadedElementData().getMetaData() != null)
            {
                mdp = MDILinkedMetaDataProvider.createImmutableBackedMetaDataProvider(getDTI().getMetaDataInfo(),
                        entry.getLoadedElementData().getMetaData());
            }
            if (isRetrieveMGSs())
            {
                mgs = entry.getLoadedElementData().getMapGeometrySupport();
            }
            if (isRetrieveVS())
            {
                vs = entry.getVisState();
            }
            myIdToElementDataMap.put(id.longValue(), new ElementData(id, ts, vs, mdp, mgs));
        }
    }
}
