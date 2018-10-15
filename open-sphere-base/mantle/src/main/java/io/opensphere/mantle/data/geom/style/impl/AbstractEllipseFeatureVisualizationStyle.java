package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableRenderProperties;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxFloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxIntegerSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.EditorPanelVisibilityDependency;
import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.IntegerSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.ParameterVisibilityConstraint;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.util.MantleConstants;

/**
 * The Class PointFeatureVisualizationStyle.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractEllipseFeatureVisualizationStyle extends AbstractLocationFeatureVisualizationStyle
{
    /** The Constant DEFAULT_SOLID_FILL_OPACITY. */
    protected static final float DEFAULT_SOLID_FILL_OPACITY = 0.3f;

    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "AbstractEllipseFeatureVisualizationStyle";

    /** The Constant ourEllipseLineWidthPropertyKey. */
    public static final String ourEllipseLineWidthPropertyKey = ourPropertyKeyPrefix + ".EllipseLineWidth";

    /** The Constant ourEllipseShowEdgeLinePropertyKey. */
    public static final String ourEllipseShowEdgeLinePropertyKey = ourPropertyKeyPrefix + ".ShowEllipseLine";

    /** The Constant ourShowCenterPointPropertyKey. */
    public static final String ourShowCenterPointPropertyKey = ourPropertyKeyPrefix + ".ShowCenterPoint";

    /** The Constant ourCenterPointSizePropertyKey. */
    public static final String ourCenterPointSizePropertyKey = ourPropertyKeyPrefix + ".CenterPointSize";

    /** The Constant ourFillStylePropertyKey. */
    public static final String ourEllipseFillStylePropertyKey = ourPropertyKeyPrefix + ".FillStyle";

    /** The Constant ourEllipseShowOnSelectPropertyKey. */
    public static final String ourEllipseShowOnSelectPropertyKey = ourPropertyKeyPrefix + ".ShowOnSelect";

    /** The Constant ourEllipseShowOnSelectPropertyKey. */
    public static final String ourEllipsoidPropertyKey = ourPropertyKeyPrefix + ".Ellipsoid";

    /** The Constant ourRimFadePropertyKey. */
    public static final String ourRimFadePropertyKey = ourPropertyKeyPrefix + ".RimFade";

    /** The Constant ourAxisUnitKey. */
    public static final String ourAxisUnitKey = ourPropertyKeyPrefix + ".AxisUnitKey";

    /** The Constant ourDefaultEllipseLineWidthParameter. */
    public static final VisualizationStyleParameter ourDefaultEllipseLineWidthParameter = new VisualizationStyleParameter(
            ourEllipseLineWidthPropertyKey, "Edge Line Width", Float.valueOf(1.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(true, false));

    /** The Constant ourDefaultShowNodesParameter. */
    public static final VisualizationStyleParameter ourDefaultShowEdgeLineParameter = new VisualizationStyleParameter(
            ourEllipseShowEdgeLinePropertyKey, "Show Edge Line", Boolean.TRUE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultShowNodesParameter. */
    public static final VisualizationStyleParameter ourDefaultShowCenterPointParameter = new VisualizationStyleParameter(
            ourShowCenterPointPropertyKey, "Show Center Point", Boolean.TRUE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultNodeSizeParameter. */
    public static final VisualizationStyleParameter ourDefaultCenterPointSizeParameter = new VisualizationStyleParameter(
            ourCenterPointSizePropertyKey, "Center Point Size", Float.valueOf(4.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(true, false));

    /** The Constant ourDefaultEllipseRimFadeParameter. */
    public static final VisualizationStyleParameter ourDefaultEllipseRimFadeParameter = new VisualizationStyleParameter(
            ourRimFadePropertyKey, "Rim Fade", Integer.valueOf(50), Integer.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultFillStyleParameter. */
    public static final VisualizationStyleParameter ourDefaultFillStyleParameter = new VisualizationStyleParameter(
            ourEllipseFillStylePropertyKey, "Fill Style", EllipseFillStyle.NO_FILL, EllipseFillStyle.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultShowEllipseOnSelectParameter. */
    public static final VisualizationStyleParameter ourDefaultShowEllipseOnSelectParameter = new VisualizationStyleParameter(
            ourEllipseShowOnSelectPropertyKey, "Ellipse On Select", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The ellipsoid style parameter. */
    public static final VisualizationStyleParameter ourDefaultEllipsoidParameter = new VisualizationStyleParameter(
            ourEllipsoidPropertyKey, "Ellipsoid", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /**
     * Instantiates a new point feature visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public AbstractEllipseFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new abstract ellipse feature visualization style.
     *
     * @param tb the tb
     * @param dtiKey the dti key
     */
    public AbstractEllipseFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public AbstractEllipseFeatureVisualizationStyle clone()
    {
        return (AbstractEllipseFeatureVisualizationStyle)super.clone();
    }

    @Override
    public boolean allChangesRequireRebuild()
    {
        return super.allChangesRequireRebuild() || isEllipsoid();
    }

    @Override
    public BaseRenderProperties getAlteredRenderProperty(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, DataTypeInfo dti, VisualizationState vs,
            VisualizationState defaultVS, MetaDataProvider mdp, BaseRenderProperties orig)
    {
        BaseRenderProperties alteredRP = super.getAlteredRenderProperty(changedParameterKeyToParameterMap, dti, vs, defaultVS,
                mdp, orig);

        handleLineWidthParameterChange(vs, changedParameterKeyToParameterMap, alteredRP);

        handleCenterPointSizeParameterChange(changedParameterKeyToParameterMap, alteredRP);

        handleRimFadeParameterChange(changedParameterKeyToParameterMap, alteredRP);

        handleColorParameterChangeForFill(changedParameterKeyToParameterMap, defaultVS, vs, alteredRP);

        return alteredRP;
    }

    @Override
    public Set<VisualizationStyleParameter> getAlwaysSaveParameters()
    {
        Set<VisualizationStyleParameter> set = super.getAlwaysSaveParameters();
        set.add(getStyleParameter(ourAxisUnitKey));
        return set;
    }

    /**
     * Gets the axis unit.
     *
     * @return the axis unit
     */
    public Class<? extends Length> getAxisUnit()
    {
        String label = (String)getStyleParameterValue(ourAxisUnitKey);
        return getToolbox().getUnitsRegistry().getUnitsProvider(Length.class).getUnitsWithSelectionLabel(label);
    }

    /**
     * Gets the center point size.
     *
     * @return the center point size
     */
    public float getCenterPointSize()
    {
        Float val = (Float)getStyleParameterValue(ourCenterPointSizePropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    /**
     * Gets the ellipse line width.
     *
     * @return the line width
     */
    public float getEllipseLineWidth()
    {
        Float val = (Float)getStyleParameterValue(ourEllipseLineWidthPropertyKey);
        return val == null ? 0 : val.floatValue();
    }

    /**
     * Gets the fill style.
     *
     * @return the fill style
     */
    public EllipseFillStyle getFillStyle()
    {
        return (EllipseFillStyle)getStyleParameterValue(ourEllipseFillStylePropertyKey);
    }

    @Override
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = super.getMiniUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        paramList.add(
                new CheckBoxStyleParameterEditorPanel(StyleUtils.createBasicMiniPanelBuilder("Show Ellipse Only When Selected"),
                        style, ourEllipseShowOnSelectPropertyKey, true));
        paramList.add(new CheckBoxStyleParameterEditorPanel(StyleUtils.createBasicMiniPanelBuilder("Ellipsoid"), style,
                ourEllipsoidPropertyKey, false));

        PanelBuilder lb = StyleUtils.createSliderMiniPanelBuilder("Edge Line");
        lb.setOtherParameter(CheckBoxFloatSliderStyleParameterEditorPanel.POST_CHECKBOX_LABEL, "Width");

        AbstractStyleParameterEditorPanel edgeLineWidthPanel = new CheckBoxIntegerSliderStyleParameterEditorPanel(lb, style,
                ourEllipseLineWidthPropertyKey, true, false, 1, 10, null, ourEllipseShowEdgeLinePropertyKey,
                CheckBoxFloatSliderStyleParameterEditorPanel.SliderVisabilityLinkType.VISIBLE_WHEN_CHECKED);
        paramList.add(edgeLineWidthPanel);

        lb = StyleUtils.createSliderMiniPanelBuilder("Center Point");
        lb.setOtherParameter(CheckBoxFloatSliderStyleParameterEditorPanel.POST_CHECKBOX_LABEL, "Size");
        AbstractStyleParameterEditorPanel centerPointPanel = new CheckBoxFloatSliderStyleParameterEditorPanel(lb, style,
                ourCenterPointSizePropertyKey, true, false, 1.0f, MAX_POINT_SIZE,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null), ourShowCenterPointPropertyKey,
                CheckBoxFloatSliderStyleParameterEditorPanel.SliderVisabilityLinkType.VISIBLE_WHEN_CHECKED);
        paramList.add(centerPointPanel);

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
        panel.addGroupAtTop(paramGrp);

        return panel;
    }

    /**
     * Gets the rim fade value (0 to 255). Only applies if the
     *
     * @return the rim fade.
     */
    public int getRimFade()
    {
        Integer val = (Integer)getStyleParameterValue(ourRimFadePropertyKey);
        return val == null ? 0 : val.intValue();
    }

    @Override
    @NonNull
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourEllipseShowOnSelectPropertyKey);
        paramList.add(new CheckBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                ourEllipseShowOnSelectPropertyKey, true));

        param = style.getStyleParameter(ourEllipsoidPropertyKey);
        paramList.add(
                new CheckBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourEllipsoidPropertyKey, true));

        PanelBuilder pb = PanelBuilder.get("Edge Line");
        pb.setOtherParameter(CheckBoxFloatSliderStyleParameterEditorPanel.POST_CHECKBOX_LABEL, "Width");

        AbstractStyleParameterEditorPanel edgeLineWidthPanel = new CheckBoxIntegerSliderStyleParameterEditorPanel(pb, style,
                ourEllipseLineWidthPropertyKey, true, false, 1, 10, null, ourEllipseShowEdgeLinePropertyKey,
                CheckBoxFloatSliderStyleParameterEditorPanel.SliderVisabilityLinkType.VISIBLE_WHEN_CHECKED);
        paramList.add(edgeLineWidthPanel);

        pb = PanelBuilder.get("Center Point");
        pb.setOtherParameter(CheckBoxFloatSliderStyleParameterEditorPanel.POST_CHECKBOX_LABEL, "Size");
        AbstractStyleParameterEditorPanel centerPointPanel = new CheckBoxFloatSliderStyleParameterEditorPanel(pb, style,
                ourCenterPointSizePropertyKey, true, false, 1.0f, MAX_POINT_SIZE,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null), ourShowCenterPointPropertyKey,
                CheckBoxFloatSliderStyleParameterEditorPanel.SliderVisabilityLinkType.VISIBLE_WHEN_CHECKED);
        paramList.add(centerPointPanel);

        param = style.getStyleParameter(ourEllipseFillStylePropertyKey);
        paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                ourEllipseFillStylePropertyKey, true, false, false, Arrays.asList(EllipseFillStyle.values())));

        param = style.getStyleParameter(ourRimFadePropertyKey);
        AbstractStyleParameterEditorPanel rimFadePanel = new IntegerSliderStyleParameterEditorPanel(
                PanelBuilder.get(param.getName()), style, ourRimFadePropertyKey, true, false, 0, 255, null);
        paramList.add(rimFadePanel);

        EditorPanelVisibilityDependency visDepend = new EditorPanelVisibilityDependency(panel, rimFadePanel);
        visDepend.addConstraint(new ParameterVisibilityConstraint(ourEllipseFillStylePropertyKey, false,
                ParameterVisibilityConstraint.MultiParameterOperator.OR, EllipseFillStyle.NO_FILL, EllipseFillStyle.SOLID));
        visDepend.evaluateStyle();
        panel.addVisibilityDependency(visDepend);

        Collection<String> labels = Arrays
                .asList(getToolbox().getUnitsRegistry().getAvailableUnitsSelectionLabels(Length.class, false));

        paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get("Axis Units"), style, ourAxisUnitKey, false, false,
                false, labels));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Basic Ellipse Style", paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultEllipseLineWidthParameter);
        setParameter(getDefaultAxisUnitParameter());
        setParameter(ourDefaultShowCenterPointParameter);
        setParameter(ourDefaultCenterPointSizeParameter);
        setParameter(ourDefaultShowEdgeLineParameter);
        setParameter(ourDefaultEllipseRimFadeParameter);
        setParameter(ourDefaultFillStyleParameter);
        setParameter(ourDefaultShowEllipseOnSelectParameter);
        setParameter(ourDefaultEllipsoidParameter);
    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        super.initialize(paramSet);
        paramSet.stream().filter(p -> p.getKey() != null && p.getKey().startsWith(ourPropertyKeyPrefix))
                .forEach(this::setParameter);
    }

    @Override
    public boolean isSelectionSensitiveStyle()
    {
        return isShowEllipseOnSelect();
    }

    /**
     * Checks to see if the center point should be visible.
     *
     * @return true, if center point should be visible.
     */
    public boolean isShowCenterPoint()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourShowCenterPointPropertyKey);
        return val != null && val.booleanValue();
    }

    /**
     * Checks to see if the center ellipse edge line should be visible.
     *
     * @return true, if edge line should be visible.
     */
    public boolean isShowEdgeLine()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourEllipseShowEdgeLinePropertyKey);
        return val != null && val.booleanValue();
    }

    /**
     * Checks to see if the center ellipse edge line should be visible.
     *
     * @return true, if edge line should be visible.
     */
    public boolean isEllipsoid()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourEllipsoidPropertyKey);
        return val != null && val.booleanValue();
    }

    /**
     * Checks if is show ellipse on select.
     *
     * @return true, if is show ellipse on select
     */
    public boolean isShowEllipseOnSelect()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourEllipseShowOnSelectPropertyKey);
        return val != null && val.booleanValue();
    }

    /**
     * Sets the axis unit.
     *
     * @param unit the unit
     * @param source the source
     */
    public void setAxisUnit(Class<? extends Length> unit, Object source)
    {
        Class<? extends Length> oldUnit = getAxisUnit();
        if (!Objects.equals(oldUnit, unit))
        {
            setParameter(ourAxisUnitKey, Length.getSelectionLabel(unit), source);
        }
    }

    /**
     * Sets the center point size.
     *
     * @param size the size
     * @param source the source
     */
    public void setCenterPointSize(float size, Object source)
    {
        setParameter(ourCenterPointSizePropertyKey, Float.valueOf(size), source);
    }

    /**
     * Sets ellipse line width.
     *
     * @param width the width
     * @param source the source
     */
    public void setEllipseLineWidth(float width, Object source)
    {
        if (width < 1)
        {
            throw new IllegalArgumentException("Ellipse line width must be positive.");
        }
        setParameter(ourEllipseLineWidthPropertyKey, Float.valueOf(width), source);
    }

    /**
     * Sets the ellipse {@link EllipseFillStyle}.
     *
     * @param style the {@link EllipseFillStyle}
     * @param source the source
     */
    public void setFillStyle(EllipseFillStyle style, Object source)
    {
        Utilities.checkNull(style, "style");
        setParameter(ourEllipseFillStylePropertyKey, style, source);
    }

    /**
     * Sets rim fade ( 0 to 255 ), if width is not 0 &lt;= rimFade &lt;= 255 it
     * will be set to the nearest bound.
     *
     * @param rimFade the rim fade
     * @param source the source
     */
    public void setRimFade(int rimFade, Object source)
    {
        int adjRimFade = rimFade < 0 ? 0 : rimFade > 255 ? 255 : rimFade;
        setParameter(ourRimFadePropertyKey, Integer.valueOf(adjRimFade), source);
    }

    /**
     * Sets the show center point.
     *
     * @param show the show center point
     * @param source the source
     */
    public void setShowCenterPoint(boolean show, Object source)
    {
        setParameter(ourShowCenterPointPropertyKey, Boolean.valueOf(show), source);
    }

    /**
     * Sets if the edge line should be visible.
     *
     * @param show the show the edge line
     * @param source the source
     */
    public void setShowEdgeLine(boolean show, Object source)
    {
        setParameter(ourEllipseShowEdgeLinePropertyKey, Boolean.valueOf(show), source);
    }

    /**
     * Sets the show ellipse on select flag.
     *
     * @param show the show
     * @param source the source
     */
    public void setShowEllipseOnSelect(boolean show, Object source)
    {
        setParameter(ourEllipseShowOnSelectPropertyKey, Boolean.valueOf(show), source);
    }

    /**
     * Convert axis length to angle.
     *
     * @param centerPoint the center point
     * @param orientation the orientation
     * @param length the length
     * @return the double
     */
    protected double convertAxisLengthToAngle(LatLonAlt centerPoint, double orientation, Length length)
    {
        LatLonAlt endPosition = GeographicBody3D.greatCircleEndPosition(centerPoint, orientation * MathUtil.DEG_TO_RAD,
                WGS84EarthConstants.RADIUS_MEAN_M, length.inMeters());
        double dist = GeographicBody3D.greatCircleDistanceR(centerPoint, endPosition) * MathUtil.RAD_TO_DEG;
        return dist;
    }

    /**
     * Determine ellipse fill colors for properties.
     *
     * @param visState the {@link VisualizationState}
     * @param fillStyle the {@link EllipseFillStyle}
     * @return the pair of colors (center, edge).
     */
    protected Pair<Color, Color> determineEllipseFillColorsForProperties(VisualizationState visState, EllipseFillStyle fillStyle)
    {
        Color c = visState.isSelected() ? MantleConstants.SELECT_COLOR
                : visState.isDefaultColor() ? getColor() : visState.getColor();
        Color cWithFade = new Color(c.getRed(), c.getGreen(), c.getBlue(), getRimFade());
        Pair<Color, Color> colors;
        switch (fillStyle)
        {
            case CENTER:
                colors = new Pair<>(c, cWithFade);
                break;
            case EDGE:
                colors = new Pair<>(cWithFade, c);
                break;
            case SOLID:
                colors = new Pair<>(cWithFade, cWithFade);
                break;
            default:
                throw new UnexpectedEnumException(fillStyle);
        }
        return colors;
    }

    /**
     * Determine primary color.
     *
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @return the color
     */
    protected Color determinePrimaryColor(FeatureIndividualGeometryBuilderData bd)
    {
        return bd.getVS().isSelected() ? MantleConstants.SELECT_COLOR
                : bd.getVS().isDefaultColor() ? getColor() : bd.getVS().getColor();
    }

    /**
     * Get the default axis unit property.
     *
     * @return The property.
     */
    protected final VisualizationStyleParameter getDefaultAxisUnitParameter()
    {
        Class<? extends Length> preferredUnits = getToolbox().getUnitsRegistry().getUnitsProvider(Length.class)
                .getPreferredFixedScaleUnits(Kilometers.ONE);
        String label = Length.getSelectionLabel(preferredUnits);
        return new VisualizationStyleParameter(ourAxisUnitKey, "Axis Unit", label, String.class,
                new VisualizationStyleParameterFlags(true, false), ParameterHint.hint(false, false));
    }

    /**
     * Handle center point size parameter change.
     *
     * @param changedParameterKeyToParameterMap the changed parameter key to
     *            parameter map
     * @param alteredRP the altered rp
     */
    private void handleCenterPointSizeParameterChange(Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap,
            BaseRenderProperties alteredRP)
    {
        VisualizationStyleParameter centerPointSizeParam = changedParameterKeyToParameterMap.get(ourCenterPointSizePropertyKey);
        if (centerPointSizeParam != null)
        {
            if (alteredRP instanceof PointRenderProperties)
            {
                PointRenderProperties dprp = (PointRenderProperties)alteredRP;
                if (dprp.getSize() != getCenterPointSize())
                {
                    dprp.setSize(getCenterPointSize());
                }
            }
            else if (alteredRP instanceof PointSizeRenderProperty)
            {
                PointSizeRenderProperty dprp = (PointSizeRenderProperty)alteredRP;
                if (dprp.getSize() != getCenterPointSize())
                {
                    dprp.setSize(getCenterPointSize());
                }
            }
        }
    }

    /**
     * Handle color parameter change for fill.
     *
     * @param changedParameterKeyToParameterMap the changed parameter key to
     *            parameter map
     * @param defaultVS the default vs
     * @param vs the vs
     * @param alteredRP the altered rp
     */
    private void handleColorParameterChangeForFill(Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap,
            VisualizationState defaultVS, VisualizationState vs, BaseRenderProperties alteredRP)
    {
        VisualizationStyleParameter colorParameter = changedParameterKeyToParameterMap.get(COLOR_PROPERTY_KEY);
        if (colorParameter != null)
        {
            if (alteredRP instanceof ScalableMeshRenderProperties)
            {
                EllipseFillStyle fillStyle = getFillStyle();
                ScalableMeshRenderProperties dprp = (ScalableMeshRenderProperties)alteredRP;
                Pair<Color, Color> colors = determineEllipseFillColorsForProperties(vs, fillStyle);
                if (colors != null)
                {
                    dprp.setBaseColor(colors.getSecondObject());
                    dprp.setColor(colors.getFirstObject());
                }
            }
            else if (alteredRP instanceof DefaultPolygonRenderProperties)
            {
                DefaultPolygonRenderProperties dprp = (DefaultPolygonRenderProperties)alteredRP;
                if (dprp.getFillColorRenderProperties() != null)
                {
                    Color primary = StyleUtils.determineColor(dprp.getColor(), vs == null ? defaultVS : vs);
                    float[] colorComp = primary.getColorComponents(null);
                    Color fillColor = new Color(colorComp[0], colorComp[1], colorComp[2], DEFAULT_SOLID_FILL_OPACITY);
                    dprp.getFillColorRenderProperties().setColor(fillColor);
                }
            }
        }
    }

    /**
     * Handle line width parameter change.
     *
     * @param vs the vs
     * @param changedParameterKeyToParameterMap the changed parameter key to
     *            parameter map
     * @param alteredRP the altered rp
     */
    private void handleLineWidthParameterChange(VisualizationState vs,
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, BaseRenderProperties alteredRP)
    {
        VisualizationStyleParameter lineWidthParameter = changedParameterKeyToParameterMap.get(ourEllipseLineWidthPropertyKey);
        if (lineWidthParameter != null && alteredRP instanceof ScalableRenderProperties)
        {
            ScalableRenderProperties dprp = (ScalableRenderProperties)alteredRP;
            float width = getEllipseLineWidth();
            if (dprp.getWidth() != width)
            {
                dprp.setWidth(width);
            }
        }
    }

    /**
     * Handle rim fade parameter change.
     *
     * @param changedParameterKeyToParameterMap the changed parameter key to
     *            parameter map
     * @param alteredRP the altered rp
     */
    private void handleRimFadeParameterChange(Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap,
            BaseRenderProperties alteredRP)
    {
        VisualizationStyleParameter rimFadeParameter = changedParameterKeyToParameterMap.get(ourRimFadePropertyKey);
        if (rimFadeParameter != null && alteredRP instanceof ScalableMeshRenderProperties)
        {
            ScalableMeshRenderProperties dprp = (ScalableMeshRenderProperties)alteredRP;
            EllipseFillStyle fillStyle = getFillStyle();
            if (fillStyle != null)
            {
                if (fillStyle == EllipseFillStyle.CENTER)
                {
                    dprp.setBaseColor(new Color(dprp.getBaseColor().getRed(), dprp.getBaseColor().getGreen(),
                            dprp.getBaseColor().getBlue(), getRimFade()));
                }
                else if (fillStyle == EllipseFillStyle.EDGE)
                {
                    // Middle
                    dprp.setColor(new Color(dprp.getColor().getRed(), dprp.getColor().getGreen(), dprp.getColor().getBlue(),
                            getRimFade()));
                }
            }
        }
    }

    /**
     * The Enum FillStyle.
     */
    public enum EllipseFillStyle
    {
        /** NO_FILL. */
        NO_FILL("No Fill"),

        /** The SOLID. */
        SOLID("Uniform"),

        /**
         * The EDGE. I know that dude. I played with that dude, man.
         */
        EDGE("Edge"),

        /** The CENTER. */
        CENTER("Center");

        /** The Label. */
        private String myLabel;

        /**
         * Instantiates a new ellipse fill style.
         *
         * @param label the label
         */
        EllipseFillStyle(String label)
        {
            myLabel = label;
        }

        @Override
        public String toString()
        {
            return myLabel;
        }
    }
}
