package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener;

public class OverrideFeatureVisualizationStyle implements FeatureVisualizationStyle
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** A map of styles to element IDs. Determines what we override with. */
    private final Map<FeatureVisualizationStyle, List<Long>> myOverrideStyles;

    /** The style we're overriding. */
    private FeatureVisualizationStyle myOverriddenStyle;

    /** Whatever data type this is applied to. */
    private String myDtiKey;

    /**
     * Default constructor.
     *
     * @param tb the toolbox
     */
    public OverrideFeatureVisualizationStyle(Toolbox tb)
    {
        myToolbox = tb;
        myOverrideStyles = New.map();
    }

    /**
     * Constructor that immediately defines an overridden style.
     *
     * @param tb the toolbox
     * @param overriddenStyle the style we care about
     */
    public OverrideFeatureVisualizationStyle(Toolbox tb, FeatureVisualizationStyle overriddenStyle)
    {
        this(tb);
        myOverriddenStyle = overriddenStyle;
    }

    @Override
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public void revertToDefaultParameters(Object source)
    {
        myOverriddenStyle.revertToDefaultParameters(source);
    }

    @Override
    public boolean setParameter(String paramKey, Object newValue, Object source) throws IllegalArgumentException
    {
        return myOverriddenStyle.setParameter(paramKey, newValue, source);
    }

    @Override
    public Set<VisualizationStyleParameter> setParameters(Set<VisualizationStyleParameter> parameters, Object source)
    {
        return myOverriddenStyle.setParameters(parameters, source);
    }

    @Override
    public void addStyleParameterChangeListener(VisualizationStyleParameterChangeListener listener)
    {
        // TODO Auto-generated method stub
        myOverriddenStyle.addStyleParameterChangeListener(listener);
    }

    @Override
    public VisualizationStyle clone()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<VisualizationStyleParameter> getAlwaysSaveParameters()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<VisualizationStyleParameter> getChangedParameters(VisualizationStyle other)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends VisualizationSupport> getConvertedClassType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDTIKey()
    {
        return myDtiKey;
    }

    @Override
    public void setDTIKey(String dtiKey)
    {
        myDtiKey = dtiKey;
    }

    @Override
    public FeatureVisualizationControlPanel getMiniUIPanel()
    {
        return myOverriddenStyle.getMiniUIPanel();
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.FEATURE;
    }

    @Override
    public String getStyleDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStyleName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the style parameter in {@link #myOverriddenStyle}, or the last
     * non-null style parameter in {@link #myOverrideStyles} that matches the
     * paramKey.
     */
    @Override
    public VisualizationStyleParameter getStyleParameter(String paramKey)
    {
        VisualizationStyleParameter overriddenParam = myOverriddenStyle.getStyleParameter(paramKey);
        VisualizationStyleParameter retval = null;
        for (FeatureVisualizationStyle style : myOverrideStyles.keySet())
        {
            VisualizationStyleParameter overrideParam = style.getStyleParameter(paramKey);
            if (overrideParam != null && !Objects.equals(overrideParam, overriddenParam))
            {
                retval = overrideParam;
            }
        }
        return retval == null ? overriddenParam : retval;
    }

    @Override
    public Set<String> getStyleParameterKeys()
    {
        Set<String> keys = myOverriddenStyle.getStyleParameterKeys();
        for (FeatureVisualizationStyle style : myOverrideStyles.keySet())
        {
            keys.addAll(style.getStyleParameterKeys());
        }
        return keys;
    }

    @Override
    public Set<VisualizationStyleParameter> getStyleParameterSet()
    {
        Set<VisualizationStyleParameter> params = myOverriddenStyle.getStyleParameterSet();
        for (FeatureVisualizationStyle style : myOverrideStyles.keySet())
        {
            params.addAll(style.getStyleParameterSet());
        }
        return params;
    }

    @Override
    public Object getStyleParameterValue(String paramKey)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeatureVisualizationControlPanel getUIPanel()
    {
        return myOverriddenStyle.getUIPanel();
    }

    @Override
    public void initialize()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void initializeFromDataType()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeStyleParameterChangeListener(VisualizationStyleParameterChangeListener listener)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean requiresShaders()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean allChangesRequireRebuild()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void createCombinedGeometry(Set<Geometry> setToAddTo, FeatureCombinedGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void createIndividualGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public AbstractRenderableGeometry deriveGeometryFromRenderPropertyChange(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, RenderPropertyPool rpp,
            AbstractRenderableGeometry geom, DataTypeInfo dti, VisualizationState vs, VisualizationState defaultVS,
            MetaDataProvider mdp)
    {
        // TODO Auto-generated method stub
        // Check if geometry is overridden
        // If not, return myOverriddenStyle.derive
        // If yes, return myOverrideStyle.derive (assume only 1)
        return null;
    }

    @Override
    public BaseRenderProperties getAlteredRenderProperty(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, DataTypeInfo dti, VisualizationState vs,
            VisualizationState defaultVS, MetaDataProvider mdp, BaseRenderProperties orig)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        return AppliesTo.INDIVIDUAL_ELEMENT;
    }

    @Override
    public Color getColor()
    {
        throw new UnsupportedOperationException("Cannot retrieve an individual Color from OverrideFeatureVisualizationStyle.");
    }

    @Override
    public Set<MapVisualizationType> getRequiredMapVisTypes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSelectionSensitiveStyle()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean requiresMetaData()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean supportsLabels()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
