package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.PointSpriteGeometry;
import io.opensphere.core.geometry.PointSpriteGeometry.Builder;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.image.processor.ImageProcessor;
import io.opensphere.core.image.processor.RotateImageProcessor;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
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
import io.opensphere.mantle.data.geom.style.PointRenderPropertyFactory;
import io.opensphere.mantle.data.geom.style.StyleAltitudeReference;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.EditorPanelVisibilityDependency;
import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.IconChooserStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.ParameterVisibilityConstraint;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.data.impl.specialkey.HeadingKey;
import io.opensphere.mantle.icon.IconImageProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.util.MantleConstants;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class IconFeatureVisualizationStyle.
 */
@SuppressWarnings("PMD.GodClass")
public class IconFeatureVisualizationStyle extends AbstractLocationFeatureVisualizationStyle
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(IconFeatureVisualizationStyle.class);

    /** The Constant MAX_ICON_SIZE_FACTOR. */
    private static final float MAX_ICON_SIZE_FACTOR = 100.0f;

    /** The Constant DEFAULT_ICON_URL. */
    public static final URL DEFAULT_ICON_URL = IconFeatureVisualizationStyle.class.getClassLoader()
            .getResource("images/Location_Pin_32.png");

    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "IconFeatureVisualizationStyle";

    /** The Constant ourPointSizePropertyKey. */
    public static final String ourIconSizePropertyKey = ourPropertyKeyPrefix + ".Size";

    /** The Constant ourMixIconColorWithElementColrPropertyKey. */
    public static final String ourMixIconColorWithElementColrPropertyKey = ourPropertyKeyPrefix + ".MixElementColor";

    /** The Constant ourDefaultIconURLPropertyKey. */
    public static final String ourDefaultIconURLPropertyKey = ourPropertyKeyPrefix + ".DefaultIconURL";

    /** The Constant gourIconPointerLocationOffsetXPropertyKey. */
    public static final String ourIconPointerLocationOffsetXPropertyKey = ourPropertyKeyPrefix + ".PointerLocationOffsetX";

    /** The Constant gourIconPointerLocationOffsetYPropertyKey. */
    public static final String ourIconPointerLocationOffsetYPropertyKey = ourPropertyKeyPrefix + ".PointerLocationOffsetY";

    /** The Constant ourDefaultPointSizePropertyKey. */
    public static final String ourDefaultPointSizePropertyKey = ourPropertyKeyPrefix + ".DefaultPointSize";

    /** The Constant ourDefaulToPropertyKey. */
    public static final String ourDefaultToPropertyKey = ourPropertyKeyPrefix + ".DefaultTo";

    /** Enable icon rotation property key. */
    public static final String ourEnableRotationPropertyKey = ourPropertyKeyPrefix + ".EnableRotation";

    /** Heading column property key. */
    public static final String ourHeadingColumnPropertyKey = ourPropertyKeyPrefix + ".HeadingColumn";

    /** The Constant ourDefaultToParameter. */
    public static final VisualizationStyleParameter ourDefaultToParameter = new VisualizationStyleParameter(
            ourDefaultToPropertyKey, "Default To", DefaultTo.ICON, DefaultTo.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultPointSizeParameter. */
    public static final VisualizationStyleParameter ourDefaultIconSizeParameter = new VisualizationStyleParameter(
            ourIconSizePropertyKey, "Icon Scale", Float.valueOf(10f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultPointSizeParameter. */
    public static final VisualizationStyleParameter ourDefaultDefaultIconURLParameter = new VisualizationStyleParameter(
            ourDefaultIconURLPropertyKey, "Default Icon URL", DEFAULT_ICON_URL.toExternalForm(), String.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultPointLocationOffsetXParameter. */
    public static final VisualizationStyleParameter ourDefaultPointerOffsetXParameter = new VisualizationStyleParameter(
            ourIconPointerLocationOffsetXPropertyKey, "Pointer Offset X", Integer.valueOf(15), Integer.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultPointerOffsetYParameter. */
    public static final VisualizationStyleParameter ourDefaultPointerOffsetYParameter = new VisualizationStyleParameter(
            ourIconPointerLocationOffsetYPropertyKey, "Pointer Offset Y", Integer.valueOf(29), Integer.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultMixIconAndElementColorParameter. */
    public static final VisualizationStyleParameter ourDefaultMixIconAndElementColorParameter = new VisualizationStyleParameter(
            ourMixIconColorWithElementColrPropertyKey, "Mix Icon/Element Color", Boolean.TRUE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultPointSizeParameter. */
    public static final VisualizationStyleParameter ourDefaultPointSizeParameter = new VisualizationStyleParameter(
            ourDefaultPointSizePropertyKey, "Default Point Size", Float.valueOf(4.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The enable icon rotation parameter. */
    public static final VisualizationStyleParameter ourDefaultEnableRotationParameter = new VisualizationStyleParameter(
            ourEnableRotationPropertyKey, "Enable Rotation Based on Heading", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The default heading column parameter. */
    public static final VisualizationStyleParameter ourDefaultHeadingColumnParameter = new VisualizationStyleParameter(
            ourHeadingColumnPropertyKey, "Heading Column", null, String.class, new VisualizationStyleParameterFlags(true, true),
            ParameterHint.hint(false, true));

    /** The Temp icon record. */
    private transient IconRecord myTempIconRecord;

    /** The Icon point render property factory. */
    private final transient IconPointRenderPropertyFactory myIconPointRenderPropertyFactory = new IconPointRenderPropertyFactory();

    /**
     * Instantiates a new icon feature visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public IconFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new icon feature visualization style.
     *
     * @param tb the {@link Toolbox}
     * @param dtiKey the dti key
     */
    public IconFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public IconFeatureVisualizationStyle clone()
    {
        return (IconFeatureVisualizationStyle)super.clone();
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
        if (bd.getMGS() instanceof MapLocationGeometrySupport)
        {
            MapLocationGeometrySupport mlgs = (MapLocationGeometrySupport)bd.getMGS();
            int iconId = getIconId(bd.getElementId());

            StyleAltitudeReference altRef = getAltitudeReference();
            Altitude.ReferenceLevel refLevel = altRef.isAutomatic()
                    ? mlgs.followTerrain() ? Altitude.ReferenceLevel.TERRAIN : mlgs.getLocation().getAltitudeReference()
                    : altRef.getReference();

            GeographicPosition gp = new GeographicPosition(LatLonAlt.createFromDegreesMeters(mlgs.getLocation().getLatD(),
                    mlgs.getLocation().getLonD(), determineAltitude(bd.getVS(), mlgs, bd.getMDP()), refLevel));
            AbstractRenderableGeometry geom = null;
            if (iconId == -1 && getDefaultTo() == DefaultTo.POINT)
            {
                geom = createPointGeometry(bd, renderPropertyPool, getDefaultPointSize(), null, setToAddTo);
            }
            else
            {
                PointSpriteGeometry.Builder<GeographicPosition> builder = new Builder<>();

                MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
                BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null
                        : bd.getDataType().getBasicVisualizationInfo();
                builder.setDataModelId(bd.getGeomId());
                builder.setPosition(gp);

                float size = 4 + getIconSize();

                PointRenderProperties props = determinePointProperties(mapVisInfo, basicVisInfo, bd, renderPropertyPool, size);
                loadDefaultIconRecord();

                IconImageProvider ip = determineIconProvider(iconId, bd, null);

                boolean isProjectionSensitive = false;
                if (isRotationEnabled() && bd.getMDP() != null)
                {
                    Float heading = getHeadingValue(bd.getMDP());
                    if (heading != null)
                    {
                        ip = determineIconProvider(iconId, bd,
                                new RotateImageProcessor(heading.doubleValue(), true, getToolbox().getMapManager()));
                        isProjectionSensitive = true;
                    }
                }

                builder.setProjectionSensitive(isProjectionSensitive);
                builder.setImageManager(new ImageManager(null, ip));

                Constraints constraints = StyleUtils.createTimeConstraintsIfApplicable(basicVisInfo, mapVisInfo, bd.getMGS(),
                        StyleUtils.getDataGroupInfoFromDti(getToolbox(), bd.getDataType()));

                geom = new PointSpriteGeometry(builder, props, constraints);
                setToAddTo.add(geom);
            }

            if (geom != null)
            {
                createLabelGeometry(setToAddTo, bd, null, geom.getConstraints(), renderPropertyPool);
            }
        }
        else
        {
            throw new IllegalArgumentException(
                    "Cannot create geometries from type " + (bd.getMGS() == null ? "NULL" : bd.getMGS().getClass().getName()));
        }
    }

    @Override
    public IconFeatureVisualizationStyle deriveForType(String dtiKey)
    {
        IconFeatureVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public BaseRenderProperties getAlteredRenderProperty(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, DataTypeInfo dti, VisualizationState vs,
            VisualizationState defaultVS, MetaDataProvider mdp, BaseRenderProperties orig)
    {
        BaseRenderProperties alteredRP = orig;
        VisualizationStyleParameter colorParameter = changedParameterKeyToParameterMap.get(COLOR_PROPERTY_KEY);
        if (colorParameter != null)
        {
            if (orig instanceof PointRenderProperties)
            {
                PointRenderProperties dprp = (PointRenderProperties)orig;
                Color c = null;
                if (isMixIconAndElementColor() || !(orig instanceof DefaultIconPointRenderProperties))
                {
                    c = StyleUtils.determineColor(dprp.getColor(), vs == null ? defaultVS : vs);
                }
                else
                {
                    c = vs == null ? Color.white : vs.isSelected() ? MantleConstants.SELECT_COLOR : Color.white;
                }

                if (!Utilities.sameInstance(c, dprp.getColor()))
                {
                    dprp.setColor(c);
                }
            }
            else if (orig instanceof LabelRenderProperties)
            {
                LabelRenderProperties dcrp = (LabelRenderProperties)orig;
                Color c = getLabelColor();
                if (!Utilities.sameInstance(c, dcrp.getColor()))
                {
                    dcrp.setColor(c);
                }
            }
            else if (orig instanceof ColorRenderProperties)
            {
                ColorRenderProperties dcrp = (ColorRenderProperties)orig;
                Color c = null;
                if (isMixIconAndElementColor() || !(orig instanceof DefaultIconPointRenderProperties))
                {
                    c = StyleUtils.determineColor(dcrp.getColor(), vs == null ? defaultVS : vs);
                }
                else
                {
                    c = vs == null ? Color.white : vs.isSelected() ? MantleConstants.SELECT_COLOR : Color.white;
                }
                if (!Utilities.sameInstance(c, dcrp.getColor()))
                {
                    dcrp.setColor(c);
                }
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

    /**
     * Gets the default point size.
     *
     * @return the point size
     */
    public float getDefaultPointSize()
    {
        Float val = (Float)getStyleParameterValue(ourDefaultPointSizePropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    /**
     * Gets the default to.
     *
     * @return the default to
     */
    public DefaultTo getDefaultTo()
    {
        return (DefaultTo)getStyleParameterValue(ourDefaultToPropertyKey);
    }

    /**
     * Gets the size (1 to 100.0).
     *
     * @return the icon size
     */
    public float getIconSize()
    {
        Float val = (Float)getStyleParameterValue(ourIconSizePropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    /**
     * Gets the icon url.
     *
     * @return the icon url
     */
    public String getIconURL()
    {
        return (String)getStyleParameterValue(ourDefaultIconURLPropertyKey);
    }

    /**
     * Gets whether rotation is enabled.
     *
     * @return whether rotation is enabled
     */
    public boolean isRotationEnabled()
    {
        return Utilities.getValue((Boolean)getStyleParameterValue(ourEnableRotationPropertyKey), Boolean.FALSE).booleanValue();
    }

    /**
     * Gets the heading column.
     *
     * @return the heading column
     */
    public String getHeadingColumn()
    {
        return (String)getStyleParameterValue(ourHeadingColumnPropertyKey);
    }

    @Override
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = super.getMiniUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourDefaultToPropertyKey);
        paramList.add(new ComboBoxStyleParameterEditorPanel(StyleUtils.createComboBoxMiniPanelBuilder(param.getName()), style,
                ourDefaultToPropertyKey, false, false, false, Arrays.asList(DefaultTo.values())));

        param = style.getStyleParameter(ourDefaultPointSizePropertyKey);
        FloatSliderStyleParameterEditorPanel pointSizePanel = new FloatSliderStyleParameterEditorPanel(
                StyleUtils.createSliderMiniPanelBuilder(param.getName()), style, ourDefaultPointSizePropertyKey, true, false,
                1.0f, MAX_POINT_SIZE, new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null));
        paramList.add(pointSizePanel);

        EditorPanelVisibilityDependency vd = new EditorPanelVisibilityDependency(panel, pointSizePanel);
        vd.addConstraint(new ParameterVisibilityConstraint(ourDefaultToPropertyKey, true, DefaultTo.POINT));
        vd.evaluateStyle();
        panel.addVisibilityDependency(vd);

        PanelBuilder pb = PanelBuilder.get("Default Icon", 20, 0, 0, 5);
        pb.setOtherParameter(AbstractStyleParameterEditorPanel.PANEL_HEIGHT, Integer.valueOf(36));
        IconChooserStyleParameterEditorPanel defIconChooserPanel = new IconChooserStyleParameterEditorPanel(pb, style,
                ourDefaultIconURLPropertyKey, false);

        vd = new EditorPanelVisibilityDependency(panel, defIconChooserPanel);
        vd.addConstraint(new ParameterVisibilityConstraint(ourDefaultToPropertyKey, true, DefaultTo.ICON));
        vd.evaluateStyle();
        panel.addVisibilityDependency(vd);
        paramList.add(defIconChooserPanel);

        param = style.getStyleParameter(ourIconSizePropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourIconSizePropertyKey, true, false, 0.0f, MAX_ICON_SIZE_FACTOR,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null)));

        paramList.add(new CheckBoxStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder("Enable Icon Rotation"),
                style, ourEnableRotationPropertyKey, false));

        if (getDTIKey() != null)
        {
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
            if (dti != null && dti.getMetaDataInfo() != null && dti.getMetaDataInfo().getKeyCount() > 0)
            {
                if (getHeadingColumn() == null)
                {
                    String headingKey = dti.getMetaDataInfo().getKeyForSpecialType(HeadingKey.DEFAULT);
                    if (headingKey != null)
                    {
                        setHeadingColumn(headingKey, this);
                    }
                    else
                    {
                        setHeadingColumn(dti.getMetaDataInfo().getKeyNames().get(0), this);
                    }
                }
                paramList.add(new ComboBoxStyleParameterEditorPanel(StyleUtils.createComboBoxMiniPanelBuilder("Heading Column"),
                        style, ourHeadingColumnPropertyKey, false, false, false, dti.getMetaDataInfo().getKeyNames()));
            }
        }

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
        panel.addGroupAtTop(paramGrp);

        return panel;
    }

    /**
     * Gets the pointer offset x.
     *
     * @return the pointer offset x
     */
    public int getPointerOffsetX()
    {
        Integer val = (Integer)getStyleParameterValue(ourIconPointerLocationOffsetXPropertyKey);
        return val == null ? 0 : val.intValue();
    }

    /**
     * Gets the pointer offset y.
     *
     * @return the pointer offset y
     */
    public int getPointerOffsetY()
    {
        Integer val = (Integer)getStyleParameterValue(ourIconPointerLocationOffsetYPropertyKey);
        return val == null ? 0 : val.intValue();
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.LOCATION_FEATURE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Feature visualization controls for icons.";
    }

    @Override
    public String getStyleName()
    {
        return "Icons";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourDefaultToPropertyKey);
        PanelBuilder pb = PanelBuilder.get(param.getName());
        pb.setOtherParameter(AbstractStyleParameterEditorPanel.PANEL_HEIGHT, Integer.valueOf(24));
        paramList.add(new ComboBoxStyleParameterEditorPanel(StyleUtils.createBasicMiniPanelBuilder(param.getName()), style,
                ourDefaultToPropertyKey, false, false, false, Arrays.asList(DefaultTo.values())));

        param = style.getStyleParameter(ourDefaultPointSizePropertyKey);
        FloatSliderStyleParameterEditorPanel pointSizePanel = new FloatSliderStyleParameterEditorPanel(
                PanelBuilder.get(param.getName()), style, ourDefaultPointSizePropertyKey, true, false, 1.0f, MAX_POINT_SIZE,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null));
        paramList.add(pointSizePanel);

        EditorPanelVisibilityDependency visDepend = new EditorPanelVisibilityDependency(panel, pointSizePanel);
        visDepend.addConstraint(new ParameterVisibilityConstraint(ourDefaultToPropertyKey, true, DefaultTo.POINT));
        visDepend.evaluateStyle();
        panel.addVisibilityDependency(visDepend);

        IconChooserStyleParameterEditorPanel defIconChooserPanel = new IconChooserStyleParameterEditorPanel(
                PanelBuilder.get("Default Icon"), style, ourDefaultIconURLPropertyKey, false);

        visDepend = new EditorPanelVisibilityDependency(panel, defIconChooserPanel);
        visDepend.addConstraint(new ParameterVisibilityConstraint(ourDefaultToPropertyKey, true, DefaultTo.ICON));
        visDepend.evaluateStyle();
        panel.addVisibilityDependency(visDepend);
        paramList.add(defIconChooserPanel);

        param = style.getStyleParameter(ourMixIconColorWithElementColrPropertyKey);
        paramList.add(new CheckBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                ourMixIconColorWithElementColrPropertyKey, false));

        param = style.getStyleParameter(ourIconSizePropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourIconSizePropertyKey,
                true, false, 0.0f, MAX_ICON_SIZE_FACTOR,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(0, null)));

        param = style.getStyleParameter(ourEnableRotationPropertyKey);
        paramList.add(new CheckBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                ourEnableRotationPropertyKey, false));

        if (getDTIKey() != null)
        {
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
            if (dti != null && dti.getMetaDataInfo() != null && dti.getMetaDataInfo().getKeyCount() > 0)
            {
                if (getHeadingColumn() == null)
                {
                    String headingKey = dti.getMetaDataInfo().getKeyForSpecialType(HeadingKey.DEFAULT);
                    if (headingKey != null)
                    {
                        setHeadingColumn(headingKey, this);
                    }
                    else
                    {
                        setHeadingColumn(dti.getMetaDataInfo().getKeyNames().get(0), this);
                    }
                }
                paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get("Heading Column"), style,
                        ourHeadingColumnPropertyKey, false, false, false, dti.getMetaDataInfo().getKeyNames()));
            }
        }

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Icon Style", paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultToParameter);
        setParameter(ourDefaultIconSizeParameter);
        setParameter(ourDefaultPointSizeParameter);
        setParameter(ourDefaultDefaultIconURLParameter);
        setParameter(ourDefaultPointerOffsetXParameter);
        setParameter(ourDefaultPointerOffsetYParameter);
        setParameter(ourDefaultMixIconAndElementColorParameter);
        setParameter(ourDefaultEnableRotationParameter);
        setParameter(ourDefaultHeadingColumnParameter);
    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        super.initialize(paramSet);
        paramSet.stream().filter(p -> p.getKey() != null && p.getKey().startsWith(ourPropertyKeyPrefix))
                .forEach(this::setParameter);
    }

    /**
     * Checks if is mix icon and element color.
     *
     * @return true, if is mix icon and element color
     */
    public boolean isMixIconAndElementColor()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourMixIconColorWithElementColrPropertyKey);
        return val != null && val.booleanValue();
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new IconFeatureVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public boolean requiresMetaData()
    {
        return super.requiresMetaData() || isRotationEnabled();
    }

    /**
     * Sets the default point size.
     *
     * @param size the size
     * @param source the source
     */
    public void setDefaultPointSize(float size, Object source)
    {
        setParameter(ourDefaultPointSizePropertyKey, Float.valueOf(size), source);
    }

    /**
     * Sets the default to.
     *
     * @param defaultTo the default to
     * @param source the source
     */
    public void setDefaultTo(DefaultTo defaultTo, Object source)
    {
        Utilities.checkNull(defaultTo, "defaultTo");
        setParameter(ourDefaultToPropertyKey, defaultTo, source);
    }

    /**
     * Sets the icon size. ( 1 to 100 )
     *
     * @param size the icon size ( 1 to 100.0 ) ( Will be clipped to bounds )
     * @param source the source
     */
    public void setIconSize(float size, Object source)
    {
        setParameter(ourIconSizePropertyKey,
                Float.valueOf(size < 1.0f ? 1.0f : size > MAX_ICON_SIZE_FACTOR ? MAX_ICON_SIZE_FACTOR : size), source);
    }

    /**
     * Sets whether icon rotation is enabled.
     *
     * @param enabled whether icon rotation is enabled
     * @param source the source
     */
    public void setRotationEnabled(boolean enabled, Object source)
    {
        setParameter(ourEnableRotationPropertyKey, Boolean.valueOf(enabled), source);
    }

    /**
     * Sets the heading column.
     *
     * @param headingColumn the heading column
     * @param source the source
     */
    public void setHeadingColumn(String headingColumn, Object source)
    {
        setParameter(ourHeadingColumnPropertyKey, headingColumn, source);
    }

    /**
     * Sets the icon url.
     *
     * @param url the url
     * @param source the source
     */
    public void setIconURL(String url, Object source)
    {
        if (setParameter(ourDefaultIconURLPropertyKey, url, source))
        {
            myTempIconRecord = null;
        }
    }

    /**
     * Sets the mix icon and element color.
     *
     * @param mix the mix
     * @param source the source
     */
    public void setMixIconAndElementColor(boolean mix, Object source)
    {
        setParameter(ourMixIconColorWithElementColrPropertyKey, Boolean.valueOf(mix), source);
    }

    /**
     * Sets the pointer offset x.
     *
     * @param xOffset the x offset
     * @param source the source
     */
    public void setPointerOffsetX(int xOffset, Object source)
    {
        setParameter(ourIconPointerLocationOffsetXPropertyKey, Integer.valueOf(xOffset), source);
    }

    /**
     * Sets the pointer offset y.
     *
     * @param yOffset the y offset
     * @param source the source
     */
    public void setPointerOffsetY(int yOffset, Object source)
    {
        setParameter(ourIconPointerLocationOffsetYPropertyKey, Integer.valueOf(yOffset), source);
    }

    @Override
    public boolean supportsLabels()
    {
        return true;
    }

    /**
     * Gets the icon ID for the given element ID.
     *
     * @param elementId the element ID
     * @return the icon ID
     */
    protected int getIconId(long elementId)
    {
        IconRegistry reg = MantleToolboxUtils.getMantleToolbox(getToolbox()).getIconRegistry();
        int iconId = reg.getIconIdForElement(elementId);
        return iconId;
    }

    /**
     * Determine icon provider.
     *
     * @param iconId the icon id
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @param imageProcessor the optional image processor
     * @return the icon image provider
     */
    private IconImageProvider determineIconProvider(int iconId, FeatureIndividualGeometryBuilderData bd,
            ImageProcessor imageProcessor)
    {
        IconImageProvider ip = null;
        IconRegistry reg = MantleToolboxUtils.getMantleToolbox(getToolbox()).getIconRegistry();
        if (iconId != -1)
        {
            IconRecord rec = reg.getIconRecordByIconId(iconId);
            if (rec == null)
            {
                rec = reg.getIconRecord(IconRegistry.DEFAULT_ICON_URL);
            }
            ip = reg.getLoadedIconPool().getIconImageProvider(rec, imageProcessor);
        }
        if (ip == null)
        {
            ip = reg.getLoadedIconPool().getIconImageProvider(myTempIconRecord, imageProcessor);
        }
        if (ip != null && !ip.canProvideImageImmediately())
        {
            ip.loadImage();
        }
        return ip;
    }

    /**
     * Determine point properties.
     *
     * @param mapVisInfo Data type level info relevant for rendering.
     * @param basicVisInfo Basic information for the data type.
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @param renderPropertyPool the {@link RenderPropertyPool}
     * @param size the size
     * @return the point render properties
     */
    private PointRenderProperties determinePointProperties(MapVisualizationInfo mapVisInfo, BasicVisualizationInfo basicVisInfo,
            FeatureIndividualGeometryBuilderData bd, RenderPropertyPool renderPropertyPool, float size)
    {
        Color color;
        PointRenderProperties props;
        if (bd.getVS().isSelected())
        {
            color = MantleConstants.SELECT_COLOR;
        }
        else
        {
            color = bd.getVS().isDefaultColor() ? getColor() : bd.getVS().getColor();
            if (!isMixIconAndElementColor())
            {
                color = new Color(0xffffff | color.getAlpha() << 24, true);
            }
        }
        props = new PointRenderPropertiesHelper(renderPropertyPool).getPointSizeRenderPropertiesIfAvailable(mapVisInfo,
                basicVisInfo, size, bd, color, myIconPointRenderPropertyFactory);

        return props;
    }

    /**
     * Load icon record.
     */
    private void loadDefaultIconRecord()
    {
        if (myTempIconRecord == null || !Objects.equals(getIconURL(), myTempIconRecord.getImageURL().toString()))
        {
            IconRegistry reg = MantleToolboxUtils.getMantleToolbox(getToolbox()).getIconRegistry();
            URL iconURL = null;
            try
            {
                iconURL = new URL(getIconURL());
                IconRecord rec = reg.getIconRecord(iconURL);
                if (rec != null)
                {
                    myTempIconRecord = rec;
                }
                else
                {
                    rec = reg.getIconRecord(DEFAULT_ICON_URL);
                    myTempIconRecord = rec;
                }
            }
            catch (MalformedURLException e)
            {
                LOGGER.error("Error creating URL from parameter value: " + getIconURL());
                myTempIconRecord = reg.getIconRecord(DEFAULT_ICON_URL);
            }
        }
    }

    /**
     * Gets the heading value.
     *
     * @param metaDataProvider the meta data provider
     * @return the heading value
     */
    private Float getHeadingValue(MetaDataProvider metaDataProvider)
    {
        Float heading = null;

        String headingColumn = getHeadingColumn();
        if (headingColumn != null)
        {
            Object value = metaDataProvider.getValue(headingColumn);
            if (value instanceof Number)
            {
                heading = Float.valueOf(((Number)value).floatValue());
            }
            else if (value instanceof String)
            {
                try
                {
                    heading = Float.valueOf((String)value);
                }
                catch (NumberFormatException e)
                {
                    heading = Float.valueOf(0);
                }
            }

            if (heading != null)
            {
                heading = Float.valueOf(Math.round(heading.floatValue()));
            }
        }

        return heading;
    }

    /**
     * The Enum DefaultTo.
     */
    public enum DefaultTo
    {
        /** The POINT. */
        POINT("Point"),

        /** The ICON. */
        ICON("Icon");

        /** The Label. */
        private String myLabel;

        /**
         * Instantiates a new DefaultTo.
         *
         * @param label the label
         */
        DefaultTo(String label)
        {
            myLabel = label;
        }

        @Override
        public String toString()
        {
            return myLabel;
        }
    }

    /**
     * The Class DefaultIconPointRenderProperties. Used as a marker class to
     * differentiate render properties for icons vs default points in this
     * style.
     */
    @SuppressWarnings("serial")
    private static class DefaultIconPointRenderProperties extends DefaultPointRenderProperties
    {
        /**
         * Instantiates a new default icon point render properties.
         *
         * @param baseRenderProperties the base render properties
         * @param sizeProperty the size property
         */
        public DefaultIconPointRenderProperties(BaseAltitudeRenderProperties baseRenderProperties,
                PointSizeRenderProperty sizeProperty)
        {
            super(baseRenderProperties, sizeProperty);
        }
    }

    /**
     * A factory for creating IconPointRenderProperty objects.
     */
    private static class IconPointRenderPropertyFactory implements PointRenderPropertyFactory
    {
        @Override
        public PointRenderProperties createPointRenderProperties(BaseAltitudeRenderProperties baseRp,
                PointSizeRenderProperty pointSizeRP)
        {
            return new DefaultIconPointRenderProperties(baseRp, pointSizeRP);
        }
    }
}
