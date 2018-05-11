package io.opensphere.mantle.data.geom.style.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.EllipseGeometry;
import io.opensphere.core.geometry.EllipseGeometryUtilities;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LineOfBearingGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultLOBRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.LOBRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableRenderProperties;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.core.units.length.StatuteMiles;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.StyleAltitudeReference;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractGroupedVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.AdvancedLengthParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ColumnAngleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ColumnLengthParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.EditorPanelVisibilityDependency;
import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.LengthSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.MultipleCheckBoxParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.ParameterVisibilityConstraint;
import io.opensphere.mantle.data.geom.style.impl.ui.RadioButtonParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.data.impl.specialkey.EllipseOrientationKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMajorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMinorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.HeadingErrorKey;
import io.opensphere.mantle.data.impl.specialkey.SpeedErrorKey;
import io.opensphere.mantle.data.impl.specialkey.SpeedKey;
import io.opensphere.mantle.util.MantleConstants;

/**
 * The Class PointFeatureVisualizationStyle.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractLOBFeatureVisualizationStyle extends AbstractLocationFeatureVisualizationStyle
{
    /** Manual length mode. */
    private static final String MANUAL_MODE = "Manual";

    /** Column length mode. */
    private static final String COLUMN_MODE = "Column";

    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "AbstractLOBFeatureVisualizationStyle";

    /** The Constant ourLOBLengthPropertyKey. */
    public static final String ourLOBLengthPropertyKey = ourPropertyKeyPrefix + ".LOBLengthNew";

    /** The Constant ourArrowLengthPropertyKey. */
    public static final String ourArrowLengthPropertyKey = ourPropertyKeyPrefix + ".ArrowLength";

    /** The Constant ourEllipseLineWidthPropertyKey. */
    public static final String ourLOBLineWidthPropertyKey = ourPropertyKeyPrefix + ".LOBLineWidth";

    /** The Constant ourOriginPointSizePropertyKey. */
    public static final String ourLOBOriginPointSizePropertyKey = ourPropertyKeyPrefix + ".OriginPointSize";

    /** The Constant ourOriginPointSizePropertyKey. */
    public static final String ourShowArrowPropertyKey = ourPropertyKeyPrefix + ".ShowArrow";

    /** The length mode property key. */
    public static final String ourLengthModePropertyKey = ourPropertyKeyPrefix + ".LengthMode";

    /** The length column property key. */
    public static final String ourLengthColumnPropertyKey = ourPropertyKeyPrefix + ".LengthColumn";

    /** The length multiplier property key. */
    public static final String ourLengthMultiplierPropertyKey = ourPropertyKeyPrefix + ".LengthMultiplier";

    /** The show error property key. */
    public static final String ourShowErrorPropertyKey = ourPropertyKeyPrefix + ".ShowError";

    /** The show ellipse property key. */
    public static final String ourShowEllipsePropertyKey = ourPropertyKeyPrefix + ".ShowEllipse";

    /** The bearing error column property key. */
    public static final String ourBearingErrorColumnPropertyKey = ourPropertyKeyPrefix + ".BearingErrorColumn";

    /** The bearing error multiplier property key. */
    public static final String ourBearingErrorMultiplierPropertyKey = ourPropertyKeyPrefix + ".BearingErrorMultiplier";

    /** The length error column property key. */
    public static final String ourLengthErrorColumnPropertyKey = ourPropertyKeyPrefix + ".LengthErrorColumn";

    /** The length error multiplier property key. */
    public static final String ourLengthErrorMultiplierPropertyKey = ourPropertyKeyPrefix + ".LengthErrorMultiplier";

    /** The Constant ourDefaultEllipseLineWidthParameter. */
    public static final VisualizationStyleParameter ourDefaultLOBLineWidthParameter = new VisualizationStyleParameter(
            ourLOBLineWidthPropertyKey, "LOB Line Width", Float.valueOf(1.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(true, false));

    /** The Constant ourDefaultNodeSizeParameter. */
    public static final VisualizationStyleParameter ourDefaultLOBLengthParameter = new VisualizationStyleParameter(
            ourLOBLengthPropertyKey, "Lob Length", new Kilometers(100), Length.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultNodeSizeParameter. */
    public static final VisualizationStyleParameter ourDefaultArrowLengthParameter = new VisualizationStyleParameter(
            ourArrowLengthPropertyKey, "Arrow Size", Float.valueOf(10f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultOriginPointSizeParameter. */
    public static final VisualizationStyleParameter ourDefaultLOBOriginPointSizeParameter = new VisualizationStyleParameter(
            ourLOBOriginPointSizePropertyKey, "Origin Point Size", Float.valueOf(4.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultShowArrowParameter. */
    public static final VisualizationStyleParameter ourDefaultShowArrowParameter = new VisualizationStyleParameter(
            ourShowArrowPropertyKey, "Show Arrow", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The length mode style parameter. */
    public static final VisualizationStyleParameter ourDefaultLengthModeParameter = new VisualizationStyleParameter(
            ourLengthModePropertyKey, "Line Length", MANUAL_MODE, String.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The length column style parameter. */
    public static final VisualizationStyleParameter ourDefaultLengthColumnParameter = new VisualizationStyleParameter(
            ourLengthColumnPropertyKey, "Length Column", null, String.class, new VisualizationStyleParameterFlags(true, true),
            ParameterHint.hint(false, true));

    /** The length multiplier style parameter. */
    public static final VisualizationStyleParameter ourDefaultLengthMultiplierParameter = new VisualizationStyleParameter(
            ourLengthMultiplierPropertyKey, "Length Multiplier", new Meters(100), Length.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The show error style parameter. */
    public static final VisualizationStyleParameter ourDefaultShowErrorParameter = new VisualizationStyleParameter(
            ourShowErrorPropertyKey, "Show Error", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The show ellipse style parameter. */
    public static final VisualizationStyleParameter ourDefaultShowEllipseParameter = new VisualizationStyleParameter(
            ourShowEllipsePropertyKey, "Show Ellipse", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The bearing error column style parameter. */
    public static final VisualizationStyleParameter ourDefaultBearingErrorColumnParameter = new VisualizationStyleParameter(
            ourBearingErrorColumnPropertyKey, "Bearing Err", null, String.class, new VisualizationStyleParameterFlags(true, true),
            ParameterHint.hint(false, true));

    /** The bearing error multiplier style parameter. */
    public static final VisualizationStyleParameter ourDefaultBearingErrorMultiplierParameter = new VisualizationStyleParameter(
            ourBearingErrorMultiplierPropertyKey, "Bearing Error Multiplier", Float.valueOf(1), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The length error column style parameter. */
    public static final VisualizationStyleParameter ourDefaultLengthErrorColumnParameter = new VisualizationStyleParameter(
            ourLengthErrorColumnPropertyKey, "Length Err ", null, String.class, new VisualizationStyleParameterFlags(true, true),
            ParameterHint.hint(false, true));

    /** The length error multiplier style parameter. */
    public static final VisualizationStyleParameter ourDefaultLengthErrorMultiplierParameter = new VisualizationStyleParameter(
            ourLengthErrorMultiplierPropertyKey, "Length Error Multiplier", new Meters(100), Length.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant MAX_LOB_LENGTH_METERS. */
    private static final Length MAX_LOB_LENGTH = new Kilometers(20000.0f);

    /** The Constant MAX_ARROW_LENGTH_KILOMETERS. */
    @SuppressWarnings("unused")
    private static final Length MAX_ARROW_LENGTH = new Kilometers(500.0f);

    /** The minimum arrow length. */
    private static final Length MIN_ARROW_LENGTH = Kilometers.ONE;

    /** Options for length units. */
    private static final List<Class<? extends Length>> LENGTH_UNITS = New.list(Kilometers.class, Meters.class, StatuteMiles.class,
            NauticalMiles.class);

    /** The length units. */
    private Class<? extends Length> myLengthUnits;

    /**
     * Instantiates a new abstract lob feature visualization style.
     *
     * @param tb the tb
     */
    public AbstractLOBFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new abstract lob feature visualization style.
     *
     * @param tb the tb
     * @param dtiKey the dti key
     */
    public AbstractLOBFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public AbstractLOBFeatureVisualizationStyle clone()
    {
        return (AbstractLOBFeatureVisualizationStyle)super.clone();
    }

    @Override
    public void createCombinedGeometry(Set<Geometry> setToAddTo, FeatureCombinedGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("null")
    @Override
    public void createIndividualGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        LineOfBearingGeometry lobGeom = null;
        if (bd.getMGS() instanceof MapLocationGeometrySupport)
        {
            Float orientation = getLobOrientation(bd.getElementId(), bd.getMGS(), bd.getMDP());
            boolean buildLobLine = orientation != null && bd.getVS().isLobVisible();
            MapLocationGeometrySupport mlgs = (MapLocationGeometrySupport)bd.getMGS();
            StyleAltitudeReference altRef = getAltitudeReference();
            Altitude.ReferenceLevel refLevel = altRef.isAutomatic()
                    ? bd.getMGS().followTerrain() ? Altitude.ReferenceLevel.TERRAIN : mlgs.getLocation().getAltitudeReference()
                    : altRef.getReference();
            GeographicPosition gp = new GeographicPosition(LatLonAlt.createFromDegreesMeters(mlgs.getLocation().getLatD(),
                    mlgs.getLocation().getLonD(), determineAltitude(bd.getVS(), mlgs, bd.getMDP()), refLevel));
            AbstractRenderableGeometry ptGeom = null;
            if (!buildLobLine || getOriginPointSize() > 0.0)
            {
                ptGeom = createPointGeometry(bd, renderPropertyPool, getOriginPointSize() == 0f ? 1.0f : getOriginPointSize(),
                        null, setToAddTo);
            }
            if (buildLobLine)
            {
                MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
                BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null
                        : bd.getDataType().getBasicVisualizationInfo();

                LineOfBearingGeometry.Builder lobBuilder = createLobBuilder(bd, orientation, gp, mapVisInfo, basicVisInfo);
                LOBRenderProperties props = determineRenderProperties(mapVisInfo, basicVisInfo, bd, renderPropertyPool, gp);

                // Add a time constraint if in time line mode.
                Constraints constraints = StyleUtils.createTimeConstraintsIfApplicable(basicVisInfo, mapVisInfo, bd.getMGS(),
                        StyleUtils.getDataGroupInfoFromDti(getToolbox(), bd.getDataType()));
                lobGeom = new LineOfBearingGeometry(lobBuilder, props, constraints);
                if (props.getLineLength() > 0)
                {
                    setToAddTo.add(lobGeom);
                }
            }
            if (ptGeom != null || lobGeom != null)
            {
                createLabelGeometry(setToAddTo, bd, gp, ptGeom == null ? lobGeom.getConstraints() : ptGeom.getConstraints(),
                        renderPropertyPool);
            }
            addErrorArcs(setToAddTo, bd, renderPropertyPool, lobGeom, gp);

            Boolean showEllipse = (Boolean)getStyleParameterValue(ourShowEllipsePropertyKey);
            if (showEllipse.booleanValue() && lobGeom != null)
            {
                EllipseGeometry ellipse = createEllipse(bd, renderPropertyPool, lobGeom, gp);
                if (ellipse != null)
                {
                    setToAddTo.add(ellipse);
                }
            }
        }
        else
        {
            throw new IllegalArgumentException(
                    "Cannot create geometries from type " + (bd.getMGS() == null ? "NULL" : bd.getMGS().getClass().getName()));
        }
    }

    @Override
    public BaseRenderProperties getAlteredRenderProperty(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, DataTypeInfo dti, VisualizationState vs,
            VisualizationState defaultVS, MetaDataProvider mdp, BaseRenderProperties orig)
    {
        BaseRenderProperties alteredRP = super.getAlteredRenderProperty(changedParameterKeyToParameterMap, dti, vs, defaultVS,
                mdp, orig);

        VisualizationStyleParameter poitSizeParameter = changedParameterKeyToParameterMap.get(ourLOBOriginPointSizePropertyKey);
        if (poitSizeParameter != null)
        {
            if (alteredRP instanceof PointRenderProperties)
            {
                PointRenderProperties dprp = (PointRenderProperties)alteredRP;
                if (dprp.getSize() != getOriginPointSize())
                {
                    dprp.setSize(getOriginPointSize());
                }
            }
            else if (alteredRP instanceof PointSizeRenderProperty)
            {
                PointSizeRenderProperty dprp = (PointSizeRenderProperty)alteredRP;
                if (dprp.getSize() != getOriginPointSize())
                {
                    dprp.setSize(getOriginPointSize());
                }
            }
        }

        VisualizationStyleParameter lineWidthParameter = changedParameterKeyToParameterMap.get(ourLOBLineWidthPropertyKey);
        if (lineWidthParameter != null && alteredRP instanceof ScalableRenderProperties)
        {
            ScalableRenderProperties dprp = (ScalableRenderProperties)alteredRP;
            if (dprp.getWidth() != getLOBLineWidth())
            {
                dprp.setWidth(getLOBLineWidth());
            }
        }

        return alteredRP;
    }

    /**
     * Gets the Arrow length.
     *
     * @return the Arrow length
     */
    public Length getArrowLength()
    {
        Float val = (Float)getStyleParameterValue(ourArrowLengthPropertyKey);
        return val == null ? Meters.ZERO : new Kilometers(val.floatValue());
    }

    /**
     * Gets the lob length.
     *
     * @param metaDataProvider the meta data provider
     * @return the lob length
     */
    public Length getLobLength(MetaDataProvider metaDataProvider)
    {
        Length length;
        String mode = (String)getStyleParameterValue(ourLengthModePropertyKey);
        if (COLUMN_MODE.equals(mode))
        {
            String column = (String)getStyleParameterValue(ourLengthColumnPropertyKey);
            Object value = metaDataProvider.getValue(column);
            try
            {
                double doubleValue = StyleUtils.convertValueToDouble(value);
                Length multiplier = (Length)getStyleParameterValue(ourLengthMultiplierPropertyKey);
                length = multiplier.multiplyBy(doubleValue);
            }
            catch (NumberFormatException e)
            {
                length = Meters.ZERO;
            }
        }
        else
        {
            length = (Length)getStyleParameterValue(ourLOBLengthPropertyKey);
        }
        if (length.compareTo(MAX_LOB_LENGTH) > 0)
        {
            length = MAX_LOB_LENGTH;
        }
        return length;
    }

    /**
     * Gets the lob line width.
     *
     * @return the line width
     */
    public float getLOBLineWidth()
    {
        Float val = (Float)getStyleParameterValue(ourLOBLineWidthPropertyKey);
        return val == null ? 0 : val.floatValue();
    }

    /**
     * Gets the lob length error.
     *
     * @param metaDataProvider the meta data provider
     * @return the lob length error
     */
    public Length getLobLengthError(MetaDataProvider metaDataProvider)
    {
        Length length = Meters.ZERO;
        Boolean show = (Boolean)getStyleParameterValue(ourShowErrorPropertyKey);
        if (show.booleanValue())
        {
            String column = (String)getStyleParameterValue(ourLengthErrorColumnPropertyKey);
            Object value = metaDataProvider.getValue(column);
            try
            {
                double doubleValue = StyleUtils.convertValueToDouble(value);
                Length multiplier = (Length)getStyleParameterValue(ourLengthErrorMultiplierPropertyKey);
                length = multiplier.multiplyBy(doubleValue);
            }
            catch (NumberFormatException e)
            {
                length = Meters.ZERO;
            }
            if (length.getMagnitude() < 0)
            {
                length = length.negate();
            }
        }
        return length;
    }

    /**
     * Gets the bearing error.
     *
     * @param metaDataProvider the meta data provider
     * @return the bearing error (in degrees)
     */
    public double getBearingError(MetaDataProvider metaDataProvider)
    {
        double error = 0;
        Boolean show = (Boolean)getStyleParameterValue(ourShowErrorPropertyKey);
        if (show.booleanValue())
        {
            String column = (String)getStyleParameterValue(ourBearingErrorColumnPropertyKey);
            Object value = metaDataProvider.getValue(column);
            try
            {
                double doubleValue = StyleUtils.convertValueToDouble(value);
                Float multiplier = (Float)getStyleParameterValue(ourBearingErrorMultiplierPropertyKey);
                error = multiplier.doubleValue() * doubleValue;
            }
            catch (NumberFormatException e)
            {
                error = 0;
            }
        }
        error = Math.min(Math.abs(error), 180);
        return error;
    }

    @Override
    @Nonnull
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = super.getMiniUIPanel();

        PanelBuilder builder = PanelBuilder.get(panel.getChangedStyle().getStyleParameter(ourArrowLengthPropertyKey).getName(), 5,
                0, 0, 5);
        builder.setOtherParameter(AbstractStyleParameterEditorPanel.PANEL_HEIGHT, Integer.valueOf(24));
        builder.setOtherParameter(FloatSliderStyleParameterEditorPanel.SHOW_SLIDER_LABELS, Boolean.FALSE);

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null,
                getParamGroup(panel, StyleUtils::createSliderMiniPanelBuilder, builder), false, 1);
        panel.addGroupAtTop(paramGrp);

        return panel;
    }

    /**
     * Gets the origin point size.
     *
     * @return the center point size
     */
    public float getOriginPointSize()
    {
        Float val = (Float)getStyleParameterValue(ourLOBOriginPointSizePropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    @Override
    @Nonnull
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Basic LOB Style",
                getParamGroup(panel, PanelBuilder::get, null));
        panel.addGroup(paramGrp);

        return panel;
    }

    /**
     * Helper method for getMiniUIPanel and getUIPanel. Prevents code
     * duplication.
     *
     * @param panel the base panel to build from
     * @param builderFunc the function that generates a PanelBuilder
     * @param arrowBuilder an optional panel builder for arrow properties; can
     *            be null
     * @return a list of parameter editor panels to be condensed into a group
     *         panel
     */
    private List<AbstractStyleParameterEditorPanel> getParamGroup(AbstractGroupedVisualizationControlPanel panel,
            Function<String, PanelBuilder> builderFunc, PanelBuilder arrowBuilder)
    {
        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourLOBLineWidthPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(builderFunc.apply(param.getName()), style,
                ourLOBLineWidthPropertyKey, true, false, 1.0f, MAX_LINE_WIDTH,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null)));

        param = style.getStyleParameter(ourLOBOriginPointSizePropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(builderFunc.apply(param.getName()), style,
                ourLOBOriginPointSizePropertyKey, true, false, 0.0f, MAX_POINT_SIZE,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null)));

        addLengthPanels(paramList, style, builderFunc, arrowBuilder, panel);
        addErrorAndEllipsePanels(paramList, style, builderFunc, panel, false);

        return paramList;
    }

    /**
     * Creates and adds the length-related panels to the list.
     *
     * @param paramList the panel list
     * @param style the style
     * @param builderBuilder the panel builder creator
     * @param arrowBuilder an optional panel builder for arrow properties; can
     *            be null
     * @param panel the panel that everything is being added to
     */
    private void addLengthPanels(Collection<? super AbstractStyleParameterEditorPanel> paramList, MutableVisualizationStyle style,
            Function<String, PanelBuilder> builderBuilder, PanelBuilder arrowBuilder, AbstractVisualizationControlPanel panel)
    {
        VisualizationStyleParameter vsp = style.getStyleParameter(ourLengthModePropertyKey);
        paramList.add(new RadioButtonParameterEditorPanel(builderBuilder.apply(vsp.getName()), style, ourLengthModePropertyKey,
                New.list(MANUAL_MODE, COLUMN_MODE)));

        myLengthUnits = getToolbox().getUnitsRegistry().getPreferredFixedScaleUnits(Length.class, MAX_LOB_LENGTH);

        AdvancedLengthParameterEditorPanel manualLengthPanel = new AdvancedLengthParameterEditorPanel(builderBuilder.apply(null),
                style, ourLOBLengthPropertyKey, LENGTH_UNITS);
        paramList.add(manualLengthPanel);

        EditorPanelVisibilityDependency visDepend = new EditorPanelVisibilityDependency(panel, manualLengthPanel);
        visDepend.addConstraint(new ParameterVisibilityConstraint(ourLengthModePropertyKey, true, MANUAL_MODE));
        visDepend.evaluateStyle();
        panel.addVisibilityDependency(visDepend);

        DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
        if (dti != null && dti.getMetaDataInfo() != null)
        {
            setDefaultValue(style, dti, ourLengthColumnPropertyKey, SpeedKey.DEFAULT);
            ColumnLengthParameterEditorPanel columnLengthPanel = new ColumnLengthParameterEditorPanel(builderBuilder.apply(null),
                    style, ourLengthColumnPropertyKey, ourLengthMultiplierPropertyKey, dti.getMetaDataInfo().getKeyNames(),
                    LENGTH_UNITS);
            paramList.add(columnLengthPanel);

            visDepend = new EditorPanelVisibilityDependency(panel, columnLengthPanel);
            visDepend.addConstraint(new ParameterVisibilityConstraint(ourLengthModePropertyKey, true, COLUMN_MODE));
            visDepend.evaluateStyle();
            panel.addVisibilityDependency(visDepend);
        }

        // Arrow stuff.
        vsp = style.getStyleParameter(ourShowArrowPropertyKey);
        CheckBoxStyleParameterEditorPanel showArrowPanel = new CheckBoxStyleParameterEditorPanel(
                builderBuilder.apply(vsp.getName()), style, ourShowArrowPropertyKey, true);
        paramList.add(showArrowPanel);

        // Max size shouldn't be 500k meters, it should be the actual max size.
        Length maxArrowLength = manualLengthPanel.getLengthBinding().get().multiplyBy(0.5);

        vsp = style.getStyleParameter(ourArrowLengthPropertyKey);
        arrowBuilder = arrowBuilder == null ? builderBuilder.apply(vsp.getName()) : arrowBuilder;
        LengthSliderStyleParameterEditorPanel arrowLenPanel = new LengthSliderStyleParameterEditorPanel(arrowBuilder, style,
                ourArrowLengthPropertyKey, false, true, MIN_ARROW_LENGTH, maxArrowLength, myLengthUnits, Kilometers.class);
        AbstractStyleParameterEditorPanel filler = new BlankPanel();
        showArrowPanel.getSiblingComponents().add(arrowLenPanel);
        showArrowPanel.getSiblingComponents().add(filler);

        manualLengthPanel.getLengthBinding().addListener(arrowLenPanel);

        visDepend = new EditorPanelVisibilityDependency(panel, arrowLenPanel);
        visDepend.addConstraint(new ParameterVisibilityConstraint(ourShowArrowPropertyKey, true, Boolean.TRUE));
        visDepend.evaluateStyle();
        panel.addVisibilityDependency(visDepend);

        visDepend = new EditorPanelVisibilityDependency(panel, filler);
        visDepend.addConstraint(new ParameterVisibilityConstraint(ourShowArrowPropertyKey, true, Boolean.FALSE));
        visDepend.evaluateStyle();
        panel.addVisibilityDependency(visDepend);
    }

    /**
     * Creates and adds the error and ellipse panels to the list.
     *
     * @param paramList the panel list
     * @param style the style
     * @param builderBuilder the panel builder creator
     * @param panel the panel that everything is being added to
     * @param isMini whether this is for the mini panel
     */
    private void addErrorAndEllipsePanels(Collection<? super AbstractStyleParameterEditorPanel> paramList,
            MutableVisualizationStyle style, Function<String, PanelBuilder> builderBuilder,
            AbstractVisualizationControlPanel panel, boolean isMini)
    {
        PanelBuilder panelBuilder = PanelBuilder.get(null, isMini ? 20 : 5, 0, 0, 0);
        panelBuilder.setOtherParameter("firstIndent", "9");
        AbstractStyleParameterEditorPanel errorAndEllipsePanel = new MultipleCheckBoxParameterEditorPanel(panelBuilder, style,
                ourShowErrorPropertyKey, ourShowEllipsePropertyKey);
        paramList.add(errorAndEllipsePanel);

        DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
        if (dti != null && dti.getMetaDataInfo() != null)
        {
            setDefaultValue(style, dti, ourBearingErrorColumnPropertyKey, HeadingErrorKey.DEFAULT);
            PanelBuilder builder = builderBuilder.apply(style.getStyleParameter(ourBearingErrorColumnPropertyKey).getName());
            builder.setOtherParameter("unitPreLabel", "+/-");
            AbstractStyleParameterEditorPanel bearingErrorPanel = new ColumnAngleParameterEditorPanel(builder, style,
                    ourBearingErrorColumnPropertyKey, ourBearingErrorMultiplierPropertyKey, dti.getMetaDataInfo().getKeyNames());
            paramList.add(bearingErrorPanel);

            EditorPanelVisibilityDependency visDepend = new EditorPanelVisibilityDependency(panel, bearingErrorPanel);
            visDepend.addConstraint(new ParameterVisibilityConstraint(ourShowErrorPropertyKey, true, Boolean.TRUE));
            visDepend.evaluateStyle();
            panel.addVisibilityDependency(visDepend);

            setDefaultValue(style, dti, ourLengthErrorColumnPropertyKey, SpeedErrorKey.DEFAULT);
            builder = builderBuilder.apply(style.getStyleParameter(ourLengthErrorColumnPropertyKey).getName());
            builder.setOtherParameter("unitPreLabel", "+/-");
            AbstractStyleParameterEditorPanel lengthErrorPanel = new ColumnLengthParameterEditorPanel(builder, style,
                    ourLengthErrorColumnPropertyKey, ourLengthErrorMultiplierPropertyKey, dti.getMetaDataInfo().getKeyNames(),
                    LENGTH_UNITS);
            paramList.add(lengthErrorPanel);

            visDepend = new EditorPanelVisibilityDependency(panel, lengthErrorPanel);
            visDepend.addConstraint(new ParameterVisibilityConstraint(ourShowErrorPropertyKey, true, Boolean.TRUE));
            visDepend.evaluateStyle();
            panel.addVisibilityDependency(visDepend);
        }
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultLOBLengthParameter);
        setParameter(ourDefaultArrowLengthParameter);
        setParameter(ourDefaultLOBLineWidthParameter);
        setParameter(ourDefaultLOBOriginPointSizeParameter);
        setParameter(ourDefaultShowArrowParameter);
        setParameter(ourDefaultLengthModeParameter);
        setParameter(ourDefaultLengthColumnParameter);
        setParameter(ourDefaultLengthMultiplierParameter);
        setParameter(ourDefaultShowErrorParameter);
        setParameter(ourDefaultShowEllipseParameter);
        setParameter(ourDefaultBearingErrorColumnParameter);
        setParameter(ourDefaultBearingErrorMultiplierParameter);
        setParameter(ourDefaultLengthErrorColumnParameter);
        setParameter(ourDefaultLengthErrorMultiplierParameter);
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
     * Sets the default value for the property from the special key, if not
     * already set.
     *
     * @param style the style
     * @param dataType the data type
     * @param propertyKey the property key
     * @param specialKey the special key
     */
    private void setDefaultValue(VisualizationStyle style, DataTypeInfo dataType, String propertyKey, SpecialKey specialKey)
    {
        if (style.getStyleParameterValue(propertyKey) == null)
        {
            String lengthColumnKey = dataType.getMetaDataInfo().getKeyForSpecialType(specialKey);
            if (lengthColumnKey != null)
            {
                style.setParameter(propertyKey, lengthColumnKey, NO_EVENT_SOURCE);
            }
        }
    }

    /**
     * Checks if is show arrow.
     *
     * @return true, if is show arrow
     */
    public boolean isShowArrow()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourShowArrowPropertyKey);
        return val != null && val.booleanValue();
    }

    /**
     * Set Arrow Length.
     *
     * @param length the arrow length
     * @param source the source
     */
    public void setArrowLength(Length length, Object source)
    {
        setParameter(ourArrowLengthPropertyKey, Float.valueOf((float)new Kilometers(length).getMagnitude()), source);
    }

    /**
     * Sets lob line width.
     *
     * @param width the width
     * @param source the source
     */
    public void setLOBLineWidth(float width, Object source)
    {
        if (width < 1)
        {
            throw new IllegalArgumentException("Lob line width must be positive.");
        }
        setParameter(ourLOBLineWidthPropertyKey, Float.valueOf(width), source);
    }

    /**
     * Sets the origin point size.
     *
     * @param size the size
     * @param source the source
     */
    public void setOriginPointSize(float size, Object source)
    {
        setParameter(ourLOBOriginPointSizePropertyKey, Float.valueOf(size), source);
    }

    /**
     * Sets the show arrow.
     *
     * @param showArrow true to show the arrow, false not.
     * @param source the source of the change.
     */
    public void setShowArrow(boolean showArrow, Object source)
    {
        setParameter(ourShowArrowPropertyKey, showArrow ? Boolean.TRUE : Boolean.FALSE, source);
    }

    /**
     * Gets the lob orientation.
     *
     * @param elementId the element id
     * @param mgs the mgs
     * @param mdi the mdi
     * @return the lob orientation
     */
    public abstract Float getLobOrientation(long elementId, MapGeometrySupport mgs, MetaDataProvider mdi);

    /**
     * Creates the lob builder.
     *
     * @param bd the bd
     * @param orientation the orientation
     * @param gp the gp
     * @param mapVisInfo the map vis info
     * @param basicVisInfo the basic vis info
     * @return the line of bearing geometry. builder
     */
    private LineOfBearingGeometry.Builder createLobBuilder(FeatureIndividualGeometryBuilderData bd, Float orientation,
            GeographicPosition gp, MapVisualizationInfo mapVisInfo, BasicVisualizationInfo basicVisInfo)
    {
        LineOfBearingGeometry.Builder lobBuilder = new LineOfBearingGeometry.Builder();
        lobBuilder.setPosition(gp);
        lobBuilder.setLineOrientation(orientation.floatValue());
        lobBuilder.setDataModelId(bd.getGeomId());
        lobBuilder.setDisplayArrow(isShowArrow());
        lobBuilder.setLineType(LineType.GREAT_CIRCLE);
        return lobBuilder;
    }

    /**
     * Determine render properties.
     *
     * @param mapVisInfo Data type level info relevant for rendering.
     * @param basicVisInfo Basic information for the data type.
     * @param bd the builder data
     * @param renderPropertyPool the render property pool
     * @param centerLocation the center (point) location
     * @return the point render properties
     */
    private LOBRenderProperties determineRenderProperties(MapVisualizationInfo mapVisInfo, BasicVisualizationInfo basicVisInfo,
            FeatureIndividualGeometryBuilderData bd, RenderPropertyPool renderPropertyPool, GeographicPosition centerLocation)
    {
        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();
        LOBRenderProperties props = new DefaultLOBRenderProperties(zOrder, true, pickable);
        VisualizationState visState = bd.getVS();
        props.setColor(visState.isSelected() ? MantleConstants.SELECT_COLOR
                : visState.isDefaultColor() ? getColor() : visState.getColor());
        props.setWidth(visState.isSelected() ? getLOBLineWidth() + MantleConstants.SELECT_WIDTH_ADDITION : getLOBLineWidth());
        float lobLength = visState.isLobVisible() ? (float)getLobLength(bd.getMDP()).inMeters() : 0.0f;
        float arrowLength = (float)getArrowLength().inMeters();
        arrowLength = Math.min(arrowLength, lobLength / 2);
        props.setBaseAltitude((float)centerLocation.getAlt().getMeters());
        props.setLineLength(lobLength);
        props.setDirectionalArrowLength(arrowLength);
        props = renderPropertyPool.getPoolInstance(props);
        return props;
    }

    /**
     * Adds error arcs to the set, if necessary.
     *
     * @param setToAddTo the set to which to add the geometries
     * @param bd the builder data
     * @param renderPropertyPool the render property pool
     * @param lobGeom the LOB geometry
     * @param centerLocation the center (point) location
     */
    private void addErrorArcs(Set<? super Geometry> setToAddTo, FeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool renderPropertyPool, LineOfBearingGeometry lobGeom, GeographicPosition centerLocation)
    {
        if (lobGeom != null && lobGeom.getRenderProperties().getLineLength() > 0)
        {
            double bearingError = getBearingError(bd.getMDP());
            Length lengthError = getLobLengthError(bd.getMDP());
            if (bearingError > 0 || lengthError.getMagnitude() > 0)
            {
                setToAddTo.add(createErrorArc(bd, renderPropertyPool, lobGeom, centerLocation, bearingError, lengthError));
            }
            if (lengthError.getMagnitude() > 0)
            {
                setToAddTo
                        .add(createErrorArc(bd, renderPropertyPool, lobGeom, centerLocation, bearingError, lengthError.negate()));
            }
        }
    }

    /**
     * Creates an error arc line geometry.
     *
     * @param bd the builder data
     * @param renderPropertyPool the render property pool
     * @param lobGeom the LOB geometry
     * @param centerLocation the center (point) location
     * @param bearingErrorDeg the bearing error in degrees
     * @param legthError the length error
     * @return the geometry
     */
    private PolylineGeometry createErrorArc(FeatureIndividualGeometryBuilderData bd, RenderPropertyPool renderPropertyPool,
            LineOfBearingGeometry lobGeom, GeographicPosition centerLocation, double bearingErrorDeg, Length legthError)
    {
        // Create the line builder
        PolylineGeometry.Builder<GeographicPosition> lineBuilder = new PolylineGeometry.Builder<>();
        lineBuilder.setDataModelId(bd.getGeomId());
        double lineLengthM = lobGeom.getRenderProperties().getLineLength() + legthError.inMeters();
        if (bearingErrorDeg > 0)
        {
            // Create the ellipse arc builder
            EllipseGeometry.ProjectedBuilder arcBuilder = new EllipseGeometry.ProjectedBuilder();
            arcBuilder.setCenter(centerLocation);
            arcBuilder.setAngle(lobGeom.getLineOrientation());
            arcBuilder.setSemiMajorAxis(lineLengthM);
            arcBuilder.setSemiMinorAxis(lineLengthM);
            arcBuilder.setProjection(getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot());
            /* 37 is the default ellipse vertex count. Since bearing error can
             * be in the range of 0 to 180, this calculation results in the
             * appropriate number of vertices (2 - 37) depending on how long the
             * arc is. */
            int vertexCount = (int)Math.ceil(bearingErrorDeg / 180 * 35 + 2);
            arcBuilder.setVertexCount(vertexCount);

            lineBuilder.setVertices(EllipseGeometryUtilities.createProjectedVertices(arcBuilder, bearingErrorDeg));
        }
        else
        {
            // Draw lines when no bearing error
            LatLonAlt center = GeographicBody3D.greatCircleEndPosition(centerLocation.getLatLonAlt(),
                    Math.toRadians(lobGeom.getLineOrientation()), WGS84EarthConstants.RADIUS_MEAN_M, lineLengthM);
            LatLonAlt point1 = GeographicBody3D.greatCircleEndPosition(center, Math.toRadians(lobGeom.getLineOrientation() - 90),
                    WGS84EarthConstants.RADIUS_MEAN_M, lobGeom.getRenderProperties().getDirectionalArrowLength());
            point1 = point1.withAltitude(centerLocation.getAlt());
            LatLonAlt point2 = GeographicBody3D.greatCircleEndPosition(center, Math.toRadians(lobGeom.getLineOrientation() + 90),
                    WGS84EarthConstants.RADIUS_MEAN_M, lobGeom.getRenderProperties().getDirectionalArrowLength());
            point2 = point2.withAltitude(centerLocation.getAlt());
            lineBuilder.setVertices(New.list(new GeographicPosition(point1), new GeographicPosition(point2)));
        }

        // Create the line render properties
        PolylineRenderProperties renderProperties = new DefaultPolylineRenderProperties(lobGeom.getRenderProperties().getZOrder(),
                true, lobGeom.getRenderProperties().isPickable());
        renderProperties.setColor(lobGeom.getRenderProperties().getColor());
        renderProperties.setWidth(lobGeom.getRenderProperties().getWidth());
        renderProperties = renderPropertyPool.getPoolInstance(renderProperties);

        // Create the constraints
        Constraints constraints = lobGeom.getConstraints();

        return new PolylineGeometry(lineBuilder, renderProperties, constraints);
    }

    /**
     * Creates an ellipse geometry.
     *
     * @param bd the builder data
     * @param renderPropertyPool the render property pool
     * @param lobGeom the LOB geometry
     * @param centerLocation the center (point) location
     * @return the geometry, or null if it couldn't be created
     */
    @SuppressWarnings("unchecked")
    private EllipseGeometry createEllipse(FeatureIndividualGeometryBuilderData bd, RenderPropertyPool renderPropertyPool,
            LineOfBearingGeometry lobGeom, GeographicPosition centerLocation)
    {
        // Create the builder
        EllipseGeometry.ProjectedBuilder builder = new EllipseGeometry.ProjectedBuilder();
        MetaDataInfo metaData = bd.getDataType().getMetaDataInfo();
        String smajKey = metaData.getKeyForSpecialType(EllipseSemiMajorAxisKey.DEFAULT);
        String sminKey = metaData.getKeyForSpecialType(EllipseSemiMinorAxisKey.DEFAULT);
        String orientKey = metaData.getKeyForSpecialType(EllipseOrientationKey.DEFAULT);
        if (smajKey != null && sminKey != null && orientKey != null)
        {
            double smajValue = StyleUtils.convertValueToDouble(bd.getMDP().getValue(smajKey));
            Class<? extends Length> smajUnit = (Class<? extends Length>)metaData.getSpecialTypeForKey(smajKey).getKeyUnit();
            builder.setSemiMajorAxis(StyleUtils.getValueInMeters(smajValue, smajUnit));

            double sminValue = StyleUtils.convertValueToDouble(bd.getMDP().getValue(sminKey));
            Class<? extends Length> sminUnit = (Class<? extends Length>)metaData.getSpecialTypeForKey(sminKey).getKeyUnit();
            builder.setSemiMinorAxis(StyleUtils.getValueInMeters(sminValue, sminUnit));

            double orientValue = StyleUtils.convertValueToDouble(bd.getMDP().getValue(orientKey));
            builder.setAngle(orientValue);
        }
        else
        {
            return null;
        }
        builder.setDataModelId(bd.getGeomId());
        builder.setCenter(centerLocation);
        builder.setProjection(getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot());

        // Create the render properties
        PolygonRenderProperties renderProperties = new DefaultPolygonRenderProperties(lobGeom.getRenderProperties().getZOrder(),
                true, lobGeom.getRenderProperties().isPickable());
        renderProperties.setColor(lobGeom.getRenderProperties().getColor());
        renderProperties.setWidth(lobGeom.getRenderProperties().getWidth());
        renderProperties = renderPropertyPool.getPoolInstance(renderProperties);

        // Create the constraints
        Constraints constraints = lobGeom.getConstraints();

        return new EllipseGeometry(builder, renderProperties, constraints);
    }

    /** A blank AbstractStyleParameterEditorPanel. */
    static class BlankPanel extends AbstractStyleParameterEditorPanel
    {
        /** The serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        protected void update()
        {
        }
    };
}
