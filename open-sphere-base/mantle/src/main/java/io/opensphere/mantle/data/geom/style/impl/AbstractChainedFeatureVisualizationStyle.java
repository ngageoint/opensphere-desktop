package io.opensphere.mantle.data.geom.style.impl;

import java.util.Map;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.ChainedFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;

/**
 * Abstract class representation of a chained FeatureVisualizationStyle. Defines
 * only methods that apply to the chaining aspect of its functionality.
 */
public abstract class AbstractChainedFeatureVisualizationStyle extends AbstractFeatureVisualizationStyle
        implements ChainedFeatureVisualizationStyle
{
    /** The previous VisualizationStyle in the chain. May be null. */
    private FeatureVisualizationStyle myPreviousStyle;

    /**
     * Instantiates a new abstract chained feature visualization style.
     *
     * @param tb the toolbox
     */
    public AbstractChainedFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new abstract chained feature visualization style.
     *
     * @param tb the toolbox
     * @param dtiKey the datatype key
     */
    public AbstractChainedFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public boolean allChangesRequireRebuild()
    {
        return hasPrevious() && getPrevious().allChangesRequireRebuild();
    }

    @Override
    public AbstractChainedFeatureVisualizationStyle clone()
    {
        AbstractChainedFeatureVisualizationStyle clone = (AbstractChainedFeatureVisualizationStyle)super.clone();
        clone.setPrevious(getPrevious());

        return clone;
    }

    @Override
    public void createCombinedGeometry(Set<Geometry> setToAddTo, FeatureCombinedGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createIndividualGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        if (hasPrevious())
        {
            Set<Geometry> temp = New.set();
            getPrevious().createIndividualGeometry(temp, bd, renderPropertyPool);

            Geometry geo = modifyGeometry(temp, bd, renderPropertyPool);
            if (geo != null)
            {
                setToAddTo.add(geo);
            }
        }
    }

    @Override
    public AbstractRenderableGeometry deriveGeometryFromRenderPropertyChange(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, RenderPropertyPool rpp,
            AbstractRenderableGeometry geom, DataTypeInfo dti, VisualizationState vs, VisualizationState defaultVS,
            MetaDataProvider mdp)
    {
        // TODO
        return hasPrevious()
                ? getPrevious().deriveGeometryFromRenderPropertyChange(changedParameterKeyToParameterMap, rpp, geom, dti, vs,
                        defaultVS, mdp)
                : super.deriveGeometryFromRenderPropertyChange(changedParameterKeyToParameterMap, rpp, geom, dti, vs, defaultVS,
                        mdp);
    }

    @Override
    public BaseRenderProperties getAlteredRenderProperty(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, DataTypeInfo dti, VisualizationState vs,
            VisualizationState defaultVS, MetaDataProvider mdp, BaseRenderProperties orig)
    {
        // TODO
        return hasPrevious()
                ? getPrevious().getAlteredRenderProperty(changedParameterKeyToParameterMap, dti, vs, defaultVS, mdp, orig)
                : super.getAlteredRenderProperty(changedParameterKeyToParameterMap, dti, vs, defaultVS, mdp, orig);
    }

    @Override
    public Set<VisualizationStyleParameter> getAlwaysSaveParameters()
    {
        // TODO
        return hasPrevious() ? getPrevious().getAlwaysSaveParameters() : super.getAlwaysSaveParameters();
    }

    @Override
    public Set<MapVisualizationType> getRequiredMapVisTypes()
    {
        Set<MapVisualizationType> myVisTypes = super.getRequiredMapVisTypes();
        if (hasPrevious())
        {
            myVisTypes.addAll(getPrevious().getRequiredMapVisTypes());
        }
        return myVisTypes;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This style is sensitive if any chained styles are sensitive.
     */
    @Override
    public boolean isSelectionSensitiveStyle()
    {
        return hasPrevious() && getPrevious().isSelectionSensitiveStyle();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This style requires meta data if any chained styles do.
     */
    @Override
    public boolean requiresMetaData()
    {
        return hasPrevious() && getPrevious().requiresMetaData();
    }

    @Override
    public Object getStyleParameterValue(String paramKey)
    {
        VisualizationStyleParameter parameter = getStyleParameter(paramKey);

        if (parameter == null)
        {
            return hasPrevious() ? getPrevious().getStyleParameterValue(paramKey) : null;
        }

        return parameter.getValue();
    }

    @Override
    public boolean supportsLabels()
    {
        return hasPrevious() && getPrevious().supportsLabels();
    }

    @Override
    public abstract VisualizationStyleParameter getStyleParameter(String paramKey);

    /**
     * Modifies the geometry created by the previous VisualizationStyle in the
     * chain.
     *
     * @param singletonGeo the set to retrieve a geometry from. contains maximum
     *            of 1 entry
     * @param bd
     * @param renderPropertyPool
     * @return a modified geometry based on setToAddTo
     */
    protected abstract AbstractRenderableGeometry modifyGeometry(Set<Geometry> singletonGeo,
            FeatureIndividualGeometryBuilderData bd, RenderPropertyPool renderPropertyPool);

    @Override
    public AppliesTo getAppliesTo()
    {
        return AppliesTo.INDIVIDUAL_ELEMENT;
    }

    @Override
    public boolean hasPrevious()
    {
        return myPreviousStyle != null;
    }

    @Override
    public FeatureVisualizationStyle getPrevious()
    {
        return myPreviousStyle;
    }

    @Override
    public void setPrevious(FeatureVisualizationStyle visualizationStyle)
    {
        myPreviousStyle = visualizationStyle;
    }

    @Override
    public FeatureVisualizationStyle getBaseStyle()
    {
        FeatureVisualizationStyle style = myPreviousStyle;
        for (; style instanceof ChainedFeatureVisualizationStyle && ((ChainedFeatureVisualizationStyle)style)
                .hasPrevious(); style = ((ChainedFeatureVisualizationStyle)style).getPrevious())
        {
        }
        return style;
    }

}
