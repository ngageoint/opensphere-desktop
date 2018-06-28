package io.opensphere.mantle.data.geom.style.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.PathVisualizationStyle;
import io.opensphere.mantle.data.geom.style.StyleAltitudeReference;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.EditorPanelVisibilityDependency;
import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.ParameterVisibilityConstraint;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.util.MantleConstants;

/**
 * The Class AbstractPathVisualizationStyle.
 */
public abstract class AbstractPathVisualizationStyle extends AbstractFeatureVisualizationStyle implements PathVisualizationStyle
{
    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "AbstractPathVisualizationStyle";

    /** The Constant ourLineThicknessPropertyKey. */
    public static final String ourLineWidthPropertyKey = ourPropertyKeyPrefix + ".LineWidth";

    /** The Constant ourShowNodesPropertyKey. */
    public static final String ourShowNodesPropertyKey = ourPropertyKeyPrefix + ".ShowNodes";

    /** The Constant ourNodeSizePropertyKey. */
    public static final String ourNodeSizePropertyKey = ourPropertyKeyPrefix + ".NodeSize";

    /** The Constant ourDefaultLineWidthParameter. */
    public static final VisualizationStyleParameter ourDefaultLineWidthParameter = new VisualizationStyleParameter(
            ourLineWidthPropertyKey, "Line Width", Float.valueOf(2.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(true, false));

    /** The Constant ourDefaultShowNodesParameter. */
    public static final VisualizationStyleParameter ourDefaultShowNodesParameter = new VisualizationStyleParameter(
            ourShowNodesPropertyKey, "Show Nodes", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultNodeSizeParameter. */
    public static final VisualizationStyleParameter ourDefaultNodeSizeParameter = new VisualizationStyleParameter(
            ourNodeSizePropertyKey, "Node Size", Float.valueOf(5.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /**
     * Instantiates a new abstract path visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public AbstractPathVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new abstract path visualization style.
     *
     * @param tb the tb
     * @param dtiKey the dti key
     */
    public AbstractPathVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public AbstractPathVisualizationStyle clone()
    {
        return (AbstractPathVisualizationStyle)super.clone();
    }

    /**
     * Creates the geographic position.
     *
     * @param lla the lla
     * @param mapVisInfo the map vis info
     * @param visState the vis state
     * @param geomSupport the geom support
     * @return the geographic position
     */
    public GeographicPosition createGeographicPosition(LatLonAlt lla, MapVisualizationInfo mapVisInfo,
            VisualizationState visState, MapPathGeometrySupport geomSupport)
    {
        StyleAltitudeReference altRef = getAltitudeReference();
        Altitude.ReferenceLevel refLevel = altRef.isAutomatic()
                ? geomSupport.followTerrain() ? Altitude.ReferenceLevel.TERRAIN : lla.getAltitudeReference()
                : altRef.getReference();
        return new GeographicPosition(LatLonAlt.createFromDegreesMeters(lla.getLatD(), lla.getLonD(),
                lla.getAltM() + visState.getAltitudeAdjust() + (mapVisInfo == null ? 0.0f : getLift()), refLevel));
    }

    /**
     * Creates the location nodes.
     *
     * @param setToAddTo the set to add to
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @param renderPropertyPool the render property pool
     */
    public void createLocationNodes(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool renderPropertyPool)
    {
        if (bd != null && bd.getMGS() instanceof MapPathGeometrySupport)
        {
            MapPathGeometrySupport mpgs = (MapPathGeometrySupport)bd.getMGS();
            if (mpgs.getLocations() != null && !mpgs.getLocations().isEmpty())
            {
                for (LatLonAlt lla : mpgs.getLocations())
                {
                    DefaultFeatureIndividualGeometryBuilderData bd2 = new DefaultFeatureIndividualGeometryBuilderData(bd);
                    SimpleMapPointGeometrySupport ptMgs = new SimpleMapPointGeometrySupport(lla);
                    ptMgs.setFollowTerrain(mpgs.followTerrain(), null);
                    ptMgs.setColor(mpgs.getColor(), null);
                    ptMgs.setTimeSpan(mpgs.getTimeSpan());
                    bd2.setMGS(ptMgs);
                    createPointGeometry(bd2, renderPropertyPool, getNodeSize(), null, setToAddTo);
                }
            }
        }
    }

    @Override
    public BaseRenderProperties getAlteredRenderProperty(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, DataTypeInfo dti, VisualizationState vs,
            VisualizationState defaultVS, MetaDataProvider mdp, BaseRenderProperties orig)
    {
        BaseRenderProperties alteredRP = super.getAlteredRenderProperty(changedParameterKeyToParameterMap, dti, vs, defaultVS,
                mdp, orig);

        if (alteredRP instanceof ScalableRenderProperties)
        {
            // TODO if this is here, shouldn't be used for something?
            VisualizationStyleParameter lineWidthParameter = changedParameterKeyToParameterMap.get(ourLineWidthPropertyKey);
            VisualizationState useVS = vs == null ? defaultVS : vs;
            boolean isSelected = useVS != null && useVS.isSelected();
            float desiredLineWidth = isSelected ? getLineWidth() + MantleConstants.SELECT_WIDTH_ADDITION : getLineWidth();
            ScalableRenderProperties dprp = (ScalableRenderProperties)alteredRP;
            if (MathUtil.isZero(dprp.getWidth() - desiredLineWidth) || lineWidthParameter != null)
            {
                dprp.setWidth(desiredLineWidth);
            }
        }
        // TODO: All the rest of the render property changes.

        return alteredRP;
    }

    /**
     * Gets the line width.
     *
     * @return the line width
     */
    public float getLineWidth()
    {
        Float val = (Float)getStyleParameterValue(ourLineWidthPropertyKey);
        return val == null ? 0 : val.floatValue();
    }

    @Override
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = super.getMiniUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder("Line Width"), style,
                ourLineWidthPropertyKey, true, false, 1.0f, MAX_LINE_WIDTH,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null)));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
        panel.addGroupAtTop(paramGrp);

        return panel;
    }

