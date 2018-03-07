package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultZOrderRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.StyleAltitudeReference;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxAndColorChooserEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.EditorPanelVisibilityDependency;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.LabelComboEditor;
import io.opensphere.mantle.data.geom.style.impl.ui.LengthSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.MultiComboBoxStyleTwoParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.MultipleCheckBoxParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.ParameterVisibilityConstraint;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.data.impl.specialkey.AltitudeKey;

/**
 * The Class AbstractFeatureVisualizationStyle.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractFeatureVisualizationStyle extends AbstractVisualizationStyle implements FeatureVisualizationStyle
{
    /** Label Z order. */
    private static final int LABEL_Z_ORDER = DefaultOrderCategory.FEATURE_CATEGORY.getOrderMax() + 1;

    /** The Constant MAX_LIFT. */
    private static final Length MAX_LIFT = new Meters(50000.0);

    /** The our text size choices. */
    private static List<Integer> ourTextSizeChoices = CollectionUtilities.listViewInt(8, 9, 10, 11, 12, 13, 14, 15, 18, 20, 26,
            32, 48, 60);

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractFeatureVisualizationStyle.class);

    /** The Constant MAX_POINT_SIZE. */
    public static final float MAX_POINT_SIZE = 50.0f;

    /** The Constant MAX_LINE_WIDTH. */
    public static final float MAX_LINE_WIDTH = 10.0f;

    /** The Constant ourPropertyKeyPrefix. */
    private static final String PROPERTY_KEY_PREFIX = "AbstractFeatureVisualizationStyle";

    /** The Constant ourColorPropertyKey. */
    public static final String COLOR_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".Color";

    /** The Constant ourLiftPropertyKey. */
    public static final String LIFT_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".Lift";

    /** The Constant ourUseAltitudePropertyKey. */
    public static final String USE_ALTITUDE_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".UseAltitude";

    /** The Constant ourUseAltitudePropertyKey. */
    public static final String IS_DEPTH_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".IsDepth";

    /** The Constant ourAltitudeReference. */
    public static final String ALTITUDE_REFERENCE_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".AltitudeReference";

    /** The Constant ourAltitudeMetaDataColumnKey. */
    public static final String ALTITUDE_METADATA_COLUMN_KEY_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".AltitudeColumn";

    /** The Constant ourAltitudeUnitPropertyKey. */
    public static final String ALTITUDE_UNIT_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".AltitudeUnit";

    /** The Constant ourLabelColumnKeyPropertykey. */
    public static final String LABEL_COLUMN_KEY_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".LabelColumn";

    /**
     * The Constant ourLabelEnabledPropertykey, used to determine if label
     * displays are enabled.
     */
    public static final String LABEL_ENABLED_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".ShowLabels";

    /** The Constant ourLabelColorKeyPropertykey. */
    public static final String LABEL_COLOR_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".LabelColor";

    /** The Constant ourLabelSizePropertyKey. */
    public static final String LABEL_SIZE_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".LabelSize";

    /** The Constant ourDefaultColorProperty. */
    public static final VisualizationStyleParameter DEFAULT_COLOR_PROPERTY = new VisualizationStyleParameter(COLOR_PROPERTY_KEY,
            "Color", Color.white, Color.class, new VisualizationStyleParameterFlags(false, false),
            ParameterHint.hint(true, false));

    /** The Constant ourDefaultLiftProperty. */
    private static final VisualizationStyleParameter DEFAULT_LIFT_PROPERTY = new VisualizationStyleParameter(LIFT_PROPERTY_KEY,
            "Lift", Double.valueOf(0.0), Double.class, new VisualizationStyleParameterFlags(false, false),
            ParameterHint.hint(false, false));

    /** The Constant ourDefaultUseAltitudeParameter. */
    private static final VisualizationStyleParameter DEFAULT_USE_ALTITUDE_PARAMETER = new VisualizationStyleParameter(
            USE_ALTITUDE_PROPERTY_KEY, "Use Altitude", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(true, false), ParameterHint.hint(false, true));

    /** The Constant ourDefaultUseAltitudeParameter. */
    private static final VisualizationStyleParameter DEFAULT_IS_DEPTH_PARAMETER = new VisualizationStyleParameter(
            IS_DEPTH_PROPERTY_KEY, "Is Depth", Boolean.FALSE, Boolean.class, new VisualizationStyleParameterFlags(true, false),
            ParameterHint.hint(false, true));

    /** The Constant ourDefaultAltitudeReferenceParameter. */
    private static final VisualizationStyleParameter DEFAULT_ALTITUDE_REFERENCE_PARAMETER = new VisualizationStyleParameter(
            ALTITUDE_REFERENCE_PROPERTY_KEY, "Altitude Reference", StyleAltitudeReference.AUTOMATIC, StyleAltitudeReference.class,
            new VisualizationStyleParameterFlags(true, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultAltitudeColumnProperty. */
    private static final VisualizationStyleParameter DEFAULT_ALTITUDE_COLUMN_PROPERTY = new VisualizationStyleParameter(
            ALTITUDE_METADATA_COLUMN_KEY_PROPERTY_KEY, "Altitude Column", null, String.class,
            new VisualizationStyleParameterFlags(true, true), ParameterHint.hint(false, true));

    /** The constant in which the enabled state of the labels are tracked. */
    public static final VisualizationStyleParameter DEFAULT_LABEL_ENABLED_PROPERTY = new VisualizationStyleParameter(
            LABEL_ENABLED_PROPERTY_KEY, "Label Enabled", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, true));

    /** The Constant ourDefaultLabelColumnProperty. */
    private static final VisualizationStyleParameter DEFAULT_LABEL_COLUMN_PROPERTY = new VisualizationStyleParameter(
            LABEL_COLUMN_KEY_PROPERTY_KEY, "Label Column", null, String.class, new VisualizationStyleParameterFlags(true, true),
            ParameterHint.hint(false, true));

    /** The Constant ourDefaultColorProperty. */
    private static final VisualizationStyleParameter DEFAULT_LABEL_COLOR_PROPERTY = new VisualizationStyleParameter(
            LABEL_COLOR_PROPERTY_KEY, "Label Color", Color.white, Color.class,
            new VisualizationStyleParameterFlags(false, false, true), ParameterHint.hint(false, false));

    /** The Constant ourDefaultLabelSizeProperty. */
    private static final VisualizationStyleParameter DEFAULT_LABEL_SIZE_PROPERTY = new VisualizationStyleParameter(
            LABEL_SIZE_PROPERTY_KEY, "Label Size", Integer.valueOf(8), Integer.class,
            new VisualizationStyleParameterFlags(true, false), ParameterHint.hint(false, false));

    /**
     * Creates the necessary geometries to properly view features below the
     * surface.
     */
    private final SubSurfaceGeometryCreator mySubsurfaceGeometryCreator = new SubSurfaceGeometryCreator();

    /**
     * Instantiates a new abstract feature visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public AbstractFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new abstract feature visualization style.
     *
     * @param tb the tb
     * @param dtiKey the dti key
     */
    public AbstractFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle#allChangesRequireRebuild()
     */
    @Override
    public boolean allChangesRequireRebuild()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.AbstractVisualizationStyle#clone()
     */
    @Override
    public AbstractFeatureVisualizationStyle clone()
    {
        return (AbstractFeatureVisualizationStyle)super.clone();
    }

    /**
     * Creates the label geometry.
     *
     * @param setToAddTo the set to add the label geometry to if one can be
     *            created.
     * @param builderData the {@link FeatureIndividualGeometryBuilderData}
     * @param position the {@link GeographicPosition}
     * @param c the {@link Constraints}
     * @param pool the {@link RenderPropertyPool}
     * @return true if created false if not.
     */
    protected boolean createLabelGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData builderData,
            GeographicPosition position, Constraints c, RenderPropertyPool pool)
    {
        boolean added = false;
        VisualizationStyleParameter labelParameter = getStyleParameter(LABEL_ENABLED_PROPERTY_KEY);
        if (labelParameter != null && Boolean.TRUE.equals(labelParameter.getValue()))
        {
            Object value = getLabelColumnValue(builderData.getElementId(), builderData.getMDP());
            if (value != null)
            {
                added = createLabelGeometrySpecific(setToAddTo, builderData, position, c, pool, value.toString());
            }
        }
        return added;
    }

    /**
     * Creates the label geometry specific.
     *
     * @param setToAddTo the set to add to
     * @param builderData the builder data
     * @param position the position
     * @param c the c
     * @param pool the rpp
     * @param labValue the lab value
     * @return true, if successful
     */
    private boolean createLabelGeometrySpecific(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData builderData,
            GeographicPosition position, Constraints c, RenderPropertyPool pool, String labValue)
    {
        boolean added = false;
        LabelGeometry.Builder<GeographicPosition> builder = new LabelGeometry.Builder<>();
        builder.setDataModelId(builderData.getGeomId());

        GeographicPosition gp = position;
        if (gp == null && builderData.getMGS() instanceof MapLocationGeometrySupport)
        {
            gp = createGeographicPosition((MapLocationGeometrySupport)builderData.getMGS(), builderData.getMDP(),
                    builderData.getVS());
        }
        else if (gp == null && builderData.getMGS() != null)
        {
            GeographicBoundingBox gbb = builderData.getMGS()
                    .getBoundingBox(getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot());
            gp = gbb.getCenter();
        }

        builder.setPosition(gp);
        builder.setHorizontalAlignment(.5f);
        builder.setVerticalAlignment(1f);
        builder.setText(labValue);
        builder.setFont(Font.SANS_SERIF + " " + getLabelSize());
        builder.setOutlined(true);

        LabelRenderProperties crp = new DefaultLabelRenderProperties(LABEL_Z_ORDER, true, false);
        crp.setColor(getLabelColor());
        crp = pool.getPoolInstance(crp);
        added = setToAddTo.add(new LabelGeometry(builder, crp, c));
        return added;
    }

    /**
     * Creates the point geometry.
     *
     * @param builderData the {@link FeatureIndividualGeometryBuilderData}
     * @param renderPropertyPool the {@link RenderPropertyPool}
     * @param stylePointSize the style point size
     * @param constraints the {@link Constraints}
     * @param setToAddTo The set to add the point to. Will also add any other
     *            geometries that may go with the point such as a dashed line
     *            point to ground position if point is below the earth.
     * @return the abstract renderable geometry
     * @throws IllegalArgumentException the illegal argument exception
     */
    protected AbstractRenderableGeometry createPointGeometry(FeatureIndividualGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool, float stylePointSize, Constraints constraints, Set<Geometry> setToAddTo)
    {
        if (!(builderData.getMGS() instanceof MapLocationGeometrySupport))
        {
            String msg = builderData.getMGS() == null ? "NULL" : builderData.getMGS().getClass().getName();
            throw new IllegalArgumentException("Cannot create geometries from type " + msg);
        }

        // Add a time constraint if in time line mode.
        Constraints timeConstraint = constraints != null || builderData.getDataType() == null ? constraints
                : StyleUtils.createTimeConstraintsIfApplicable(builderData.getDataType().getBasicVisualizationInfo(),
                        builderData.getDataType().getMapVisualizationInfo(), builderData.getMGS(),
                        StyleUtils.getDataGroupInfoFromDti(getToolbox(), builderData.getDataType()));

        GeographicPosition position = createGeographicPosition((MapLocationGeometrySupport)builderData.getMGS(),
                builderData.getMDP(), builderData.getVS());

        PointGeometryFactory factory = new PointGeometryFactory(renderPropertyPool);
        if (position.getAlt().getMeters() < 0)
        {
            setToAddTo.addAll(mySubsurfaceGeometryCreator.createSubsurfaceGeometry(builderData, renderPropertyPool,
                    stylePointSize, factory, constraints, getColor(), position));
        }

        AbstractRenderableGeometry centerPoint = factory.createPointGeometry(builderData, position, stylePointSize, getColor(),
                timeConstraint);
        setToAddTo.add(centerPoint);

        return centerPoint;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle#deriveGeometryFromRenderPropertyChange(java.util.Map,
     *      io.opensphere.mantle.data.geom.factory.RenderPropertyPool,
     *      io.opensphere.core.geometry.AbstractRenderableGeometry,
     *      io.opensphere.mantle.data.DataTypeInfo,
     *      io.opensphere.mantle.data.element.VisualizationState,
     *      io.opensphere.mantle.data.element.VisualizationState,
     *      io.opensphere.mantle.data.element.MetaDataProvider)
     */
    @Override
    public AbstractRenderableGeometry deriveGeometryFromRenderPropertyChange(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, RenderPropertyPool rpp,
            AbstractRenderableGeometry geom, DataTypeInfo dti, VisualizationState vs, VisualizationState defaultVS,
            MetaDataProvider mdp)
    {
        BaseRenderProperties prop = geom.getRenderProperties().clone();
        prop = getAlteredRenderProperty(changedParameterKeyToParameterMap, dti, vs, defaultVS, mdp, prop);
        prop = rpp.getPoolInstance(prop);
        return geom.derive(prop, geom.getConstraints());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle#getAlteredRenderProperty(java.util.Map,
     *      io.opensphere.mantle.data.DataTypeInfo,
     *      io.opensphere.mantle.data.element.VisualizationState,
     *      io.opensphere.mantle.data.element.VisualizationState,
     *      io.opensphere.mantle.data.element.MetaDataProvider,
     *      io.opensphere.core.geometry.renderproperties.BaseRenderProperties)
     */
    @Override
    public BaseRenderProperties getAlteredRenderProperty(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, DataTypeInfo dti, VisualizationState vs,
            VisualizationState defaultVS, MetaDataProvider mdp, BaseRenderProperties orig)
    {
        BaseRenderProperties alteredRP = orig;
        VisualizationState useVS = vs == null ? defaultVS : vs;
        boolean isSelected = useVS != null && useVS.isSelected();

        if (alteredRP instanceof PointRenderProperties)
        {
            alterZOrderIfNecessary(isSelected, dti, ((PointRenderProperties)alteredRP).getBaseRenderProperties());
        }
        else
        {
            alterZOrderIfNecessary(isSelected, dti, alteredRP);
        }

        VisualizationStyleParameter colorParameter = changedParameterKeyToParameterMap.get(COLOR_PROPERTY_KEY);
        if (colorParameter != null)
        {
            if (orig instanceof PointRenderProperties)
            {
                PointRenderProperties dprp = (PointRenderProperties)orig;
                Color color = StyleUtils.determineColor(dprp.getColor(), vs == null ? defaultVS : vs);
                if (!Utilities.sameInstance(color, dprp.getColor()))
                {
                    dprp.setColor(color);
                }
            }
            else if (orig instanceof LabelRenderProperties)
            {
                LabelRenderProperties dcrp = (LabelRenderProperties)orig;
                Color color = getLabelColor();
                if (!Utilities.sameInstance(color, dcrp.getColor()))
                {
                    dcrp.setColor(color);
                }
            }
            else if (orig instanceof ColorRenderProperties)
            {
                ColorRenderProperties dcrp = (ColorRenderProperties)orig;
                Color color = StyleUtils.determineColor(dcrp.getColor(), vs == null ? defaultVS : vs);
                if (!Utilities.sameInstance(color, dcrp.getColor()))
                {
                    dcrp.setColor(color);
                }
            }
        }
        return alteredRP;
    }

    /**
     * Gets the altitude column property name.
     *
     * @return the altitude column property name
     */
    private String getAltitudeColumnPropertyName()
    {
        return (String)getStyleParameterValue(ALTITUDE_METADATA_COLUMN_KEY_PROPERTY_KEY);
    }

    /**
     * Gets the altitude column value.
     *
     * @param mdp the {@link MetaDataProvider}
     * @return the altitude column value in meters
     */
    public Double getAltitudeColumnValueM(MetaDataProvider mdp)
    {
        Double result = null;
        if (mdp != null)
        {
            String altColKey = getAltitudeColumnPropertyName();
            if (altColKey != null)
            {
                Class<? extends Length> altUnit = getAltitudeUnit();
                try
                {
                    double val = StyleUtils.getValueInMeters(StyleUtils.convertValueToDouble(mdp.getValue(altColKey)), altUnit);
                    if (Boolean.TRUE.equals(getStyleParameter(IS_DEPTH_PROPERTY_KEY).getValue()))
                    {
                        val = -val;
                    }
                    result = Double.valueOf(val);
                }
                catch (NumberFormatException e)
                {
                    result = null;
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("DataType[" + getDTIKey()
                                + "] Could not retrieve altitude from feature because it has no valid altitude data ["
                                + mdp.getValue(altColKey) + "] available.", e);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets the altitude reference.
     *
     * @return the altitude reference
     */
    protected StyleAltitudeReference getAltitudeReference()
    {
        StyleAltitudeReference level = (StyleAltitudeReference)getStyleParameterValue(ALTITUDE_REFERENCE_PROPERTY_KEY);
        return level == null ? StyleAltitudeReference.AUTOMATIC : level;
    }

    /**
     * Gets the altitude unit.
     *
     * @return the altitude unit
     */
    private Class<? extends Length> getAltitudeUnit()
    {
        String label = (String)getStyleParameterValue(ALTITUDE_UNIT_PROPERTY_KEY);
        return getToolbox().getUnitsRegistry().getUnitsProvider(Length.class).getUnitsWithSelectionLabel(label);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.AbstractVisualizationStyle#getAlwaysSaveParameters()
     */
    @Override
    public Set<VisualizationStyleParameter> getAlwaysSaveParameters()
    {
        Set<VisualizationStyleParameter> set = super.getAlwaysSaveParameters();
        set.add(getStyleParameter(ALTITUDE_UNIT_PROPERTY_KEY));
        return set;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle#getColor()
     */
    @Override
    public Color getColor()
    {
        return (Color)getStyleParameterValue(COLOR_PROPERTY_KEY);
    }

    /**
     * Gets the label color.
     *
     * @return the color
     */
    public Color getLabelColor()
    {
        return (Color)getStyleParameterValue(LABEL_COLOR_PROPERTY_KEY);
    }

    /**
     * Gets the label column property name.
     *
     * @return the label column property name
     */
    public Object getLabelColumnPropertyName()
    {
        return getStyleParameterValue(LABEL_COLUMN_KEY_PROPERTY_KEY);
    }

    /**
     * Gets the label size.
     *
     * @return the label size
     */
    public int getLabelSize()
    {
        Integer val = (Integer)getStyleParameterValue(LABEL_SIZE_PROPERTY_KEY);
        return val == null ? 8 : val.intValue();
    }

    /**
     * Gets the lift.
     *
     * @return the lift
     */
    public double getLift()
    {
        Double dVal = (Double)getStyleParameterValue(LIFT_PROPERTY_KEY);
        return dVal == null ? 0.0 : dVal.doubleValue();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#getMiniUIPanel()
     */
    @Override
    @Nonnull
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = new GroupedMiniStyleEditorPanel(this);
        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();
        if (getDTIKey() != null)
        {
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
            if (dti != null && dti.getMetaDataInfo() != null && dti.getMetaDataInfo().getKeyCount() > 0)
            {
                AbstractStyleParameterEditorPanel altitudeAndDepthPanel = new MultipleCheckBoxParameterEditorPanel(
                        PanelBuilder.get(null, 20, 0, 0, 0), style, USE_ALTITUDE_PROPERTY_KEY, IS_DEPTH_PROPERTY_KEY);
                paramList.add(altitudeAndDepthPanel);

                if (supportsLabels())
                {
                    CheckBoxStyleParameterEditorPanel labelEnabledPanel = new CheckBoxStyleParameterEditorPanel(
                            StyleUtils.createSliderMiniPanelBuilder("Always Show Labels"), style, LABEL_ENABLED_PROPERTY_KEY,
                            false);
                    paramList.add(labelEnabledPanel);

                    LabelComboEditor labelPanel = new LabelComboEditor(getToolbox().getEventManager(),
                            StyleUtils.createComboBoxMiniPanelBuilder("Labels:"), style, LABEL_COLUMN_KEY_PROPERTY_KEY, dti,
                            true);
                    paramList.add(labelPanel);

                    ComboBoxAndColorChooserEditorPanel labelSizeColorPanel = new ComboBoxAndColorChooserEditorPanel(
                            StyleUtils.createComboBoxMiniPanelBuilder("Label Size & Color"), style, LABEL_SIZE_PROPERTY_KEY,
                            false, ourTextSizeChoices, true, false, LABEL_COLOR_PROPERTY_KEY, null);
                    paramList.add(labelSizeColorPanel);

                    EditorPanelVisibilityDependency visDepend = new EditorPanelVisibilityDependency(panel, labelSizeColorPanel);
                    visDepend.addConstraint(new ParameterVisibilityConstraint(LABEL_COLUMN_KEY_PROPERTY_KEY, false, null));
                    visDepend.evaluateStyle();
                    panel.addVisibilityDependency(visDepend);
                }
            }
        }

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, paramList, false, 0);
        panel.addGroup(paramGrp);
        return panel;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle#getRequiredMapVisTypes()
     */
    @Override
    public Set<MapVisualizationType> getRequiredMapVisTypes()
    {
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.AbstractVisualizationStyle#getUIPanel()
     */
    @Override
    @Nonnull
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = new GroupedStyleParameterEditorPanel(this, true);
        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();
        Class<? extends Length> units = getToolbox().getUnitsRegistry().getPreferredFixedScaleUnits(Length.class, MAX_LIFT);
        paramList.add(new LengthSliderStyleParameterEditorPanel(PanelBuilder.get("Lift"), style, LIFT_PROPERTY_KEY, false, true,
                Meters.ZERO, MAX_LIFT, units, Meters.class));

        if (getDTIKey() != null)
        {
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
            if (dti != null && dti.getMetaDataInfo() != null && dti.getMetaDataInfo().getKeyCount() > 0)
            {
                PanelBuilder lb = PanelBuilder.get("Altitude Reference");
                lb.setOtherParameter(AbstractStyleParameterEditorPanel.PANEL_HEIGHT, Integer.valueOf(46));
                ComboBoxStyleParameterEditorPanel altRefPanel = new ComboBoxStyleParameterEditorPanel(lb, style,
                        ALTITUDE_REFERENCE_PROPERTY_KEY, false, false, false, Arrays.asList(StyleAltitudeReference.values()));
                altRefPanel.getValueToAlertMap().put(StyleAltitudeReference.ELLIPSOID,
                        "Warning: Some values may be drawn under terrain");
                altRefPanel.getValueToAlertMap().put(StyleAltitudeReference.ORIGIN,
                        "Warning: Some values may be drawn under terrain");
                paramList.add(altRefPanel);

                lb = PanelBuilder.get(null, 5, 0, 0, 0);
                AbstractStyleParameterEditorPanel isDepthPanel = new MultipleCheckBoxParameterEditorPanel(lb, style,
                        USE_ALTITUDE_PROPERTY_KEY, IS_DEPTH_PROPERTY_KEY);
                paramList.add(isDepthPanel);

                Collection<String> labels = Arrays
                        .asList(getToolbox().getUnitsRegistry().getAvailableUnitsSelectionLabels(Length.class, false));
                PanelBuilder pb = PanelBuilder.get("Altitude Column");
                pb.setOtherParameter(AbstractStyleParameterEditorPanel.PANEL_HEIGHT, Integer.valueOf(36));
                paramList.add(new MultiComboBoxStyleTwoParameterEditorPanel(pb, style, ALTITUDE_METADATA_COLUMN_KEY_PROPERTY_KEY,
                        ALTITUDE_UNIT_PROPERTY_KEY, false, dti.getMetaDataInfo().getKeyNames(), false, true, labels, false, false,
                        Nulls.STRING));

                if (supportsLabels())
                {
                    CheckBoxStyleParameterEditorPanel labelEnabledPanel = new CheckBoxStyleParameterEditorPanel(
                            StyleUtils.createSliderMiniPanelBuilder("Always Show Labels"), style, LABEL_ENABLED_PROPERTY_KEY,
                            false);
                    paramList.add(labelEnabledPanel);

                    pb = PanelBuilder.get("Labels");
                    LabelComboEditor labelPanel = new LabelComboEditor(getToolbox().getEventManager(), pb, style,
                            LABEL_COLUMN_KEY_PROPERTY_KEY, dti, false);
                    paramList.add(labelPanel);

                    pb = PanelBuilder.get("Label Size & Color");
                    pb.setOtherParameter(AbstractStyleParameterEditorPanel.PANEL_HEIGHT, Integer.valueOf(36));
                    ComboBoxAndColorChooserEditorPanel labelSizeColorPanel = new ComboBoxAndColorChooserEditorPanel(pb, style,
                            LABEL_SIZE_PROPERTY_KEY, false, ourTextSizeChoices, true, false, LABEL_COLOR_PROPERTY_KEY, null);
                    paramList.add(labelSizeColorPanel);

                    EditorPanelVisibilityDependency visDepend = new EditorPanelVisibilityDependency(panel, labelSizeColorPanel);
                    visDepend.addConstraint(new ParameterVisibilityConstraint(LABEL_COLUMN_KEY_PROPERTY_KEY, false, null));
                    visDepend.evaluateStyle();
                    panel.addVisibilityDependency(visDepend);
                }
            }
        }

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Basic Feature Style", paramList);
        panel.addGroup(paramGrp);
        return panel;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#initialize()
     */
    @Override
    public void initialize()
    {
        setParameter(DEFAULT_COLOR_PROPERTY);
        setParameter(DEFAULT_LIFT_PROPERTY);
        setParameter(DEFAULT_USE_ALTITUDE_PARAMETER);
        setParameter(DEFAULT_IS_DEPTH_PARAMETER);
        setParameter(DEFAULT_ALTITUDE_REFERENCE_PARAMETER);
        setParameter(DEFAULT_ALTITUDE_COLUMN_PROPERTY);
        setParameter(getDefaultAltitudeUnitProperty());
        setParameter(DEFAULT_LABEL_COLUMN_PROPERTY);
        setParameter(DEFAULT_LABEL_COLOR_PROPERTY);
        setParameter(DEFAULT_LABEL_SIZE_PROPERTY);
        setParameter(DEFAULT_LABEL_ENABLED_PROPERTY);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#initialize(java.util.Set)
     */
    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        for (VisualizationStyleParameter p : paramSet)
        {
            if (p.getKey() != null && p.getKey().startsWith(PROPERTY_KEY_PREFIX))
            {
                setParameter(p);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#initializeFromDataType()
     */
    @Override
    public void initializeFromDataType()
    {
        if (getDTIKey() != null)
        {
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
            if (dti != null)
            {
                if (dti.getMetaDataInfo() != null)
                {
                    String altKey = dti.getMetaDataInfo().getKeyForSpecialType(AltitudeKey.DEFAULT);
                    if (altKey != null)
                    {
                        setParameter(ALTITUDE_METADATA_COLUMN_KEY_PROPERTY_KEY, altKey, NO_EVENT_SOURCE);
                        SpecialKey sk = dti.getMetaDataInfo().getSpecialTypeForKey(altKey);
                        if (sk instanceof AltitudeKey)
                        {
                            setParameter(ALTITUDE_UNIT_PROPERTY_KEY,
                                    Length.getSelectionLabel(((AltitudeKey)sk).getAltitudeUnit()), NO_EVENT_SOURCE);
                        }
                    }
                }
                if (dti.getBasicVisualizationInfo() != null)
                {
                    setParameter(COLOR_PROPERTY_KEY, dti.getBasicVisualizationInfo().getDefaultTypeColor(), NO_EVENT_SOURCE);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle#isSelectionSensitiveStyle()
     */
    @Override
    public boolean isSelectionSensitiveStyle()
    {
        return false;
    }

    /**
     * Checks to see if the feature should use altitude information when
     * available.
     *
     * @return true, if use altitude
     */
    public boolean isUseAltitude()
    {
        Boolean val = (Boolean)getStyleParameterValue(USE_ALTITUDE_PROPERTY_KEY);
        return val != null && val.booleanValue();
    }

    /**
     * Tests to determine if labels are currently enabled.
     *
     * @return true if labels should be displayed, false otherwise.
     */
    public boolean isLabelEnabled()
    {
        Boolean val = (Boolean)getStyleParameterValue(LABEL_ENABLED_PROPERTY_KEY);
        return val != null && val.booleanValue();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle#requiresMetaData()
     */
    @Override
    public boolean requiresMetaData()
    {
        return getDTIKey() != null && (getAltitudeColumnPropertyName() != null || getLabelColumnPropertyName() != null);
    }

    /**
     * Determine altitude.
     *
     * @param visState the vis state
     * @param mlgs the mlgs
     * @param mdi the mdi
     * @return the double
     */
    protected double determineAltitude(VisualizationState visState, MapLocationGeometrySupport mlgs, MetaDataProvider mdi)
    {
        Double altFromProperty = isUseAltitude() ? getAltitudeColumnValueM(mdi) : null;
        return (altFromProperty == null ? mlgs.getLocation().getAltM() + visState.getAltitudeAdjust()
                : altFromProperty.doubleValue()) + getLift();
    }

    /**
     * Gets the label column value.
     *
     * @param elementId the element id
     * @param mdp the {@link MetaDataProvider}
     * @return the label value or null if not found.
     */
    public Object getLabelColumnValue(long elementId, MetaDataProvider mdp)
    {
        if (mdp == null)
        {
            return null;
        }

        Object lblCol = getLabelColumnPropertyName();

        // case of a simple String
        if (lblCol instanceof String)
        {
            return mdp.getValue((String)lblCol);
        }

        // case of a List of (hopefully) Strings
        if (lblCol instanceof List)
        {
            StringBuilder buf = new StringBuilder();
            for (Object obj : (List<?>)lblCol)
            {
                if (obj instanceof String)
                {
                    StyleUtils.appendLine(buf, StyleUtils.labelString((String)obj, mdp));
                }
            }
            return buf.toString();
        }

        return null;
    }

    /**
     * Alter z order if necessary.
     *
     * @param isSelected the is selected
     * @param dti the dti
     * @param brp the brp
     */
    private void alterZOrderIfNecessary(boolean isSelected, DataTypeInfo dti, BaseRenderProperties brp)
    {
        if (brp instanceof DefaultZOrderRenderProperties)
        {
            MapVisualizationInfo vi = dti == null ? null : dti.getMapVisualizationInfo();
            int maxZOrder = ZOrderRenderProperties.TOP_Z;
            if (dti != null && dti.getOrderKey() != null)
            {
                maxZOrder = getToolbox().getOrderManagerRegistry().getOrderManager(dti.getOrderKey()).getCategory()
                        .getOrderRange().getMaximumInteger();
            }
            int zOrder = isSelected ? maxZOrder : vi == null ? 0 : vi.getZOrder();
            if (zOrder != brp.getZOrder())
            {
                DefaultZOrderRenderProperties drp = (DefaultZOrderRenderProperties)brp;
                drp.setZOrder(zOrder);
                drp.setRenderingOrder(isSelected ? 1 : 0);
            }
        }
    }

    /**
     * Creates the geographic position from a {@link MapLocationGeometrySupport}
     * .
     *
     * @param mlgs the mlgs
     * @param mdp the mdp
     * @param visState the vis state
     * @return the point geometry. builder
     */
    private GeographicPosition createGeographicPosition(MapLocationGeometrySupport mlgs, MetaDataProvider mdp,
            VisualizationState visState)
    {
        StyleAltitudeReference altRef = getAltitudeReference();
        Altitude.ReferenceLevel refLevel = altRef.isAutomatic()
                ? mlgs.followTerrain() ? Altitude.ReferenceLevel.TERRAIN : mlgs.getLocation().getAltitudeReference()
                : altRef.getReference();
        double altM = determineAltitude(visState, mlgs, mdp);
        boolean altEquals = mlgs.getLocation().getAltitudeReference() == refLevel
                && MathUtil.isZero(mlgs.getLocation().getAltM() - altM);
        // Attempt to reuse the LatLonAlt to save memory
        LatLonAlt location = altEquals ? mlgs.getLocation()
                : LatLonAlt.createFromDegreesMeters(mlgs.getLocation().getLatD(), mlgs.getLocation().getLonD(), altM, refLevel);
        return new GeographicPosition(location);
    }

    /**
     * Get the default altitude unit property.
     *
     * @return The property.
     */
    private VisualizationStyleParameter getDefaultAltitudeUnitProperty()
    {
        Class<? extends Length> preferredUnits = getToolbox().getUnitsRegistry().getUnitsProvider(Length.class)
                .getPreferredFixedScaleUnits(Meters.ONE);
        String label = Length.getSelectionLabel(preferredUnits);
        return new VisualizationStyleParameter(ALTITUDE_UNIT_PROPERTY_KEY, "Altitude Unit", label, String.class,
                new VisualizationStyleParameterFlags(true, false), ParameterHint.hint(false, true));
    }
}
