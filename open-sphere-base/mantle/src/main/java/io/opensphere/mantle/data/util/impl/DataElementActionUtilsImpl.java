package io.opensphere.mantle.data.util.impl;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.CacheEntryView;
import io.opensphere.mantle.data.cache.CacheQuery;
import io.opensphere.mantle.data.cache.CacheQueryException;
import io.opensphere.mantle.data.cache.QueryAccessConstraint;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.event.DataTypeInfoGeometryRebuildRequestChangeEvent;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.util.DataElementActionUtils;
import io.opensphere.mantle.data.util.purge.PurgeConfirmHelper;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Implementation of utilities package for actions that can be taken with a
 * DataElement.
 */
public class DataElementActionUtilsImpl implements DataElementActionUtils
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataElementActionUtilsImpl.class);

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new data element action utils.
     *
     * @param tb the {@link Toolbox}
     */
    public DataElementActionUtilsImpl(Toolbox tb)
    {
        myToolbox = tb;
    }

    /**
     * Get the system toolbox.
     *
     * @return The toolbox.
     */
    private Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public boolean gotoSelectedFeature(long deId, boolean flyTo)
    {
        long[] id = new long[1];
        id[0] = deId;
        return gotoSelectedFeature(id, flyTo);
    }

    @Override
    public synchronized boolean gotoSelectedFeature(long[] dataElementIds, boolean flyTo)
    {
        GeographicBoundingBox gbb = null;
        Projection proj = getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot();
        for (long deId : dataElementIds)
        {
            MapGeometrySupport mgs = MantleToolboxUtils.getDataElementLookupUtils(myToolbox).getMapGeometrySupport(deId);
            if (mgs != null)
            {
                if (gbb == null)
                {
                    gbb = mgs.getBoundingBox(proj);
                }
                else
                {
                    gbb = GeographicBoundingBox.merge(gbb, mgs.getBoundingBox(proj));
                }
            }
            else
            {
                return false;
            }
        }

        if (gbb != null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug((flyTo ? "FLYTO: " : "GOTO: ") + gbb.toString());
            }
            DynamicViewer view = myToolbox.getMapManager().getStandardViewer();
            ViewerAnimator animator;
            if (gbb.getWidth() > 0.0 || gbb.getHeight() > 0.0)
            {
                animator = new ViewerAnimator(view, gbb.getVertices(), true);
            }
            else
            {
                animator = new ViewerAnimator(view, gbb.getCenter());
            }
            if (flyTo)
            {
                animator.start();
            }
            else
            {
                animator.snapToPosition();
            }
        }

        return true;
    }

    @Override
    public void purgeDataElements(final DataTypeInfo dtiHint, final long[] idsToPurge, Component confirmDialogParentComponent)
    {
        if (PurgeConfirmHelper.confirmProceedWithPurge(myToolbox, confirmDialogParentComponent, this))
        {
            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController().removeDataElements(dtiHint,
                            idsToPurge);
                }
            });
            t.start();
        }
    }

    @Override
    public int purgeDataElements(final Predicate<? super VisualizationState> vsFilter, Set<DataTypeInfo> dtiSet,
            Set<TimeSpan> tsOfInterest, Component confirmDialogParentComponent)
    {
        Utilities.checkNull(vsFilter, "vsFilter");
        List<Long> resultIds = null;
        if (PurgeConfirmHelper.confirmProceedWithPurge(myToolbox, confirmDialogParentComponent, this))
        {
            PurgeCacheQuery purgeQuery = new PurgeCacheQuery(vsFilter, dtiSet, tsOfInterest);
            MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().query(purgeQuery);
            resultIds = purgeQuery.getResultIds();
            if (resultIds != null && !resultIds.isEmpty())
            {
                long[] idArray = CollectionUtilities.toLongArray(resultIds);
                MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController().removeDataElements(null, idArray);
            }
        }
        return resultIds == null ? 0 : resultIds.size();
    }

    @Override
    public void queryPointRadiusFromDataElmentCenters(List<Long> dataElementIds, double radiusMeters, Object source)
    {
        List<MapGeometrySupport> mgsList = MantleToolboxUtils.getDataElementLookupUtils(myToolbox)
                .getMapGeometrySupport(dataElementIds);
        if (mgsList != null)
        {
            for (MapGeometrySupport mgs : mgsList)
            {
                GeographicBoundingBox bb = mgs
                        .getBoundingBox(getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot());
                if (bb != null)
                {
                    LatLonAlt center = bb.getCenter().getLatLonAlt();
                    LatLonAlt edge = GeographicBody3D.greatCircleEndPosition(center, 0, WGS84EarthConstants.RADIUS_MEAN_M,
                            radiusMeters);
                    Polygon pg = JTSUtilities.createCircle(center, edge, JTSUtilities.NUM_CIRCLE_SEGMENTS);
                    List<PolygonGeometry> selectionBounds = JTSCoreGeometryUtilities.buildSelectionPolygonSet(pg, Color.blue);
                    MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(myToolbox);
                    mantleToolbox.getQueryRegionManager().addQueryRegion(selectionBounds,
                            mantleToolbox.getDataGroupController().getQueryableDataTypeKeys());
                }
            }
        }
    }

    @Override
    public void requestGeometryRebuild(DataTypeInfo dti, Object source)
    {
        DataTypeInfoGeometryRebuildRequestChangeEvent evt = new DataTypeInfoGeometryRebuildRequestChangeEvent(dti, source);
        myToolbox.getEventManager().publishEvent(evt);
    }

    /**
     * The Class PurgeCacheQuery. Constructs a query to the data element cache
     * that filters elements by visualization state, data type(optional), and
     * time(optional), and returns a set of cache ids that can be used to remove
     * the data elements from the application.
     */
    private static class PurgeCacheQuery extends CacheQuery
    {
        /** The DataTypeInfo Key Set to filter. */
        private Set<String> myDTIKeySet;

        /** The filter on data type info flag. */
        private final boolean myFilterOnDTI;

        /** The Id results. */
        private List<Long> myIdResults;

        /** The VisualizationState filter. */
        private final Predicate<? super VisualizationState> myVisStateFilter;

        /**
         * Instantiates a new purge cache query.
         *
         * @param visStateFilter the vis state filter
         * @param dtiSet the dti set
         * @param tsOfInterest the ts of interest
         */
        @SuppressWarnings("null")
        public PurgeCacheQuery(Predicate<? super VisualizationState> visStateFilter, Set<DataTypeInfo> dtiSet,
                Set<TimeSpan> tsOfInterest)
        {
            super(new QueryAccessConstraint(true, true, false, false, false));
            myIdResults = new LinkedList<>();
            myVisStateFilter = visStateFilter;
            if (tsOfInterest != null && !tsOfInterest.isEmpty())
            {
                setTimesOfInterest(tsOfInterest.toArray(new TimeSpan[tsOfInterest.size()]));
            }

            myFilterOnDTI = dtiSet != null && !dtiSet.isEmpty();
            if (myFilterOnDTI)
            {
                myDTIKeySet = new HashSet<>();
                for (DataTypeInfo dti : dtiSet)
                {
                    myDTIKeySet.add(dti.getTypeKey());
                }
            }
        }

        @Override
        public boolean accepts(CacheEntryView entry) throws CacheQueryException
        {
            boolean accept = true;
            if (myVisStateFilter != null)
            {
                accept = myVisStateFilter.test(entry.getVisState());
            }
            if (accept && myFilterOnDTI)
            {
                accept = myDTIKeySet.contains(entry.getDataTypeKey());
            }
            return accept;
        }

        @Override
        public void finalizeQuery()
        {
            myIdResults = new ArrayList<>(myIdResults);
        }

        /**
         * Gets the result ids.
         *
         * @return the result ids
         */
        public List<Long> getResultIds()
        {
            return myIdResults;
        }

        @Override
        public void process(Long id, CacheEntryView entry) throws CacheQueryException
        {
            myIdResults.add(id);
        }
    }
}