    /**
     * Gets the node size.
     *
     * @return the node size
     */
    public float getNodeSize()
    {
        Float val = (Float)getStyleParameterValue(ourNodeSizePropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    @Override
    @NonNull
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourLineWidthPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourLineWidthPropertyKey,
                true, false, 1.0f, MAX_LINE_WIDTH, new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null)));

        param = style.getStyleParameter(ourShowNodesPropertyKey);
        paramList.add(
                new CheckBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourShowNodesPropertyKey, true));

        param = style.getStyleParameter(ourNodeSizePropertyKey);
        AbstractStyleParameterEditorPanel ptSizePanel = new FloatSliderStyleParameterEditorPanel(
                PanelBuilder.get(param.getName()), style, ourNodeSizePropertyKey, true, false, 1.0f, MAX_POINT_SIZE,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null));
        paramList.add(ptSizePanel);

        EditorPanelVisibilityDependency visDepend = new EditorPanelVisibilityDependency(panel, ptSizePanel);
        visDepend.addConstraint(new ParameterVisibilityConstraint(ourShowNodesPropertyKey, true, Boolean.TRUE));
        visDepend.evaluateStyle();
        panel.addVisibilityDependency(visDepend);

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Basic Path Style", paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultLineWidthParameter);
        setParameter(ourDefaultShowNodesParameter);
        setParameter(ourDefaultNodeSizeParameter);
    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        super.initialize(paramSet);
        for (VisualizationStyleParameter p : paramSet)
        {
            if (p.getKey() != null && p.getKey().startsWith(ourPropertyKeyPrefix))
            {
                setParameter(p);
            }
        }
    }

    /**
     * Checks to see if nodes should be visible.
     *
     * @return true, if is show nodes
     */
    public boolean isShowNodes()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourShowNodesPropertyKey);
        return val != null && val.booleanValue();
    }

    /**
     * Sets line width.
     *
     * @param width the width
     * @param source the source
     */
    public void setLineWidth(float width, Object source)
    {
        if (width < 1)
        {
            throw new IllegalArgumentException("Line width must be positive.");
        }
        setParameter(ourLineWidthPropertyKey, Float.valueOf(width), source);
    }

    /**
     * Sets the node size.
     *
     * @param size the size
     * @param source the source
     */
    public void setNodeSize(float size, Object source)
    {
        setParameter(ourNodeSizePropertyKey, Float.valueOf(size), source);
    }

    /**
     * Sets the show nodes.
     *
     * @param show the show
     * @param source the source
     */
    public void setShowNodes(boolean show, Object source)
    {
        setParameter(ourNodeSizePropertyKey, Boolean.valueOf(show), source);
    }
}
