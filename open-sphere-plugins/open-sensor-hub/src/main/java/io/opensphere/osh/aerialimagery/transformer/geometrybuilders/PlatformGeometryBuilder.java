package io.opensphere.osh.aerialimagery.transformer.geometrybuilders;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.collada.ColladaParser;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.PolylineGeometry.Builder;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.math.VectorUtilities;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Builds the geometries representing the aircraft carrying the camera.
 */
public class PlatformGeometryBuilder implements GeometryBuilder
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(PlatformGeometryBuilder.class);

    /**
     * The resource path to the collada model file to render.
     */
    private static final String ourColladaModel = "/sad_drone_2.dae";

    /** Multiplier used to scale the geometry. */
    private static final double SCALE = .01;

    /**
     * The map manager.
     */
    private final MapContext<? extends Viewer> myMapManager;

    /**
     * The collection of published geometries.
     */
    private final Map<String, List<Geometry>> myPublished = Collections.synchronizedMap(New.map());

    /**
     * The geometries of the collada model.
     */
    private List<Geometry> myVehicleGeometries;

    /**
     * Constructs a new vehicle geometry builder.
     *
     * @param mapManager The map manager.
     */
    public PlatformGeometryBuilder(MapContext<? extends Viewer> mapManager)
    {
        myMapManager = mapManager;
    }

    @Override
    public Pair<List<Geometry>, List<Geometry>> buildGeometries(PlatformMetadata model, DataTypeInfo uavDataType,
            DataTypeInfo videoLayer)
    {
        String typeKey = uavDataType.getTypeKey();

        List<Geometry> toPublish = New.list();

        if (!myPublished.containsKey(typeKey))
        {
            List<Geometry> geometries = buildGeometries(uavDataType);
            myPublished.put(typeKey, geometries);
            toPublish.addAll(geometries);
        }

        List<Geometry> vehicle = myPublished.get(typeKey);
        updateTransform(model, vehicle);

        return new Pair<>(toPublish, New.list());
    }

    @Override
    public boolean cachePublishedGeometries()
    {
        return false;
    }

    /**
     * Closes any resources and returns the geometries that need to be removed.
     *
     * @return The geometries to remove.
     */
    public List<Geometry> close()
    {
        List<Geometry> toRemove = New.list();

        for (List<Geometry> geoms : myPublished.values())
        {
            toRemove.addAll(geoms);
        }

        return toRemove;
    }

    /**
     * Builds the vehicle geometries for the given {@link DataTypeInfo}.
     *
     * @param uavDataType The uav layer.
     * @return The vehicle geometries.
     */
    private List<Geometry> buildGeometries(DataTypeInfo uavDataType)
    {
        Color color = uavDataType.getBasicVisualizationInfo().getTypeColor();
        List<Geometry> results;
        if (myVehicleGeometries == null)
        {
            results = buildVehicleGeometries(color);
        }
        else
        {
            results = New.list();
            PolygonMeshGeometry.MeshBuilder<ModelPosition> builder = new PolygonMeshGeometry.MeshBuilder<>();
            ColorRenderProperties renderProperties = null;
            PolylineRenderProperties lineRenderProperties = null;
            for (Geometry geom : myVehicleGeometries)
            {
                if (geom instanceof PolygonMeshGeometry)
                {
                    PolygonMeshGeometry meshGeom = (PolygonMeshGeometry)geom;
                    builder.setPolygonMesh(meshGeom.getPolygonMesh());
                    builder.setDataModelId(geom.getDataModelId());
                    builder.setRapidUpdate(geom.isRapidUpdate());
                    if (renderProperties == null)
                    {
                        renderProperties = meshGeom.getRenderProperties().clone();
                        renderProperties.setColor(color);
                    }
                    results.add(new PolygonMeshGeometry(builder, renderProperties, meshGeom.getConstraints()));
                }
                else if (geom instanceof PolylineGeometry)
                {
                    PolylineGeometry lineGeom = (PolylineGeometry)geom;
                    Builder<? extends Position> lineBuilder = lineGeom.createBuilder();
                    if (lineRenderProperties == null)
                    {
                        lineRenderProperties = lineGeom.getRenderProperties().clone();
                        lineRenderProperties.setColor(color);
                    }
                    results.add(new PolylineGeometry(lineBuilder, lineRenderProperties, lineGeom.getConstraints()));
                }
            }
        }

        return results;
    }

    /**
     * Builds the vehicle geometries.
     *
     * @param color The color to make the geometries.
     * @return the collection
     */
    private List<Geometry> buildVehicleGeometries(Color color)
    {
        List<Geometry> results;
        results = New.list();
        try (InputStream is = this.getClass().getResourceAsStream(ourColladaModel))
        {
            int zOrder = ZOrderRenderProperties.TOP_Z - 100;
            PolylineRenderProperties lineProps = new DefaultPolylineRenderProperties(zOrder, true, true);
            lineProps.setColor(color);
            PolygonMeshRenderProperties meshProps = new DefaultPolygonMeshRenderProperties(zOrder, true, true, true);
            meshProps.setColor(color);
            meshProps.setLighting(LightingModelConfigGL.getDefaultLight());

            new ColladaParser(lineProps, meshProps, null).parseModels(is, results);
        }
        catch (JAXBException | IOException e)
        {
            LOGGER.error(e, e);
        }
        return results;
    }

    /**
     * Get the transform to put the feed's vehicle model in the correct position
     * and attitude.
     *
     * @param value The metadata object.
     * @return The transform.
     */
    private Matrix4d getTransform(PlatformMetadata value)
    {
        double heading = value.getYawAngle();
        double pitch = value.getPitchAngle();
        double roll = value.getRollAngle();
        Vector3d scaleVector = new Vector3d(SCALE, SCALE, SCALE);

        Projection projection = myMapManager.getProjection();
        Vector3d model = projection.convertToModel(new GeographicPosition(value.getLocation()), Vector3d.ORIGIN);

        return VectorUtilities.getModelTransform(model, heading, pitch, roll, scaleVector);
    }

    /**
     * Update the transform map for the given feed and time.
     *
     * @param metadata The metadata containing the location and orientation of
     *            the vehicle.
     * @param vehicleGeoms The geometries of the vehicles.
     */
    private void updateTransform(PlatformMetadata metadata, List<Geometry> vehicleGeoms)
    {
        Matrix4d transform = getTransform(metadata);

        vehicleGeoms.stream().filter(g -> g instanceof PolygonMeshGeometry)
                .forEach(geom -> ((PolygonMeshGeometry)geom).getRenderProperties().setTransform(transform));
    }
}
