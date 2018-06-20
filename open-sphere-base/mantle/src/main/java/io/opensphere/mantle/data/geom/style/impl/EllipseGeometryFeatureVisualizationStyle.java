package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.EllipseGeometry;
import io.opensphere.core.geometry.EllipseScalableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultMeshScalableRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.geom.MapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.StyleAltitudeReference;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMajorAxisKey;

/**
 * The Class PointFeatureVisualizationStyle.
 */
@SuppressWarnings("PMD.GodClass")
public class EllipseGeometryFeatureVisualizationStyle extends AbstractEllipseFeatureVisualizationStyle
{
    /** The Constant ourPropertyKeyPrefix. */
    @SuppressWarnings("hiding")
    public static final String ourPropertyKeyPrefix = "EllipseGeometryFeatureVisualizationStyle";

    /**
     * Instantiates a new point feature visualization style.
     *
     * @param tb the {@link Toolbox}-
     *
     */
    public EllipseGeometryFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new ellipse geometry feature visualization style.
     *
     * @param tb the tb
     * @param dtiKey the dti key
     */
    public EllipseGeometryFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public EllipseGeometryFeatureVisualizationStyle clone()
    {
        return (EllipseGeometryFeatureVisualizationStyle)super.clone();
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
        MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null : bd.getDataType().getBasicVisualizationInfo();
        Constraints constraints = StyleUtils.createTimeConstraintsIfApplicable(basicVisInfo, mapVisInfo, bd.getMGS(),
                StyleUtils.getDataGroupInfoFromDti(getToolbox(), bd.getDataType()));
        @SuppressWarnings("PMD.PrematureDeclaration")
        int geomCount = setToAddTo.size();

        if (bd.getMGS() instanceof MapEllipseGeometrySupport)
        {
            if (bd.getVS().isSelected() || !isShowEllipseOnSelect())
            {
                // Build edge geometry
                if (isShowEdgeLine())
                {
                    setToAddTo.add(createEllipseEdgeGeometry(bd, renderPropertyPool, constraints));
                }

                // Build fill geometry
                if (getFillStyle() != null && getFillStyle() != EllipseFillStyle.NO_FILL
                        && !(getFillStyle() == EllipseFillStyle.SOLID && isShowEdgeLine()))
                {
                    setToAddTo.add(createEllipseFillGeometry(bd, renderPropertyPool, constraints));
                }
            }

            // Build center point geometry
            if (isShowCenterPoint())
            {
                createPointGeometry(bd, renderPropertyPool, getCenterPointSize(), constraints, setToAddTo);
            }
        }
        else if (bd.getMGS() instanceof MapLocationGeometrySupport)
        {
            createPointGeometry(bd, renderPropertyPool, getCenterPointSize(), constraints, setToAddTo);
        }
        else
        {
            throw new IllegalArgumentException(
                    "Cannot create geometries from type " + (bd.getMGS() == null ? "NULL" : bd.getMGS().getClass().getName()));
        }
        if (setToAddTo.size() > geomCount)
        {
            createLabelGeometry(setToAddTo, bd, null, constraints, renderPropertyPool);
        }
    }

    @Override
    public EllipseGeometryFeatureVisualizationStyle deriveForType(String dtiKey)
    {
        EllipseGeometryFeatureVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        return AppliesTo.INDIVIDUAL_ELEMENT;
    }

    @Override
    public Class<? extends MapGeometrySupport> getConvertedClassType()
    {
        return MapEllipseGeometrySupport.class;
    }

    @Override
    public Set<MapVisualizationType> getRequiredMapVisTypes()
    {
        return Collections.singleton(MapVisualizationType.ELLIPSE_ELEMENTS);
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.LOCATION_FEATURE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Feature visualization controls for static ellipses, where the ellipse parameters"
                + " are specified when the feature is inserted into the tool.";
    }

