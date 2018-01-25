package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;

/**
 * The Class DefaultFeatureIndividualGeometryBuilderData.
 */
public class DefaultFeatureIndividualGeometryBuilderData implements FeatureIndividualGeometryBuilderData
{
    /** The DataTypeInfo. */
    private DataTypeInfo myDTI;

    /** The Element id. */
    private long myElementId;

    /** The Geometry id. */
    private long myGeometryId;

    /** The MetaDataProvider. */
    private MetaDataProvider myMDP;

    /** The MapGeometrySupport. */
    private MapGeometrySupport myMGS;

    /** The VisualizationState. */
    private VisualizationState myVS;

    /**
     * Instantiates a new default feature individual geometry builder data.
     */
    public DefaultFeatureIndividualGeometryBuilderData()
    {
    }

    /**
     * Copy CTOR. (not a deep copy).
     *
     * @param other the other
     */
    public DefaultFeatureIndividualGeometryBuilderData(FeatureIndividualGeometryBuilderData other)
    {
        set(other.getElementId(), other.getGeomId(), other.getDataType(), other.getVS(), other.getMGS(), other.getMDP());
    }

    /**
     * Instantiates a new default feature individual geometry builder data.
     *
     * @param elemId the element id
     * @param geomId the geometry id
     * @param dti the {@link DataTypeInfo}
     * @param vs the {@link VisualizationState}
     * @param mgs the {@link MapGeometrySupport}
     * @param mdp the {@link MetaDataProvider}
     */
    public DefaultFeatureIndividualGeometryBuilderData(long elemId, long geomId, DataTypeInfo dti, VisualizationState vs,
            MapGeometrySupport mgs, MetaDataProvider mdp)
    {
        set(elemId, geomId, dti, vs, mgs, mdp);
    }

    @Override
    public DataTypeInfo getDataType()
    {
        return myDTI;
    }

    @Override
    public long getElementId()
    {
        return myElementId;
    }

    @Override
    public long getGeomId()
    {
        return myGeometryId;
    }

    @Override
    public MetaDataProvider getMDP()
    {
        return myMDP;
    }

    @Override
    public MapGeometrySupport getMGS()
    {
        return myMGS;
    }

    @Override
    public VisualizationState getVS()
    {
        return myVS;
    }

    /**
     * Sets all fields.
     *
     * @param elemId the element id
     * @param geomId the geometry id
     * @param dti the {@link DataTypeInfo}
     * @param vs the {@link VisualizationState}
     * @param mgs the {@link MapGeometrySupport}
     * @param mdp the {@link MetaDataProvider}
     */
    public final void set(long elemId, long geomId, DataTypeInfo dti, VisualizationState vs, MapGeometrySupport mgs,
            MetaDataProvider mdp)
    {
        myElementId = elemId;
        myGeometryId = geomId;
        myDTI = dti;
        myVS = vs;
        myMGS = mgs;
        myMDP = mdp;
    }

    /**
     * Sets the {@link DataTypeInfo}.
     *
     * @param dti the {@link DataTypeInfo}
     */
    public void setDataType(DataTypeInfo dti)
    {
        myDTI = dti;
    }

    /**
     * Sets the element id.
     *
     * @param elementId the new element id
     */
    public void setElementId(long elementId)
    {
        myElementId = elementId;
    }

    /**
     * Sets the geometry id.
     *
     * @param geometryId the new geometry id
     */
    public void setGeomId(long geometryId)
    {
        myGeometryId = geometryId;
    }

    /**
     * Sets the {@link MetaDataProvider}.
     *
     * @param mdp the new {@link MetaDataProvider}
     */
    public void setMDP(MetaDataProvider mdp)
    {
        myMDP = mdp;
    }

    /**
     * Sets the {@link MapGeometrySupport}.
     *
     * @param mgs the new {@link MapGeometrySupport}
     */
    public void setMGS(MapGeometrySupport mgs)
    {
        myMGS = mgs;
    }

    /**
     * Sets the {@link VisualizationState}.
     *
     * @param vs the new {@link VisualizationState}
     */
    public void setVS(VisualizationState vs)
    {
        myVS = vs;
    }
}
