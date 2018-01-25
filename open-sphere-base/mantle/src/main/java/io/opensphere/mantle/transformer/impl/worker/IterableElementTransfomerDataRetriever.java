package io.opensphere.mantle.transformer.impl.worker;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
public class IterableElementTransfomerDataRetriever extends AbstractElementTransfomerDataRetriever
        implements Iterable<ElementData>
{
    /** The ID list. */
    private List<Long> myIDList;

    /** The MD list. */
    private List<List<Object>> myMDList;

    /** The MGS list. */
    private List<MapGeometrySupport> myMGSList;

    /** The TS list. */
    private List<TimeSpan> myTSList;

    /** The VS list. */
    private List<VisualizationState> myVSList;

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
    public IterableElementTransfomerDataRetriever(Toolbox tb, DataTypeInfo dti, List<Long> idsOfInterest, boolean retrieveVS,
            boolean retrieveTSs, boolean retrieveMDPs, boolean retrieveMGS)
    {
        super(tb, dti, idsOfInterest, retrieveVS, retrieveTSs, retrieveMDPs, retrieveMGS);
    }

    @Override
    public Iterator<ElementData> iterator()
    {
        return new Iterator<ElementData>()
        {
            private final ElementData myED = new ElementData();

            private final Iterator<Long> myIDItr = myIDList.iterator();

            private final Iterator<List<Object>> myMDItr = isRetrieveMDPs() ? myMDList.iterator() : null;

            private final Iterator<MapGeometrySupport> myMGSItr = isRetrieveMGSs() ? myMGSList.iterator() : null;

            private final Iterator<TimeSpan> myTSItr = isRetrieveTSs() ? myTSList.iterator() : null;

            private final Iterator<VisualizationState> myVSItr = isRetrieveVS() ? myVSList.iterator() : null;

            @Override
            public boolean hasNext()
            {
                return myIDItr.hasNext();
            }

            @Override
            public ElementData next()
            {
                MetaDataProvider mdp = null;
                if (isRetrieveMDPs())
                {
                    List<Object> objList = myMDItr.next();
                    if (objList != null)
                    {
                        mdp = MDILinkedMetaDataProvider.createImmutableBackedMetaDataProvider(getDTI().getMetaDataInfo(),
                                objList);
                    }
                }

                Long idVal = myIDItr.next();
                TimeSpan tsVal = null;
                if (isRetrieveTSs())
                {
                    tsVal = myTSItr.next();
                }
                VisualizationState vsVal = null;
                if (isRetrieveVS())
                {
                    vsVal = myVSItr.next();
                }
                MapGeometrySupport mgsVal = null;
                if (isRetrieveMGSs())
                {
                    mgsVal = myMGSItr.next();
                }

                myED.updateValues(idVal, tsVal, vsVal, mdp, mgsVal);
                return myED;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Retrieve data.
     */
    @Override
    public void retrieveData()
    {
        myIDList = new LinkedList<>();
        myTSList = isRetrieveTSs() ? new LinkedList<TimeSpan>() : null;
        myVSList = isRetrieveVS() ? new LinkedList<VisualizationState>() : null;
        myMDList = isRetrieveMDPs() ? new LinkedList<List<Object>>() : null;
        myMGSList = isRetrieveMGSs() ? new LinkedList<MapGeometrySupport>() : null;
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
            myIDList.add(id);
            if (isRetrieveTSs())
            {
                myTSList.add(null);
            }
            if (isRetrieveMDPs())
            {
                myMDList.add(null);
            }
            if (isRetrieveMGSs())
            {
                myMGSList.add(null);
            }
            if (isRetrieveVS())
            {
                myVSList.add(null);
            }
        }

        @Override
        public void process(Long id, CacheEntryView entry) throws CacheQueryException
        {
            myIDList.add(id);
            if (isRetrieveTSs())
            {
                myTSList.add(entry.getTime());
            }
            if (isRetrieveMDPs())
            {
                myMDList.add(entry.getLoadedElementData().getMetaData());
            }
            if (isRetrieveMGSs())
            {
                myMGSList.add(entry.getLoadedElementData().getMapGeometrySupport());
            }
            if (isRetrieveVS())
            {
                myVSList.add(entry.getVisState());
            }
        }
    }
}
