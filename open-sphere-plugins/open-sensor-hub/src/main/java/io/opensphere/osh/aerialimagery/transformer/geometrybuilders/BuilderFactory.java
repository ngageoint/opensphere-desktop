package io.opensphere.osh.aerialimagery.transformer.geometrybuilders;

import java.util.List;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.model.PlatformMetadataAndImage;

/**
 * Given a model, this class knows the series of builders to create and chain in
 * order to build a complete set of geometries to publish to the globe.
 */
public class BuilderFactory
{
    /**
     * The footprint builder.
     */
    private final FootprintGeometryBuilder myFootprintBuilder;

    /**
     * The image builder.
     */
    private final ImageGeometryBuilder myImageBuilder;

    /**
     * Builds a collada model representing the uav.
     */
    private final PlatformGeometryBuilder myVehicleBuilder;

    /**
     * Constructs a new geometry builder factory.
     *
     * @param orderRegistry The order manager registry.
     * @param mapManager The map manager.
     */
    public BuilderFactory(OrderManagerRegistry orderRegistry, MapContext<? extends Viewer> mapManager)
    {
        myFootprintBuilder = new FootprintGeometryBuilder(orderRegistry);
        myImageBuilder = new ImageGeometryBuilder(orderRegistry);
        myVehicleBuilder = new PlatformGeometryBuilder(mapManager);
    }

    /**
     * Closes resources and returns the geometries that need to be removed.
     *
     * @return The geometries that need to be removed.
     */
    public List<Geometry> close()
    {
        List<Geometry> toRemove = New.list();

        toRemove.addAll(myImageBuilder.close());
        toRemove.addAll(myVehicleBuilder.close());

        return toRemove;
    }

    /**
     * Creates the builders to use for the model.
     *
     * @param model The model to build geometries for.
     * @return The list of builders that know how to build geometries for the
     *         given model.
     */
    public List<GeometryBuilder> createBuilders(PlatformMetadata model)
    {
        List<GeometryBuilder> builders = New.list();

        if (model instanceof PlatformMetadataAndImage)
        {
            builders.add(myImageBuilder);
        }
        else
        {
            builders.add(myFootprintBuilder);
            builders.add(myVehicleBuilder);
        }

        return builders;
    }
}