    @Override
    public String getStyleName()
    {
        return "Ellipses";
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
                    SpecialKey sk = dti.getMetaDataInfo().getSpecialTypeForKey(smaKey);
                    if (sk instanceof EllipseSemiMajorAxisKey)
                    {
                        setParameter(ourAxisUnitKey, Length.getSelectionLabel(((EllipseSemiMajorAxisKey)sk).getSemiMajorUnit()),
                                NO_EVENT_SOURCE);
                    }
                }
            }
        }
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new EllipseGeometryFeatureVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public boolean supportsLabels()
    {
        return true;
    }

    /**
     * Creates the ellipse edge geometry.
     *
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @param renderPropertyPool the render property pool
     * @param constraints the constraints
     * @return the ellipse geometry
     */
    private EllipseGeometry createEllipseEdgeGeometry(FeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool renderPropertyPool, Constraints constraints)
    {
        MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null : bd.getDataType().getBasicVisualizationInfo();

        MapEllipseGeometrySupport megs = (MapEllipseGeometrySupport)bd.getMGS();

        EllipseGeometry.ProjectedBuilder ellipseBuilder = new EllipseGeometry.ProjectedBuilder();
        ellipseBuilder.setProjection(getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot());

        StyleAltitudeReference altRef = getAltitudeReference();
        Altitude.ReferenceLevel refLevel = altRef.isAutomatic()
                ? bd.getMGS().followTerrain() ? Altitude.ReferenceLevel.TERRAIN : megs.getLocation().getAltitudeReference()
                : altRef.getReference();

        LatLonAlt centerPoint = LatLonAlt.createFromDegreesMeters(megs.getLocation().getLatD(), megs.getLocation().getLonD(),
                determineAltitude(bd.getVS(), megs, bd.getMDP()), refLevel);
        ellipseBuilder.setCenter(new GeographicPosition(centerPoint));
        ellipseBuilder.setAngle(megs.getOrientation());

        ellipseBuilder.setSemiMajorAxis(StyleUtils.getValueInMeters(megs.getSemiMajorAxis(), getAxisUnit()));
        ellipseBuilder.setSemiMinorAxis(StyleUtils.getValueInMeters(megs.getSemiMinorAxis(), getAxisUnit()));
        ellipseBuilder.setDataModelId(bd.getGeomId());

        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();

        Color primaryColor = determinePrimaryColor(bd);
        float[] colorComp = primaryColor.getColorComponents(null);
        Color fillColor = new Color(colorComp[0], colorComp[1], colorComp[2], DEFAULT_SOLID_FILL_OPACITY);
        ColorRenderProperties fillColorProps = new DefaultColorRenderProperties(zOrder, true, pickable, false);
        fillColorProps.setColor(fillColor);

        PolygonRenderProperties props = createPolygonRenderProperties(bd, pickable, zOrder, primaryColor, fillColorProps);
        props = renderPropertyPool.getPoolInstance(props);
        EllipseGeometry eg = new EllipseGeometry(ellipseBuilder, props, constraints);
        return eg;
    }

    /**
     * Creates the ellipse fill geometry.
     *
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     * @param renderPropertyPool the render property pool
     * @param constraints the constraints
     * @return the abstract renderable geometry
     */
    private AbstractRenderableGeometry createEllipseFillGeometry(FeatureIndividualGeometryBuilderData bd,
            RenderPropertyPool renderPropertyPool, Constraints constraints)
    {
        MapVisualizationInfo mapVisInfo = bd.getDataType() == null ? null : bd.getDataType().getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = bd.getDataType() == null ? null : bd.getDataType().getBasicVisualizationInfo();
        MapEllipseGeometrySupport megs = (MapEllipseGeometrySupport)bd.getMGS();
        EllipseFillStyle fillStyle = getFillStyle();

        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();

        ScalableMeshRenderProperties props = new DefaultMeshScalableRenderProperties(zOrder, true, pickable);
        props.setLighting(LightingModelConfigGL.getDefaultLight());
        Pair<Color, Color> colors = determineEllipseFillColorsForProperties(bd.getVS(), fillStyle);
        props.setBaseColor(colors.getSecondObject());
        props.setColor(colors.getFirstObject());
        props = renderPropertyPool.getPoolInstance(props);

        StyleAltitudeReference altRef = getAltitudeReference();
        Altitude.ReferenceLevel refLevel = altRef.isAutomatic()
                ? bd.getMGS().followTerrain() ? Altitude.ReferenceLevel.TERRAIN : megs.getLocation().getAltitudeReference()
                : altRef.getReference();

        LatLonAlt centerPoint = LatLonAlt.createFromDegreesMeters(megs.getLocation().getLatD(), megs.getLocation().getLonD(),
                determineAltitude(bd.getVS(), megs, bd.getMDP()), refLevel);

        EllipseScalableGeometry.Builder builder = new EllipseScalableGeometry.Builder();
        builder.setProjection(getToolbox().getMapManager().getProjection(Viewer3D.class).getSnapshot());
        builder.setAngleDegrees(megs.getOrientation());
        builder.setSemiMajorAxis((float)StyleUtils.getValueInMeters(megs.getSemiMajorAxis(), getAxisUnit()));
        builder.setSemiMinorAxis((float)StyleUtils.getValueInMeters(megs.getSemiMinorAxis(), getAxisUnit()));
        builder.setDataModelId(bd.getGeomId());
        builder.setPosition(new GeographicPosition(centerPoint));

        return new EllipseScalableGeometry(builder, props, constraints);
    }

    /**
     * Creates the polygon render properties.
     *
     * @param bd the bd
     * @param pickable the pickable
     * @param zOrder the z order
     * @param primaryColor the primary color
     * @param fillColorProps the fill color props
     * @return the polygon render properties
     */
    private PolygonRenderProperties createPolygonRenderProperties(FeatureIndividualGeometryBuilderData bd, boolean pickable,
            int zOrder, Color primaryColor, ColorRenderProperties fillColorProps)
    {
        PolygonRenderProperties props = getFillStyle() == EllipseFillStyle.SOLID
                ? new DefaultPolygonRenderProperties(zOrder, true, pickable, fillColorProps)
                : new DefaultPolygonRenderProperties(zOrder, true, pickable);
        props.setWidth(getEllipseLineWidth());
        props.setColor(primaryColor);
        return props;
    }
}
