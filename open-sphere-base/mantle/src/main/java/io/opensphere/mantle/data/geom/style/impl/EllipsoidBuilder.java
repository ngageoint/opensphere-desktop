package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;

import io.opensphere.core.MapManager;
import io.opensphere.core.geometry.EllipsoidGeometry;
import io.opensphere.core.geometry.EllipsoidGeometryBuilder;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.ColorMaterialModeParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.FaceParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.LightModelVectorParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.LightVectorParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.MaterialVectorParameterType;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.units.length.Length;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Class that builds an {@link EllipsoidGeometry} using parameters within an
 * ellipse feature style.
 */
public class EllipsoidBuilder
{
    /**
     * Used to help build the ellipsoid.
     */
    private final MapManager myMapManager;

    /**
     * Constructs a new ellipsoid builder.
     *
     * @param mapManager Used to build the ellipsoid.
     */
    public EllipsoidBuilder(MapManager mapManager)
    {
        myMapManager = mapManager;
    }

    /**
     * Creates the ellipsoid geometry.
     *
     * @param bd Contains information about the feature to build.
     * @param smaSmiOrn The semi major, minor, and orientation.
     * @param gp The geographic position.
     * @param constraints Any display constraints on the feature.
     * @param ellipsoidQuality The quality of the ellipsoid.
     * @param style The style the feature is going to be drawn in.
     *
     * @return The ellipsoid geometry.
     */
    public Geometry createEllipsoid(FeatureIndividualGeometryBuilderData bd, double[] smaSmiOrn, GeographicPosition gp,
            Constraints constraints, int ellipsoidQuality, AbstractEllipseFeatureVisualizationStyle style)
    {
        DataTypeInfo dti = bd.getDataType();
        VisualizationState visState = bd.getVS();

        MapVisualizationInfo mapVisInfo = dti == null ? null : dti.getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = dti == null ? null : dti.getBasicVisualizationInfo();

        EllipsoidGeometryBuilder<GeographicPosition> builder = new EllipsoidGeometryBuilder<>(myMapManager);
        Class<? extends Length> axisUnits = style.getAxisUnit();
        Length semiMajor = Length.create(axisUnits, smaSmiOrn[0]);
        Length semiMinor = Length.create(axisUnits, smaSmiOrn[1]);
        double semiMinorMeters = semiMinor.inMeters() * 2;
        builder.setAxisAMeters(semiMajor.inMeters() * 2);
        builder.setAxisBMeters(semiMinorMeters);
        builder.setAxisCMeters(semiMinorMeters);
        Color color = visState.isSelected() ? MantleConstants.SELECT_COLOR
                : visState.isDefaultColor() ? style.getColor() : visState.getColor();
        builder.setColor(color);
        builder.setHeading(smaSmiOrn[2]);
        builder.setQuality(ellipsoidQuality);

        builder.setLocation(gp);

        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();
        int zOrder = mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();
        DefaultPolygonMeshRenderProperties meshProps = new DefaultPolygonMeshRenderProperties(zOrder, true, pickable, true);
        meshProps.setLighting(getLightingModel());
        builder.setDataModelId(bd.getGeomId());

        return new EllipsoidGeometry(builder, meshProps, constraints);
    }

    /**
     * Gets the lighting model to test light rendering ellipsoids.
     *
     * @return The lighting model.
     */
    private LightingModelConfigGL getLightingModel()
    {
        LightingModelConfigGL.Builder builder = new LightingModelConfigGL.Builder();
        builder.setLightNumber(0);
        builder.setFace(FaceParameterType.FRONT);
        builder.setColorMaterialMode(ColorMaterialModeParameterType.AMBIENT_AND_DIFFUSE);

        final float[] ambientLight = { 0.50f, 0.50f, 0.50f, 1f };
        final float[] diffuseLight = { 1f, 1f, 1f, 1f };
        final float[] position = { -0.17f, 0.61f, 0.81f, 0f };
        final float[] specular = { 0.74f, 0.77f, 0.77f, 1f };
        final float[] specularReflectivity = { 0.9f, 0.9f, 0.9f, 1f };
        final float shininess = 80f;

        builder.addLightModelVectorParameter(LightModelVectorParameterType.LIGHT_MODEL_AMBIENT, ambientLight);
        builder.addLightParameterVector(LightVectorParameterType.DIFFUSE, diffuseLight);
        builder.addLightParameterVector(LightVectorParameterType.POSITION, position);
        builder.addLightParameterVector(LightVectorParameterType.SPECULAR, specular);
        builder.addMaterialVectorParameter(MaterialVectorParameterType.SPECULAR, specularReflectivity);
        builder.addMaterialShininessParameter(shininess);

        return new LightingModelConfigGL(builder);
    }
}
