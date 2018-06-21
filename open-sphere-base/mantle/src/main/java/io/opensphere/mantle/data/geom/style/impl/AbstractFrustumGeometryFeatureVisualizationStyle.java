package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.FrustumGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.DefaultMeshScalableRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.StyleAltitudeReference;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.util.MantleConstants;

/**
 * The Class PointFeatureVisualizationStyle.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractFrustumGeometryFeatureVisualizationStyle extends AbstractLocationFeatureVisualizationStyle
{
    /** The Constant DECIMAL_FORMAT. */
    private static final String DECIMAL_FORMAT = "%.0f";

    /** The Constant TO_PERCENT_MULTIPLIER. */
    private static final double TO_PERCENT_MULTIPLIER = 100.0;

    /** The Constant MAX_HEIGHT_POWER. */
    private static final float MAX_HEIGHT_POWER = 10.0f;

    /** The Constant MAX_WIDTH_SCALE. */
    private static final float MAX_WIDTH_SCALE = 5.0f;

    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "AbstractFrustumGeometryFeatureVisualizationStyle";

    /** The Constant ourWidthScalePropertyKey. */
    public static final String ourTopScalePropertyKey = ourPropertyKeyPrefix + ".TopScaleFeaturePropertyKey";

    /** The Constant ourWidthScalePropertyKey. */
    public static final String ourWidthScalePropertyKey = ourPropertyKeyPrefix + ".WidthScaleFeaturePropertyKey";

    /** The Constant ourHeightScalePropertyKey. */
    public static final String ourHeightScalePropertyKey = ourPropertyKeyPrefix + ".HeightScaleFeaturePropertyKey";

    /** The Constant ourHeightPowerPropertyKey. */
    public static final String ourHeightPowerPropertyKey = ourPropertyKeyPrefix + ".HeightPowerFeaturePropertyKey";

    /** The Constant ourAltitudeMetaDataColumnKey. */
    public static final String ourHeightByMetaDataColumnKeyPropertyKey = ourPropertyKeyPrefix + ".HeightByColumn";

    /** The Constant ourTransparentBasePropertyKey. */
    public static final String ourTransparentBasePropertyKey = ourPropertyKeyPrefix + ".TransparentBase";

    /** The Constant ourInvertPropertyKey. */
    public static final String ourInvertPropertyKey = ourPropertyKeyPrefix + ".Invert";

    /** The Constant ourDefaultWidthParameter. */
    public static final VisualizationStyleParameter ourDefaultTopScaleParameter = new VisualizationStyleParameter(
            ourTopScalePropertyKey, "Top Scale", Float.valueOf(0.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultWidthParameter. */
    public static final VisualizationStyleParameter ourDefaultWidthScaleParameter = new VisualizationStyleParameter(
            ourWidthScalePropertyKey, "Width Scale", Float.valueOf(1.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultScaleParameter. */
    public static final VisualizationStyleParameter ourDefaultHeightScaleParameter = new VisualizationStyleParameter(
            ourHeightScalePropertyKey, "Height Scale", Float.valueOf(0.5f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultScaleParameter. */
    public static final VisualizationStyleParameter ourDefaultHeightPowerParameter = new VisualizationStyleParameter(
            ourHeightPowerPropertyKey, "Height Power", Float.valueOf(0.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultWidthParameter. */
    public static final VisualizationStyleParameter ourDefaultHeightByColumnKeyParameter = new VisualizationStyleParameter(
            ourHeightByMetaDataColumnKeyPropertyKey, "Height By", null, String.class,
            new VisualizationStyleParameterFlags(true, true), ParameterHint.hint(false, true));

    /** The Constant ourDefaultWidthParameter. */
    public static final VisualizationStyleParameter ourDefaultTransparentBaseParameter = new VisualizationStyleParameter(
            ourTransparentBasePropertyKey, "Transparent Base", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultWidthParameter. */
    public static final VisualizationStyleParameter ourDefaultInvertParameter = new VisualizationStyleParameter(
            ourInvertPropertyKey, "Invert", Boolean.FALSE, Boolean.class, new VisualizationStyleParameterFlags(false, false),
            ParameterHint.hint(false, false));

    /**
     * Instantiates a new point feature visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public AbstractFrustumGeometryFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new point feature visualization style.
     *
     * @param tb the {@link Toolbox}
     * @param dtiKey the dti key
     */
    public AbstractFrustumGeometryFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public boolean allChangesRequireRebuild()
    {
        return true;
    }

    @Override
    public AbstractFrustumGeometryFeatureVisualizationStyle clone()
    {
        return (AbstractFrustumGeometryFeatureVisualizationStyle)super.clone();
    }

    @Override
    public void createCombinedGeometry(Set<Geometry> setToAddTo, FeatureCombinedGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createIndividualGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        FrustumGeometry geom = createFrustumGeometry(builderData, renderPropertyPool);
        if (geom != null)
        {
            createLabelGeometry(setToAddTo, builderData, null, geom.getConstraints(), renderPropertyPool);
        }
        setToAddTo.add(geom);
    }

    @Override
    public AbstractFrustumGeometryFeatureVisualizationStyle deriveForType(String dtiKey)
    {
        AbstractFrustumGeometryFeatureVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        return AppliesTo.INDIVIDUAL_ELEMENT;
    }

    /**
     * Gets the base radius.
     *
     * @return the base radius
     */
    public abstract float getBaseRadius();

    @Override
    public Class<? extends MapGeometrySupport> getConvertedClassType()
    {
        return MapLocationGeometrySupport.class;
    }

    /**
     * Gets the height by property name.
     *
     * @return the height by property name
     */
    public String getHeightByColumnPropertyName()
    {
        return (String)getStyleParameterValue(ourHeightByMetaDataColumnKeyPropertyKey);
    }

    /**
     * Gets the height power ( 0 to 10 ).
     *
     * Basically a multiplier on height that is 1^POWER
     *
     * @return the power
     */
    public float getHeightPower()
    {
        Float val = (Float)getStyleParameterValue(ourHeightPowerPropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    /**
     * Gets the scale ( 0 to 1 ).
     *
     * @return the scale
     */
    public float getHeightScale()
    {
        Float val = (Float)getStyleParameterValue(ourHeightScalePropertyKey);
        return val == null ? 1.0f : val.floatValue();
    }

    /**
     * Gets the max height.
     *
     * @return the max height
     */
    public abstract float getMaxHeight();

    @Override
    @NonNull
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = super.getMiniUIPanel();
        MutableVisualizationStyle style = panel.getChangedStyle();
        List<AbstractStyleParameterEditorPanel> paramList = New.list();

        VisualizationStyleParameter param = style.getStyleParameter(ourWidthScalePropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourWidthScalePropertyKey, true, false, 0.0f, MAX_WIDTH_SCALE,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null, DECIMAL_FORMAT)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * TO_PERCENT_MULTIPLIER;
                        return String.format(getStringFormat(), Double.valueOf(aVal)) + "%";
                    }
                }));

        param = style.getStyleParameter(ourTopScalePropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourTopScalePropertyKey, true, false, 0.0f, 1.0f,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null, DECIMAL_FORMAT)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * TO_PERCENT_MULTIPLIER;
                        return String.format(getStringFormat(), Double.valueOf(aVal)) + "%";
                    }
                }));

        param = style.getStyleParameter(ourHeightScalePropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourHeightScalePropertyKey, true, false, 0.0f, 1.0f,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null, DECIMAL_FORMAT)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * TO_PERCENT_MULTIPLIER;
                        return String.format(getStringFormat(), Double.valueOf(aVal)) + "%";
                    }
                }));

        param = style.getStyleParameter(ourHeightPowerPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourHeightPowerPropertyKey, true, false, 0.0f, MAX_HEIGHT_POWER,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = Math.exp(val);
                        return String.format(getStringFormat(), Double.valueOf(aVal));
                    }
                }));

        param = style.getStyleParameter(ourTransparentBasePropertyKey);
        paramList.add(new CheckBoxStyleParameterEditorPanel(StyleUtils.createBasicMiniPanelBuilder(param.getName()), style,
                ourTransparentBasePropertyKey, true));

        if (getDTIKey() != null)
        {
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
            if (dti != null && dti.getMetaDataInfo() != null && dti.getMetaDataInfo().getKeyCount() > 0)
            {
                paramList.add(new ComboBoxStyleParameterEditorPanel(StyleUtils.createComboBoxMiniPanelBuilder("Height-By"), style,
                        ourHeightByMetaDataColumnKeyPropertyKey, false, false, true, dti.getMetaDataInfo().getKeyNames()));
            }
        }

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
        panel.addGroupAtTop(paramGrp);

        return panel;
    }

    /**
     * Gets the number of points.
     *
     * @return the number of points
     */
    public abstract int getNumberOfPoints();

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.LOCATION_FEATURE;
    }

    /**
     * Gets the top scale.
     *
     * @return the top scale 0 to 1.
     */
    public float getTopScale()
    {
        Float val = (Float)getStyleParameterValue(ourTopScalePropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    @Override
    @NonNull
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourWidthScalePropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourWidthScalePropertyKey,
                true, false, 0.0f, MAX_WIDTH_SCALE,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null, DECIMAL_FORMAT)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * TO_PERCENT_MULTIPLIER;
                        return String.format(getStringFormat(), Double.valueOf(aVal)) + "%";
                    }
                }));

        param = style.getStyleParameter(ourTopScalePropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourTopScalePropertyKey,
                true, false, 0.0f, 1.0f, new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null, DECIMAL_FORMAT)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * TO_PERCENT_MULTIPLIER;
                        return String.format(getStringFormat(), Double.valueOf(aVal)) + "%";
                    }
                }));

        param = style.getStyleParameter(ourInvertPropertyKey);
        paramList
                .add(new CheckBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourInvertPropertyKey, true));

        param = style.getStyleParameter(ourHeightScalePropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                ourHeightScalePropertyKey, true, false, 0.0f, 1.0f,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null, DECIMAL_FORMAT)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * TO_PERCENT_MULTIPLIER;
                        return String.format(getStringFormat(), Double.valueOf(aVal)) + "%";
                    }
                }));

        param = style.getStyleParameter(ourHeightPowerPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                ourHeightPowerPropertyKey, true, false, 0.0f, MAX_HEIGHT_POWER,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = Math.exp(val);
                        return String.format(getStringFormat(), Double.valueOf(aVal));
                    }
                }));

        param = style.getStyleParameter(ourTransparentBasePropertyKey);
        paramList.add(new CheckBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                ourTransparentBasePropertyKey, true));

        if (getDTIKey() != null)
        {
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
            if (dti != null && dti.getMetaDataInfo() != null && dti.getMetaDataInfo().getKeyCount() > 0)
            {
                paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get("Height-By Column"), style,
                        ourHeightByMetaDataColumnKeyPropertyKey, false, false, true, dti.getMetaDataInfo().getKeyNames()));
            }
        }

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(getParameterPanelName(), paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    /**
     * Gets the width.
     *
     * @return the width 0 to 5.
     */
    public float getWidthScale()
    {
        Float val = (Float)getStyleParameterValue(ourWidthScalePropertyKey);
        return val == null ? 1.0f : val.floatValue();
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultTopScaleParameter);
        setParameter(ourDefaultInvertParameter);
        setParameter(ourDefaultWidthScaleParameter);
        setParameter(ourDefaultHeightScaleParameter);
        setParameter(ourDefaultHeightPowerParameter);
        setParameter(ourDefaultHeightByColumnKeyParameter);
        setParameter(ourDefaultTransparentBaseParameter);
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
     * Gets the inverted flag.
     *
     * @return the true for inverted, false for normal
     */
    public boolean isInverted()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourInvertPropertyKey);
        return val != null && val.booleanValue();
    }

    /**
     * Gets the transparent base flag.
     *
     * @return the true for transparent, false for opaque
     */
    public boolean isTransparentBase()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourTransparentBasePropertyKey);
        return val != null && val.booleanValue();
    }

    @Override
    public boolean requiresMetaData()
    {
        return super.requiresMetaData() || getDTIKey() != null && getHeightByColumnPropertyName() != null;
    }

    /**
     * Sets the height by property name.
     *
     * @param propName the property name
     * @param source the source doing the setting
     * @throws IllegalArgumentException if style has no data type.
     * @throws IllegalArgumentException if data type does not have the specified
     *             property key
     * @throws IllegalStateException if data type does not support metadata.
     */
    public void setHeightByColumnPropertyName(String propName, Object source)
    {
        StyleUtils.setMetaDataColumnKeyProperty(this, ourHeightByMetaDataColumnKeyPropertyKey, propName, source);
    }

    /**
     * Sets the height power.
     *
     * Basically a multiplier on height that is 1^POWER
     *
     * @param power the power ( 0.0 to 10.0 ) ( if out of range will be clipped
     *            to range bound )
     * @param source the source doing the setting
     */
    public void setHeightPower(float power, Object source)
    {
        float aPower = power < 0.0 ? 0.0f : power > MAX_HEIGHT_POWER ? MAX_HEIGHT_POWER : power;
        setParameter(ourHeightPowerPropertyKey, Float.valueOf(aPower), source);
    }

    /**
     * Sets the height scale.
     *
     * @param scale the scale ( 0.0 to 1.0 ) ( if out of range will be clipped
     *            to range bound )
     * @param source the source doing the setting
     */
    public void setHeightScale(float scale, Object source)
    {
        float aScale = scale < 0.0 ? 0.0f : scale > 1.0f ? 1.0f : scale;
        setParameter(ourHeightScalePropertyKey, Float.valueOf(aScale), source);
    }

    /**
     * Sets the invert flag.
     *
     * @param inverted the true for inverted, false for normal
     * @param source the source
     */
    public void setInverted(boolean inverted, Object source)
    {
        setParameter(ourInvertPropertyKey, inverted ? Boolean.TRUE : Boolean.FALSE, source);
    }

    /**
     * Sets the top scale.
     *
     * @param topScale the topScale ( 0.0 to 1.0 ) ( if out of range will be
     *            clipped to range bound )
     * @param source the source making the change.
     */
    public void setTopScale(float topScale, Object source)
    {
        float aTopScale = topScale < 0.0 ? 0.0f : topScale > 1.0f ? 1.0f : topScale;
        setParameter(ourTopScalePropertyKey, Float.valueOf(aTopScale), source);
    }

    /**
     * Sets the transparent base flag.
     *
     * @param transparentBase the true for transparent, false for opaque
     * @param source the source
     */
    public void setTransparentBase(boolean transparentBase, Object source)
    {
        setParameter(ourTransparentBasePropertyKey, transparentBase ? Boolean.TRUE : Boolean.FALSE, source);
    }

    /**
     * Sets the width.
     *
     * @param width the width ( 0.0 to 5.0 ) ( if out of range will be clipped
     *            to range bound )
     * @param source the source making the change.
     */
    public void setWidthScale(float width, Object source)
    {
        float aWidth = width < 0.0 ? 0.0f : width > MAX_WIDTH_SCALE ? MAX_WIDTH_SCALE : width;
        setParameter(ourWidthScalePropertyKey, Float.valueOf(aWidth), source);
    }

    /**
     * Gets the height by column value.
     *
     * @param elementId the element id
     * @param mdp the {@link MetaDataProvider}
     * @return the altitude column value
     */
    protected Double getHeightByColumnValue(long elementId, MetaDataProvider mdp)
    {
        Double result = Double.valueOf(0.0);
        if (mdp != null)
        {
            String heightByColKey = getHeightByColumnPropertyName();
            if (heightByColKey != null)
            {
                try
                {
                    result = Double.valueOf(StyleUtils.convertValueToDouble(mdp.getValue(heightByColKey)));
                }
                catch (NumberFormatException e)
                {
                    result = Double.valueOf(0.0);
                }
            }
        }
        return result;
    }

    /**
     * Gets the parameter panel name.
     *
     * @return the parameter panel name
     */
    protected abstract String getParameterPanelName();

    /**
     * Creates the frustum geometry.
     *
     * @param bd the builder data
     * @param renderPropertyPool the {@link RenderPropertyPool}
     * @return the {@link Geometry}
     */
    private FrustumGeometry createFrustumGeometry(FeatureIndividualGeometryBuilderData bd, RenderPropertyPool renderPropertyPool)
    {
        FrustumGeometry resultGeom = null;
        MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null : bd.getDataType().getBasicVisualizationInfo();

        if (bd.getMGS() instanceof MapLocationGeometrySupport)
        {
            MapLocationGeometrySupport mlgs = (MapLocationGeometrySupport)bd.getMGS();

            Color c = bd.getVS().isSelected() ? MantleConstants.SELECT_COLOR : bd.getVS().getColor();

            Color baseColor = c;
            if (isTransparentBase())
            {
                baseColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 30);
            }

            double alt = determineAltitude(bd.getVS(), mlgs, bd.getMDP());

            float baseR = determineBaseRadius();
            float topR = determineTopRadius();
            float height = determineHeight(bd.getElementId(), bd.getMDP());

            int zOrder = mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();
            boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
            ScalableMeshRenderProperties featureProps = new DefaultMeshScalableRenderProperties(zOrder, true, pickable);
            featureProps.setColor(c);
            featureProps.setLighting(LightingModelConfigGL.getDefaultLight());
            featureProps.setBaseAltitude((float)alt);
            featureProps.setBaseColor(baseColor);
            featureProps.setWidth(baseR);
            featureProps.setHeight(height);

            featureProps = renderPropertyPool.getPoolInstance(featureProps);

            StyleAltitudeReference altRef = getAltitudeReference();
            Altitude.ReferenceLevel refLevel = altRef.isAutomatic()
                    ? bd.getMGS().followTerrain() ? Altitude.ReferenceLevel.TERRAIN : mlgs.getLocation().getAltitudeReference()
                    : altRef.getReference();

            FrustumGeometry.Builder<GeographicPosition> builder = new FrustumGeometry.Builder<GeographicPosition>();
            builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegreesMeters(mlgs.getLocation().getLatD(),
                    mlgs.getLocation().getLonD(), alt, refLevel)));
            builder.setDataModelId(bd.getGeomId());
            builder.setCircularPoints(getNumberOfPoints());

            builder.setBaseRadius(baseR);
            builder.setTopRadius(topR);

            resultGeom = new FrustumGeometry(builder, featureProps, StyleUtils.createTimeConstraintsIfApplicable(basicVisInfo,
                    mapVisInfo, bd.getMGS(), StyleUtils.getDataGroupInfoFromDti(getToolbox(), bd.getDataType())));
        }
        return resultGeom;
    }

    /**
     * Determine the radius of the frustum at the base.
     *
     * @return the base radius.
     */
    private float determineBaseRadius()
    {
        if (isInverted())
        {
            return getBaseRadius() * getTopScale() * getWidthScale();
        }
        else
        {
            return getBaseRadius() * getWidthScale();
        }
    }

    /**
     * Determine the height of the frustum.
     *
     * @param elementId the element id
     * @param mdp the mdp
     * @return the height.
     */
    private float determineHeight(long elementId, MetaDataProvider mdp)
    {
        float height = getMaxHeight();
        if (getHeightByColumnPropertyName() != null)
        {
            Double dHeight = getHeightByColumnValue(elementId, mdp);
            if (dHeight != null)
            {
                height = dHeight.floatValue() * 100;
            }
        }
        return height * getHeightScale() * (float)Math.exp(getHeightPower());
    }

    /**
     * Determine the radius of the frustum at the top.
     *
     * @return the top radius.
     */
    private float determineTopRadius()
    {
        if (isInverted())
        {
            return getBaseRadius() * getWidthScale();
        }
        else
        {
            return getBaseRadius() * getTopScale() * getWidthScale();
        }
    }

//    @Override
//    public BaseRenderProperties getAlteredRenderProperty(
//            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, DataTypeInfo dti, VisualizationState vs,
//            VisualizationState defaultVS, MetaDataProvider mdp, BaseRenderProperties orig)
//    {
//        BaseRenderProperties alteredRP = super.getAlteredRenderProperty(changedParameterKeyToParameterMap, dti, vs, defaultVS,
//                mdp, orig);
//        VisualizationStyleParameter poitSizeParameter = changedParameterKeyToParameterMap.get(ourPointSizePropertyKey);
//        if (poitSizeParameter != null)
//        {
//            if (alteredRP instanceof PointRenderProperties)
//            {
//                PointRenderProperties dprp = (PointRenderProperties)alteredRP;
//                if (dprp.getSize() != getPointSize())
//                {
//                    dprp.setSize(getPointSize());
//                }
//            }
//            else if (alteredRP instanceof PointSizeRenderProperty)
//            {
//                PointSizeRenderProperty dprp = (PointSizeRenderProperty)alteredRP;
//                if (dprp.getSize() != getPointSize())
//                {
//                    dprp.setSize(getPointSize());
//                }
//            }
//        }
//        return alteredRP;
//    }
}
