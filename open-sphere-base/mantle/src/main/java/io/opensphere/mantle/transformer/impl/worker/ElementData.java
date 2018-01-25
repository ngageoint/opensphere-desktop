package io.opensphere.mantle.transformer.impl.worker;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * The Class ElementData.
 */
public class ElementData
{
    /** The ID. */
    private Long myID;

    /** The Map geometry support. */
    private MapGeometrySupport myMapGeometrySupport;

    /** The Meta data provider. */
    private MetaDataProvider myMetaDataProvider;

    /** The Time span. */
    private TimeSpan myTimeSpan;

    /** The Visualization state. */
    private VisualizationState myVisualizationState;

    /**
     * Instantiates a new element data.
     */
    public ElementData()
    {
        /* intentionally blank */
    }

    /**
     * Instantiates a new element data.
     *
     * @param id the id
     * @param ts the ts
     * @param vs the vs
     * @param mdp the mdp
     * @param mgs the mgs
     */
    public ElementData(Long id, TimeSpan ts, VisualizationState vs, MetaDataProvider mdp, MapGeometrySupport mgs)
    {
        updateValues(id, ts, vs, mdp, mgs);
    }

    /**
     * Found.
     *
     * @return true, if successful
     */
    public boolean found()
    {
        return myVisualizationState != null || myTimeSpan != null || myMetaDataProvider != null || myMapGeometrySupport != null;
    }

    /**
     * Gets the iD.
     *
     * @return the iD
     */
    public Long getID()
    {
        return myID;
    }

    /**
     * Gets the map geometry support.
     *
     * @return the map geometry support
     */
    public MapGeometrySupport getMapGeometrySupport()
    {
        return myMapGeometrySupport;
    }

    /**
     * Gets the meta data provider.
     *
     * @return the meta data provider
     */
    public MetaDataProvider getMetaDataProvider()
    {
        return myMetaDataProvider;
    }

    /**
     * Gets the time span.
     *
     * @return the time span
     */
    public TimeSpan getTimeSpan()
    {
        return myTimeSpan;
    }

    /**
     * Gets the visualization state.
     *
     * @return the visualization state
     */
    public VisualizationState getVisualizationState()
    {
        return myVisualizationState;
    }

    /**
     * Update values.
     *
     * @param id the id
     * @param ts the ts
     * @param vs the vs
     * @param mdp the mdp
     * @param mgs the mgs
     */
    public final void updateValues(Long id, TimeSpan ts, VisualizationState vs, MetaDataProvider mdp, MapGeometrySupport mgs)
    {
        myID = id;
        myVisualizationState = vs;
        myTimeSpan = ts;
        myMetaDataProvider = mdp;
        myMapGeometrySupport = mgs;
    }
}
