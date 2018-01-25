package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.geometry.EllipsoidGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelData;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.TextureGroup;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateController;

/**
 * Processor for {@link PolygonMeshGeometry}s. This class determines the model
 * coordinates of input geometries and putting them in the cache for use by the
 * renderer.
 */
public class PolygonMeshProcessor extends TextureProcessor<PolygonMeshGeometry>
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(PolygonMeshProcessor.class);

    /** Comparator that determines geometry processing priority. */
    private final Comparator<? super PolygonMeshGeometry> myPriorityComparator;

    /**
     * Construct a polygon mesh processor.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public PolygonMeshProcessor(ProcessorBuilder builder, GeometryRenderer<PolygonMeshGeometry> renderer)
    {
        super(PolygonMeshGeometry.class, builder, renderer, 0);
        Utilities.checkNull(builder.getPriorityComparator(), "builder.getPriorityComparator()");
        myPriorityComparator = builder.getPriorityComparator();
    }

    @Override
    public void generateDryRunGeometries()
    {
        PolygonMeshGeometry.Builder<ScreenPosition> polyBuilder = new PolygonMeshGeometry.Builder<ScreenPosition>();
        PolygonMeshRenderProperties props = new DefaultPolygonMeshRenderProperties(0, true, true, true);
        props.setColor(Color.BLUE);
        props.setLighting(LightingModelConfigGL.getDefaultLight());

        List<ScreenPosition> positions = new ArrayList<>();
        List<Vector3d> normals = new ArrayList<>();

        normals.add(new Vector3d(0, 0, 1));
        normals.add(new Vector3d(0, 0, 1));
        normals.add(new Vector3d(0, 0, 1));
        normals.add(new Vector3d(0, 0, 1));

        positions.add(new ScreenPosition(0, 0));
        positions.add(new ScreenPosition(1, 0));
        positions.add(new ScreenPosition(1, 1));
        positions.add(new ScreenPosition(0, 0));

        polyBuilder.setPolygonVertexCount(4);
        polyBuilder.setPositions(positions);
        polyBuilder.setNormals(normals);

        Collection<PolygonMeshGeometry> geoms = Collections.singleton(new PolygonMeshGeometry(polyBuilder, props, null));
        receiveObjects(this, geoms, Collections.<PolygonMeshGeometry>emptySet());
    }

    @Override
    public void handleProjectionChanged(ProjectionChangedEvent evt)
    {
        if (!sensitiveToProjectionChanges())
        {
            return;
        }

        Lock writeLock = getProjectionChangeLock().writeLock();
        writeLock.lock();
        try
        {
            for (PolygonMeshGeometry geometry : getGeometries())
            {
                if (geometry instanceof EllipsoidGeometry)
                {
                    ((EllipsoidGeometry)geometry).handleProjectionChanged();
                }
            }
            // Clear must come first because super's implementation may trigger
            // drawing to occur.
            getCache().clearCacheAssociations(PolygonMeshData.class);
            super.handleProjectionChanged(evt);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public boolean sensitiveToProjectionChanges()
    {
        return true;
    }

    @Override
    protected void cacheData(PolygonMeshGeometry geo, AbstractRenderer.ModelData data)
    {
        PolygonMeshData pmd = (PolygonMeshData)((TextureModelData)data).getModelData();
        CacheProvider cache = getCache();
        cache.putCacheAssociation(geo.getPolygonMesh(), pmd, PolygonMeshData.class, pmd.getSizeBytes(), 0L);
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        getCache().clearCacheAssociation(
                geoms.stream()
                        .map(geom -> geom instanceof PolygonMeshGeometry ? ((PolygonMeshGeometry)geom).getPolygonMesh() : null),
                PolygonMeshData.class);
    }

    @Override
    protected boolean enableAutoMipMap()
    {
        return true;
    }

    @Override
    protected ModelData getCachedModelData(PolygonMeshGeometry geom, TextureCoords imageTexCoords, ModelData override)
    {
        PolygonMeshData mesh = (PolygonMeshData)(override == null ? null : ((TextureModelData)override).getModelData());
        if (mesh == null)
        {
            mesh = getCache().getCacheAssociation(geom.getPolygonMesh(), PolygonMeshData.class);
        }
        return mesh;
    }

    @Override
    protected Comparator<? super PolygonMeshGeometry> getPriorityComparator()
    {
        return myPriorityComparator;
    }

    @Override
    protected void handleBlankImage(PolygonMeshGeometry geom)
    {
    }

    @Override
    protected boolean isOnScreen(PolygonMeshGeometry geom, boolean useTime)
    {
        boolean isOnScreen = super.isOnScreen(geom, useTime);

        if (isOnScreen && geom.getImageManager() != null
                && (geom.getTextureCoords() == null || geom.getPositions().size() != geom.getTextureCoords().size()))
        {
            isOnScreen = false;
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Rejected polygon mesh invalid texture coordinates " + geom);
            }
        }

        if (isOnScreen && geom.getConstraints() != null && geom.getConstraints().getLocationConstraint() != null)
        {
            Vector3d model = getPositionConverter().convertPositionToModel(
                    new GeographicPosition(geom.getConstraints().getLocationConstraint()), getMapContext().getProjection(),
                    Vector3d.ORIGIN);
            if (model != null)
            {
                isOnScreen = getViewer().isInView(model, 0f);
            }
        }

        return isOnScreen;
    }

    @Override
    protected TextureModelData processGeometry(PolygonMeshGeometry geo, Projection projectionSnapshot,
            AbstractRenderer.ModelData override, TimeBudget timeBudget)
    {
        TextureGroup textureGroup = processTextureForGeometry(geo, override, timeBudget);
        TextureModelData modelData = null;
        if (override != null)
        {
            modelData = new TextureModelData(((TextureModelData)override).getModelData(), textureGroup);
        }
        else
        {
            modelData = new TextureModelData(createMeshData(geo, projectionSnapshot), textureGroup);
        }

        return modelData;
    }

    @Override
    protected void processTextureLoaded(Collection<? extends PolygonMeshGeometry> textureLoaded,
            StateController<PolygonMeshGeometry> controller)
    {
        if (textureLoaded.isEmpty())
        {
            return;
        }

        Lock readLock = getProjectionChangeLock().readLock();
        readLock.lock();
        try
        {
            for (PolygonMeshGeometry geom : textureLoaded)
            {
                PolygonMeshData modelData = getCache().getCacheAssociation(geom.getPolygonMesh(), PolygonMeshData.class);
                if (modelData == null)
                {
                    modelData = createMeshData(geom, getProjectionSnapshot());
                    cacheData(geom, new TextureModelData(modelData, null));
                    setOnscreenDirty();
                }
            }
        }
        finally
        {
            readLock.unlock();
        }

        controller.changeState(textureLoaded, State.READY);
    }

    /**
     * Creates the mesh model data object.
     *
     * @param geo The geometry to create the model data from.
     * @param projectionSnapshot The snapshot of the current {@link Projection}.
     * @return The mesh model data.
     */
    private PolygonMeshData createMeshData(PolygonMeshGeometry geo, Projection projectionSnapshot)
    {
        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        List<Vector3d> positionList = getPositionConverter().convertPositionsToModel(geo.getPositions(), projection,
                Vector3d.ORIGIN);
        PolygonMeshData meshData = new PolygonMeshData(positionList, geo.getNormals(), geo.getIndices(), geo.getColors(),
                geo.getTextureCoords(), geo.getPolygonVertexCount(), false);
        return meshData;
    }
}
