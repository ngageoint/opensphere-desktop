package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.EllipseGeometry;
import io.opensphere.core.geometry.EllipseScalableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultMeshScalableRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
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
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.data.impl.specialkey.EllipseOrientationKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMajorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMinorAxisKey;

/**
 * The Class DynamicEllipseFeatureVisualization.
 */
@SuppressWarnings("PMD.GodClass")
public class DynamicEllipseFeatureVisualization extends AbstractEllipseFeatureVisualizationStyle
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DynamicEllipseFeatureVisualization.class);

    /**
     * The smoothness of the ellipsoid we draw.
     */
    private static final int ELLISPOID_QUALITY = 40;

    /** The Constant ourPropertyKeyPrefix. */
    public static final String PROPERTY_KEY_PREFIX = "DynamicEllipseFeatureVisualization";

    /** The Constant ourSemiMajorAxisColumnKey. */
    public static final String ourSemiMajorAxisColumnKey = PROPERTY_KEY_PREFIX + ".SemiMajorAxisColumnKey";

    /** The Constant ourSemiMinorAxisColumnKey. */
    public static final String ourSemiMinorAxisColumnKey = PROPERTY_KEY_PREFIX + ".SemiMinorAxisColumnKey";

    /** The Constant ourSemiMinorAxisUnitKey. */
    public static final String ourSemiMinorAxisUnitKey = PROPERTY_KEY_PREFIX + ".SemiMinorAxisUnitKey";

    /** The Constant ourOrientationColumnKey. */
    public static final String ourOrientationColumnKey = PROPERTY_KEY_PREFIX + ".OrientationColumnKey";

    /** The Constant ourDefaultSemiMajorAxisParameter. */
    public static final VisualizationStyleParameter ourDefaultSemiMajorAxisParameter = new VisualizationStyleParameter(
            ourSemiMajorAxisColumnKey, "Semi-Major Axis Column", null, String.class,
            new VisualizationStyleParameterFlags(true, true), ParameterHint.hint(false, true));

    /** The Constant ourDefaultSemiMinorAxisParameter. */
    public static final VisualizationStyleParameter ourDefaultSemiMinorAxisParameter = new VisualizationStyleParameter(
            ourSemiMinorAxisColumnKey, "Semi-Minor Axis Column", null, String.class,
            new VisualizationStyleParameterFlags(true, true), ParameterHint.hint(false, true));

    /** The Constant ourDefaultOrientationParameter. */
    public static final VisualizationStyleParameter ourDefaultOrientationParameter = new VisualizationStyleParameter(
            ourOrientationColumnKey, "Orientation Column", null, String.class, new VisualizationStyleParameterFlags(true, true),
            ParameterHint.hint(false, true));

    /**
     * Builds an ellipsoid geometry when the ellipsoid style is selected.
     */
    private final EllipsoidBuilder myEllipsoidBuilder;

    /**
     * Instantiates a new dynamic ellipse feature visualization.
     *
     * @param tb the {@link Toolbox}
     */
    public DynamicEllipseFeatureVisualization(Toolbox tb)
    {
        super(tb);
        myEllipsoidBuilder = new EllipsoidBuilder(tb.getMapManager());
    }

    /**
     * Instantiates a new dynamic ellipse feature visualization.
     *
     * @param tb the {@link Toolbox}
     * @param dtiKey the dti key
     */
    public DynamicEllipseFeatureVisualization(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
        myEllipsoidBuilder = new EllipsoidBuilder(tb.getMapManager());
    }

    @Override
    public DynamicEllipseFeatureVisualization clone()
    {
        return (DynamicEllipseFeatureVisualization)super.clone();
    }

    @Override
    public void createCombinedGeometry(Set<Geometry> setToAddTo, FeatureCombinedGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createIndividualGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool renderPropertyPool)
    {
        MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null : bd.getDataType().getBasicVisualizationInfo();
        Constraints constraints = StyleUtils.createTimeConstraintsIfApplicable(basicVisInfo, mapVisInfo, bd.getMGS(),
                StyleUtils.getDataGroupInfoFromDti(getToolbox(), bd.getDataType()));

        if (bd.getMGS() instanceof MapLocationGeometrySupport)
        {
            boolean added = false;
            MapLocationGeometrySupport mlgs = (MapLocationGeometrySupport)bd.getMGS();
            LatLonAlt centerPoint = createLocation(bd.getElementId(), mlgs, bd.getVS(), bd.getMDP());
            double[] smaSmiOrn = getSmaSmiOrnFromMetaData(centerPoint, bd.getElementId(), bd.getMDP());
            if (smaSmiOrn != null && (bd.getVS().isSelected() || !isShowEllipseOnSelect()))
            {
                GeographicPosition gp = new GeographicPosition(centerPoint);

                if (isEllipsoid())
                {
                    added = setToAddTo
                            .add(myEllipsoidBuilder.createEllipsoid(bd, smaSmiOrn, gp, constraints, ELLISPOID_QUALITY, this));
                    centerPoint = LatLonAlt.createFromDegreesMeters(centerPoint.getLatD(), centerPoint.getLonD(),
                            centerPoint.getAltM() + smaSmiOrn[1] * 2, centerPoint.getAltitudeReference());
                }
                else
                {
                    if (isShowEdgeLine())
                    {
                        added = setToAddTo.add(createEllipseEdgeGeometry(bd, renderPropertyPool, mapVisInfo, basicVisInfo,
                                constraints, smaSmiOrn, gp));
                    }

                    // Build fill geometry
                    EllipseFillStyle fillStyle = getFillStyle();
                    if (fillStyle != null && fillStyle != EllipseFillStyle.NO_FILL)
                    {
                        added = setToAddTo.add(createEllipseFillGeometry(bd.getGeomId(), smaSmiOrn, gp, bd.getVS(),
                                bd.getDataType(), renderPropertyPool, constraints));
                    }
                }
            }

            if (smaSmiOrn == null || isShowCenterPoint())
            {
                added = createPointGeometry(bd, renderPropertyPool, getCenterPointSize(), constraints, setToAddTo) != null;
            }

            if (added)
            {
                createLabelGeometry(setToAddTo, bd, new GeographicPosition(centerPoint), constraints, renderPropertyPool);
            }
        }
        else
        {
            throw new IllegalArgumentException(
                    "Cannot create geometries from type " + (bd.getMGS() == null ? "NULL" : bd.getMGS().getClass().getName()));
        }
    }

    @Override
    public DynamicEllipseFeatureVisualization deriveForType(String dtiKey)
    {
        DynamicEllipseFeatureVisualization clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        // Only return individual element.
        return AppliesTo.INDIVIDUAL_ELEMENT;
    }

    @Override
    public Class<? extends MapGeometrySupport> getConvertedClassType()
    {
        // Just do the base class type so we can catch all location based types
        // with metadata.
        return MapLocationGeometrySupport.class;
    }

    /**
     * Gets the orientation column key.
     *
     * @return the orientation column key
     */
    public String getOrientationColumnKey()
    {
        return (String)getStyleParameterValue(ourOrientationColumnKey);
    }

    /**
     * Gets the semi major column key.
     *
     * @return the semi major column key
     */
    public String getSemiMajorColumnKey()
    {
        return (String)getStyleParameterValue(ourSemiMajorAxisColumnKey);
    }

    /**
     * Gets the semi minor column key.
     *
     * @return the semi minor column key
     */
    public String getSemiMinorColumnKey()
    {
        return (String)getStyleParameterValue(ourSemiMinorAxisColumnKey);
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.LOCATION_FEATURE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Feature visualization controls for dynamic ellipses, where the ellipse parameters"
                + " semi-major axis, semi-minor axis, and orientation can be selected from meta-data column values";
    }

    @Override
    public String getStyleName()
    {
        return "Ellipses (Dynamic)";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        if (getDTIKey() != null)
        {
            MutableVisualizationStyle style = panel.getChangedStyle();
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), style.getDTIKey());
            if (dti != null && dti.getMetaDataInfo() != null && dti.getMetaDataInfo().getKeyCount() > 0)
            {
                List<AbstractStyleParameterEditorPanel> paramList = New.list();

                VisualizationStyleParameter param = style.getStyleParameter(ourSemiMajorAxisColumnKey);
                paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                        ourSemiMajorAxisColumnKey, false, false, true, dti.getMetaDataInfo().getKeyNames()));

                param = style.getStyleParameter(ourSemiMinorAxisColumnKey);
                paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                        ourSemiMinorAxisColumnKey, false, false, true, dti.getMetaDataInfo().getKeyNames()));

                param = style.getStyleParameter(ourOrientationColumnKey);
                paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                        ourOrientationColumnKey, false, false, true, dti.getMetaDataInfo().getKeyNames()));

                StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Dynamic Ellipse", paramList);
                panel.addGroup(paramGrp);
            }
        }

        return panel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultSemiMajorAxisParameter);
        setParameter(getDefaultAxisUnitParameter());
        setParameter(ourDefaultSemiMinorAxisParameter);
        setParameter(ourDefaultOrientationParameter);
    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        super.initialize(paramSet);
        for (VisualizationStyleParameter p : paramSet)
        {
            if (p.getKey() != null && p.getKey().startsWith(PROPERTY_KEY_PREFIX))
            {
                setParameter(p);
            }
        }
    }

    @Override
    public void initializeFromDataType()
    {
        super.initializeFromDataType();
        if (getDTIKey() != null)
        {
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
            if (dti != null && dti.getMetaDataInfo() != null)
            {
                String smaKey = dti.getMetaDataInfo().getKeyForSpecialType(EllipseSemiMajorAxisKey.DEFAULT);
                if (smaKey != null)
                {
                    setParameter(ourSemiMajorAxisColumnKey, smaKey, NO_EVENT_SOURCE);
                    SpecialKey sk = dti.getMetaDataInfo().getSpecialTypeForKey(smaKey);
                    if (sk instanceof EllipseSemiMajorAxisKey)
                    {
                        setParameter(ourAxisUnitKey, Length.getSelectionLabel(((EllipseSemiMajorAxisKey)sk).getSemiMajorUnit()),
                                NO_EVENT_SOURCE);
                    }
                }
                String smiKey = dti.getMetaDataInfo().getKeyForSpecialType(EllipseSemiMinorAxisKey.DEFAULT);
                if (smiKey != null)
                {
                    setParameter(ourSemiMinorAxisColumnKey, smiKey, NO_EVENT_SOURCE);
                }
                String ornKey = dti.getMetaDataInfo().getKeyForSpecialType(EllipseOrientationKey.DEFAULT);
                if (ornKey != null)
                {
                    setParameter(ourOrientationColumnKey, ornKey, NO_EVENT_SOURCE);
                }
            }
        }
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new DynamicEllipseFeatureVisualization(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public boolean requiresMetaData()
    {
        // Yes we require metadata.
        return true;
    }

    /**
     * Sets the orientation key.
     *
     * @param key the key
     * @param source the source
     */
    public void setOrientationKey(String key, Object source)
    {
        StyleUtils.setMetaDataColumnKeyProperty(this, ourOrientationColumnKey, key, source);
    }

    /**
     * Sets the semi major column key.
     *
     * @param key the key
     * @param source the source
     */
    public void setSemiMajorColumnKey(String key, Object source)
    {
        StyleUtils.setMetaDataColumnKeyProperty(this, ourSemiMajorAxisColumnKey, key, source);
    }

    /**
     * Sets the semi minor column key.
     *
     * @param key the key
     * @param source the source
     */
    public void setSemiMinorColumnKey(String key, Object source)
    {
        StyleUtils.setMetaDataColumnKeyProperty(this, ourSemiMinorAxisColumnKey, key, source);
    }

    @Override
    public boolean supportsLabels()
    {
        return true;
    }

    /**
     * Create a polyline ellipse.
     *
     * @param bd The geometry builder data.
     * @param renderPropertyPool The render property pool.
     * @param mapVisInfo The map visualization info.
     * @param basicVisInfo The basic visualization info.
     * @param constraints The geometry constraints.
     * @param smaSmiOrn The semi-major axis length, semi-minor axis length, and
     *            the orientation clockwise from north.
     * @param gp The geographic center.
     * @return the newly create geometry.
     */
    private Geometry createEllipseEdgeGeometry(FeatureIndividualGeometryBuilderData bd, RenderPropertyPool renderPropertyPool,
            MapVisualizationInfo mapVisInfo, BasicVisualizationInfo basicVisInfo, Constraints constraints, double[] smaSmiOrn,
            GeographicPosition gp)
    {
        EllipseGeometry.ProjectedBuilder builder = new EllipseGeometry.ProjectedBuilder();
        builder.setProjection(getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot());

        builder.setCenter(gp);
        builder.setAngle(smaSmiOrn[2]);

        builder.setSemiMajorAxis(StyleUtils.getValueInMeters(smaSmiOrn[0], getAxisUnit()));
        builder.setSemiMinorAxis(StyleUtils.getValueInMeters(smaSmiOrn[1], getAxisUnit()));

        builder.setDataModelId(bd.getGeomId());

        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();

        Color primaryColor = determinePrimaryColor(bd);

        PolygonRenderProperties props = new DefaultPolygonRenderProperties(zOrder, true, pickable);
        props.setColor(primaryColor);
        props.setWidth(getEllipseLineWidth());
        props = renderPropertyPool.getPoolInstance(props);
        return new EllipseGeometry(builder, props, constraints);
    }

    /**
     * Creates the ellipse fill geometry.
     *
     * @param geomId the geom id
     * @param smaSmiOrn the sma smi orn array
     * @param gp the {@link GeographicPosition}
     * @param visState the {@link VisualizationState}
     * @param dti the {@link DataTypeInfo}
     * @param renderPropertyPool the {@link RenderPropertyPool}
     * @param constraints the {@link Constraints}
     * @return the abstract renderable geometry
     */
    private Geometry createEllipseFillGeometry(long geomId, double[] smaSmiOrn, GeographicPosition gp,
            VisualizationState visState, DataTypeInfo dti, RenderPropertyPool renderPropertyPool, Constraints constraints)
    {
        MapVisualizationInfo mapVisInfo = dti == null ? null : dti.getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = dti == null ? null : dti.getBasicVisualizationInfo();
        EllipseFillStyle fillStyle = getFillStyle();

        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();
        ScalableMeshRenderProperties props = new DefaultMeshScalableRenderProperties(zOrder, true, pickable);
        props.setLighting(LightingModelConfigGL.getDefaultLight());
        props = renderPropertyPool.getPoolInstance(props);

        Projection projection = getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot();
        Pair<Color, Color> colors = determineEllipseFillColorsForProperties(visState, fillStyle);
        props.setBaseColor(colors.getSecondObject());
        props.setColor(colors.getFirstObject());

        EllipseScalableGeometry.Builder builder = new EllipseScalableGeometry.Builder();
        builder.setProjection(projection);
        builder.setAngleDegrees((float)smaSmiOrn[2]);
        builder.setSemiMajorAxis((float)StyleUtils.getValueInMeters(smaSmiOrn[0], getAxisUnit()));
        builder.setSemiMinorAxis((float)StyleUtils.getValueInMeters(smaSmiOrn[1], getAxisUnit()));
        builder.setDataModelId(geomId);
        builder.setPosition(gp);

        return new EllipseScalableGeometry(builder, props, constraints);
    }

    /**
     * Creates the location.
     *
     * @param elementId the element id
     * @param mlgs the mlgs
     * @param visState the vis state
     * @param mdi the mdi
     * @return the lat lon alt
     */
    private LatLonAlt createLocation(long elementId, MapLocationGeometrySupport mlgs, VisualizationState visState,
            MetaDataProvider mdi)
    {
        StyleAltitudeReference altRef = getAltitudeReference();
        Altitude.ReferenceLevel refLevel = altRef.isAutomatic()
                ? mlgs.followTerrain() ? Altitude.ReferenceLevel.TERRAIN : mlgs.getLocation().getAltitudeReference()
                : altRef.getReference();
        return LatLonAlt.createFromDegreesMeters(mlgs.getLocation().getLatD(), mlgs.getLocation().getLonD(),
                determineAltitude(visState, mlgs, mdi), refLevel);
    }

    /**
     * Gets the sma smi orn from meta data.
     *
     * @param centerPoint the center point
     * @param elementId the element id
     * @param mdp the mdi
     * @return the sma smi orn from meta data
     */
    public double[] getSmaSmiOrnFromMetaData(LatLonAlt centerPoint, long elementId, MetaDataProvider mdp)
    {
        double[] result = null;
        String smaKey = getSemiMajorColumnKey();
        String smiKey = getSemiMinorColumnKey();
        String ornKey = getOrientationColumnKey();
        if (smaKey != null || smiKey != null)
        {
            try
            {
                double smaVal = smaKey == null ? StyleUtils.convertValueToDouble(mdp.getValue(smiKey))
                        : StyleUtils.convertValueToDouble(mdp.getValue(smaKey));
                double smiVal = smiKey == null ? smaVal
                        : smaKey == null ? smaVal : StyleUtils.convertValueToDouble(mdp.getValue(smiKey));
                double ornVal = ornKey == null ? 0. : StyleUtils.convertValueToDouble(mdp.getValue(ornKey));

                result = new double[3];
                result[0] = smaVal;
                result[1] = smiVal;
                result[2] = ornVal;
            }
            catch (NumberFormatException e)
            {
                result = null;
                LOGGER.error("Error converting SMA/SMI/ORN from data type" + getDTIKey() + " ElId[" + elementId + "] Values: SMA["
                        + mdp.getValue(smaKey) + "] SMI[" + mdp.getValue(smiKey) + "] ORN[" + mdp.getValue(ornKey) + "]");
            }
        }
        return result;
    }
}
