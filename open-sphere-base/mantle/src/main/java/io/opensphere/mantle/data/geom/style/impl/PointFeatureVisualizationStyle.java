package io.opensphere.mantle.data.geom.style.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.util.MantleConstants;

/**
 * The Class PointFeatureVisualizationStyle.
 */
public class PointFeatureVisualizationStyle extends AbstractLocationFeatureVisualizationStyle
{
    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "PointFeatureVisualizationStyle";

    /** The Constant ourPointSizePropertyKey. */
    public static final String ourPointSizePropertyKey = ourPropertyKeyPrefix + ".PointSize";

    /** The Constant ourDefaultPointSizeParameter. */
    public static final VisualizationStyleParameter ourDefaultPointSizeParameter = new VisualizationStyleParameter(
            ourPointSizePropertyKey, "Point Size", Float.valueOf(4.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(true, false));

    /**
     * Instantiates a new point feature visualization style.
     *
     * @param tb the tb
     */
    public PointFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new point feature visualization style.
     *
     * @param tb the tb
     * @param dtiKey the dti key
     */
    public PointFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public PointFeatureVisualizationStyle clone()
    {
        return (PointFeatureVisualizationStyle)super.clone();
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
        float size = bd.getVS().isSelected() ? getPointSize() + MantleConstants.SELECT_SIZE_ADDITION : getPointSize();
        AbstractRenderableGeometry geom = createPointGeometry(bd, renderPropertyPool, size, null, setToAddTo);
        if (geom != null)
        {
            createLabelGeometry(setToAddTo, bd, null, geom.getConstraints(), renderPropertyPool);
        }
    }

    @Override
    public PointFeatureVisualizationStyle deriveForType(String dtiKey)
    {
        PointFeatureVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public BaseRenderProperties getAlteredRenderProperty(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, DataTypeInfo dti, VisualizationState vs,
            VisualizationState defaultVS, MetaDataProvider mdp, BaseRenderProperties orig)
    {
        BaseRenderProperties alteredRP = super.getAlteredRenderProperty(changedParameterKeyToParameterMap, dti, vs, defaultVS,
                mdp, orig);
        // TODO if this is here, shouldn't be used for something?
        VisualizationStyleParameter poitSizeParameter = changedParameterKeyToParameterMap.get(ourPointSizePropertyKey);
        float size = vs != null && vs.isSelected() ? getPointSize() + MantleConstants.SELECT_SIZE_ADDITION : getPointSize();

        if (alteredRP instanceof PointRenderProperties)
        {
            PointRenderProperties dprp = (PointRenderProperties)alteredRP;
            if (!MathUtil.isZero(dprp.getSize() - size) || poitSizeParameter != null)
            {
                dprp.setSize(size);
            }
        }
        else if (alteredRP instanceof PointSizeRenderProperty)
        {
            PointSizeRenderProperty dprp = (PointSizeRenderProperty)alteredRP;
            if (!MathUtil.isZero(dprp.getSize() - size) || poitSizeParameter != null)
            {
                dprp.setSize(size);
            }
        }
        return alteredRP;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        return AppliesTo.INDIVIDUAL_ELEMENT;
    }

    @Override
    public Class<? extends MapGeometrySupport> getConvertedClassType()
    {
        return MapLocationGeometrySupport.class;
    }

    @Override
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel mPanel = super.getMiniUIPanel();

        List<AbstractStyleParameterEditorPanel> pList = New.list();
        MutableVisualizationStyle style = mPanel.getChangedStyle();

        pList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder("Point Size"), style,
                ourPointSizePropertyKey, true, false, 1.0f, MAX_POINT_SIZE,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null)));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, pList, false, 1);
        mPanel.addGroupAtTop(paramGrp);

        return mPanel;
    }

    /**
     * Gets the point size.
     *
     * @return the point size
     */
    public float getPointSize()
    {
        Float val = (Float)getStyleParameterValue(ourPointSizePropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.LOCATION_FEATURE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Feature visualization controls for points.";
    }

    @Override
    public String getStyleName()
    {
        return "Points";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel aPanel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> pList2 = New.list();
        MutableVisualizationStyle style = aPanel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourPointSizePropertyKey);
        pList2.add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourPointSizePropertyKey,
                true, false, 1.0f, MAX_POINT_SIZE, new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null)));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Point Style", pList2);
        aPanel.addGroup(paramGrp);

        return aPanel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultPointSizeParameter);
    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        super.initialize(paramSet);
        paramSet.stream().filter(p -> p.getKey() != null && p.getKey().startsWith(ourPropertyKeyPrefix))
                .forEach(this::setParameter);
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new PointFeatureVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Sets the point size.
     *
     * @param size the size
     * @param source the source
     */
    public void setPointSize(float size, Object source)
    {
        setParameter(ourPointSizePropertyKey, Float.valueOf(size), source);
    }

    @Override
    public boolean supportsLabels()
    {
        return true;
    }
}
